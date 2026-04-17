package network;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TestClient2 {
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
                        if (response.startsWith(Protocol.LOGIN_SUCCESS)) {
                            System.out.println("Login exitoso. Esperando batalla...");
                        }
                        if (response.startsWith(Protocol.TURN_REQUEST)) {
                            System.out.println("Respuesta automática: enviando ATTACK:0");
                            out.println("ATTACK:0");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Conexión cerrada por el servidor.");
                }
            }).start();
            String loginCommand = "LOGIN:Misty:clave456";
            System.out.println("Enviando login automático: " + loginCommand);
            out.println(loginCommand);
            System.out.println("Puedes escribir comandos manuales mientras esperas.");
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                out.println(command);
            }

        } catch (IOException e) {
            System.err.println("❌ No se pudo conectar al servidor. ¿Está GameServer corriendo?");
        }
    }
}
