package me.harpervenom.hotspot.game.map;

import me.harpervenom.hotspot.game.Game;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

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

        MapData mapData = game.getSettings().getMapData();

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

    private final Deque<MapData> mapBag = new ArrayDeque<>();

    public MapData pickRandomMapData() {
        List<MapData> maps = mapLoader.getMapsData();
        if (maps == null || maps.isEmpty()) {
            throw new IllegalStateException("No maps available");
        }

        // Refill & reshuffle when empty or map list changed
        if (mapBag.isEmpty() || mapBag.size() != maps.size()) {
            refillBag(maps);
        }

        return mapBag.pollFirst();
    }

    private void refillBag(List<MapData> maps) {
        List<MapData> shuffled = new ArrayList<>(maps);
        Collections.shuffle(shuffled, ThreadLocalRandom.current());

        mapBag.clear();
        mapBag.addAll(shuffled);
    }

    public void close() {
        worldManager.deleteWorlds();
    }
    public List<MapData> getMapsData() {
        return mapLoader.getMapsData();
    }
}
