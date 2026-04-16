import database.DatabaseManager;
import database.UserDAO;
import models.PokemonLive;
import models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== PokemonSocket Test Main ===");

        testDatabaseConnection();
        testUserAuthenticationAndTeam();

        System.out.println("=== Test finalizado ===");
    }

    private static void testDatabaseConnection() {
        System.out.println("Comprobando conexión a la base de datos...");
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn != null && conn.isValid(2)) {
                System.out.println("Conexión válida a PostgreSQL.");
            } else {
                System.err.println("No se pudo validar la conexión.");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    private static void testUserAuthenticationAndTeam() {
        String username = "Red";
        String passwordHash = "hash_password_aqui";

        System.out.println("Autenticando usuario: " + username);
        UserDAO userDAO = new UserDAO();
        User user = userDAO.authenticateUser(username, passwordHash);

        if (user == null) {
            System.err.println("No se pudo autenticar al usuario. Verifica Users y password_hash.");
            return;
        }

        System.out.println("Usuario autenticado: " + user.getUsername() + " (id=" + user.getId() + ")");

        List<PokemonLive> team = userDAO.getUserTeam(user.getId());
        if (team == null || team.isEmpty()) {
            System.out.println("El usuario no tiene Pokémon en su equipo o no se pudo cargar.");
            return;
        }

        System.out.println("Equipo de " + user.getUsername() + ":");
        for (PokemonLive pokemon : team) {
            if (pokemon == null) {
                continue;
            }
            System.out.printf("- %s (Pokedex %d) Nivel %d HP %d/%d Estado %s\n",
                    pokemon.getNickname().isBlank() ? "SinApodo" : pokemon.getNickname(),
                    pokemon.getPokedexId(),
                    pokemon.getLevel(),
                    pokemon.getCurrentHp(),
                    pokemon.getMaxHp(),
                    pokemon.getStatus());

            if (pokemon.getMoves() != null) {
                for (int i = 0; i < pokemon.getMoves().length; i++) {
                    if (pokemon.getMoves()[i] != null) {
                        System.out.printf("    Movimiento %d: %s (%s) PP %d/%d\n",
                                i + 1,
                                pokemon.getMoves()[i].getName(),
                                pokemon.getMoves()[i].getCategory(),
                                pokemon.getMoves()[i].getCurrentPp(),
                                pokemon.getMoves()[i].getPp());
                    }
                }
            }
        }
    }
}
