package engine;

import database.BattleDAO;
import database.UserDAO;
import models.Move;
import models.PokemonLive;
import models.User;
import network.ClientHandler;
import network.Protocol;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BattleRoom implements Runnable {
    private final ClientHandler playerA;
    private final ClientHandler playerB;
    private User userA;
    private User userB;
    private List<PokemonLive> teamA;
    private List<PokemonLive> teamB;

    public BattleRoom(ClientHandler playerA, ClientHandler playerB) {
        this.playerA = playerA;
        this.playerB = playerB;
        playerA.setBattleRoom(this);
        playerB.setBattleRoom(this);
    }

    @Override
    public void run() {
        try {
            executeBattle();
        } catch (Exception e) {
            sendErrorToPlayers("Error en la sala de batalla: " + e.getMessage());
        } finally {
            playerA.closeConnection();
            playerB.closeConnection();
        }
    }

    private void executeBattle() {
        if (!loadPlayers()) {
            sendErrorToPlayers("No fue posible cargar los datos de batalla.");
            return;
        }

        if (teamA.isEmpty() || teamB.isEmpty()) {
            sendErrorToPlayers("Uno de los equipos no tiene Pokémon completos.");
            return;
        }

        PokemonLive activeA = teamA.get(0);
        PokemonLive activeB = teamB.get(0);

        sendBattleStart(activeA, activeB);

        while (!activeA.isFainted() && !activeB.isFainted()) {
            Map<String, String> actionAData = playerA.requestMoveSelection(activeA);
            Map<String, String> actionBData = playerB.requestMoveSelection(activeB);

            TurnManager.TurnAction actionA = buildTurnAction(playerA.getUser(), activeA, actionAData);
            TurnManager.TurnAction actionB = buildTurnAction(playerB.getUser(), activeB, actionBData);

            List<TurnManager.TurnAction> ordered = TurnManager.orderActions(Arrays.asList(actionA, actionB));

            for (TurnManager.TurnAction action : ordered) {
                if (activeA.isFainted() || activeB.isFainted()) {
                    break;
                }
                executeAction(action, activeA, activeB);
            }
        }

        finalizeBattle(activeA, activeB);
    }

    private boolean loadPlayers() {
        UserDAO userDAO = new UserDAO();
        userA = playerA.getUser();
        userB = playerB.getUser();

        if (userA == null || userB == null) {
            return false;
        }

        teamA = userDAO.getUserTeam(userA.getId());
        teamB = userDAO.getUserTeam(userB.getId());
        return teamA != null && teamB != null;
    }

    private void sendBattleStart(PokemonLive activeA, PokemonLive activeB) {
        Map<String, String> payloadA = new HashMap<>();
        payloadA.put("opponent", userB.getUsername());
        payloadA.put("yourPokemon", activeA.getDisplayName());
        payloadA.put("opponentPokemon", activeB.getDisplayName());
        playerA.sendMessage(Protocol.createMessage(Protocol.TYPE_START_BATTLE, payloadA));

        Map<String, String> payloadB = new HashMap<>();
        payloadB.put("opponent", userA.getUsername());
        payloadB.put("yourPokemon", activeB.getDisplayName());
        payloadB.put("opponentPokemon", activeA.getDisplayName());
        playerB.sendMessage(Protocol.createMessage(Protocol.TYPE_START_BATTLE, payloadB));
    }

    private TurnManager.TurnAction buildTurnAction(User trainer, PokemonLive pokemon, Map<String, String> actionData) {
        String moveIndexString = actionData.getOrDefault("move_index", "0");
        int moveIndex;
        try {
            moveIndex = Integer.parseInt(moveIndexString);
        } catch (NumberFormatException e) {
            moveIndex = 0;
        }

        Move selectedMove = selectMove(pokemon, moveIndex);
        return new TurnManager.TurnAction(trainer.getUsername(), pokemon, selectedMove);
    }

    private Move selectMove(PokemonLive pokemon, int moveIndex) {
        if (pokemon == null || pokemon.getMoves() == null) {
            return null;
        }

        if (moveIndex < 0 || moveIndex >= pokemon.getMoves().length || pokemon.getMoves()[moveIndex] == null) {
            for (Move move : pokemon.getMoves()) {
                if (move != null) {
                    return move;
                }
            }
            return null;
        }
        return pokemon.getMoves()[moveIndex];
    }

    private void executeAction(TurnManager.TurnAction action, PokemonLive activeA, PokemonLive activeB) {
        PokemonLive attacker = action.getPokemon();
        PokemonLive defender = attacker == activeA ? activeB : activeA;
        Move move = action.getSelectedMove();

        if (move == null) {
            sendActionUpdate(attacker, defender, 0, "No hay movimiento válido.");
            return;
        }

        int damage = calculateDamage(move, attacker, defender);
        defender.applyDamage(damage);
        sendActionUpdate(attacker, defender, damage, move.getName());
    }

    private int calculateDamage(Move move, PokemonLive attacker, PokemonLive defender) {
        if (move == null || attacker == null || defender == null) {
            return 0;
        }

        double multiplier = 1.0;
        multiplier *= DamageCalculator.getTypeMultiplier(move.getType(), defender.getType1());
        if (defender.getType2() != null && !defender.getType2().isBlank()) {
            multiplier *= DamageCalculator.getTypeMultiplier(move.getType(), defender.getType2());
        }

        double baseDamage = move.getPower();
        double attack = attacker.getAttack();
        double defense = Math.max(defender.getDefense(), 1);
        int damage = (int) Math.max(1, Math.floor((baseDamage * attack / defense) * multiplier));
        return damage;
    }

    private void sendActionUpdate(PokemonLive attacker, PokemonLive defender, int damage, String moveName) {
        Map<String, String> updateA = createUpdatePayload(attacker, defender, damage, moveName);
        updateA.put("teamHp", Integer.toString(getTeamHp(playerA.getUser())));
        playerA.sendMessage(Protocol.createMessage(Protocol.TYPE_UPDATE_STATE, updateA));

        Map<String, String> updateB = createUpdatePayload(attacker, defender, damage, moveName);
        updateB.put("teamHp", Integer.toString(getTeamHp(playerB.getUser())));
        playerB.sendMessage(Protocol.createMessage(Protocol.TYPE_UPDATE_STATE, updateB));
    }

    private Map<String, String> createUpdatePayload(PokemonLive attacker, PokemonLive defender,
                                                    int damage, String moveName) {
        Map<String, String> payload = new HashMap<>();
        payload.put("attacker", attacker.getDisplayName());
        payload.put("defender", defender.getDisplayName());
        payload.put("move", moveName != null ? moveName : "Desconocido");
        payload.put("damage", Integer.toString(damage));
        payload.put("defenderHp", Integer.toString(defender.getCurrentHp()));
        payload.put("defenderMaxHp", Integer.toString(defender.getMaxHp()));
        return payload;
    }

    private int getTeamHp(User user) {
        if (user == null) {
            return 0;
        }
        if (user.getId() == userA.getId()) {
            return teamA.stream().mapToInt(PokemonLive::getCurrentHp).sum();
        }
        return teamB.stream().mapToInt(PokemonLive::getCurrentHp).sum();
    }

    private void finalizeBattle(PokemonLive activeA, PokemonLive activeB) {
        User winner;
        User loser;
        if (activeA.isFainted() && !activeB.isFainted()) {
            winner = userB;
            loser = userA;
        } else if (!activeA.isFainted() && activeB.isFainted()) {
            winner = userA;
            loser = userB;
        } else {
            winner = null;
            loser = null;
        }

        if (winner != null && loser != null) {
            BattleDAO battleDAO = new BattleDAO();
            battleDAO.recordBattleResult(winner.getId(), loser.getId(), "Online");
            new UserDAO().updateElo(winner.getId(), winner.getEloRating() + 15, true);
            new UserDAO().updateElo(loser.getId(), loser.getEloRating() - 10, false);
            sendBattleEnd(winner.getUsername(), loser.getUsername());
        } else {
            sendBattleEnd("Empate", "Empate");
        }
    }

    private void sendBattleEnd(String winnerName, String loserName) {
        Map<String, String> payload = new HashMap<>();
        payload.put("winner", winnerName);
        payload.put("loser", loserName);
        playerA.sendMessage(Protocol.createMessage(Protocol.TYPE_END_BATTLE, payload));
        playerB.sendMessage(Protocol.createMessage(Protocol.TYPE_END_BATTLE, payload));
    }

    private void sendErrorToPlayers(String message) {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", message);
        playerA.sendMessage(Protocol.createMessage(Protocol.TYPE_ERROR, payload));
        playerB.sendMessage(Protocol.createMessage(Protocol.TYPE_ERROR, payload));
    }
}

