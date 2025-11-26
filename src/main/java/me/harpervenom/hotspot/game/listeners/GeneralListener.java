package me.harpervenom.hotspot.game.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.vault.loot.CustomItems.isBreakable;

public class GeneralListener implements Listener {

    // Applying Enchantments
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Ensure the player is interacting with an inventory
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Get clicked item (item in inventory) and held item (item in cursor)
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack heldItem = event.getCursor();

        if (clickedItem == null) return;

        // Ensure the held item is an enchanted book
        if (heldItem.getType() != Material.ENCHANTED_BOOK) return;
        if (event.getClick() != ClickType.RIGHT) return; // Apply only on right-click

        // Get enchantments from the book
        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) heldItem.getItemMeta();
        if (bookMeta == null) return;

        Map<Enchantment, Integer> bookEnchantments = bookMeta.getStoredEnchants();

        // Ensure the clicked item can be enchanted
        if (clickedItem.getType() == Material.ENCHANTED_BOOK) return; // Prevent applying books to books

        ItemMeta itemMeta = clickedItem.getItemMeta();
        if (itemMeta == null) return;

        boolean applied = false;

        // Apply each valid enchantment
        for (Map.Entry<Enchantment, Integer> entry : bookEnchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int bookLevel = entry.getValue();

            // Check if the enchantment can be applied to this item
            if (!enchantment.canEnchantItem(clickedItem)) continue;

            // Get current level of the enchantment on the item
            int currentLevel = itemMeta.getEnchantLevel(enchantment);
            int maxLevel = enchantment.getMaxLevel();

            // Calculate new level, ensuring it does not exceed max
            int newLevel = Math.min(currentLevel + bookLevel, maxLevel);

            // Apply if there's an increase
            if (newLevel > currentLevel) {
                itemMeta.addEnchant(enchantment, newLevel, true);
                applied = true;
            }
        }

        // If at least one enchantment was applied, update the item
        if (applied) {
            clickedItem.setItemMeta(itemMeta);

            // Consume the enchanted book
            if (heldItem.getAmount() > 1) {
                heldItem.setAmount(heldItem.getAmount() - 1);
            } else {
                event.getWhoClicked().setItemOnCursor(null);
            }

            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.6f, 1f);
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 0.8f);
        }
    }

    @EventHandler
    public void onPlayerDrinkPotion(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();
        Material type = item.getType();

        if (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
            Player player = e.getPlayer();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Remove only one glass bottle, if it exists
                for (ItemStack content : player.getInventory().getContents()) {
                    if (content != null && content.getType() == Material.GLASS_BOTTLE) {
                        content.setAmount(content.getAmount() - 1);
                        break;
                    }
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        ItemStack item = e.getItem();
        if (isBreakable(item)) {
            return;
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        Player player = e.getPlayer();
        boolean hasBow = player.getInventory().contains(Material.BOW);
        boolean hasCrossbow = player.getInventory().contains(Material.CROSSBOW);

        if (e.getArrow().getType() == EntityType.TRIDENT) return;

        if (!hasBow && !hasCrossbow) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        Item item = event.getItem();
        Material type = item.getItemStack().getType();

        if (type == Material.ARROW || type == Material.TIPPED_ARROW || type == Material.SPECTRAL_ARROW) {
            Entity entity = event.getEntity();
            if (!canPickupArrows(entity)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean canPickupArrows(Entity entity) {
        if (!(entity instanceof Player player)) return true;
        PlayerInventory inv = player.getInventory();

        return inv.contains(Material.CROSSBOW) ||
                inv.contains(Material.BOW) ||
                inv.getItemInOffHand().getType() == Material.CROSSBOW ||
                inv.getItemInOffHand().getType() == Material.BOW;
    }
}
