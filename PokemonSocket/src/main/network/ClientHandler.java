package network;

import database.UserDAO;
import engine.BattleRoom;
import models.PokemonLive;
import models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private BufferedReader reader;
    private PrintWriter writer;
    private User user;
    private BattleRoom battleRoom;
    private volatile boolean connected = true;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            handleHandshake();
        } catch (IOException e) {
            closeConnection();
        }
    }

    private void handleHandshake() {
        try {
            Protocol.Message message = readMessage();
            if (message == null || !Protocol.TYPE_HANDSHAKE.equals(message.getType())) {
                sendError("Handshake inválido.");
                closeConnection();
                return;
            }

            String username = message.getPayload().get("username");
            String passwordHash = message.getPayload().get("password_hash");
            if (username == null || passwordHash == null) {
                sendError("Credenciales incompletas.");
                closeConnection();
                return;
            }

            UserDAO userDAO = new UserDAO();
            User authenticated = userDAO.authenticateUser(username, passwordHash);
            if (authenticated == null) {
                sendError("Autenticación fallida.");
                closeConnection();
                return;
            }

            this.user = authenticated;
            Map<String, String> payload = new HashMap<>();
            payload.put("message", "Autenticación exitosa.");
            payload.put("username", user.getUsername());
            sendMessage(Protocol.createMessage(Protocol.TYPE_MATCHMADE, payload));
            server.queuePlayer(this);
        } catch (IOException e) {
            closeConnection();
        }
    }

    public void setBattleRoom(BattleRoom battleRoom) {
        this.battleRoom = battleRoom;
    }

    public User getUser() {
        return user;
    }

    public Protocol.Message readMessage() throws IOException {
        String raw = reader.readLine();
        if (raw == null) {
            throw new IOException("Conexión cerrada por el cliente.");
        }
        return Protocol.parseMessage(raw);
    }

    public void sendMessage(String rawMessage) {
        if (writer != null && !socket.isClosed()) {
            writer.println(rawMessage);
        }
    }

    public void sendError(String message) {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", message);
        sendMessage(Protocol.createMessage(Protocol.TYPE_ERROR, payload));
    }

    public Map<String, String> requestMoveSelection(PokemonLive activePokemon) {
        Map<String, String> payload = new HashMap<>();
        payload.put("pokemon", activePokemon.getDisplayName());
        payload.put("hp", Integer.toString(activePokemon.getCurrentHp()));
        sendMessage(Protocol.createMessage(Protocol.TYPE_REQUEST_MOVE, payload));

        try {
            Protocol.Message response = readMessage();
            if (response == null || !Protocol.TYPE_SEND_MOVE.equals(response.getType())) {
                Map<String, String> fallback = new HashMap<>();
                fallback.put("move_index", "0");
                return fallback;
            }
            return response.getPayload();
        } catch (IOException e) {
            closeConnection();
            Map<String, String> fallback = new HashMap<>();
            fallback.put("move_index", "0");
            return fallback;
        }
    }

    public void closeConnection() {
        connected = false;
        server.removeWaiting(this);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }
}
