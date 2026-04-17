package network;

import engine.BattleRoom;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private final int port;
    private final BlockingQueue<ClientHandler> waitingPlayers = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor de Pokémon iniciado en el puerto " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                executor.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("Error iniciando el servidor: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    public void queuePlayer(ClientHandler player) {
        ClientHandler waiting = waitingPlayers.poll();
        if (waiting == null) {
            waitingPlayers.offer(player);
            Map<String, String> payload = new HashMap<>();
            payload.put("message", "Esperando rival...");
            player.sendMessage(Protocol.createMessage(Protocol.TYPE_WAITING, payload));
        } else {
            BattleRoom room = new BattleRoom(waiting, player);
            executor.submit(room);
        }
    }

    public void removeWaiting(ClientHandler player) {
        waitingPlayers.remove(player);
    }

    public static void main(String[] args) {
        int port = 5000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        new Server(port).start();
    }
}
