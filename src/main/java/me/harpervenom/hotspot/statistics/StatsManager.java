package me.harpervenom.hotspot.statistics;

import me.harpervenom.hotspot.database.Database;
import me.harpervenom.hotspot.game.profile.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StatsManager {

    private final Database db;
    private final HashMap<UUID, Stats> allStats = new HashMap<>();

    public StatsManager(Database db) {
        this.db = db;
    }

    public void loadStats(Player player) {
        UUID id = player.getUniqueId();
        db.players.getOrCreateStats(id).thenAccept(stats -> {
            this.allStats.put(id, stats);
        });
    }

    public Stats getStats(Player player) {
        UUID id = player.getUniqueId();
        Stats stats = this.allStats.get(id);
        if (stats == null) {
            loadStats(player);
        }
        return stats;
    }

    public void updateProfiles(List<GameProfile> profiles) {
        for (GameProfile profile : profiles) {
            Player player = profile.getPlayer();
            Stats stats = allStats.get(player.getUniqueId());
            stats.addGameStats(profile.getStats());
            db.players.updateStats(player.getUniqueId(), stats);
        }
    }
}
