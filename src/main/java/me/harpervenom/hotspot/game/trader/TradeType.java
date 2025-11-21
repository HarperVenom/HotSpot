package me.harpervenom.hotspot.game.trader;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import me.harpervenom.hotspot.game.profile.GameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.harpervenom.hotspot.utils.Utils.*;

public enum TradeType {
    PICKAXE("pickaxe", 30, 1.5, null, false, Material.WOODEN_PICKAXE, Material.COPPER_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE),
    LEGGINGS("leggings", 15, 1.5, EquipmentSlot.LEGS, false, Material.LEATHER_LEGGINGS, Material.COPPER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS),
    HELMET("helmet", 10, 1.5, EquipmentSlot.HEAD, false, Material.LEATHER_HELMET, Material.COPPER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET),
    BOOTS("boots", 10, 1.5, EquipmentSlot.FEET, false, Material.LEATHER_BOOTS, Material.COPPER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS),

    FOOD("food", 3, 0, null, true, Material.COOKED_BEEF),
    BLOCKS("blocks", 3, 0, null, true, Material.ROOTED_DIRT);

    private final static ItemStack maxLevelItem;

    static {
        maxLevelItem = createItemStack(Material.BLACK_STAINED_GLASS_PANE, text("Максимальный уровень", NamedTextColor.RED), null);
    }

    private final String id;
    private final ItemStack[] items;
    private final int basePrice;
    private final double factor;
    private final EquipmentSlot armorSlot;

    private final boolean quantitative;
    private ItemStack item;

    TradeType(String id, int basePrice, double factor, EquipmentSlot armorSlot, boolean quantitative, Material... materials) {
        this.id = id;
        this.basePrice = basePrice;
        this.factor = factor;
        this.armorSlot = armorSlot;
        this.quantitative = quantitative;

        List<Component> lore = List.of(text("Остается после смерти", NamedTextColor.GRAY));
        this.items = Arrays.stream(materials).map(m -> createItem(m, lore)).toArray(ItemStack[]::new);

        if (quantitative) {
            item = createItem(materials[0], null);
        }
    }

    public ItemStack getShopItemStack(GameProfile profile) {
        if (quantitative) {
            ItemStack shopItemStack;
            shopItemStack = item.clone();
            if (this == BLOCKS) item.setAmount(8);
            addLoreLine(shopItemStack, text("Цена: " + basePrice, NamedTextColor.AQUA));
            return shopItemStack;
        }

        int lvl = profile.getUpgradesManager().getTradeLevel(this);

        ItemStack shopItemStack;

//        if (lvl == getMaxLevel()) return maxLevelItem;

        shopItemStack = items[Math.max(0, lvl - 1)].clone();

        if (lvl == 0 && shopItemStack.getItemMeta() instanceof LeatherArmorMeta meta) {
            meta.setColor(Color.fromRGB(168, 168, 168)); // gray
            shopItemStack.setItemMeta(meta);
            shopItemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY,
                    TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.DYED_COLOR).build());
        }

        addLoreLine(shopItemStack, text("Уровень: " + profile.getUpgradesManager().getTradeLevel(this), NamedTextColor.YELLOW));
        addLoreLine(shopItemStack, getPriceLine(profile));
        return shopItemStack;
    }

    public int getCurrentPrice(GameProfile profile) {
        double price = basePrice;
        for (int i = 0; i < profile.getUpgradesManager().getTradeLevel(this); i++) {
            price *= factor;
        }

        double additionalFactor = 0;

        return (int) (price + price * additionalFactor);
    }

    public ItemStack getCurrentItem(GameProfile profile) {
        int level = profile.getUpgradesManager().getTradeLevel(this);

        return (level > 0) ? items[level-1].clone() : new ItemStack(Material.AIR);
    }

    public boolean upgrade(GameProfile profile) {
        if (quantitative) {
            boolean successful = profile.getEconomyManager().takePayment(basePrice);
            if (successful) {
                Player player = profile.getPlayer().getPlayer();
                if (this == FOOD) {
                    addItemToInventory(player, new ItemStack(Material.COOKED_BEEF, 1));
                }
                if (this == BLOCKS) {
                    addItemToInventory(player, new ItemStack(Material.ROOTED_DIRT, 8));
                }
            }
            return successful;
        }

        if (isMaxLevel(profile)) return false;
        boolean successful = profile.getEconomyManager().takePayment(getCurrentPrice(profile));
        if (!successful) return false;
        profile.getUpgradesManager().increaseLevel(this);
        return true;
    }

    private Component getPriceLine(GameProfile profile) {
        if (isMaxLevel(profile)) {
            return text("Максимальный уровень", NamedTextColor.YELLOW);
        }

        return text("Цена: " + getCurrentPrice(profile), NamedTextColor.AQUA);
    }

    private static ItemStack createItem(Material material, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        List<Component> oldLore = meta.lore();
        if (oldLore == null) {
            oldLore = new ArrayList<>();
        }

        if (lore != null) oldLore.addAll(lore);

        meta.lore(oldLore);
        item.setItemMeta(meta);

        return item;
    }

    public String getId() {
        return id;
    }

    public EquipmentSlot getArmorSlot() {
        return armorSlot;
    }

    public boolean isMaxLevel(GameProfile profile) {
        return profile.getUpgradesManager().getTradeLevel(this) == getMaxLevel();
    }

    public int getMaxLevel() {
        return items.length;
    }
}
