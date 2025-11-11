package me.harpervenom.hotspot.game.vault.loot;

import me.harpervenom.hotspot.game.profile.GameProfile;

import java.util.List;
import java.util.Random;

import static me.harpervenom.hotspot.game.vault.loot.Loot.*;

public enum LootCategory {
    POTIONS(potions),
    EQUIPMENT(equipment),
    CHESTPLATES(chestPlates),
    ARMOR_ENCHANTS(armorEnchants),

    BOOTS_ENCHANTS(bootsEnchants),

    SWORD_ENCHANTS(swordEnchants),
    AXE_ENCHANT(axeEnchants),

    TRIDENT(trident),
    TRIDENT_ENCHANTS(tridentEnchants),

    TRIDENT_RIPTIDE(tridentRiptide),
    TRIDENT_RIPTIDE_ENCHANTS(tridentRiptideEnchants),

    MACE(mace),
    MACE_ENCHANTS(maceEnchants),

    BOW_ENCHANTS(bowEnchants),
    CROSSBOW_ENCHANTS(crossBowEnchants),

    ARROWS(arrows)
    ;

    private final List<LootEntry> entries;

    LootCategory(List<LootEntry> entries) {
        this.entries = entries;
    }

    public List<LootEntry> getEntries() {
        return entries;
    }

    public LootEntry pickRandomEntry(GameProfile profile, Random random) {
        if (entries.size() == 1) {
            return entries.getFirst();
        }

        double totalWeight = entries.stream().mapToDouble(entry -> entry.getAdjustedWeight(profile)).sum();

        // Normalize each item's chance to fit in [0, 1] range
        double randomValue = random.nextDouble();
        double cumulativeChance = 0.0;

        for (LootEntry entry : entries) {
            cumulativeChance += entry.getAdjustedWeight(profile) / totalWeight;  // Normalize the chance
            if (randomValue <= cumulativeChance) {
                return entry;
            }
        }
        return null;
    }
}
