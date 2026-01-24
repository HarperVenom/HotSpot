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

    private BukkitRunnable particleTask = null;
    private BukkitTask ringTask = null;

    private BlockDisplay display;

    public Point(Location location, GameMap map) {
        this.location = location;
        this.map = map;
    }

    public void build() {
        block = location.getBlock();

        update();

        spawnDisplay();
    }

    public void update() {
        Material material;
        if (team == null) {
            material = Material.WHITE_CONCRETE;
        } else {
            spectatorSound(Sound.BLOCK_BEACON_ACTIVATE, 1, 1.5f);
            material = team.getPointMaterial();
        }

        block.setType(material);

        spawnDustParticles(block, team == null ? Color.WHITE : toBukkitColor(team.getColor()));
    }

    public void setTeam(GameTeam team) {
        this.team = team;
        update();
    }

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

    public boolean isBlock(Block b) {
        return (block.getLocation().equals(b.getLocation()));
    }
    public GameTeam getTeam() {
        return team;
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
            }
        };

        particleTask.runTaskTimer(plugin, 0L, 20L);
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
    }

    public void spectatorSound(Sound sound, float volume, float pitch) {
        for (Player player : map.getWorld().getPlayers()) {
            if (player.getGameMode() != GameMode.SPECTATOR) continue;
            player.playSound(player, sound, volume, pitch);
        }
    }
}
