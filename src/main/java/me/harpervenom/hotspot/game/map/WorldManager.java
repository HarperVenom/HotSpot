package me.harpervenom.hotspot.game.map;

import me.harpervenom.hotspot.game.map.world.VoidChunkGenerator;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class WorldManager {

    private final Set<World> busyWorlds = new HashSet<>();
    private final World[] worlds = new World[plugin.getConfig().getInt("game_worlds", 3)];

    public void createWorlds() {
        for (int i = 0; i < worlds.length; i++) {
            String worldName = "gameWorld " + i;

            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);

            try {
                if (worldFolder.exists()) {
                    Bukkit.unloadWorld(worldName, false);
                    deleteWorld(worldFolder);
                }
            } catch (IOException e) {
                plugin.getLogger().info("Failed to delete old world data: " + e.getMessage());
                e.printStackTrace();
            }

            WorldCreator creator = new WorldCreator(worldName);
            creator.generator(new VoidChunkGenerator(worldName));
            creator.type(WorldType.FLAT);
            World world = Bukkit.createWorld(creator);
            if (world == null) return;
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.DISABLE_RAIDS, true);
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
            world.setGameRule(GameRule.DO_INSOMNIA, false);
            world.setGameRule(GameRule.WATER_SOURCE_CONVERSION, false);
            world.setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
            world.setDifficulty(Difficulty.HARD);
            worlds[i] = world;
        }
    }

    public void deleteWorlds() {
        for (World world : worlds) {
            File worldFolder = new File(Bukkit.getWorldContainer(), world.getName());
            try {
                if (worldFolder.exists()) {
                    Bukkit.unloadWorld(world.getName(), false);
                    deleteWorld(worldFolder);
                }
            } catch (IOException e) {
                plugin.getLogger().info("Failed to copy world data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public World pickWorld() {
        for (World world : worlds) {
            if (!busyWorlds.contains(world)) {
                busyWorlds.add(world); // mark as busy
                world.setTime(1000);
                world.setWeatherDuration(0);
                plugin.getLogger().info("World (" + world.getName() + ") is now in use");
                return world;
            }
        }
        return null;
    }

    public World pickWorld(Player p) {
        if (worlds.length != 1 && !p.isOp() && busyWorlds.size() >= worlds.length - 1) return null;
        return pickWorld();
    }

    public void freeWorld(World world) {
        if (busyWorlds.contains(world)) {
            busyWorlds.remove(world);
            plugin.getLogger().info("World (" + world.getName() + ") is now free for use");
        }
    }

    // Method to delete a world folder
    public void deleteWorld(File path) throws IOException {
        FileUtils.deleteDirectory(path);
    }
}
