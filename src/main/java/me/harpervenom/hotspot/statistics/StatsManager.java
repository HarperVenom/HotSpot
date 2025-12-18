package me.harpervenom.hotspot.statistics;

import me.harpervenom.hotspot.database.Database;
import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.game.team.GameTeam;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static me.harpervenom.hotspot.statistics.Stats.*;
import static me.harpervenom.hotspot.utils.Utils.text;

public class StatsManager {

    private final Database db;
    private GameManager gameManager;
    private final HashMap<UUID, Stats> allStats = new HashMap<>();

    public StatsManager(Database db) {
        this.db = db;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void loadStats(UUID id) {
        db.players.getOrCreateStats(id).thenAccept(stats -> {
            allStats.put(id, stats);
            updateTab();
        });
    }

    public Stats getStats(UUID id) {
        Stats stats = this.allStats.get(id);
        if (stats == null) {
            loadStats(id);
        }
        return stats;
    }

    public void updateProfiles(List<GameProfile> profiles) {
        for (GameProfile profile : profiles) {
            Stats stats = allStats.get(profile.getId());
            stats.addGameStats(profile.getStats());
            db.players.updateStats(profile.getId(), stats);
        }
    }

    public void updateTab() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            Stats stats = getStats(player.getUniqueId());
            if (stats == null) return;

            player.playerListName(
                    stats.getLevelIcon()
                            .append(stats.getRankIcon())
                            .append(stats.getSkillIcon())
                            .append(text(" ").append(getName(player))));
        });
    }

    public Component getName(OfflinePlayer offlinePlayer) {
        Component name = text(offlinePlayer.getName());
        if (!(offlinePlayer instanceof Player player)) return name;
        if (gameManager != null) {
            Game game = gameManager.getGame(player);
            if (game != null) {
                GameTeam team = game.getPlayerManager().getTeam(player);
                if (!game.getPlayerManager().isSpectator(player) && team != null) {
                    name = name.color(team.getColor());
                }
            }
        }
        return name;
    }
}
