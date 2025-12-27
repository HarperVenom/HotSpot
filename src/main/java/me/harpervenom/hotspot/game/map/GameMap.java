package me.harpervenom.hotspot.game.map;

import me.harpervenom.hotspot.game.vault.Vault;
import me.harpervenom.hotspot.game.point.Point;
import me.harpervenom.hotspot.game.trader.Trader;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameMap {

    private final MapData mapData;
    private final World world;

    private final List<Location> spawns;
    private final List<Trader> traders;
    private final List<Point> points;
    private final List<Vault> vaults;

    private final Set<Block> blocks = new HashSet<>();

    public static List<Material> immuneMaterials = List.of(Material.IRON_BLOCK, Material.COAL_BLOCK);

    public GameMap(MapData mapData, World world) {
        this.world = world;
        this.mapData = mapData;

        spawns = mapData.getSpawns().stream()
                .map(loc -> new Location(world, loc.x+0.5, loc.y+1, loc.z+0.5, loc.yaw, 0))
                .toList();

        traders = mapData.getTraders().stream()
                .map(loc -> new Trader(new Location(world, loc.x+0.5, loc.y+1, loc.z+0.5, loc.yaw, 0)))
                .toList();

        points = mapData.getMonuments().stream()
                .map(loc -> new Point(new Location(world, loc.x, loc.y, loc.z), this))
                .collect(Collectors.toList());

        vaults = mapData.getVaults().stream()
                .map(loc -> new Vault(new Location(world, loc.x, loc.y, loc.z)))
                .collect(Collectors.toList());
    }

    public boolean canPlace(Block b) {
        Location blockLoc = b.getLocation();

        for (Location spawn : spawns) {
            spawn = spawn.clone().subtract(0.5, 0, 0.5);

            if (Math.abs(spawn.getX() - blockLoc.getX()) <= 1 &&
                    Math.abs(spawn.getZ() - blockLoc.getZ()) <= 1) {
                return false;
            }
        }
        return true;
    }

    public boolean canBrake(Block b) {
        if (b.getType().getHardness() == 0.0 || b.getType() == Material.ICE) return true;
        return blocks.contains(b);
    }

    public void addBlock(Block b) {
        blocks.add(b);
    }

    public void removeBlock(Block b) {
        blocks.remove(b);
    }

    public MapData getMapData() {
        return mapData;
    }
    public String getName() {
        return mapData.getName();
    }
    public String getDisplayName() {
        return mapData.getDisplayName();
    }

    public World getWorld() {
        return world;
    }
    public List<Location> getSpawns() {
        return spawns;
    }
    public List<Trader> getTraders() {
        return traders;
    }
    public List<Point> getPoints() {
        return points;
    }
    public List<Vault> getVaults() {
        return vaults;
    }



//    public void updateMonumentsDisplay() {
//        for (Player player : world.getPlayers()) {
//            updateMonumentsDisplay(player);
//        }
//    }

//    public void updateMonumentsDisplay(Player p, boolean isOn) {
//        if (monuments == null) return;
//        for (Monument monument : monuments) {
//            monument.highlightForPlayer(p, isOn);
//        }
//    }
//
//    public void updateMonumentsDisplay(Player p) {
//        boolean isOn = p.getInventory().getItemInMainHand().getType().toString().contains("PICKAXE");
//        updateMonumentsDisplay(p, isOn);
//    }

//    public void delete() {
////        for (Trader trader : traders) {
////            trader.remove();
////        }
////
////        for (Monument monument : monuments) {
////            monument.remove();
////        }
//
//        for (Block block : blocks) {
//            block.setType(Material.AIR);
//        }
//
//        clearWorldAsync(world, null);
//    }
}
