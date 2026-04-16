package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BattleDAO {

    public void recordBattleResult(int winnerId, int loserId, String matchType) {
        String query = "INSERT INTO combates (tipo, id_entrenador1, id_entrenador2, resultado) VALUES (?, ?, ?, 'Victoria')";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, matchType);
            pstmt.setInt(2, winnerId);
            pstmt.setInt(3, loserId);

            pstmt.executeUpdate();
            System.out.println("✅ Combate registrado en el historial de la base de datos.");
        } catch (SQLException e) {
            System.err.println("❌ Error registrando el combate: " + e.getMessage());
        }
    }
}
