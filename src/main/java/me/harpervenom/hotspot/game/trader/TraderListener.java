package me.harpervenom.hotspot.game.trader;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.profile.GameProfile;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class TraderListener implements Listener {

    private final GameManager gameManager;

    public TraderListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onTraderInteract(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();
        Game game = gameManager.getGame(entity.getWorld());
        if (game == null) return;

        Trader trader = game.getTraderManager().getTrader(entity);
        if (trader == null) return;

        e.setCancelled(true);

        GameProfile profile = game.getPlayerManager().getProfile(e.getPlayer());
        if (profile == null) return;

        me.harpervenom.hotspot.game.trader.Trader.openShopWindow(profile);
    }

    @EventHandler
    public void onVillagerTransform(EntityTransformEvent event) {
        if (event.getTransformReason() == EntityTransformEvent.TransformReason.LIGHTNING
                && event.getEntityType() == EntityType.VILLAGER) {

            event.setCancelled(true); // Prevent the transformation
        }
    }
}
