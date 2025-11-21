package me.harpervenom.hotspot.game.profile;

import me.harpervenom.hotspot.game.trader.TradeType;
import me.harpervenom.hotspot.game.vault.loot.LootClass;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.HashMap;
import java.util.Map;

import static me.harpervenom.hotspot.game.vault.loot.CustomItems.hideArmorTrim;
import static me.harpervenom.hotspot.utils.Utils.*;
import static me.harpervenom.hotspot.utils.Utils.addItemToInventory;

public class EquipmentManager {

    private final static String initialItemKey = "initial";

    private final GameProfile profile;

    private boolean hasWeapon = false;
    private boolean hasChest = false;

    public EquipmentManager(GameProfile profile) {
        this.profile = profile;
    }

    public void giveItems() {
        Player player = profile.getPlayer();
        if (player == null) return;

        PlayerInventory inventory = player.getInventory();

        ItemStack pickaxe = getItem(TradeType.PICKAXE);
        if (pickaxe.getItemMeta() != null) {
            addLoreLine(pickaxe, text("*ШИФТ + ПКМ в руках телепортирует тебя на базу*"));
        }

        inventory.setItem(1, pickaxe);
        inventory.setItem(2, new ItemStack(Material.COOKED_BEEF, 8));
        inventory.setItem(3, new ItemStack(Material.ROOTED_DIRT, 32));

        inventory.setLeggings(getItem(TradeType.LEGGINGS));
        inventory.setHelmet(getItem(TradeType.HELMET));
        inventory.setBoots(getItem(TradeType.BOOTS));
    }

    public boolean hasChest() {
        return hasChest;
    }
    public void setHasChest(boolean hasChest) {
        this.hasChest = hasChest;
    }
    public boolean hasWeapon() {
        return hasWeapon;
    }
    public void setHasWeapon(boolean hasWeapon) {
        this.hasWeapon = hasWeapon;
    }

    public void replaceItem(TradeType type) {
        Player player = profile.getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack newItem = getItem(type);

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);

            if (hasItemKeyword(item, type.getId())) {
                transferEnchantments(item, newItem);
                inventory.setItem(i, newItem);
                return; // Stop after replacing the first matching item
            }
        }

        // If no matching item was found, simply add the new item to the inventory
        addItemToInventory(player, newItem);
    }

    public void replaceArmorItem(TradeType type) {
        Player playerEntity = profile.getPlayer();
        PlayerInventory inventory = playerEntity.getInventory();
        ItemStack currentArmor = inventory.getItem(type.getArmorSlot());

        boolean isWearingArmor = currentArmor.getType() != Material.AIR;
        boolean isSameId = isWearingArmor && hasItemKeyword(currentArmor, type.getId());

        ItemStack newArmor = getItem(type);

        if (isWearingArmor && !isSameId) {
            addItemToInventory(playerEntity, currentArmor);
        }

        // If the player was wearing armor with the same ID, transfer enchantments
        if (isSameId) {
            transferEnchantments(currentArmor, newArmor);
        }

        applyArmorTrim(newArmor);

        // Equip the new armor
        inventory.setItem(type.getArmorSlot(), newArmor);
    }

    private void applyArmorTrim(ItemStack armor) {
        if (profile.getTeam() == null) return;
        TrimMaterial trimMaterial = TRIM_MATERIALS.get(profile.getTeam().getColor());

        if (trimMaterial != null && armor.getItemMeta() instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(new ArmorTrim(trimMaterial, TrimPattern.FLOW)); // Adjust pattern as needed
            armor.setItemMeta(armorMeta);
        }
    }

    private void transferEnchantments(ItemStack from, ItemStack to) {
        if (from == null || to == null) return;

        ItemMeta fromMeta = from.getItemMeta();
        ItemMeta toMeta = to.getItemMeta();

        if (fromMeta instanceof EnchantmentStorageMeta enchantMeta) {
            EnchantmentStorageMeta newEnchantMeta = (EnchantmentStorageMeta) toMeta;
            for (Map.Entry<Enchantment, Integer> entry : enchantMeta.getStoredEnchants().entrySet()) {
                newEnchantMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
            }
        } else {
            for (Map.Entry<Enchantment, Integer> entry : fromMeta.getEnchants().entrySet()) {
                toMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        to.setItemMeta(toMeta);
    }

    private ItemStack getItem(TradeType type) {
        ItemStack item = type.getCurrentItem(profile);

        applyArmorTrim(item);
        hideArmorTrim(item);

        addItemKeyword(item, type.getId());
        addItemKeyword(item, initialItemKey);
        return item;
    }

    private static final Map<NamedTextColor, TrimMaterial> TRIM_MATERIALS = Map.of(
            NamedTextColor.RED, TrimMaterial.REDSTONE,
            NamedTextColor.BLUE, TrimMaterial.LAPIS
    );
}
