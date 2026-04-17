package network;

import engine.BattleRoom;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 1234;
    private static List<ClientHandler> waitingLobby = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("=== Iniciando Servidor de Batallas Pokémon ===");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor escuchando en el puerto: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión entrante: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    
    public static synchronized void joinLobby(ClientHandler player) {
        if (!player.isLoggedIn()) {
            player.sendMessage("ERROR:LOGIN_REQUIRED");
            return;
        }

        waitingLobby.add(player);
        System.out.println(player.getUser().getUsername() + " ha entrado al lobby.");

        if (waitingLobby.size() >= 2) {
            ClientHandler player1 = waitingLobby.remove(0);
            ClientHandler player2 = waitingLobby.remove(0);

            System.out.println("Emparejando a " + player1.getUser().getUsername() + " vs " + player2.getUser().getUsername());
            BattleRoom battleRoom = new BattleRoom(player1, player2);
            new Thread(battleRoom).start();
        } else {
            player.sendMessage("WAITING_OPPONENT:Buscando partida...");
        }
    }
}
