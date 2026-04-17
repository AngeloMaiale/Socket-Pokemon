package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BattleDAO {

    public void recordBattleResult(int winnerId, int loserId, String mode) {
        String sql = "INSERT INTO combates (id_entrenador1, id_entrenador2, ganador_id) VALUES (?, ?, ?)";
        try (Connection conn = database.DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, winnerId);
            pstmt.setInt(2, loserId);
            pstmt.setInt(3, winnerId);
            pstmt.executeUpdate();

            System.out.println("[DB] Resultado guardado exitosamente.");
        } catch (SQLException e) {
            System.err.println("[DB ERROR] No se pudo guardar la batalla: " + e.getMessage());
        }
    }
}