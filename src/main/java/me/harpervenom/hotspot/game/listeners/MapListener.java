package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.map.GameMap;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import static me.harpervenom.hotspot.utils.Utils.text;

public class MapListener implements Listener {

    private final GameManager gameManager;

    public MapListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Block b = e.getBlock();
        Game game = gameManager.getGame(b.getWorld());
        if (game == null) return;

        if (!game.getMap().canPlace(b)) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            p.sendActionBar(text("Территория защищена!", NamedTextColor.RED));
            return;
        }

        game.getMap().addBlock(b);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        Game game = gameManager.getGame(b.getWorld());
        if (game == null) return;

        GameMap gameMap = game.getMap();

        if (!gameMap.canBrake(b)) {
            e.setCancelled(true);
            return;
        }

        gameMap.removeBlock(b);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if ((e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL && e.getEntity() instanceof Monster) ||
                e.getEntity().getType() == EntityType.SLIME || e.getEntity().getType() == EntityType.BAT) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBLockBreak(BlockBreakEvent e) {
        e.setDropItems(false);
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent e) {
        if (e.getSource().getType() == Material.FIRE) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFallingBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if (e.getCause() == WeatherChangeEvent.Cause.NATURAL) e.setCancelled(true);
    }
}
