package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.profile.GameProfile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.map.GameMap.immuneMaterials;
import static me.harpervenom.hotspot.game.vault.loot.CustomItems.mudBombId;
import static me.harpervenom.hotspot.game.vault.loot.CustomItems.vacuumBombId;
import static me.harpervenom.hotspot.utils.Utils.getItemId;
import static me.harpervenom.hotspot.utils.Utils.playSound;

public class BombsListener implements Listener {

    private final GameManager gameManager;

    private final HashMap<UUID, ItemStack> usedItem = new HashMap<>();

    public BombsListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        usedItem.put(e.getPlayer().getUniqueId(), e.getItem());
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player player)) return;
        Projectile projectile = e.getEntity();

        ItemStack item = usedItem.get(player.getUniqueId());
        if (item == null) return;
        String itemId = getItemId(item);

        if (itemId == null || !(itemId.equals(mudBombId) || itemId.equals(vacuumBombId))) return;

        // Store the custom ID on the projectile
        NamespacedKey key = new NamespacedKey(plugin, "custom_id");
        projectile.getPersistentDataContainer().set(key, PersistentDataType.STRING, itemId);
    }

    @EventHandler
    public void onMudBombHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Egg egg)) return;

        NamespacedKey key = new NamespacedKey(plugin, "custom_id");
        if (egg.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            String id = egg.getPersistentDataContainer().get(key, PersistentDataType.STRING);

            // Get the location where the egg landed
            Location landingLocation = e.getEntity().getLocation();
            World world = landingLocation.getWorld();

            Game game = gameManager.getGame(world);
            if (game == null) return;
            GameMap map = game.getMap();

            landingLocation.getWorld().playSound(landingLocation, Sound.BLOCK_MUD_PLACE, 3, 0.4f);
            playSound(Sound.BLOCK_MUD_PLACE, 0.2f, 0.4f, game.getPlayers());

            int radius = 2;
            // Loop through a cubic region, but only place blocks within a spherical radius
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location location = landingLocation.clone().add(x, y, z);

                        // Check if the location is within the spherical radius
                        if (location.distance(landingLocation) <= radius) {
                            // Only place the block if the current block is air
                            if (map.canPlace(location.getBlock())
                                    && isReplaceable(location.getBlock())) {

                                world.getBlockAt(location).setType(Material.CLAY);

                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    world.spawnParticle(Particle.BLOCK,
                                            location.clone().add(0.3, 0.3, 0.3),
                                            20, 0.5, 0.5, 0.5, 0.1,
                                            Bukkit.createBlockData(Material.CLAY)
                                    );
                                }, 1);

                                map.addBlock(location.getBlock());
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onVacuumBombHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();

        // Read custom ID from projectile
        NamespacedKey key = new NamespacedKey(plugin, "custom_id");
        String customId = projectile.getPersistentDataContainer().get(key, PersistentDataType.STRING);

        if (customId == null || !customId.equals(vacuumBombId)) return;

        Location center = projectile.getLocation();
        World world = center.getWorld();

        Game game = gameManager.getGame(world);
        if (game == null) return;
        GameMap map = game.getMap();

        projectile.remove(); // Remove the projectile on impact

        final int durationTicks = 35;
        final double radius = 5.0;
        final double maxPullForce = 0.3; // reasonable force that can be resisted

        if (!(e.getEntity().getShooter() instanceof Player shooter)) return;

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= durationTicks) {
                    cancel();
                    return;
                }

                double strength = 1;

                for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                    if (entity instanceof Player player) {
                        if (player.getGameMode() == GameMode.SPECTATOR) continue;
                        GameProfile profile = game.getPlayerManager().getProfile(player);
                        if (profile.isProtected()) continue;
                    }
                    if (game.getPlayerManager().areSameTeam(shooter, entity)) continue;
//                    if (p != null && p.getTeam() != null && team != null && team.equals(p.getTeam())) continue;

                    Vector pullDir = center.toVector().subtract(entity.getLocation().toVector());
                    double distance = pullDir.length();

                    if (distance == 0 || distance > radius) continue;

                    pullDir.normalize();

                    // Distance-based attenuation (optional)
                    double distanceFactor = 1 - 0.5 * (distance / radius); // Closer = stronger

                    Vector pull = pullDir.multiply(maxPullForce * strength * distanceFactor);

                    if (distance < 1) {
                        entity.setVelocity(pull);
                    } else {
                        entity.setVelocity(entity.getVelocity().add(pull));
                    }
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Every tick

        int blockRadius = (int) radius;

        for (int x = -blockRadius; x <= blockRadius; x++) {
            for (int y = -blockRadius; y <= blockRadius; y++) {
                for (int z = -blockRadius; z <= blockRadius; z++) {
                    Location location = center.clone().add(x, y, z);

                    // Check if the location is within the spherical radius
                    if (location.distance(center) <= blockRadius) {
                        // Only place the block if the current block is air
                        if (map.canBrake(location.getBlock()) && !immuneMaterials.contains(location.getBlock().getType())) {
                            world.getBlockAt(location).setType(Material.AIR);
                        }
                    }
                }
            }
        }

        //Particles
        Color color = Color.BLACK;
        spawnHollowSphere(center, radius, color, 2, 300);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > durationTicks) { // Run for 2 seconds
                    cancel();
                    return;
                }

                center.getWorld().spawnParticle(Particle.PORTAL, center, 20);

                Particle.DustOptions dust = new Particle.DustOptions(color, 1);
                center.getWorld().spawnParticle(Particle.DUST, center, 10, 0.2, 0.2, 0.2, 1, dust);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        int period = 10;

        projectile.getWorld().playSound(projectile.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 0.9f, 1.2f);
        playSound(Sound.BLOCK_CONDUIT_DEACTIVATE, 0.2f, 1.2f, game.getPlayers());

        new BukkitRunnable(){
            int tick = 0;

            @Override
            public void run() {
                if (tick >= durationTicks / period) {
                    cancel();
                    return;
                }

                projectile.getWorld().playSound(projectile.getLocation(), Sound.ENTITY_BREEZE_CHARGE, 0.5f, 0.5f);

                tick++;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    public static void spawnHollowSphere(Location center, double radius, Color color, float size, int points) {
        World world = center.getWorld();
        if (world == null) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, size);

        // Loop through spherical coordinates
        for (int i = 0; i < points; i++) {
            double theta = Math.acos(2.0 * i / points - 1.0); // polar angle
            double phi = Math.PI * (1 + Math.sqrt(5)) * i;    // azimuthal angle

            double x = radius * Math.sin(theta) * Math.cos(phi);
            double y = radius * Math.cos(theta);
            double z = radius * Math.sin(theta) * Math.sin(phi);

            Location particleLoc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
        }
    }

    boolean isReplaceable(Block b) {
        return b.getType().getHardness() == 0.0 || b.getType() == Material.AIR || b.getType() == Material.WATER
                || b.getType() == Material.LAVA || b.getType() == Material.VINE || b.getType() == Material.GLOW_LICHEN;
    }
}
