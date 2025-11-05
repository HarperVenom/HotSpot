package me.harpervenom.hotspot.game.vault;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.GameProfile;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

import static me.harpervenom.hotspot.game.vault.VaultManager.getItemOwner;
import static me.harpervenom.hotspot.game.vault.VaultManager.removeItemOwner;

public class VaultListener implements Listener {

    private final GameManager gameManager;

    public VaultListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onActivate(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = e.getPlayer();
        Block b = e.getClickedBlock();
        if (b == null) return;
        if (b.getType() == Material.DISPENSER) {
            e.setCancelled(true);
        }
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        Game game = gameManager.getGame(b.getWorld());
        if (game == null) return;

        Vault vault = game.getVaultManager().getLootDropper(b);
        if (vault == null) return;

        GameProfile profile = game.getPlayerManager().getProfile(player);
        if (profile == null) return;

        vault.open(profile);
    }

    @EventHandler
    public void onItemPickUp(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        UUID playerId = player.getUniqueId();
        Item item = e.getItem();

        UUID ownerId = getItemOwner(item.getItemStack());

        if (ownerId == null || ownerId.equals(playerId)) {
            removeItemOwner(item.getItemStack());
            return;
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void onMerge(ItemMergeEvent e) {
        Item item = e.getEntity();
        Item target = e.getTarget();

        if (getItemOwner(item.getItemStack()) != null || getItemOwner(target.getItemStack()) != null) e.setCancelled(true);
    }
}
