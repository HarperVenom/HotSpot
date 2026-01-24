package me.harpervenom.hotspot.game.vault.loot;

import me.harpervenom.hotspot.game.profile.EquipmentManager;
import me.harpervenom.hotspot.game.profile.GameProfile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public enum LootClass {
    SWORD(1.2, new ItemStack(Material.WOODEN_SWORD)),
    AXE(1, new ItemStack(Material.WOODEN_AXE)),

    SPEAR(0.8, new ItemStack(Material.WOODEN_SPEAR)),

    TRIDENT(0.5, new ItemStack(Material.TRIDENT)),
    TRIDENT_RIPTIDE(0.5, new ItemStack(Material.TRIDENT)),
    MACE(0.5, new ItemStack(Material.MACE)),

    BOW(0.1, new ItemStack(Material.BOW)),
    CROSSBOW(0.1, new ItemStack(Material.CROSSBOW)),
    ;

    private final double probability;
    private final ItemStack mainItem;

    LootClass(double probability, ItemStack mainItem) {
        this.probability = probability;
        this.mainItem = mainItem;
    }

    public ItemStack getMainItem() {
        return mainItem.clone();
    }

    public ItemStack pickItem(GameProfile profile) {
        EquipmentManager equipment = profile.getEquipmentManager();

        if (!equipment.hasWeapon()) {
            equipment.setHasWeapon(true);
            profile.getLootManager().addItem(getMainItem());
            return getMainItem();
        }

        LootPool pool = new LootPool();

        pool.addCategory(LootCategory.EQUIPMENT, 0.9);
        pool.addCategory(LootCategory.POTIONS, 0.2);
        pool.addCategory(LootCategory.ARMOR_ENCHANTS, 0.05);
        pool.addCategory(LootCategory.BOOTS_ENCHANTS, 0.04);

        if (!equipment.hasChest()) {
            pool.addCategory(LootCategory.CHESTPLATES, 1);
        }

        double baseEnchantsWeight = 0.2;
        // weapon-specific
        switch (this) {
            case SWORD -> pool.addCategory(LootCategory.SWORD_ENCHANTS, baseEnchantsWeight);
            case AXE -> pool.addCategory(LootCategory.AXE_ENCHANT, baseEnchantsWeight);
            case SPEAR -> {
                pool.addCategory(LootCategory.SPEAR, 0.05);
                pool.addCategory(LootCategory.SPEAR_ENCHANTS, baseEnchantsWeight);
            }
            case TRIDENT -> {
                pool.addCategory(LootCategory.TRIDENT, 0.05);
                pool.addCategory(LootCategory.TRIDENT_ENCHANTS, baseEnchantsWeight);
            }
            case TRIDENT_RIPTIDE -> {
                pool.addCategory(LootCategory.TRIDENT, 0.05);
                pool.addCategory(LootCategory.TRIDENT_RIPTIDE, 0.05);
                pool.addCategory(LootCategory.TRIDENT_RIPTIDE_ENCHANTS, baseEnchantsWeight);
            }
            case MACE -> {
                pool.addCategory(LootCategory.MACE, 0.2);
                pool.addCategory(LootCategory.MACE_ENCHANTS, baseEnchantsWeight);
            }
            case BOW -> {
                pool.addCategory(LootCategory.BOW_ENCHANTS, baseEnchantsWeight);
                pool.addCategory(LootCategory.ARROWS, 1);
            }
            case CROSSBOW -> {
                pool.addCategory(LootCategory.CROSSBOW_ENCHANTS, baseEnchantsWeight);
                pool.addCategory(LootCategory.ARROWS, 1);
            }
        }

        LootCategory category = pool.pickRandomCategory(profile);

        if (category == LootCategory.CHESTPLATES) {
            equipment.setHasChest(true);
        }

        LootEntry entry = pool.pickRandomEntry(category, profile);
        profile.getLootManager().addReceived(entry);
        ItemStack itemStack = entry.create();
        profile.getLootManager().addItem(itemStack);
        return itemStack;
    }

    /** Pick a random class based on weighted probabilities */
    public static LootClass getRandomClass(LootClass oldClass) {
        // Single shared RNG
        Random rng = LootPool.random; // or your global RNG location

        double total = 0;
        for (LootClass c : values()) {
            if (c != oldClass) {
                total += c.probability;
            }
        }

        // Safety fallback if all but oldClass are filtered out
        if (total <= 0) {
            return oldClass != null ? oldClass : SWORD;
        }

        double rand = rng.nextDouble() * total;
        double cumulative = 0;

        for (LootClass c : values()) {
            if (c == oldClass) continue; // filter
            cumulative += c.probability;
            if (rand <= cumulative) {
                return c;
            }
        }

        // Extreme fallback (should rarely happen)
        return SWORD;
    }
}

