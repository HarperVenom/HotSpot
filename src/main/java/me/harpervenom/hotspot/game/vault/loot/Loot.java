package me.harpervenom.hotspot.game.vault.loot;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static me.harpervenom.hotspot.game.vault.loot.CustomItems.*;

public class Loot {

    public static final List<LootEntry> equipment, chestPlates, arrows, potions, trident, tridentRiptide, mace,
            swordEnchants, axeEnchants, bowEnchants, tridentEnchants, tridentRiptideEnchants,
            crossBowEnchants, maceEnchants, armorEnchants, bootsEnchants;
    public static final LootEntry rocketItem;

    static {
        CustomItems.createCustomItems();

        potions = List.of(
                LootEntry.makeItemEntry(strengthPotion, 1),
                LootEntry.makeItemEntry(speedPotion, 1),
                LootEntry.makeItemEntry(resistancePotion, 1),
                LootEntry.makeItemEntry(jumpPotion, 1),
                LootEntry.makeItemEntry(healingPotion, 1),
                LootEntry.makeItemEntry(hastePotion, 1),
                LootEntry.makeItemEntry(fireResistancePotion, 0.4),

                LootEntry.makeItemEntry(slownessPotion, 1),
                LootEntry.makeItemEntry(weaknessPotion, 1),
                LootEntry.makeItemEntry(fatiguePotion, 1),
                LootEntry.makeItemEntry(poisonPotion, 1),
                LootEntry.makeItemEntry(hungerPotion, 1),

                LootEntry.makeItemEntry(invisibilityPotion, 0.1),
                LootEntry.makeItemEntry(levitationPotion, 1)
        );

        armorEnchants = List.of(
                LootEntry.makeBookEntry(Enchantment.BLAST_PROTECTION, 4, 1),
                LootEntry.makeBookEntry(Enchantment.FIRE_PROTECTION, 4, 1),
                LootEntry.makeBookEntry(Enchantment.PROJECTILE_PROTECTION, 4, 1),
                LootEntry.makeBookEntry(Enchantment.PROTECTION, 4, 2),
                LootEntry.makeBookEntry(Enchantment.THORNS, 3, 1)
        );


        bootsEnchants = List.of(
                LootEntry.makeBookEntry(Enchantment.DEPTH_STRIDER, 3, 0.1),
                LootEntry.makeBookEntry(Enchantment.FEATHER_FALLING, 4, 1),
                LootEntry.makeBookEntry(Enchantment.FROST_WALKER, 1, 0.1)
        );

        rocketItem = LootEntry.makeItemEntry(rocket, 1, 2, 0.3);

        equipment = List.of(
                LootEntry.makeItemEntry(Material.SHIELD, 1, 1, 0.05),

                LootEntry.makeItemEntry(mudBomb, 1.2),
                LootEntry.makeItemEntry(pillarRelic, 1),
                LootEntry.makeItemEntry(reflectionRelic, 0.8),
                LootEntry.makeItemEntry(vacuumBomb, 0.8),

                LootEntry.makeItemEntry(horseEgg, 1, 1, 0.02),
                LootEntry.makeItemEntry(camelEgg, 1, 1, 0.01),

                LootEntry.makeItemEntry(tnt, 1, 1, 0.4),

                LootEntry.makeItemEntry(Material.WIND_CHARGE, 2, 4, 0.8),
                LootEntry.makeItemEntry(Material.ENDER_PEARL, 1, 1, 0.3),
                LootEntry.makeItemEntry(Material.COAL_BLOCK, 2, 4, 0.5),
                LootEntry.makeItemEntry(Material.GOLDEN_APPLE, 1, 1, 0.5),

                LootEntry.makeItemEntry(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 0.02),
                LootEntry.makeItemEntry(Material.TOTEM_OF_UNDYING, 1, 1, 0.05)
        );

        chestPlates = List.of(
                LootEntry.makeItemEntry(survivorJacket, 1),
                LootEntry.makeItemEntry(sunPlate, 1),
                LootEntry.makeItemEntry(ironPlate, 1),

                LootEntry.makeItemEntry(diamondPlate, 0.6),

                LootEntry.makeItemEntry(chainPlate, 0.4),
                LootEntry.makeItemEntry(explosionPlate, 110.4),

                LootEntry.makeItemEntry(tankPlate, 0.1),

                LootEntry.makeItemEntry(Material.ELYTRA, 0.05)
        );

        swordEnchants = List.of(
                LootEntry.makeBookEntry(Enchantment.SHARPNESS, 1, 2),
                LootEntry.makeBookEntry(Enchantment.FIRE_ASPECT, 1, 1),
                LootEntry.makeBookEntry(Enchantment.KNOCKBACK, 1, 1)
//                new EnchantedBookWithChance(Enchantment.SWEEPING_EDGE, 1, 1)
        );

        axeEnchants = List.of(
                LootEntry.makeBookEntry(Enchantment.SHARPNESS, 1, 1));

        bowEnchants = List.of(
                LootEntry.makeBookEntry(Enchantment.POWER, 1, 2),
                LootEntry.makeBookEntry(Enchantment.FLAME, 1, 1),
                LootEntry.makeBookEntry(Enchantment.PUNCH, 1, 1),
                LootEntry.makeBookEntry(Enchantment.INFINITY, 1, 0.02)
        );

        crossBowEnchants = List.of(
                LootEntry.makeBookEntry(Enchantment.MULTISHOT, 1, 1),
                LootEntry.makeBookEntry(Enchantment.PIERCING, 1, 1),
                LootEntry.makeBookEntry(Enchantment.QUICK_CHARGE, 1, 1)
        );

        tridentEnchants = List.of(
                LootEntry.makeBookEntry(Enchantment.LOYALTY, 1, 1),
                LootEntry.makeBookEntry(Enchantment.CHANNELING, 1, 1)
        );
        tridentRiptideEnchants = List.of(
                LootEntry.makeBookEntry(Enchantment.RIPTIDE, 1, 1)
        );

        trident = List.of(
                LootEntry.makeItemEntry(thunderRelic, 1, 1, 1)
        );
        tridentRiptide = List.of(
                LootEntry.makeItemEntry(fallingPotion, 1, 1, 1)
        );

        maceEnchants = List.of(
                LootEntry.makeBookEntry(Enchantment.WIND_BURST, 1, 1),
                LootEntry.makeBookEntry(Enchantment.BREACH, 1, 1),
                LootEntry.makeBookEntry(Enchantment.DENSITY, 1, 1)
        );

        mace = List.of(
                LootEntry.makeItemEntry(jump5Potion, 0.1),
                LootEntry.makeItemEntry(Material.WIND_CHARGE, 4, 6, 1)
        );

        int min = 2;
        int max = 5;

        arrows = List.of(
                LootEntry.makeItemEntry(new ItemStack(Material.ARROW), 5, 10, 10),
                LootEntry.makeItemEntry(new ItemStack(Material.SPECTRAL_ARROW), 1, min, max),
                LootEntry.makeItemEntry(slownessArrow, min, max, 1),
                LootEntry.makeItemEntry(weaknessArrow, min, max, 1),
                LootEntry.makeItemEntry(poisonArrow, min, max, 1),
                LootEntry.makeItemEntry(levitationArrow, min, max, 1),
                LootEntry.makeItemEntry(darknessArrow, min, max, 1),
                LootEntry.makeItemEntry(fatigueArrow, min, max, 1),
                LootEntry.makeItemEntry(hungerArrow, min, max, 1),
                LootEntry.makeItemEntry(witherArrow, min, max, 0.5)
        );
    }
}
