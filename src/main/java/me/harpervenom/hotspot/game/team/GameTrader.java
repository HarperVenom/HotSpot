package me.harpervenom.hotspot.game.team;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class GameTrader {

    private final Location location;
    private Villager villager; // or Entity if you want to stay generic

    public GameTrader(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void spawn() {
        if (villager == null && location.getWorld() != null) {
            villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.setCustomName("Trader");
        }
    }

    public void remove() {
        if (villager != null && !villager.isDead()) {
            villager.remove();
            villager = null;
        }
    }

    public boolean isSpawned() {
        return villager != null && villager.isValid();
    }
}

