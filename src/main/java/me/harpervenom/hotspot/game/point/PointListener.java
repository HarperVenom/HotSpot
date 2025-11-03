package me.harpervenom.hotspot.game.point;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import static me.harpervenom.hotspot.utils.Utils.text;

public class PointListener implements Listener {

    private final GameManager gameManager;

    public PointListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPointCapture(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();

        Game game = gameManager.getGame(block.getWorld());
        if (game == null) return;

        Point point = game.getPointManager().getPoint(block);
        if (point == null) return;

        e.setCancelled(true);

        game.getPointManager().capture(point, player);
    }

//        Monument monument = game.
//
//        game.captureMonument(block, player);
//
//        GameTeam team = p.getTeam();
//        if (team == null) return;
//        Game game = team.getGame();
//        if (game == null) return;
//        Monument monument = game.getMonumentByBlock(block);
//        if (monument == null) return;
//        e.setCancelled(true);
//
//        if (team.equals(monument.getTeam())) return;
//
//        if (monument.isProtected()) {
//            p.getPlayer().sendMessage(text("Точка под защитой.", NamedTextColor.LIGHT_PURPLE));
//            p.getPlayer().playSound(block.getLocation(), Sound.BLOCK_HEAVY_CORE_BREAK, 1, 0.2f);
//            return;
//        }
//
//        boolean captured = monument.setTeam(team);
//
//        if (!captured) return;
//
//        p.getProfile().addCapture();
//
//        game.getGameScoreboard().update();
//    }

//    @EventHandler
//    public void onWorldChange(PlayerChangedWorldEvent e) {
//        Player player = e.getPlayer();
//        GameMap map = getMap(player);
//        if (map == null) return;
//
//        Bukkit.getScheduler().runTaskLater(plugin, () -> {
//            map.updateMonumentsDisplay(player, false);
//        }, 5);
//    }
//
//    @EventHandler
//    public void onPickaxeHeld(PlayerItemHeldEvent e) {
//        Player player = e.getPlayer();
//        ItemStack newItem = player.getInventory().getItem(e.getNewSlot());
//        GameMap map = getMap(player);
//        if (map == null) return;
//
//        boolean display = newItem != null && newItem.getType().name().contains("PICKAXE");
//
//        map.updateMonumentsDisplay(player, display);
//    }
}
