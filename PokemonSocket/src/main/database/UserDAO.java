package main.database;

package com.tuusuario.pokemon.database;

import com.tuusuario.pokemon.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Método que el Dev 1 llamará cuando un cliente envíe sus credenciales
    public User authenticateUser(String username, String passwordHash) {
        String query = "SELECT id, username, elo_rating FROM Users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getInt("elo_rating")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error autenticando usuario: " + e.getMessage());
        }
        return null;
    }

    public void updateElo(int userId, int newElo, boolean isWinner) {
        String query = "UPDATE Users SET elo_rating = ?, " +
                (isWinner ? "wins = wins + 1 " : "losses = losses + 1 ") +
                "WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, newElo);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error actualizando ELO: " + e.getMessage());
        }
    }
}