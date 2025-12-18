package me.harpervenom.hotspot.game.vault;

import me.harpervenom.hotspot.game.Game;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.*;

public class VaultManager {

    public static int lootBoxCooldown = 60;

    private final Game game;

    private final List<Vault> vaults;
    private int time = lootBoxCooldown;

    public VaultManager(Game game) {
        this.game = game;
        vaults = game.getMap().getVaults();
    }

    public void setup() {
        for (Vault vault : vaults) {
            vault.build();
        }
    }

    public void update() {
        time--;

        if (time == 0) {
            for (Vault vault : vaults) {
                vault.reset();
            }

            sendMessage(text("[Игра] ", NamedTextColor.GRAY).append(text("Хранилища обновлены", NamedTextColor.YELLOW)), game.getPlayers());
            playSound(Sound.BLOCK_VAULT_INSERT_ITEM, 0.5f, 1, game.getPlayers());

            time = lootBoxCooldown;
        }
    }

    public void resetForPlayer(Player player) {
        for (Vault vault : vaults) {
            vault.resetForPlayer(player);
        }
    }

    public Vault getLootDropper(Block b) {
        for (Vault vault : vaults) {
            if (vault.isBlock(b)) return vault;
        }
        return null;
    }

    public int getTime() {
        return time;
    }

    public static void setItemOwner(ItemStack item, UUID ownerId) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "item_owner");

        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, ownerId.toString());
        item.setItemMeta(meta);
    }

    public static UUID getItemOwner(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "item_owner");

        String ownerStr = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (ownerStr == null) return null;

        try {
            return UUID.fromString(ownerStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void removeItemOwner(ItemStack item) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "item_owner");
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (container.has(key, PersistentDataType.STRING)) {
            container.remove(key);
            item.setItemMeta(meta);
        }
    }
}
