package me.harpervenom.hotspot.game.point;

import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.team.GameTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;

import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.text;
import static me.harpervenom.hotspot.utils.Utils.toBukkitColor;

public class Point {

    private final GameMap map;

    private final Location location;
    private GameTeam team;

    private Block block;
    private Material material;

    private boolean disabled = false;

    private BukkitRunnable particleTask = null;
    private BukkitTask ringTask = null;

    private ArmorStand hologram;
    private BlockDisplay display;

    private boolean isProtected;

    public Point(Location location, GameMap map) {
        this.location = location;
        this.map = map;
    }

    public void build() {
        block = location.getBlock();

//        spawnHologram();
//        Bukkit.getScheduler().runTaskLater(plugin, () -> {
//            hologram.setCustomNameVisible(false);
//        }, 30 * 20);
        update();

        spawnDisplay();
    }

    public void update() {
        if (team == null) {
            material = Material.WHITE_CONCRETE;
        } else {
//            team.playSound(Sound.BLOCK_BEACON_ACTIVATE, 1, 1.5f);
            spectatorSound(Sound.BLOCK_BEACON_ACTIVATE, 1, 1.5f);
            material = team.getMaterial();
        }

        block.setType(material);

//        spawnDisplay();

//        if (team == null) {
//            updateHologram(text("Сломай блок", NamedTextColor.WHITE));
//        } else {
//            hologram.setCustomNameVisible(false);
//        }

        spawnDustParticles(block, team == null ? Color.WHITE : toBukkitColor(team.getColor()));
    }

    public void setTeam(GameTeam team) {
        this.team = team;
        update();
    }

//    public boolean setTeam(GameTeam team) {
//        if (disabled) return false;
//
//        if (this.team != null) {
//            if (this.team.equals(team)) {
//                return false;
//            }
////            this.team.playSound(Sound.BLOCK_BEACON_DEACTIVATE, 1, 1.5f);
//        }
//
//        this.team = team;
//
////        spawnHollowSphere(block.getLocation().clone().add(0.5, 0.5, 0.5), 1, toBukkitColor(team.getColor()), 1, 50);
//
//        update();
//
//        return true;
//    }

//    private void cover() {
//        if (block == null) return;
//
//        block.getWorld().playSound(block.getLocation().clone().add(0.5, 0.5, 0.5),
//                Sound.BLOCK_HEAVY_CORE_BREAK, 1.5f, 0.8f);
//
//        // Get the block's location
//        Location loc = block.getLocation();
//
//        // Offsets for each face except the bottom
//        BlockFace[] faces = {
//                BlockFace.UP,
//                BlockFace.NORTH,
//                BlockFace.SOUTH,
//                BlockFace.EAST,
//                BlockFace.WEST
//        };
//
//        for (BlockFace face : faces) {
//            Block adjacent = block.getRelative(face);
//            adjacent.setType(Material.IRON_BLOCK);
//            map.addBlock(adjacent);
//        }
//    }

    private void spawnDisplay() {
        if (display != null) display.remove();

        display = block.getWorld().spawn(
                block.getLocation().clone().add(0.5, 0.5, 0.5), // center of the block
                BlockDisplay.class,
                display -> {
                    display.setBlock(block.getBlockData());

                    double size = 0.99f;

                    // Optional: scale it slightly smaller (90%)
                    Transformation transform = display.getTransformation();
                    transform.getScale().set(size, size, size);
                    transform.getTranslation().set(-size/2, -size/2, -size/2);
                    display.setTransformation(transform);
                }
        );
        display.setGlowing(true);
    }

    private void spawnHologram() {
        World world = block.getWorld();

        hologram = world.spawn(block.getLocation().clone().add(0.5, 1, 0.5), ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.customName(text("", NamedTextColor.WHITE));
            stand.setCustomNameVisible(true);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setSmall(true);
        });
    }

    private void updateHologram(Component text) {
        hologram.customName(text);
    }

    public boolean isBlock(Block b) {
        return (block.getLocation().equals(b.getLocation()));
    }
    public GameTeam getTeam() {
        return team;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;

        if (isProtected) startRingTask(block);
        else {
            if (ringTask != null) {
                ringTask.cancel();
                ringTask = null;
            }
        }
    }

    public void disable() {
        team = null;
        disabled = true;
        hologram.customName(null);
        material = Material.LIGHT_GRAY_CONCRETE;
    }

    public void spawnDustParticles(Block block, Color color) {
        if (particleTask != null) {
            particleTask.cancel();
        }

        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!block.getLocation().isWorldLoaded()) {
                    cancel();
                    particleTask = null;
                    return;
                }

                Location baseLocation = block.getLocation().clone().add(0.5, 0.5, 0.5);

                // Spread the particles slightly outside the block's center
                double spreadX = 0.3;  // Spread along the X axis
                double spreadY = 0.3;  // Spread along the Y axis
                double spreadZ = 0.3;  // Spread along the Z axis

                block.getWorld().spawnParticle(
                        Particle.DUST, // Particle type
                        baseLocation,      // Origin of the particle (center of the block)
                        30,               // Number of particles
                        spreadX, spreadY, spreadZ, // Spread in all directions
                        0,                 // Extra, optional (set to 0 for no additional behavior)
                        new Particle.DustOptions(color, 1.5f)  // Dust color and size (1 is the size)
                );

                // Column
//                for (int i = 0; i <= 10; i++) {
//                    Location columnLoc = baseLocation.clone().add(0, i * 1.2, 0); // every 1.5 blocks up
//
//                    float size = 7f - (i * 0.5f); // decrease size by 0.5 each step
//                    if (size < 1f) size = 1f;     // don't let it go below 1 (minimum visible size)
//
//                    block.getWorld().spawnParticle(
//                            Particle.DUST,
//                            columnLoc,
//                            1, // 1 particle per spot
//                            0, 0, 0, 0,
//                            new Particle.DustOptions(color, size),
//                            true
//                    );
//                }
            }
        };

        particleTask.runTaskTimer(plugin, 0L, 20L);
    }


    public void startRingTask(Block block) {
        if (ringTask != null) {
            ringTask.cancel();
        }

        ringTask = new BukkitRunnable() {
            final Location center = block.getLocation().clone().add(0.5, 0.5, 0.5);
            final double radius = 1;
            final int points = 36;
            final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.PURPLE, 1);

            @Override
            public void run() {
                if (!block.getLocation().isWorldLoaded()) {
                    cancel();
                    ringTask = null;
                    return;
                }

                World world = center.getWorld();
                if (world == null) return;

                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;

                    // 1. Horizontal ring (XZ plane)
                    double x1 = center.getX() + radius * Math.cos(angle);
                    double z1 = center.getZ() + radius * Math.sin(angle);
                    world.spawnParticle(Particle.DUST, new Location(world, x1, center.getY(), z1), 0, dustOptions);

                    // 2. Vertical ring (YZ plane)
                    double y2 = center.getY() + radius * Math.cos(angle);
                    double z2 = center.getZ() + radius * Math.sin(angle);
                    world.spawnParticle(Particle.DUST, new Location(world, center.getX(), y2, z2), 0, dustOptions);

                    // 3. Diagonal ring (XY plane)
                    double x3 = center.getX() + radius * Math.cos(angle);
                    double y3 = center.getY() + radius * Math.sin(angle);
                    world.spawnParticle(Particle.DUST, new Location(world, x3, y3, center.getZ()), 0, dustOptions);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public void setViewers(List<Player> players) {
        for (Player player : block.getWorld().getPlayers()) {
            player.hideEntity(plugin, display);
        }

        for (Player player : players) {
            player.showEntity(plugin, display);
        }
    }

    public void remove() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }

        if (ringTask != null) {
            ringTask.cancel();
            ringTask = null;
        }

        if (hologram != null) {
            hologram.remove();
        }
    }

    public void spectatorSound(Sound sound, float volume, float pitch) {
        for (Player player : map.getWorld().getPlayers()) {
            if (player.getGameMode() != GameMode.SPECTATOR) continue;
            player.playSound(player, sound, volume, pitch);
        }
    }

    public boolean isProtected() {
        return isProtected;
    }
}
