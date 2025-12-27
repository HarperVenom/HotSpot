package me.harpervenom.hotspot.game.vault.loot;

import me.harpervenom.hotspot.game.profile.GameProfile;

import java.util.*;

public class LootPool {

    public static final Random random = new Random();

    private final Map<LootCategory, Double> categoryWeights = new HashMap<>();

    public void addCategory(LootCategory category, double weight) {
        categoryWeights.put(category, weight);
    }

    // weights are static
    public LootCategory pickRandomCategory(GameProfile profile) {
        Map<LootCategory, Double> adjustedWeights = new HashMap<>();

        for (var entry : categoryWeights.entrySet()) {
            LootCategory category = entry.getKey();
            double baseCategoryWeight = entry.getValue();

            double adjustedEntrySum = category.getEntries().stream()
                    .mapToDouble(e -> e.getAdjustedWeight(profile))
                    .sum();

            if (adjustedEntrySum == 0) {
                adjustedWeights.put(category, 0.0);
            } else {
                adjustedWeights.put(category, baseCategoryWeight);
            }
        }

        double totalWeight = adjustedWeights.values().stream().mapToDouble(w -> w).sum();
        double randomValue = random.nextDouble();
        double cumulative = 0.0;
        for (var category : adjustedWeights.entrySet()) {

            cumulative += category.getValue() / totalWeight;
            if (randomValue <= cumulative) {
                return category.getKey();
            }
        }
        return null;
    }

    public LootEntry pickRandomEntry(LootCategory category, GameProfile profile) {
        return category.pickRandomEntry(profile, random);
    }
}

