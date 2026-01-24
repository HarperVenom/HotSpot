package me.harpervenom.hotspot.game.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.*;

public class SmartWeaponListener implements Listener {

    private static final Map<Material, Material> UPGRADE_PATH = Map.ofEntries(
            Map.entry(Material.WOODEN_SWORD, Material.STONE_SWORD),
            Map.entry(Material.STONE_SWORD, Material.IRON_SWORD),
            Map.entry(Material.IRON_SWORD, Material.DIAMOND_SWORD),
            Map.entry(Material.DIAMOND_SWORD, Material.NETHERITE_SWORD),

            Map.entry(Material.WOODEN_AXE, Material.STONE_AXE),
            Map.entry(Material.STONE_AXE, Material.IRON_AXE),
            Map.entry(Material.IRON_AXE, Material.DIAMOND_AXE),
            Map.entry(Material.DIAMOND_AXE, Material.NETHERITE_AXE),

            Map.entry(Material.WOODEN_SPEAR, Material.STONE_SPEAR),
            Map.entry(Material.STONE_SPEAR, Material.COPPER_SPEAR),
            Map.entry(Material.COPPER_SPEAR, Material.IRON_SPEAR),
            Map.entry(Material.IRON_SPEAR, Material.DIAMOND_SPEAR),
            Map.entry(Material.DIAMOND_SPEAR, Material.NETHERITE_SPEAR)
    );

    private static final Map<Material, Integer> DAMAGE_REQUIRED = Map.ofEntries(
            Map.entry(Material.WOODEN_SWORD, 20),
            Map.entry(Material.STONE_SWORD, 40),
            Map.entry(Material.IRON_SWORD, 60),
            Map.entry(Material.DIAMOND_SWORD, 80),

            Map.entry(Material.WOODEN_AXE, 25),
            Map.entry(Material.STONE_AXE, 50),
            Map.entry(Material.IRON_AXE, 75),
            Map.entry(Material.DIAMOND_AXE, 100),

            Map.entry(Material.WOODEN_SPEAR, 15),
            Map.entry(Material.STONE_SPEAR, 30),
            Map.entry(Material.COPPER_SPEAR, 45),
            Map.entry(Material.IRON_SPEAR, 60),
            Map.entry(Material.DIAMOND_SPEAR, 75)
    );

    public static int getUpgradeLevel(Material itemType) {
        int level = 1;
        Material current = itemType;

        while (true) {
            // Try to find a previous material that upgrades to this one
            Material previous = null;
            for (Map.Entry<Material, Material> entry : UPGRADE_PATH.entrySet()) {
                if (entry.getValue() == current) {
                    previous = entry.getKey();
                    break;
                }
            }

            if (previous == null) {
                break;
            }

            level++;
            current = previous;
        }

        return level;
    }

//    public static String getMaterialLevel(Material type) {
//        // Return the appropriate level string based on the material (Wooden, Stone, etc.)
//        if (type == Material.WOODEN_SWORD || type == Material.WOODEN_AXE) return "Wooden";
//        if (type == Material.STONE_SWORD || type == Material.STONE_AXE) return "Stone";
//        if (type == Material.IRON_SWORD || type == Material.IRON_AXE) return "Iron";
//        if (type == Material.GOLDEN_SWORD || type == Material.GOLDEN_AXE) return "Golden";
//        if (type == Material.DIAMOND_SWORD || type == Material.DIAMOND_AXE) return "Diamond";
//        if (type == Material.NETHERITE_SWORD || type == Material.NETHERITE_AXE) return "Netherite";
//
//        return "Unknown"; // Fallback in case of unsupported materials
//    }

    public static ItemStack smartSword, smartAxe, smartSpear;
    public static Component smartSwordName, smartAxeName, smartSpearName;

    static {
        smartSword = new ItemStack(Material.WOODEN_SWORD);
        smartSwordName = text("Меч", NamedTextColor.WHITE);
        setCustomName(smartSword, smartSwordName);
        updateWeaponLore(smartSword, 0);

        smartAxe = new ItemStack(Material.WOODEN_AXE);
        smartAxeName = text("Топор", NamedTextColor.WHITE);
        setCustomName(smartAxe, smartAxeName);
        updateWeaponLore(smartAxe, 0);

        smartSpear = new ItemStack(Material.WOODEN_SPEAR);
        smartSpearName = text("Копье", NamedTextColor.WHITE);
        setCustomName(smartSpear, smartSpearName);
        updateWeaponLore(smartSpear, 0);
    }

    public static void updateWeaponLore(ItemStack item, double progress) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Clear the current lore and create a new list
        List<Component> lore = new ArrayList<>();

        // Round the progress to one decimal place
        progress = Math.round(progress * 10.0) / 10.0;

        // Assuming the item has levels (like wooden -> stone -> iron -> etc.)
        Material type = item.getType();
//        String level = getMaterialLevel(type); // A method to return the material level (Wooden, Stone, etc.)

        // Add upgrade progress to the lore (e.g., "Progress: 45.0/100")
        int required = DAMAGE_REQUIRED.getOrDefault(type, Integer.MAX_VALUE);
//        lore.add(text("Прогресс: " + progress + "/" + required, NamedTextColor.LIGHT_PURPLE));

        Component newName;

        if (item.getType().toString().contains("SWORD")) {
            newName = smartSwordName;
        } else if (item.getType().toString().contains("AXE")) {
            newName = smartAxeName;
        } else {
            newName = smartSpearName;
        }

        int level = getUpgradeLevel(item.getType());
        newName = newName.append(text(" " + level));
        if (level < 5) {
            newName = newName.append(text(" [" + progress + "/" + required + "]", NamedTextColor.YELLOW));
        }

        meta.displayName(newName);

        // Update the lore and set it to the item meta
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    @EventHandler(priority = EventPriority.HIGH)  // or HIGHEST if you really need last word
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent e)) return;
        if (e.isCancelled()) return;

        if (!(e.getDamager() instanceof Player player)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;

        Material type = item.getType();
        if (!UPGRADE_PATH.containsKey(type)) return;

        // Get current progress from custom data method
        Double currentObj = getCustomData(item, "upgrade_progress", PersistentDataType.DOUBLE);
        double current = (currentObj != null) ? currentObj : 0.0; // Default to 0.0 if null
        if (Double.isNaN(current)) current = 0.0;

        double newTotal = current + e.getFinalDamage();
        int required = DAMAGE_REQUIRED.getOrDefault(type, Integer.MAX_VALUE);

        if (newTotal >= required) {
            Material nextMaterial = UPGRADE_PATH.get(type);
            double leftover = newTotal - required;

            // Create a new item for the upgraded version
            ItemStack upgraded = new ItemStack(nextMaterial);
            setCustomName(upgraded, getCustomName(item));
            updateWeaponLore(upgraded, leftover);

            // Transfer enchantments
            if (!item.getEnchantments().isEmpty()) {
                for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                    upgraded.addEnchantment(entry.getKey(), entry.getValue());
                }
            }

            // Set new progress using the custom set function
            setCustomData(upgraded, "upgrade_progress", PersistentDataType.DOUBLE, leftover);

            player.getInventory().setItemInMainHand(upgraded);
            player.playSound(player, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 0.8f, 1.2f);
        } else {
            // Just update progress
            setCustomData(item, "upgrade_progress", PersistentDataType.DOUBLE, newTotal);
            updateWeaponLore(item, newTotal);
        }
    }

    public static <T, Z> void setCustomData(ItemStack item, String keyName, PersistentDataType<T, Z> dataType, Z value) {
        if (item == null || item.getType().isAir()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, keyName);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, dataType, value);
        item.setItemMeta(meta);
    }

    public static <T, Z> Z getCustomData(ItemStack item, String keyName, PersistentDataType<T, Z> dataType) {
        if (item == null || item.getType().isAir()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        NamespacedKey key = new NamespacedKey(plugin, keyName);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.has(key, dataType) ? container.get(key, dataType) : null;
    }
}
