package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class DamageListener implements Listener {

    private final GameManager gameManager;

    public DamageListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        e.setDroppedExp(0);
        e.getDrops().clear();
    }

    @EventHandler
    public void onHandDamageByHand(EntityDamageByEntityEvent e) {
        Game game = gameManager.getGame(e.getEntity().getWorld());
        if (game == null) return;
        game.getDamageManager().handleHandDamage(e);
    }

    @EventHandler
    public void onDamageByProjectile(EntityDamageByEntityEvent e) {
        Game game = gameManager.getGame(e.getEntity().getWorld());
        if (game == null) return;
        game.getDamageManager().handleProjectileDamage(e);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        Game game = gameManager.getGame(e.getEntity().getWorld());
        if (game == null) return;
        game.getDamageManager().processDamage(e);
    }
}
