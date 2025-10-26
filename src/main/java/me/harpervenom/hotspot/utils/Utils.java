package me.harpervenom.hotspot.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public class Utils {

    private final static NamespacedKey key = new NamespacedKey(plugin, "custom_id");

    public static Location getLocationFromConfig(String path, boolean isBottom) {
        FileConfiguration config = plugin.getConfig(); // from your plugin
        if (!config.contains(path)) {
            plugin.getLogger().warning("Config section '" + path + "' not found!");
            return null;
        }

        String worldName = config.getString(path + ".world", Bukkit.getWorlds().getFirst().getName());
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' not found, using lobby world.");
            world = Bukkit.getWorld("lobby");
        }

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");

        if (isBottom) {
            x += 0.5;
            y += 1;
            z += 0.5;
        }

        float yaw = (float) config.getDouble(path + ".yaw", 0.0);
        float pitch = (float) config.getDouble(path + ".pitch", 0.0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Location getLocationFromConfig(String path) {
        return getLocationFromConfig(path, false);
    }

    public static ItemStack createItemStack(Material material, Component title, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        NamespacedKey key = new NamespacedKey(plugin, "dummy_modifier");
        AttributeModifier dummyModifier = new AttributeModifier(
                key, 0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND
        );
        meta.addAttributeModifier(Attribute.MAX_HEALTH, dummyModifier);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
        meta.displayName(title);
        if (lore != null) meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void addLoreLine(ItemStack item, Component line) {
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(line);
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public static Component text(String text, NamedTextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }

    public static Component text(String text, TextColor color) {
        return Component.text(text, color).decoration(TextDecoration.ITALIC, false);
    }

    public static Component text(String text) {
        return text(text, WHITE);
    }

    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static ItemStack setId(ItemStack item, String id) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(key, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    public static String getId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        return data.get(key, PersistentDataType.STRING);
    }

    public static boolean hasId(ItemStack item, String id) {
        String itemId = getId(item);
        return itemId != null && itemId.equals(id);
    }

    public static void addItemToInventory(Player p, ItemStack item) {
        addItemToInventory(p, item, false);
    }

    public static void addItemToInventory(Player p, ItemStack item, boolean isSilent) {
        if (item == null) return;
        HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(item);

        // If inventory is full, drop the item at the player's location
        if (!leftovers.isEmpty()) {
            p.getWorld().dropItemNaturally(p.getLocation(), item);
        }

        if (!isSilent) {
            p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 2);
        }
    }

    public static void consumeOneFromHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            return; // nothing to consume
        }

        int amount = item.getAmount();
        if (amount <= 1) {
            // Remove item completely if last one
            player.getInventory().setItemInMainHand(null);
        } else {
            // Just reduce stack size
            item.setAmount(amount - 1);
            player.getInventory().setItemInMainHand(item);
        }
    }

    public static boolean consume(Player player, String id, int quantity) {
        PlayerInventory inv = player.getInventory();
        int totalFound = 0;

        // First, count total amount of the item in the inventory
        for (ItemStack stack : inv.getContents()) {
            if (stack == null) continue;
            if (hasId(stack, id)) {
                totalFound += stack.getAmount();
            }
        }

        // Not enough
        if (totalFound < quantity) return false;

        // Remove items
        int remaining = quantity;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && hasId(stack, id)) {
                int take = Math.min(stack.getAmount(), remaining);
                stack.setAmount(stack.getAmount() - take);
                remaining -= take;
                if (stack.getAmount() <= 0) inv.setItem(i, null);
                if (remaining <= 0) break;
            }
        }

        return true;
    }
}
