package main.network;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 1234;

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("✅ Conectado al servidor Pokémon.");
            Scanner scanner = new Scanner(System.in);
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("\n[SERVIDOR]: " + response);
                    }
                } catch (IOException e) {
                    System.out.println("Conexión cerrada por el servidor.");
                }
            }).start();
            System.out.println("Escribe el comando (Ej: LOGIN:Red:123456):");
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                out.println(command);
            }

        } catch (IOException e) {
            System.err.println("❌ No se pudo conectar al servidor. ¿Está GameServer corriendo?");
        }
    }
}
