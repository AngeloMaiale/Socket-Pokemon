package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BattleDAO {

    public void recordBattleResult(int winnerId, int loserId, String mode) {
        String sql = "INSERT INTO Battles (winner_id, loser_id, mode) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, winnerId);
            pstmt.setInt(2, loserId);
            pstmt.setString(3, mode);
            pstmt.executeUpdate();

            System.out.println("[DB] Resultado guardado exitosamente.");
        } catch (SQLException e) {
            System.err.println("[DB ERROR] No se pudo guardar la batalla: " + e.getMessage());
        }
    }
}