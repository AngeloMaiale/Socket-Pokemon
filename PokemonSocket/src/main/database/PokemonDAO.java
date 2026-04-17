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
    private static final String TEAM_QUERY =
            "SELECT " +
                    "  ep.id_instancia AS id, ep.id_pokemon_especie AS pokedex_id, ep.apodo AS nickname, ep.nivel AS level, ep.current_hp, " +
                    "  ep.ps_iv AS iv_hp, ep.ataque_iv AS iv_attack, ep.defensa_iv AS iv_defense, ep.velocidad_iv AS iv_speed, " +
                    "  ep.ps_ev AS ev_hp, ep.ataque_ev AS ev_attack, ep.defensa_ev AS ev_defense, ep.velocidad_ev AS ev_speed, " +
                    "  ep.estado AS status, p.nombre AS nombre_pokemon, eb.ps AS base_hp, eb.ataque AS base_attack, eb.defensa AS base_defense, eb.velocidad AS base_speed, " +
                    "  t1.nombre AS type1, t2.nombre AS type2, " +

                    "  m1.id_movimiento AS move1_id, m1.nombre AS move1_name, m1.id_tipo AS move1_type, m1.potencia AS move1_power, m1.precision AS move1_accuracy, m1.pp AS move1_max_pp, " +
                    "  COALESCE(m1.pp, 0) AS move1_current_pp, m1.categoria AS move1_category, " +

                    "  m2.id_movimiento AS move2_id, m2.nombre AS move2_name, m2.id_tipo AS move2_type, m2.potencia AS move2_power, m2.precision AS move2_accuracy, m2.pp AS move2_max_pp, " +
                    "  COALESCE(m2.pp, 0) AS move2_current_pp, m2.categoria AS move2_category, " +

                    "  m3.id_movimiento AS move3_id, m3.nombre AS move3_name, m3.id_tipo AS move3_type, m3.potencia AS move3_power, m3.precision AS move3_accuracy, m3.pp AS move3_max_pp, " +
                    "  COALESCE(m3.pp, 0) AS move3_current_pp, m3.categoria AS move3_category, " +

                    "  m4.id_movimiento AS move4_id, m4.nombre AS move4_name, m4.id_tipo AS move4_type, m4.potencia AS move4_power, m4.precision AS move4_accuracy, m4.pp AS move4_max_pp, " +
                    "  COALESCE(m4.pp, 0) AS move4_current_pp, m4.categoria AS move4_category " +

                    "FROM equipo_pokemon ep " +
                    "JOIN pokemon p ON ep.id_pokemon_especie = p.id_pokemon " +
                    "JOIN estadisticas_base eb ON ep.id_pokemon_especie = eb.id_pokemon " +
                    "LEFT JOIN pokemon_tipos pt1 ON ep.id_pokemon_especie = pt1.id_pokemon AND pt1.es_principal = true " +
                    "LEFT JOIN tipos t1 ON pt1.id_tipo = t1.id_tipo " +
                    "LEFT JOIN pokemon_tipos pt2 ON ep.id_pokemon_especie = pt2.id_pokemon AND pt2.es_principal = false " +
                    "LEFT JOIN tipos t2 ON pt2.id_tipo = t2.id_tipo " +
                    "LEFT JOIN movimientos m1 ON ep.move1_id = m1.id_movimiento " +
                    "LEFT JOIN movimientos m2 ON ep.move2_id = m2.id_movimiento " +
                    "LEFT JOIN movimientos m3 ON ep.move3_id = m3.id_movimiento " +
                    "LEFT JOIN movimientos m4 ON ep.move4_id = m4.id_movimiento " +
                    "WHERE ep.id_entrenador = ? " +
                    "ORDER BY ep.position";

    public List<PokemonLive> getTeamByUserId(int userId) {
        List<PokemonLive> team = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(TEAM_QUERY)) {
            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Move[] moves = new Move[4];
                    moves[0] = buildMove(rs, "move1_id", "move1_name", "move1_type", "move1_power", "move1_accuracy", "move1_max_pp", "move1_current_pp", "move1_category");
                    moves[1] = buildMove(rs, "move2_id", "move2_name", "move2_type", "move2_power", "move2_accuracy", "move2_max_pp", "move2_current_pp", "move2_category");
                    moves[2] = buildMove(rs, "move3_id", "move3_name", "move3_type", "move3_power", "move3_accuracy", "move3_max_pp", "move3_current_pp", "move3_category");
                    moves[3] = buildMove(rs, "move4_id", "move4_name", "move4_type", "move4_power", "move4_accuracy", "move4_max_pp", "move4_current_pp", "move4_category");

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
                            rs.getString("type1"),
                            rs.getString("type2"),
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
                           String powerField, String accuracyField, String maxPpField, String currentPpField, String categoryField)
            throws SQLException {
        Integer moveId = rs.getObject(idField, Integer.class);
        if (moveId == null) {
            return null;
        }

        int maxPp = rs.getInt(maxPpField);
        int currentPp = rs.getInt(currentPpField);
        if (rs.wasNull() || currentPp <= 0) {
            currentPp = maxPp;
        }

        return new Move(
                moveId,
                rs.getString(nameField),
                rs.getString(typeField),
                rs.getInt(powerField),
                rs.getInt(accuracyField),
                maxPp,
                currentPp,
                0,
                rs.getString(categoryField)
        );
    }
    public void updatePokemonState(PokemonLive pokemon) {
        String query = "UPDATE TrainerPokemon SET current_hp = ?, status = ?, move1_pp = ?, move2_pp = ?, move3_pp = ?, move4_pp = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, pokemon.getCurrentHp());
            pstmt.setString(2, pokemon.getStatus());
            pstmt.setInt(3, pokemon.getMoves()[0] != null ? pokemon.getMoves()[0].getCurrentPp() : 0);
            pstmt.setInt(4, pokemon.getMoves()[1] != null ? pokemon.getMoves()[1].getCurrentPp() : 0);
            pstmt.setInt(5, pokemon.getMoves()[2] != null ? pokemon.getMoves()[2].getCurrentPp() : 0);
            pstmt.setInt(6, pokemon.getMoves()[3] != null ? pokemon.getMoves()[3].getCurrentPp() : 0);
            pstmt.setInt(7, pokemon.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error actualizando estado del Pokémon: " + e.getMessage());
        }
    }

    public void healTeam(int userId) {
        String query = "UPDATE TrainerPokemon SET current_hp = NULL, status = 'NONE' WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.executeUpdate();

            List<PokemonLive> team = getTeamByUserId(userId);
            for (PokemonLive pokemon : team) {
                pokemon.setCurrentHp(pokemon.getMaxHp());
                pokemon.setStatus("NONE");
                updatePokemonState(pokemon);
            }

            System.out.println("✅ Equipo del usuario " + userId + " curado completamente.");
        } catch (SQLException e) {
            System.err.println("❌ Error curando al equipo: " + e.getMessage());
        }
    }
}
