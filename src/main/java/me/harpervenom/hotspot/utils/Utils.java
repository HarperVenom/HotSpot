package me.harpervenom.hotspot.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

public class Utils {

    private final static NamespacedKey key = new NamespacedKey(plugin, "custom_id");

    public static Location getLocationFromConfig(String path) {
        return getLocationFromConfig(path, false);
    }

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

    public static void setCustomName(ItemStack item, Component name) {
        if (item == null || name == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Set the custom name
            meta.displayName(name);
            item.setItemMeta(meta);
        }
    }

    public static Component getCustomName(ItemStack item) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            return meta.displayName();
        }

        return null;
    }

    public static void setCustomLore(ItemStack item, List<Component> loreLines) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Clear the lore if loreLines is null
            // Set an empty list for lore
            meta.lore(Objects.requireNonNullElseGet(loreLines, ArrayList::new)); // Set the provided lore
            item.setItemMeta(meta);
        }
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

    public static void addItemKeyword(ItemStack item, String keyword) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "custom_keywords");
        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Retrieve existing keywords
        Set<String> keywords = new HashSet<>();
        String existing = container.get(key, PersistentDataType.STRING);

        if (existing != null && !existing.isEmpty()) {
            keywords.addAll(Arrays.asList(existing.split(",")));
        }

        // Add new keyword if not already present
        if (!keywords.contains(keyword)) {
            keywords.add(keyword);
            container.set(key, PersistentDataType.STRING, String.join(",", keywords));
        }

        item.setItemMeta(meta);
    }

    public static boolean hasItemKeyword(ItemStack item, String keyword) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "custom_keywords");

        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return false;

        String storedKeywords = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (storedKeywords == null || storedKeywords.isEmpty()) return false;

        return Arrays.asList(storedKeywords.split(",")).contains(keyword);
    }

    public static void setItemId(ItemStack item, String id) {
        if (item == null || id == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "custom_id");

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
    }

    public static String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "custom_id");

        return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    public static boolean hasItemId(ItemStack item, String id) {
        if (item == null || !item.hasItemMeta()) return false;

        String itemId = getItemId(item);
        if (itemId == null) return false;

        return itemId.equals(id);
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

//    public static void consumeOneFromHand(Player player) {
//        ItemStack item = player.getInventory().getItemInMainHand();
//
//        if (item.getType().isAir()) {
//            return; // nothing to consume
//        }
//
//        int amount = item.getAmount();
//        if (amount <= 1) {
//            // Remove item completely if last one
//            player.getInventory().setItemInMainHand(null);
//        } else {
//            // Just reduce stack size
//            item.setAmount(amount - 1);
//            player.getInventory().setItemInMainHand(item);
//        }
//    }
//
//    public static boolean consume(Player player, String id, int quantity) {
//        PlayerInventory inv = player.getInventory();
//        int totalFound = 0;
//
//        // First, count total amount of the item in the inventory
//        for (ItemStack stack : inv.getContents()) {
//            if (stack == null) continue;
//            if (hasId(stack, id)) {
//                totalFound += stack.getAmount();
//            }
//        }
//
//        // Not enough
//        if (totalFound < quantity) return false;
//
//        // Remove items
//        int remaining = quantity;
//        for (int i = 0; i < inv.getSize(); i++) {
//            ItemStack stack = inv.getItem(i);
//            if (stack != null && hasId(stack, id)) {
//                int take = Math.min(stack.getAmount(), remaining);
//                stack.setAmount(stack.getAmount() - take);
//                remaining -= take;
//                if (stack.getAmount() <= 0) inv.setItem(i, null);
//                if (remaining <= 0) break;
//            }
//        }
//
//        return true;
//    }

    public static void sendMessage(Component message, List<Player> players) {
        for (Player player : players) {
            player.sendMessage(message);
        }
    }

    public static void sendActionBarMessage(Component message, List<Player> players) {
        for (Player player : players) {
            player.sendActionBar(message);
        }
    }

    public static void playSound(Sound sound, float volume, float pitch, List<Player> players) {
        for (Player player : players) {
            if (player != null) {
                player.playSound(player, sound, volume, pitch);
            }
        }
    }

    public static void sendTitle(Component title, Component subtitle, List<Player> players) {
        for (Player player : players) {
            player.showTitle(Title.title(title, subtitle));
        }
    }

    public static Color toBukkitColor(NamedTextColor namedTextColor) {
        if (namedTextColor == null) return null;

        if (namedTextColor == NamedTextColor.RED) {
            return Color.fromRGB(255, 0, 0); // Red
        } else if (namedTextColor == NamedTextColor.BLUE) {
            return Color.fromRGB(0, 0, 255); // Blue
        } else if (namedTextColor == NamedTextColor.GREEN) {
            return Color.fromRGB(0, 255, 0); // Green
        } else if (namedTextColor == NamedTextColor.YELLOW) {
            return Color.fromRGB(255, 255, 0); // Yellow
        }
        return null; // Return null if not one of the specified colors
    }

    public static Component getScoreboardLine(List<Component> lines) {
        Component configLine = text(
                plugin.getConfig().getString("scoreboard_line"),
                TextColor.color(179, 161, 89)
        );

        int longest = lines.stream()
                .mapToInt(Utils::visibleLength)
                .max()
                .orElse(0);

        int targetLength = longest + 3;
        return padToLength(configLine, targetLength);
    }

    private static int visibleLength(Component component) {
        return PlainTextComponentSerializer.plainText()
                .serialize(component)
                .length();
    }

    private static Component padToLength(Component component, int targetLength) {
        String text = PlainTextComponentSerializer.plainText().serialize(component);
        int diff = targetLength - text.length();
        if (diff <= 0) return component;

        int left = diff / 2;
        int right = diff - left;

        return Component.text("-".repeat(left))
                .append(component)
                .append(Component.text("-".repeat(right)));
    }
}
