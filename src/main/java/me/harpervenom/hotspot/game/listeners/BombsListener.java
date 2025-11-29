package me.harpervenom.hotspot.game.listeners;

import io.papermc.paper.event.entity.EntityMoveEvent;
import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.profile.GameProfile;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.map.GameMap.immuneMaterials;
import static me.harpervenom.hotspot.game.vault.loot.CustomItems.mudBombId;
import static me.harpervenom.hotspot.game.vault.loot.CustomItems.vacuumBombId;
import static me.harpervenom.hotspot.utils.Utils.getItemId;
import static me.harpervenom.hotspot.utils.Utils.playSound;

public class BombsListener implements Listener {

    private final GameManager gameManager;

    private final Map<Projectile, Location> tracked = new ConcurrentHashMap<>();

    public BombsListener(GameManager gameManager) {
        this.gameManager = gameManager;
        runProjectileScanning();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player player)) return;

        Projectile projectile = e.getEntity();
        if (!(projectile instanceof Snowball || projectile instanceof Egg)) return;

        ItemStack used = player.getInventory().getItemInMainHand();
        if (used.getType().isAir()) return;

        String itemId = getItemId(used);
        if (itemId == null) return;

        if (!itemId.equals(mudBombId) && !itemId.equals(vacuumBombId)) return;

        // ✅ single source of truth
        NamespacedKey key = new NamespacedKey(plugin, "custom_id");
        projectile.getPersistentDataContainer().set(
                key,
                PersistentDataType.STRING,
                itemId
        );

        // ✅ start CCD tracking
        tracked.put(projectile, projectile.getLocation().clone());
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        Location impact = e.getEntity().getLocation();
        activateBombIfPresent(projectile, impact);
    }

    private String getCustomId(Projectile p) {
        NamespacedKey key = new NamespacedKey(plugin, "custom_id");
        return p.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private void runProjectileScanning() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<Projectile, Location>> it =
                        tracked.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry<Projectile, Location> entry = it.next();
                    Projectile p = entry.getKey();

                    if (!p.isValid() || p.isDead()) {
                        it.remove();
                        continue;
                    }

                    Location from = entry.getValue();
                    Location to = p.getLocation();

                    checkTunneling(p, from, to);

                    entry.setValue(to.clone());
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private void checkTunneling(Projectile p, Location from, Location to) {
        Vector delta = to.toVector().subtract(from.toVector());
        double dist = delta.length();

        if (dist < 0.0001) return;

        Vector dir = delta.normalize();
        World world = p.getWorld();

        RayTraceResult ray = world.rayTraceEntities(
                from,
                dir,
                dist,
                e ->
                        e instanceof LivingEntity &&
                                e != p.getShooter() &&
                                !e.isDead()
        );

        if (ray == null || ray.getHitEntity() == null) return;

        Location impact = ray.getHitPosition().toLocation(p.getWorld());

        activateBombIfPresent(p, impact);
        p.remove();
    }

    private void activateBombIfPresent(Projectile projectile, Location impact) {
        String id = getCustomId(projectile);
        if (id == null) return;

//        Bukkit.broadcastMessage("[BombCCD] ACTIVATED via " +
//                (projectile.isDead() ? "EVENT" : "CCD") +
//                " id=" + id +
//                " speed=" + projectile.getVelocity().length());

        Player shooter = null;
        if (projectile.getShooter() instanceof Player player) {
            shooter = player;
        }

        if (id.equals(mudBombId)) {
            activateMudBomb(impact);
        }
        else if (id.equals(vacuumBombId) && shooter != null) {
            activateVacuumBomb(impact, shooter);
        }
    }

    private void activateMudBomb(Location location) {
        World world = location.getWorld();

        Game game = gameManager.getGame(world);
        if (game == null) return;
        GameMap map = game.getMap();

        location.getWorld().playSound(location, Sound.BLOCK_MUD_PLACE, 3, 0.4f);
        playSound(Sound.BLOCK_MUD_PLACE, 0.2f, 0.4f, game.getPlayers());

        int radius = 2;
        // Loop through a cubic region, but only place blocks within a spherical radius
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = location.clone().add(x, y, z);

                    // Check if the location is within the spherical radius
                    if (loc.distance(location) <= radius) {
                        // Only place the block if the current block is air
                        if (map.canPlace(loc.getBlock())
                                && isReplaceable(loc.getBlock())) {

                            world.getBlockAt(loc).setType(Material.MUD);

                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                world.spawnParticle(Particle.BLOCK,
                                        loc.clone().add(0.3, 0.3, 0.3),
                                        20, 0.5, 0.5, 0.5, 0.1,
                                        Bukkit.createBlockData(Material.MUD)
                                );
                            }, 1);

                            map.addBlock(loc.getBlock());
                        }
                    }
                }
            }
        }
    }

    private void activateVacuumBomb(Location location, Player shooter) {
        World world = location.getWorld();

        Game game = gameManager.getGame(world);
        if (game == null) return;
        GameMap map = game.getMap();

        final int durationTicks = 35;
        final double radius = 5.0;
        final double maxPullForce = 0.3; // reasonable force that can be resisted

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick >= durationTicks) {
                    cancel();
                    return;
                }

                double strength = 1;

                for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                    if (entity instanceof Player player) {
                        if (player.getGameMode() == GameMode.SPECTATOR) continue;
                        GameProfile profile = game.getPlayerManager().getProfile(player);
                        if (profile.isProtected()) continue;
                    }
                    if (game.getPlayerManager().areSameTeam(shooter, entity)) continue;
//                    if (p != null && p.getTeam() != null && team != null && team.equals(p.getTeam())) continue;

                    Vector pullDir = location.toVector().subtract(entity.getLocation().toVector());
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
                    Location loc = location.clone().add(x, y, z);

                    // Check if the location is within the spherical radius
                    if (loc.distance(location) <= blockRadius) {
                        // Only place the block if the current block is air
                        if (map.canBrake(loc.getBlock()) && !immuneMaterials.contains(loc.getBlock().getType())) {
                            world.getBlockAt(loc).setType(Material.AIR);
                        }
                    }
                }
            }
        }

        //Particles
        Color color = Color.BLACK;
        spawnHollowSphere(location, radius, color, 2, 300);
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > durationTicks) { // Run for 2 seconds
                    cancel();
                    return;
                }

                location.getWorld().spawnParticle(Particle.PORTAL, location, 20);

                Particle.DustOptions dust = new Particle.DustOptions(color, 1);
                location.getWorld().spawnParticle(Particle.DUST, location, 10, 0.2, 0.2, 0.2, 1, dust);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        int period = 10;

        location.getWorld().playSound(location, Sound.BLOCK_CONDUIT_DEACTIVATE, 0.9f, 1.2f);
        playSound(Sound.BLOCK_CONDUIT_DEACTIVATE, 0.2f, 1.2f, game.getPlayers());

        new BukkitRunnable(){
            int tick = 0;

            @Override
            public void run() {
                if (tick >= durationTicks / period) {
                    cancel();
                    return;
                }

                location.getWorld().playSound(location, Sound.ENTITY_BREEZE_CHARGE, 0.5f, 0.5f);

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
