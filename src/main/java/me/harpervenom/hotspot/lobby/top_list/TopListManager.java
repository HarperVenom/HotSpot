package me.harpervenom.hotspot.lobby.top_list;

import me.harpervenom.hotspot.database.Database;
import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameListener;
import me.harpervenom.hotspot.lobby.LobbyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.text;

public class TopListManager implements GameListener {

    private final Database db;
    private final LobbyManager lobbyManager;

    private final Map<String, TopList> topLists = new HashMap<>();

    public TopListManager(Database db, LobbyManager lobbyManager) {
        this.db = db;
        this.lobbyManager = lobbyManager;
    }

    public void loadLists() {
        String basePath = "top_lists";

        ConfigurationSection section = plugin.getConfig().getConfigurationSection(basePath);
        if (section == null) return;

        for (String statName : section.getKeys(false)) {
            loadTopList(statName);
        }
    }

    private void loadTopList(String statName) {
        String configPath = "top_lists." + statName;
        String title = plugin.getConfig().getString(configPath + ".title");
        Location loc = getLocationFromConfig(configPath);

        db.players.getTopPlayersByStat(statName, 10).thenAccept(map -> {
            Map<Component, Component> lines = buildLines(map);

            Bukkit.getScheduler().runTask(plugin, () -> {
                TopList list = new TopList(loc, lines, title);
                topLists.put(statName, list);
                list.update();
            });
        });
    }

    private void updateList(String statName) {
        TopList list = topLists.get(statName);
        if (list == null) return;

        db.players.getTopPlayersByStat(statName, 10).thenAccept(map -> {
            Map<Component, Component> newLines = buildLines(map);

            Bukkit.getScheduler().runTask(plugin, () -> {
                list.setLines(newLines);
                list.update();
            });
        });
    }

    private Map<Component, Component> buildLines(Map<UUID, Double> map) {
        Map<Component, Component> lines = new LinkedHashMap<>();

        map.forEach((id, score) -> {
            OfflinePlayer p = Bukkit.getOfflinePlayer(id);

            String formatted;
            if (score % 1 == 0) {
                formatted = String.valueOf(score.intValue());
            } else {
                formatted = String.valueOf(score);
            }

            lines.put(
                    text(p.getName()),
                    text(formatted, TextColor.color(176, 224, 230))
            );
        });

        return lines;
    }


    public void update() {
        topLists.keySet().forEach(this::updateList);
    }

    private Location getLocationFromConfig(String path) {
        FileConfiguration config = plugin.getConfig(); // from your plugin
        if (!config.contains(path)) {
            plugin.getLogger().warning("Config section '" + path + "' not found!");
            return null;
        }

        World world = lobbyManager.getLobbyWorld();

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");

        return new Location(world, x, y, z);
    }

    @Override
    public void onGameEnd(Game game) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            update();
        }, 3 * 20);
    }
}
