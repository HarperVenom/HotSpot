package me.harpervenom.hotspot.database.managers;

import me.harpervenom.hotspot.statistics.Stats;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PlayersManager {

    private final Connection connection;

    public PlayersManager(Connection connection) throws SQLException {
        this.connection = connection;

            try (Statement statement = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS players (" +
                        "id TEXT NOT NULL, " +
                        "exp INTEGER NOT NULL DEFAULT 0, " +
                        "rank DOUBLE NOT NULL DEFAULT 0.0, " +
                        "games_played INTEGER NOT NULL, " +
                        "games_won INTEGER NOT NULL, " +
                        "kills INTEGER NOT NULL, " +
                        "deaths INTEGER NOT NULL, " +
                        "damage_dealt DOUBLE NOT NULL, " +
                        "damage_received DOUBLE NOT NULL, " +
                        "damage_prevented DOUBLE NOT NULL, " +
                        "points_captured INTEGER NOT NULL, " +

                        "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
                statement.executeUpdate(sql);
            }
    }

    public CompletableFuture<Stats> create(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO players (" +
                    "id, exp, rank, games_played, games_won, kills, deaths, " +
                    "damage_dealt, damage_received, damage_prevented, points_captured" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, id.toString());
                ps.setInt(2, 0);
                ps.setDouble(3, 0);
                ps.setInt(4, 0);
                ps.setInt(5, 0);
                ps.setInt(6, 0);
                ps.setInt(7, 0);
                ps.setDouble(8, 0.0);
                ps.setDouble(9, 0.0);
                ps.setDouble(10, 0.0);
                ps.setInt(11, 0);

                ps.executeUpdate();

                return new Stats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }


    public CompletableFuture<Stats> getOrCreateStats(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT exp, rank, games_played, games_won, kills, deaths, damage_dealt, damage_received, damage_prevented, points_captured " +
                    "FROM players WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, id.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Stats(
                                rs.getInt("exp"),
                                rs.getDouble("rank"),
                                rs.getInt("games_played"),
                                rs.getInt("games_won"),
                                rs.getInt("kills"),
                                rs.getInt("deaths"),
                                rs.getDouble("damage_dealt"),
                                rs.getDouble("damage_received"),
                                rs.getDouble("damage_prevented"),
                                rs.getInt("points_captured")
                        );
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // If not found, insert new and return default stats
            return create(id).join(); // Safe because `create()` is already async
        });
    }


    public CompletableFuture<Void> updateStats(UUID id, Stats stats) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE players SET " +
                    "exp = ?, " +
                    "rank = ?, " +
                    "games_played = ?, " +
                    "games_won = ?, " +
                    "kills = ?, " +
                    "deaths = ?, " +
                    "damage_dealt = ?, " +
                    "damage_received = ?, " +
                    "damage_prevented = ?, " +
                    "points_captured = ? " +
                    "WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, stats.getExp());
                ps.setDouble(2, stats.getRank());
                ps.setInt(3, stats.getGamesPlayed());
                ps.setInt(4, stats.getGamesWon());
                ps.setInt(5, stats.getKills());
                ps.setInt(6, stats.getDeaths());
                ps.setDouble(7, stats.getDealtDamage());
                ps.setDouble(8, stats.getTakenDamage());
                ps.setDouble(9, stats.getPreventedDamage());
                ps.setDouble(10, stats.getCapturedPoints());
                ps.setString(11, id.toString());
                ps.executeUpdate();

                System.out.println(stats.getCapturedPoints());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<Void> updateAllStats(Map<UUID, Stats> statsMap) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<UUID, Stats> entry : statsMap.entrySet()) {
            futures.add(updateStats(entry.getKey(), entry.getValue()));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public CompletableFuture<Map<UUID, Double>> getTopPlayersByStat(String statColumn, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            Map<UUID, Double> result = new LinkedHashMap<>();

            // Whitelist to avoid SQL injection
            Set<String> allowed = Set.of(
                    "damage_dealt",
                    "damage_taken",
                    "damage_prevented",
                    "kills",
                    "deaths",
                    "points_captured",
                    "games_won",
                    "games_played"
            );

            if (!allowed.contains(statColumn)) {
                throw new IllegalArgumentException("Invalid stat column: " + statColumn);
            }

            String sql = """
                SELECT id, %s
                FROM players
                ORDER BY %s DESC
                LIMIT ?
                """.formatted(statColumn, statColumn);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, limit);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("id"));
                        double value = rs.getInt(statColumn);
                        result.put(id, value);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return result;
        });
    }


//    public CompletableFuture<List<UUID>> getTopPlayersByStat(String statColumn, int limit) {
//        return CompletableFuture.supplyAsync(() -> {
//            List<UUID> result = new ArrayList<>();
//
//            // IMPORTANT: whitelist columns to avoid SQL injection
//            if (!Set.of(
//                    "damage_dealt",
//                    "damage_taken",
//                    "damage_prevented",
//                    "kills",
//                    "deaths",
//                    "points_captured",
//                    "games_won",
//                    "games_played"
//            ).contains(statColumn)) {
//                throw new IllegalArgumentException("Invalid stat column: " + statColumn);
//            }
//
//            String sql = """
//            SELECT id
//            FROM players
//            ORDER BY %s DESC
//            LIMIT ?
//        """.formatted(statColumn);
//
//            try (PreparedStatement ps = connection.prepareStatement(sql)) {
//                ps.setInt(1, limit);
//
//                try (ResultSet rs = ps.executeQuery()) {
//                    while (rs.next()) {
//                        result.add(UUID.fromString(rs.getString("id")));
//                    }
//                }
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//
//            return result;
//        });
//    }
}

