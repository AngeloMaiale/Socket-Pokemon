package engine;

import database.BattleDAO;
import database.PokemonDAO;
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
import java.util.concurrent.ThreadLocalRandom;

public class BattleRoom implements Runnable {
    private static final long ACTION_TIMEOUT_MS = 30000;

    private final ClientHandler playerA;
    private final ClientHandler playerB;
    private User userA;
    private User userB;
    private List<PokemonLive> teamA;
    private List<PokemonLive> teamB;
    private final PokemonDAO pokemonDAO = new PokemonDAO();
    private boolean battleAborted = false;

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

        while (true) {
            if (!hasAlivePokemon(teamA) || !hasAlivePokemon(teamB)) {
                break;
            }

            if (activeA == null || activeA.isFainted()) {
                activeA = promptSwitchPokemon(playerA, playerB, teamA);
                if (battleAborted) return;
                if (activeA == null) break;
            }
            if (activeB == null || activeB.isFainted()) {
                activeB = promptSwitchPokemon(playerB, playerA, teamB);
                if (battleAborted) return;
                if (activeB == null) break;
            }

            if (activeA == null || activeB == null) {
                break;
            }

            Map<String, String> actionAData = playerA.requestMoveSelection(activeA);
            if (actionAData == null) {
                handleTimeoutForfeit(playerA, playerB);
                return;
            }

            Map<String, String> actionBData = playerB.requestMoveSelection(activeB);
            if (actionBData == null) {
                handleTimeoutForfeit(playerB, playerA);
                return;
            }

            TurnManager.TurnAction actionA = buildTurnAction(playerA.getUser(), activeA, actionAData);
            TurnManager.TurnAction actionB = buildTurnAction(playerB.getUser(), activeB, actionBData);

            List<TurnManager.TurnAction> ordered = TurnManager.orderActions(Arrays.asList(actionA, actionB));

            for (TurnManager.TurnAction action : ordered) {
                if (activeA.isFainted() || activeB.isFainted()) {
                    break;
                }
                executeAction(action, activeA, activeB);
            }

            applyStatusDamage(activeA, playerA);
            applyStatusDamage(activeB, playerB);

            if (activeA.isFainted()) {
                activeA = promptSwitchPokemon(playerA, playerB, teamA);
                if (battleAborted || activeA == null) return;
            }
            if (activeB.isFainted()) {
                activeB = promptSwitchPokemon(playerB, playerA, teamB);
                if (battleAborted || activeB == null) return;
            }

            if (activeA == null || activeB == null) {
                break;
            }
        }

        finalizeBattle(activeA, activeB);
    }

    private boolean hasAlivePokemon(List<PokemonLive> team) {
        return team.stream().anyMatch(p -> p != null && !p.isFainted());
    }

    private PokemonLive promptSwitchPokemon(ClientHandler player, ClientHandler opponent, List<PokemonLive> team) {
        if (!hasAlivePokemon(team)) {
            return null;
        }

        player.sendMessage(Protocol.SWITCH_REQUEST + ":Elige índice de cambio (SWITCH:0-" + (team.size() - 1) + ")");
        Map<String, String> action = player.requestSwitchPokemon();
        if (action == null) {
            handleTimeoutForfeit(player, opponent);
            return null;
        }

        int switchIndex;
        try {
            switchIndex = Integer.parseInt(action.getOrDefault("switch_index", "0"));
        } catch (NumberFormatException e) {
            switchIndex = 0;
        }

        if (switchIndex < 0 || switchIndex >= team.size()) {
            switchIndex = 0;
        }

        PokemonLive selected = team.get(switchIndex);
        if (selected == null || selected.isFainted()) {
            for (PokemonLive pokemon : team) {
                if (pokemon != null && !pokemon.isFainted()) {
                    selected = pokemon;
                    break;
                }
            }
        }

        if (selected == null || selected.isFainted()) {
            return null;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("activePokemon", selected.getDisplayName());
        player.sendMessage(Protocol.createMessage(Protocol.SWITCH_REQUEST, payload));
        return selected;
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

        if (!attacker.canAct()) {
            sendActionUpdate(attacker, defender, 0, attacker.getDisplayName() + " no puede moverse!");
            return;
        }

        if (move.getCurrentPp() <= 0) {
            sendActionUpdate(attacker, defender, 0, move.getName() + " no tiene PP.");
            return;
        }

        if (!isMoveSuccessful(move, attacker, defender)) {
            move.setCurrentPp(move.getCurrentPp() - 1);
            pokemonDAO.updatePokemonState(attacker);
            sendActionUpdate(attacker, defender, 0, move.getName() + " falló!");
            return;
        }

        boolean isStatusMove = move.getPower() <= 0 || "STATUS".equalsIgnoreCase(move.getCategory());
        int damage = 0;

        move.setCurrentPp(move.getCurrentPp() - 1);
        if (!isStatusMove) {
            damage = calculateDamage(move, attacker, defender);
            defender.applyDamage(damage);
        } else {
            applyStatusEffect(move, defender);
        }

        pokemonDAO.updatePokemonState(attacker);
        pokemonDAO.updatePokemonState(defender);
        sendActionUpdate(attacker, defender, damage, move.getName());
    }

    private int calculateDamage(Move move, PokemonLive attacker, PokemonLive defender) {
        if (move == null || attacker == null || defender == null) {
            return 0;
        }

        if (move.getPower() <= 0 || "STATUS".equalsIgnoreCase(move.getCategory())) {
            return 0;
        }

        double multiplier = 1.0;
        multiplier *= DamageCalculator.getTypeMultiplier(move.getType(), defender.getType1());
        if (defender.getType2() != null && !defender.getType2().isBlank()) {
            multiplier *= DamageCalculator.getTypeMultiplier(move.getType(), defender.getType2());
        }

        if (move.getType() != null && (move.getType().equalsIgnoreCase(attacker.getType1()) || move.getType().equalsIgnoreCase(attacker.getType2()))) {
            multiplier *= 1.5;
        }

        if (DamageCalculator.isCriticalHit()) {
            multiplier *= 1.5;
        }

        double randomFactor = 0.85 + Math.random() * 0.15;
        multiplier *= randomFactor;

        return DamageCalculator.calculateDamage(attacker.getLevel(), move.getPower(), attacker.getAttack(), Math.max(defender.getDefense(), 1), multiplier);
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
        boolean aliveA = hasAlivePokemon(teamA);
        boolean aliveB = hasAlivePokemon(teamB);

        if (aliveA && !aliveB) {
            winner = userA;
            loser = userB;
        } else if (!aliveA && aliveB) {
            winner = userB;
            loser = userA;
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

    private void handleTimeoutForfeit(ClientHandler timedOutPlayer, ClientHandler opponent) {
        if (timedOutPlayer == null || opponent == null) {
            return;
        }
        sendErrorToPlayers("El oponente no respondió a tiempo. Fin de batalla.");
        battleAborted = true;

        User winner = opponent.getUser();
        User loser = timedOutPlayer.getUser();
        if (winner != null && loser != null) {
            BattleDAO battleDAO = new BattleDAO();
            battleDAO.recordBattleResult(winner.getId(), loser.getId(), "Timeout");
            new UserDAO().updateElo(winner.getId(), winner.getEloRating() + 15, true);
            new UserDAO().updateElo(loser.getId(), loser.getEloRating() - 10, false);
        }
        sendBattleEnd(opponent.getUser().getUsername(), timedOutPlayer.getUser().getUsername());
    }

    private void applyStatusDamage(PokemonLive pokemon, ClientHandler player) {
        if (pokemon == null || player == null) {
            return;
        }

        int priorHp = pokemon.getCurrentHp();
        pokemon.applyStatusDamage();

        if (pokemon.getCurrentHp() < priorHp) {
            pokemonDAO.updatePokemonState(pokemon);
            sendActionUpdate(pokemon, pokemon, priorHp - pokemon.getCurrentHp(), "Daño por estado: " + pokemon.getStatus());
        }
    }

    private boolean isMoveSuccessful(Move move, PokemonLive attacker, PokemonLive defender) {
        if (move == null || move.getAccuracy() <= 0) {
            return true;
        }
        int roll = ThreadLocalRandom.current().nextInt(1, 101);
        return roll <= move.getAccuracy();
    }

    private void applyStatusEffect(Move move, PokemonLive target) {
        if (move == null || target == null || target.isFainted()) {
            return;
        }

        if (target.getStatus() != null && !target.getStatus().isBlank() && !target.getStatus().equalsIgnoreCase("NONE")) {
            return;
        }

        String name = move.getName();
        if (name == null) {
            return;
        }

        String normalized = name.trim().toUpperCase();
        switch (normalized) {
            case "THUNDER WAVE":
            case "THUNDERWAVE":
                target.setStatus("PARALYZED");
                break;
            case "TOXIC":
                target.setStatus("POISONED");
                break;
            case "WILL O WISP":
            case "WILLOWISP":
                target.setStatus("BURNED");
                break;
            default:
                break;
        }
    }

    private boolean canAct(PokemonLive pokemon) {
        if (pokemon == null) {
            return false;
        }
        return pokemon.canAct();
    }

    private void sendErrorToPlayers(String message) {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", message);
        playerA.sendMessage(Protocol.createMessage(Protocol.TYPE_ERROR, payload));
        playerB.sendMessage(Protocol.createMessage(Protocol.TYPE_ERROR, payload));
    }
}

