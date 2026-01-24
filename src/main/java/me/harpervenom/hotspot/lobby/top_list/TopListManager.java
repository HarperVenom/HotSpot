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
                list.generate();
            });
        });
    }

    private void updateList(String statName) {
        TopList list = topLists.remove(statName);
        if (list == null) return;
        list.remove();
        loadTopList(statName);
    }

    private Map<Component, Component> buildLines(Map<UUID, Double> map) {
        Map<Component, Component> lines = new LinkedHashMap<>();

        try {
            map.forEach((id, score) -> {
                OfflinePlayer offline = Bukkit.getOfflinePlayer(id);
                String name = offline.getName();

                // Fallback name if never joined / name unknown
                String displayName = (name != null) ? name : id.toString().substring(0, 8) + "...";

                String formatted;
                if (score % 1 == 0) {
                    formatted = String.valueOf(score.intValue());
                } else {
                    formatted = String.valueOf(score);
                }

                lines.put(
                        text(displayName),
                        text(formatted, TextColor.color(176, 224, 230))
                );
            });
        } catch (Exception e) {
            plugin.getLogger().severe("ERROR in buildLines() while processing top list");
            plugin.getLogger().severe("Map size: " + map.size());
            e.printStackTrace();  // This will show the full stack trace in console

            // Optional: return empty map or fallback to prevent chain failure
            return new LinkedHashMap<>();  // or throw if you want to fail loudly
        }

        return lines;
    }


    public void update() {
        for (var key : new ArrayList<>(topLists.keySet())) {
            updateList(key);
        }
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
