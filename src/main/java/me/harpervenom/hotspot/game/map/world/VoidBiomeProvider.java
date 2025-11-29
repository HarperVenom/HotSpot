package me.harpervenom.hotspot.game.map.world;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VoidBiomeProvider extends BiomeProvider {
    private final String worldName;
    private final List<Biome> biomes = List.of(
            Biome.GROVE, Biome.JUNGLE, Biome.PLAINS, Biome.SWAMP, Biome.TAIGA
    );

    private static final Set<Biome> usedBiomes = new HashSet<>();
    private final Biome chosenBiome;

    public VoidBiomeProvider(String worldName) {
        this.worldName = worldName;

        List<Biome> available = new ArrayList<>(biomes);
        available.removeAll(usedBiomes); // remove already assigned

        if (available.isEmpty()) {
            usedBiomes.clear(); // reset when all taken
            available = new ArrayList<>(biomes);
        }

        // pick random from available
        Random random = new Random();
        this.chosenBiome = available.get(random.nextInt(available.size()));

        usedBiomes.add(chosenBiome);
    }

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        return chosenBiome;
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return List.of(chosenBiome);
    }
}
