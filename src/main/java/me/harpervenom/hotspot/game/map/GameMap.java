package me.harpervenom.hotspot.game.map;

import me.harpervenom.hotspot.game.point.Point;
import me.harpervenom.hotspot.game.team.GameTrader;
import me.harpervenom.hotspot.game.team.TeamBase;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameMap {

    private final String name;
    private final String displayName;
    private final Material material;
    private final World world;

    private final List<Location> spawns;
    private final List<TeamBase> bases;

    private final List<Point> points;
//    private final List<LootDropper> lootDroppers;

    private final Set<Block> blocks = new HashSet<>();

    public static List<Material> immuneMaterials = List.of(Material.IRON_BLOCK, Material.COAL_BLOCK);

    public GameMap(MapData mapData, World world) {
        this.world = world;
        name = mapData.getName();
        displayName = mapData.getDisplayName();
        material = mapData.getMaterial();

        spawns = mapData.getSpawns().stream()
                .map(loc -> new Location(world, loc.x+0.5, loc.y+1, loc.z+0.5, loc.yaw, 0))
                .toList();

        List<GameTrader> traders = mapData.getTraders().stream()
                .map(loc -> new GameTrader(new Location(world, loc.x+0.5, loc.y+1, loc.z+0.5, loc.yaw, 0)))
                .toList();

        this.bases = new ArrayList<>();
        for (int i = 0; i < spawns.size(); i++) {
            Location spawn = spawns.get(i);
            GameTrader trader = i < traders.size() ? traders.get(i) : null;
            bases.add(new TeamBase(spawn, trader));
        }

        points = mapData.getMonuments().stream()
                .map(loc -> new Point(new Location(world, loc.x, loc.y, loc.z), this))
                .collect(Collectors.toList());

//        lootDroppers = mapData.getVaults().stream()
//                .map(loc -> new LootDropper(new Location(world, loc.x, loc.y, loc.z)))
//                .collect(Collectors.toList());
    }

    public boolean isBlockProtected(Block b) {
        Location blockLoc = b.getLocation();

        for (Location spawn : spawns) {
            spawn = spawn.clone().subtract(0.5, 0, 0.5);

            if (Math.abs(spawn.getX() - blockLoc.getX()) <= 1 &&
                    Math.abs(spawn.getZ() - blockLoc.getZ()) <= 1) {
                return true;
            }
        }
        return false;
    }

    public void addBlock(Block b) {
        blocks.add(b);
    }

    public boolean isBreakable(Block b) {
        if (b.getType().getHardness() == 0.0) return true;
        return blocks.contains(b);
    }

    public void removeBlock(Block b) {
        blocks.remove(b);
    }

    public String getName() {
        return name;
    }
    public String getDisplayName() {
        return displayName;
    }

    public World getWorld() {
        return world;
    }
    public List<TeamBase> getBases() {
        return bases;
    }
    public List<Point> getPoints() {
        return points;
    }
//
//    public List<LootDropper> getLootDroppers() {
//        return lootDroppers;
//    }
//
//    public LootDropper getLootDropper(Block b) {
//        for (LootDropper lootDropper : lootDroppers) {
//            if (lootDropper.isBlock(b)) return lootDropper;
//        }
//        return null;
//    }

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
