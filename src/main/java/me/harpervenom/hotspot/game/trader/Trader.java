package me.harpervenom.hotspot.game.trader;

import me.harpervenom.hotspot.game.profile.GameProfile;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import static me.harpervenom.hotspot.utils.Utils.text;

public class Trader {

    private final Location location;
    private Villager villager;

    public Trader(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Villager spawn() {
        if (villager == null && location.getWorld() != null) {
            villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
            villager.setAI(false);
            villager.setInvulnerable(true);
            villager.customName(text("Магазин", TextColor.color(77, 230, 46)));
            villager.setCustomNameVisible(true);
        }
        return villager;
    }

    public static void openShopWindow(GameProfile profile) {
        Player player = profile.getPlayer();
        TraderWindow window = new TraderWindow(profile);
        window.open(player);
    }

    public boolean isSpawned() {
        return villager != null && villager.isValid();
    }
    public Villager getVillager() {
        return villager;
    }
}

