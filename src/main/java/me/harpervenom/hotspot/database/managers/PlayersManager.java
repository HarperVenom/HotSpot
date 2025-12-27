package me.harpervenom.hotspot.database.managers;

import me.harpervenom.hotspot.statistics.Stats;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class PlayersManager implements Listener {

    private final Connection connection;

    public PlayersManager(Connection connection) throws SQLException {
        this.connection = connection;

        try (Statement statement = connection.createStatement()) {
            String createTable = """
            CREATE TABLE IF NOT EXISTS players (
                id TEXT PRIMARY KEY,
                nickname TEXT DEFAULT '',
                exp INTEGER NOT NULL DEFAULT 0,
                rank DOUBLE NOT NULL DEFAULT 0.0,
                games_played INTEGER NOT NULL DEFAULT 0,
                games_won INTEGER NOT NULL DEFAULT 0,
                kills INTEGER NOT NULL DEFAULT 0,
                deaths INTEGER NOT NULL DEFAULT 0,
                damage_dealt DOUBLE NOT NULL DEFAULT 0.0,
                damage_received DOUBLE NOT NULL DEFAULT 0.0,
                damage_prevented DOUBLE NOT NULL DEFAULT 0.0,
                points_captured INTEGER NOT NULL DEFAULT 0,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;
            statement.executeUpdate(createTable);
        }

        addNicknameColumnIfMissing();

        migrateMissingNicknames();
    }

    private void addNicknameColumnIfMissing() throws SQLException {
        // Step 1: Check if 'nickname' column already exists
        boolean columnExists = false;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(players)")) {

            while (rs.next()) {
                String colName = rs.getString("name");
                if ("nickname".equalsIgnoreCase(colName)) {
                    columnExists = true;
                    break;
                }
            }
        }

        // Step 2: Add the column only if missing
        if (!columnExists) {
            try (Statement stmt = connection.createStatement()) {
                // TEXT is flexible; you can change to VARCHAR(64) if you want length limit
                // NULL allowed (no NOT NULL), default NULL
                String alter = "ALTER TABLE players ADD COLUMN nickname TEXT";
                stmt.executeUpdate(alter);

                // Optional: log success (good for debugging)
                System.out.println("[HotSpot] Added 'nickname' column to players table.");
            }
        } else {
            // Optional: log that column already exists
            // System.out.println("[HotSpot] 'nickname' column already exists - skipping.");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String currentName = player.getName();

        CompletableFuture.runAsync(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE players SET nickname = ? WHERE id = ? AND (nickname IS NULL OR nickname = '' OR nickname != ?)")) {
                ps.setString(1, currentName);
                ps.setString(2, uuid.toString());
                ps.setString(3, currentName);  // only update if different or missing
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    plugin.getLogger().fine("Updated nickname for " + player.getName());
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to update nickname for " + uuid);
            }
        });
    }

    public CompletableFuture<Stats> getOrCreateStats(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            String nickname = null;

            OfflinePlayer offline = Bukkit.getOfflinePlayer(id);
            if (offline.hasPlayedBefore()) {
                nickname = offline.getName();
            }

            try {
                // 1. Try to insert (create if not exists)
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO players (id, nickname, exp, rank, games_played, games_won, kills, deaths, " +
                                "damage_dealt, damage_received, damage_prevented, points_captured) " +
                                "VALUES (?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0) " +
                                "ON CONFLICT(id) DO NOTHING")) {

                    insert.setString(1, id.toString());
                    insert.setString(2, nickname);  // null is fine if DEFAULT allows it
                    insert.executeUpdate();
                }

                // 2. Always select the current row (whether newly created or existing)
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT exp, rank, games_played, games_won, kills, deaths, " +
                                "damage_dealt, damage_received, damage_prevented, points_captured " +
                                "FROM players WHERE id = ?")) {

                    select.setString(1, id.toString());
                    try (ResultSet rs = select.executeQuery()) {
                        if (!rs.next()) {
                            // Extremely rare - row disappeared right after insert
                            throw new RuntimeException("Failed to retrieve player stats after creation");
                        }

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
                // Better logging
                plugin.getLogger().severe("Database error for player " + id + ": " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to get/create player stats", e);
            }
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void migrateMissingNicknames() {
        CompletableFuture.runAsync(() -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT id FROM players WHERE nickname IS NULL OR nickname = ''")) {

                int updated = 0;
                while (rs.next()) {
                    String uuidStr = rs.getString("id");
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(uuidStr);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in database: " + uuidStr);
                        continue;
                    }

                    // Get current name (cached if player has joined before)
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
                    String name = offline.hasPlayedBefore() ? offline.getName() : null;

                    if (name != null && !name.isEmpty()) {
                        try (PreparedStatement update = connection.prepareStatement(
                                "UPDATE players SET nickname = ? WHERE id = ?")) {
                            update.setString(1, name);
                            update.setString(2, uuidStr);
                            update.executeUpdate();
                            updated++;
                        }
                    } else {
                        // Optional: set to "Unknown" or leave as NULL
                        // plugin.getLogger().info("No known name for UUID: " + uuidStr);
                    }
                }

                if (updated > 0) {
                    plugin.getLogger().info("Migrated " + updated + " missing nicknames.");
                } else {
                    plugin.getLogger().info("No missing nicknames found to migrate.");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to migrate nicknames: " + e.getMessage());
                e.printStackTrace();
            }
        });
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
}

