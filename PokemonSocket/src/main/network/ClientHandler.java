package network;

import database.UserDAO;
import engine.BattleRoom;
import models.Move;
import models.PokemonLive;
import models.User;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private User user; 
    private BattleRoom battleRoom; 
    private BattleSession battleSession; 
    private boolean loggedIn = false;
    private String lastInput = null;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error al inicializar ClientHandler: " + e.getMessage());
        }
    }

    

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setBattleRoom(BattleRoom battleRoom) {
        this.battleRoom = battleRoom;
    }

    public void setBattleSession(BattleSession battleSession) {
        this.battleSession = battleSession;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /**
     * Pausa el hilo de la batalla hasta que este cliente envíe un movimiento.
     * Requerido por la lógica de turnos de BattleRoom.
     */
    public synchronized Map<String, String> requestMoveSelection(PokemonLive activePokemon) {
        StringBuilder movesList = new StringBuilder("SISTEMA: Es el turno de ")
                .append(activePokemon.getNickname()).append(". Movimientos:");
        Move[] moves = activePokemon.getMoves();
        if (moves != null) {
            for (int i = 0; i < moves.length; i++) {
                if (moves[i] != null) {
                    movesList.append(" ").append(i).append("=").append(moves[i].getName())
                            .append("(PP:").append(moves[i].getCurrentPp()).append("/").append(moves[i].getPp()).append(")");
                }
            }
        }
        movesList.append(". Elige ataque con ATTACK:indice");
        sendMessage(Protocol.TURN_REQUEST + ":" + movesList.toString());

        final long timeout = 30000;
        long deadline = System.currentTimeMillis() + timeout;
        lastInput = null;
        while (lastInput == null || !lastInput.startsWith("ATTACK:")) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                return null;
            }
            try {
                wait(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        Map<String, String> action = new HashMap<>();
        String[] parts = lastInput.split(":", 2);
        String moveIndex = parts.length > 1 ? parts[1] : "0";
        action.put("move_index", moveIndex);
        return action;
    }

    public synchronized Map<String, String> requestSwitchPokemon() {
        sendMessage(Protocol.SWITCH_REQUEST + ":Elige índice de cambio (SWITCH:0-5)");

        final long timeout = 30000;
        long deadline = System.currentTimeMillis() + timeout;
        lastInput = null;
        while (lastInput == null || !lastInput.startsWith("SWITCH:")) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                return null;
            }
            try {
                wait(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        Map<String, String> action = new HashMap<>();
        String[] parts = lastInput.split(":", 2);
        String switchIndex = parts.length > 1 ? parts[1] : "0";
        action.put("switch_index", switchIndex);
        return action;
    }

    public void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Conexión cerrada para el usuario: " + (user != null ? user.getUsername() : "Desconocido"));
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    

    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                
                if (inputLine.startsWith("ATTACK:") || inputLine.startsWith("SWITCH:")) {
                    synchronized (this) {
                        this.lastInput = inputLine;
                        this.notifyAll();
                    }
                } else if (inputLine.startsWith("LOGIN:")) {
                    String[] parts = inputLine.split(":", 3);
                    if (parts.length < 3) {
                        sendMessage(Protocol.LOGIN_FAIL);
                        closeConnection();
                        break;
                    }
                    String username = parts[1];
                    String passwordHash = parts[2];
                    User authenticated = new UserDAO().authenticateUser(username, passwordHash);
                    if (authenticated != null) {
                        setUser(authenticated);
                        loggedIn = true;
                        sendMessage(Protocol.LOGIN_SUCCESS);
                        Server.joinLobby(this);
                    } else {
                        sendMessage(Protocol.LOGIN_FAIL);
                        closeConnection();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("El cliente se ha desconectado de forma abrupta.");
        } finally {
            closeConnection();
        }
    }
}