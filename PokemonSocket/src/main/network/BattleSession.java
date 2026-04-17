package network;

import database.BattleDAO;
import database.PokemonDAO;
import database.UserDAO;
import engine.DamageCalculator;
import engine.TurnManager;
import models.Move;
import models.PokemonLive;
import java.util.List;

public class BattleSession implements Runnable {
    private ClientHandler player1;
    private ClientHandler player2;

    private List<PokemonLive> team1;
    private List<PokemonLive> team2;

    private PokemonLive activeP1;
    private PokemonLive activeP2;

    private PokemonDAO pokemonDAO;
    private BattleDAO battleDAO;
    private UserDAO userDAO;

    private TurnManager.TurnAction actionP1 = null;
    private TurnManager.TurnAction actionP2 = null;
    private boolean battleRunning = true;

    public BattleSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.pokemonDAO = new PokemonDAO();
        this.battleDAO = new BattleDAO();
        this.userDAO = new UserDAO();

        player1.setBattleSession(this);
        player2.setBattleSession(this);
    }

    @Override
    public void run() {
        try {
            team1 = pokemonDAO.getTeamByUserId(player1.getUser().getId());
            team2 = pokemonDAO.getTeamByUserId(player2.getUser().getId());
            if (team1.isEmpty() || team2.isEmpty()) {
                broadcast("ERROR: Uno de los jugadores no tiene equipo listo.");
                return;
            }
            activeP1 = team1.get(0);
            activeP2 = team2.get(0);
            player1.sendMessage(Protocol.RES_BATTLE_START + ":" + player2.getUser().getUsername() + ":" + activeP2.getNickname());
            player2.sendMessage(Protocol.RES_BATTLE_START + ":" + player1.getUser().getUsername() + ":" + activeP1.getNickname());
            while (battleRunning) {
                Thread.sleep(100);
            }

        } catch (Exception e) {
            System.err.println("Error en la sesión de batalla: " + e.getMessage());
        }
    }

    public synchronized void registerAction(ClientHandler player, int moveId) {
        Move selectedMove = null;
        PokemonLive currentPkmn = (player == player1) ? activeP1 : activeP2;

        for (Move m : currentPkmn.getMoves()) {
            if (m != null && m.getId() == moveId) {
                selectedMove = m;
                break;
            }
        }

        if (selectedMove == null) return;
        if (player == player1) {
            actionP1 = new TurnManager.TurnAction(String.valueOf(player1.getUser().getId()), activeP1, selectedMove);
        } else {
            actionP2 = new TurnManager.TurnAction(String.valueOf(player2.getUser().getId()), activeP2, selectedMove);
        }
        if (actionP1 != null && actionP2 != null) {
            processTurn();
        }
    }

    private void processTurn() {
        List<TurnManager.TurnAction> order = TurnManager.determineTurnOrder(actionP1, actionP2);

        for (TurnManager.TurnAction action : order) {
            if (!battleRunning) break;
            PokemonLive attacker = action.getPokemon();
            PokemonLive defender = (attacker == activeP1) ? activeP2 : activeP1;
            Move move = action.getSelectedMove();
            int damage = 10;
            try {
            } catch (Exception e) {}
            defender.setCurrentHp(Math.max(0, defender.getCurrentHp() - damage));
            broadcast("MSG:" + attacker.getNickname() + " usó " + move.getName() + " e hizo " + damage + " de daño.");
            pokemonDAO.updatePokemonState(defender);

            if (defender.getCurrentHp() <= 0) {
                handleFaint(defender);
            }
        }
        actionP1 = null;
        actionP2 = null;
        if (battleRunning) {
            broadcast("NEXT_TURN:Esperando movimientos...");
        }
    }

    private void handleFaint(PokemonLive fainted) {
        broadcast("MSG:" + fainted.getNickname() + " se ha debilitado.");
        boolean p1Lost = activeP1.getCurrentHp() <= 0;

        if (p1Lost) {
            endBattle(player2, player1);
        } else {
            endBattle(player1, player2);
        }
    }

    private void endBattle(ClientHandler winner, ClientHandler loser) {
        battleRunning = false;
        winner.sendMessage(Protocol.RES_BATTLE_END + ":VICTORIA");
        loser.sendMessage(Protocol.RES_BATTLE_END + ":DERROTA");
        battleDAO.recordBattleResult(winner.getUser().getId(), loser.getUser().getId(), "Online");
        userDAO.updateElo(winner.getUser().getId(), winner.getUser().getEloRating() + 15, true);
        userDAO.updateElo(loser.getUser().getId(), loser.getUser().getEloRating() - 10, false);
    }

    private void broadcast(String message) {
        player1.sendMessage(message);
        player2.sendMessage(message);
    }
}