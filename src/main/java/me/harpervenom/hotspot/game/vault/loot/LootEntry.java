package me.harpervenom.hotspot.game.vault.loot;

import me.harpervenom.hotspot.game.profile.GameProfile;
import org.apache.logging.log4j.util.Supplier;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static me.harpervenom.hotspot.utils.Utils.getItemId;

public class LootEntry {

    private final String key;
    private final Enchantment enchantment;

    private final double weight;
//    private final double frequencyFactor;
    private final Supplier<ItemStack> itemSupplier;

    public LootEntry(String key, double weight, Supplier<ItemStack> itemSupplier) {
        this.key = key;
        this.enchantment = null;
        this.weight = weight;
//        this.frequencyFactor = frequencyFactor;
        this.itemSupplier = itemSupplier;
    }

    public LootEntry(String key, Enchantment enchantment, double weight, Supplier<ItemStack> itemSupplier) {
        this.key = key;
        this.enchantment = enchantment;
        this.weight = weight;
//        this.frequencyFactor = frequencyFactor;
        this.itemSupplier = itemSupplier;
    }

    public double getWeight() {
        return weight;
    }

    public double getAdjustedWeight(GameProfile profile) {
        if (enchantment != null) {
            List<ItemStack> items = Arrays.stream(profile.getPlayer().getInventory().getContents()).toList();
            if (!hasItemForEnchant(enchantment, items)) {
                return 0;
            }
        }

        return weight;

//        long receivedCount = profile.getReceivedCount(key);
//
//        if (receivedCount <= 0) {
//            return weight;
//        }
//
//        // Reduce chance based on frequency factor and count
//        return weight * Math.pow(frequencyFactor, receivedCount);
    }

    public ItemStack create() {
        return itemSupplier.get();
    }

//    public double getFrequencyFactor() {
//        return frequencyFactor;
//    }

    public String getKey() {
        return key;
    }

    private static final double baseFreq = 0.9;

    public static LootEntry makeItemEntry(Material mat, double weight) {
        return makeItemEntry(new ItemStack(mat), 1, 1, weight);
    }
    public static LootEntry makeItemEntry(Material mat, int min, int max, double weight) {
        return makeItemEntry(new ItemStack(mat), min, max, weight);
    }

    public static LootEntry makeItemEntry(ItemStack item, double weight) {
        return makeItemEntry(item, 1, 1, weight);
    }
//    public static LootEntry makeItemEntry(ItemStack item, int min, int max, double weight) {
//        return makeItemEntry(item, min, max, weight);
//    }
    public static LootEntry makeItemEntry(ItemStack item, int min, int max, double weight) {
        String key = getKey(item);
        return new LootEntry(key, weight, () -> {
            Random r = new Random();
            item.setAmount(r.nextInt(max) + min);
            return item;
        });
    }

    public static LootEntry makeBookEntry(Enchantment enchant, int maxLvl, double weight) {
        String key = enchant.key().asMinimalString();
        return new LootEntry(key, enchant, weight, () -> {
            Random r = new Random();
            ItemStack stack = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();

            int level = r.nextInt(maxLvl) + 1;

            meta.addStoredEnchant(enchant, level, true);
            stack.setItemMeta(meta);

            return stack;
        });
    }

    public static String getKey(ItemStack item) {
        String id = getItemId(item);
        if (id != null) {
            return id;
        }
        return item.getType().toString();
    }

    public boolean hasItemForEnchant(Enchantment enchantment, List<ItemStack> items) {
        int totalItems = 0;
        int totalLevels = 0;
        int maxLevel = enchantment.getMaxLevel();

        for (ItemStack item : items) {
            if (item == null) continue;

            if (item.containsEnchantment(enchantment)) {
                totalLevels += item.getEnchantmentLevel(enchantment);
            }

            if (item.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                if (meta != null && meta.hasStoredEnchant(enchantment)) {
                    totalLevels += meta.getStoredEnchantLevel(enchantment);
                }
            }

            if (enchantment.canEnchantItem(item)) {
                totalItems++;
            }
        }

        return totalItems * maxLevel > totalLevels;
    }
}