package database;

import models.Move;
import models.PokemonLive;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PokemonDAO {
    private static final String TEAM_QUERY = "SELECT tp.id, tp.pokedex_id, tp.nickname, tp.level, tp.current_hp, tp.iv_hp, tp.iv_attack, tp.iv_defense, tp.iv_speed, "
            + "tp.ev_hp, tp.ev_attack, tp.ev_defense, tp.ev_speed, tp.status, "
            + "p.base_hp, p.base_attack, p.base_defense, p.base_speed, "
            + "m1.id AS move1_id, m1.name AS move1_name, m1.type AS move1_type, m1.power AS move1_power, m1.accuracy AS move1_accuracy, m1.pp AS move1_pp, m1.category AS move1_category, "
            + "m2.id AS move2_id, m2.name AS move2_name, m2.type AS move2_type, m2.power AS move2_power, m2.accuracy AS move2_accuracy, m2.pp AS move2_pp, m2.category AS move2_category, "
            + "m3.id AS move3_id, m3.name AS move3_name, m3.type AS move3_type, m3.power AS move3_power, m3.accuracy AS move3_accuracy, m3.pp AS move3_pp, m3.category AS move3_category, "
            + "m4.id AS move4_id, m4.name AS move4_name, m4.type AS move4_type, m4.power AS move4_power, m4.accuracy AS move4_accuracy, m4.pp AS move4_pp, m4.category AS move4_category "
            + "FROM TrainerPokemon tp "
            + "JOIN Pokedex p ON tp.pokedex_id = p.id "
            + "LEFT JOIN Moves m1 ON tp.move1_id = m1.id "
            + "LEFT JOIN Moves m2 ON tp.move2_id = m2.id "
            + "LEFT JOIN Moves m3 ON tp.move3_id = m3.id "
            + "LEFT JOIN Moves m4 ON tp.move4_id = m4.id "
            + "WHERE tp.user_id = ? "
            + "ORDER BY tp.position";

    public List<PokemonLive> getTeamByUserId(int userId) {
        List<PokemonLive> team = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(TEAM_QUERY)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Move[] moves = new Move[4];
                    moves[0] = buildMove(rs, "move1_id", "move1_name", "move1_type", "move1_power", "move1_accuracy", "move1_pp", "move1_category");
                    moves[1] = buildMove(rs, "move2_id", "move2_name", "move2_type", "move2_power", "move2_accuracy", "move2_pp", "move2_category");
                    moves[2] = buildMove(rs, "move3_id", "move3_name", "move3_type", "move3_power", "move3_accuracy", "move3_pp", "move3_category");
                    moves[3] = buildMove(rs, "move4_id", "move4_name", "move4_type", "move4_power", "move4_accuracy", "move4_pp", "move4_category");

                    int level = rs.getInt("level");
                    int ivHp = rs.getInt("iv_hp");
                    int ivAttack = rs.getInt("iv_attack");
                    int ivDefense = rs.getInt("iv_defense");
                    int ivSpeed = rs.getInt("iv_speed");
                    int evHp = rs.getInt("ev_hp");
                    int evAttack = rs.getInt("ev_attack");
                    int evDefense = rs.getInt("ev_defense");
                    int evSpeed = rs.getInt("ev_speed");

                    int baseHp = rs.getInt("base_hp");
                    int baseAttack = rs.getInt("base_attack");
                    int baseDefense = rs.getInt("base_defense");
                    int baseSpeed = rs.getInt("base_speed");

                    int maxHp = PokemonLive.calculateHp(baseHp, ivHp, evHp, level);
                    int attack = PokemonLive.calculateStat(baseAttack, ivAttack, evAttack, level);
                    int defense = PokemonLive.calculateStat(baseDefense, ivDefense, evDefense, level);
                    int speed = PokemonLive.calculateStat(baseSpeed, ivSpeed, evSpeed, level);

                    int currentHp = rs.getInt("current_hp");
                    if (rs.wasNull() || currentHp <= 0) {
                        currentHp = maxHp;
                    }

                    String status = rs.getString("status");
                    if (status == null || status.isBlank()) {
                        status = "NONE";
                    }

                    PokemonLive pokemon = new PokemonLive(
                            rs.getInt("id"),
                            rs.getInt("pokedex_id"),
                            rs.getString("nickname"),
                            level,
                            currentHp,
                            maxHp,
                            attack,
                            defense,
                            speed,
                            moves,
                            status
                    );
                    team.add(pokemon);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo equipo de usuario: " + e.getMessage());
        }

        return team;
    }

    private Move buildMove(ResultSet rs, String idField, String nameField, String typeField,
                           String powerField, String accuracyField, String ppField, String categoryField)
            throws SQLException {
        Integer moveId = rs.getObject(idField, Integer.class);
        if (moveId == null) {
            return null;
        }

        return new Move(
                moveId,
                rs.getString(nameField),
                rs.getString(typeField),
                rs.getInt(powerField),
                rs.getInt(accuracyField),
                rs.getInt(ppField),
                rs.getInt(ppField),
                rs.getString(categoryField)
        );
    }
}
