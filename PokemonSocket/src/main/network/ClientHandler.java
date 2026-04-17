package network;

import engine.BattleRoom;
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
        sendMessage("SISTEMA: Es el turno de " + activePokemon.getNickname() + ". Elige ataque (ATTACK:0 o ATTACK:1)");

        lastInput = null;
        while (lastInput == null) {
            try {
                wait(); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Map<String, String> action = new HashMap<>();
        
        String moveIndex = lastInput.split(":")[1];
        action.put("move_index", moveIndex);
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
                
                if (inputLine.startsWith("ATTACK:")) {
                    synchronized (this) {
                        this.lastInput = inputLine;
                        this.notifyAll();
                    }
                }
                
                else if (inputLine.startsWith("LOGIN:")) {
                    
                }
            }
        } catch (IOException e) {
            System.out.println("El cliente se ha desconectado de forma abrupta.");
        } finally {
            closeConnection();
        }
    }
}