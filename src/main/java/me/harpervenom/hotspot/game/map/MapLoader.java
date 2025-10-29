package me.harpervenom.hotspot.game.map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class MapLoader {

    private final WorldEditManager worldEditManager;

    private final List<MapData> maps = new ArrayList<>();
    private final File mapsFolder = new File(plugin.getDataFolder(), "maps");

    public MapLoader() {
        worldEditManager = new WorldEditManager();
    }

    public void loadMaps() {
        if (!mapsFolder.exists() || !mapsFolder.isDirectory()) {
            plugin.getLogger().info("Maps folder not found or is not a directory. No maps to load.");
            return;
        }

        File[] files = mapsFolder.listFiles();
        if (files == null) {
            plugin.getLogger().info("Failed to retrieve files in the maps folder.");
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        // Iterate over each directory in the maps folder
        for (File folder : files) {
            if (folder.isDirectory()) {
                MapData mapData = loadMapData(folder);
                if (mapData == null) return;
                worldEditManager.loadSchematic(folder, mapData);
                maps.add(mapData);
                plugin.getLogger().info("Map " + mapData.getName() + " loaded.");
            }
        }
    }

    private MapData loadMapData(File folder) {
        String name = folder.getName();
        File configFile = new File(folder, "config.yml");
        if (!configFile.exists()) {
            plugin.getLogger().info("Map config file not found for map: " + name);
            return null;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        String displayName = config.getString("display_name");
        Material material = Material.valueOf(config.getString("material"));
        int maxPlayersPerTeam = config.getInt("max_players_per_team");
        int deathProtection = config.contains("death_protection") ? config.getInt("death_protection") : 20;
        String author = config.getString("author");

        List<Loc> spawns = new ArrayList<>();
        List<Loc> traders = new ArrayList<>();
        int numberOfTeams = 0;

        ConfigurationSection teamsSection = config.getConfigurationSection("teams");
        if (teamsSection != null) {
            for (String teamKey : teamsSection.getKeys(false)) {
                ConfigurationSection teamSection = teamsSection.getConfigurationSection(teamKey);
                if (teamSection != null) {
                    spawns.add(getLocFromField(teamSection, "spawn"));
                    traders.add(getLocFromField(teamSection, "trader"));
                    numberOfTeams++;
                }
            }
        }

        int maxPlayers = numberOfTeams * maxPlayersPerTeam;

        List<Loc> monuments = new ArrayList<>();
        ConfigurationSection monumentsSection = config.getConfigurationSection("monuments");
        if (monumentsSection != null) {
            for (String key : monumentsSection.getKeys(false)) {
                monuments.add(getLocFromSection(monumentsSection.getConfigurationSection(key)));
            }
        }

        List<Loc> vaults = new ArrayList<>();
        ConfigurationSection vaultsSection = config.getConfigurationSection("vaults");
        if (vaultsSection != null) {
            for (String key : vaultsSection.getKeys(false)) {
                vaults.add(getLocFromSection(vaultsSection.getConfigurationSection(key)));
            }
        }

        return new MapData(
                name,
                displayName,
                material,
                maxPlayersPerTeam,
                deathProtection,
                author,
                maxPlayers,
                numberOfTeams,
                spawns,
                traders,
                monuments,
                vaults,
                folder
        );
    }

    public CompletableFuture<Void> pasteMap(MapData mapData, World world) {
        return worldEditManager.pasteSchematicAsync(mapData, world);
    }

    public CompletableFuture<Void> eraseMap(World world) {
        return worldEditManager.clearWorldAsync(world);
    }

    public List<MapData> getMaps() {
        return maps;
    }

    private static Loc getLocFromSection(ConfigurationSection section) {
        if (section == null) return null;

        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");
        String facing = section.getString("facing");

        return new Loc(x, y, z, facing);
    }

    private static Loc getLocFromField(ConfigurationSection section, String path) {
        if (section == null) return null;

        int x = section.getInt(path + ".x");
        int y = section.getInt(path + ".y");
        int z = section.getInt(path + ".z");
        String facing = section.getString(path + ".facing");

        return new Loc(x, y, z, facing);
    }
}
