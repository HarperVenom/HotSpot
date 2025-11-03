package me.harpervenom.hotspot.game.map;

import me.harpervenom.hotspot.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class MapManager {

    private final WorldManager worldManager;
    private final MapLoader mapLoader;
    private final List<GameMap> activeMaps = new ArrayList<>();

    public MapManager() {
        worldManager = new WorldManager();
        mapLoader = new MapLoader();

        worldManager.createWorlds();

        mapLoader.loadMaps();
    }

    public CompletableFuture<GameMap> createMap(Game game) {
        CompletableFuture<GameMap> futureMap = new CompletableFuture<>();
        World world = worldManager.pickWorld();
        if (world == null) {
            plugin.getLogger().info("Failed to create a map. No worlds available");
            return null;
        }

        MapData mapData = mapLoader.getMaps().getFirst();

        GameMap map = new GameMap(mapData, world);
        activeMaps.add(map);

        mapLoader.pasteMap(mapData, world).thenRun(() -> {
            futureMap.complete(map);
            plugin.getLogger().info("Map created in world (" + world.getName() + ")");
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to paste map in world (" + world.getName() + "): " + ex.getMessage());
            return null;
        });

        return futureMap;
    }

    public void removeMap(GameMap map) {
        World world = map.getWorld();

        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof Player)) {
                entity.remove();
            }
        }

        mapLoader.eraseMap(world).thenRun(() -> {
            activeMaps.remove(map);
            plugin.getLogger().info("Map erased in world (" + world.getName() + ")");
            worldManager.freeWorld(world);
        }).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to erase map in world (" + world.getName() + "): " + ex.getMessage());
            return null;
        });
    }

    public void close() {
        worldManager.deleteWorlds();
    }
}
