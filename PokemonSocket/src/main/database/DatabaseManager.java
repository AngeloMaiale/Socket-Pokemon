package main.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/pokemon_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "tu_password";

    private static Connection connection = null;

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Conexión a PostgreSQL establecida.");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Error: Driver de PostgreSQL no encontrado.");
                e.printStackTrace();
            }
        }
        return connection;
    }
}
