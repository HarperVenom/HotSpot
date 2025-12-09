package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.listeners.ExplosionListener.createExplosion;
import static me.harpervenom.hotspot.game.vault.loot.CustomItems.*;
import static me.harpervenom.hotspot.utils.Utils.*;
import org.bukkit.util.Vector;

public class ArmorListener implements Listener {

    private final GameManager gameManager;

    private final Set<UUID> cooldowns = new HashSet<>();// Make sure this matches your item ID

    public ArmorListener(GameManager gameManager) {
        this.gameManager = gameManager;
        startTankPlateChecker();
    }

    @EventHandler
    public void onGoldenPlateActivate(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (!hasItemId(chestplate, sunPlateId)) return;

        UUID playerId = player.getUniqueId();
        if (cooldowns.contains(playerId)) return;

        double finalDamage = e.getFinalDamage();
        if (finalDamage < 2.0 && finalDamage < player.getHealth()) return;

        double absorption = sunPlateAbsorption;
        double damage = e.getFinalDamage();

        if (damage >= absorption) {
            e.setDamage(damage - absorption);
        } else {
            e.setDamage(0);
            absorption -= damage;
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 0, false, true));
            player.setAbsorptionAmount(absorption);
        }

        cooldowns.add(playerId);

        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        world.playSound(playerLoc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.6f, 1f);
        world.spawnParticle(Particle.END_ROD, playerLoc.clone().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.01);

        player.setCooldown(chestplate.getType(), sunPlateCooldown * 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cooldowns.remove(playerId);
        }, sunPlateCooldown * 20L);
    }

    @EventHandler
    public void onIronPlateActivate(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (!hasItemId(chestplate, ironPlateId)) return;

        UUID playerId = player.getUniqueId();
        if (cooldowns.contains(playerId)) return;

        Entity causingEntity = e.getDamageSource().getCausingEntity();
        if (!(causingEntity instanceof Player damager)) return;

        damager.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 20 * ironPlateDurationNegative, 0, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20 * ironPlateDuration, 1, false, true));

        cooldowns.add(playerId);

        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        world.playSound(playerLoc, Sound.PARTICLE_SOUL_ESCAPE, 2f, 1f);
        world.spawnParticle(Particle.SOUL, playerLoc.clone().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.01);

        player.setCooldown(chestplate.getType(), ironPlateCooldown * 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cooldowns.remove(playerId);
        }, ironPlateCooldown * 20L);
    }

    @EventHandler
    public void onDiamondPlateActivate(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (!hasItemId(chestplate, diamondPlateId)) return;

        UUID playerId = player.getUniqueId();
        if (cooldowns.contains(playerId)) return;

        Entity causingEntity = e.getDamageSource().getCausingEntity();
        if (!(causingEntity instanceof Player damager)) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * diamondPlateDuration, 2, false, true));

        cooldowns.add(playerId);

        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        world.playSound(playerLoc, Sound.ENTITY_ALLAY_ITEM_GIVEN, 2f, 1f);
        world.spawnParticle(Particle.POOF, playerLoc.clone().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.01);

        player.setCooldown(chestplate.getType(), diamondPlateCooldown * 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cooldowns.remove(playerId);
        }, diamondPlateCooldown * 20L);
    }

    @EventHandler
    public void onChainPlateActivate(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (!hasItemId(chestplate, chainPlateId)) return;

        UUID playerId = player.getUniqueId();
        if (cooldowns.contains(playerId)) return;

        Entity causingEntity = e.getDamageSource().getCausingEntity();
        if (!(causingEntity instanceof Player damager)) return;

        Game game = gameManager.getGame(player.getWorld());
        if (game == null) return;

        game.getDamageManager().assignLastDamager(damager, player);
        damager.setFireTicks(20 * chainPlateDuration);

        cooldowns.add(playerId);

        World world = player.getWorld();
        Location playerLoc = player.getLocation();
        world.playSound(playerLoc, Sound.ENTITY_BLAZE_HURT, 0.8f, 1f);
        world.spawnParticle(Particle.FLAME, playerLoc.clone().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.01);

        player.setCooldown(chestplate.getType(), chainPlateCooldown * 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cooldowns.remove(playerId);
        }, chainPlateCooldown * 20L);
    }

    @EventHandler
    public void onDamageReceived(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        boolean hasTank = hasItemId(chestplate, tankPlateId);
        if (!hasTank) return;

        if (e.getFinalDamage() < player.getHealth()) return;

        if (player.getHealth() < 6) return;

        double desiredFinal = player.getHealth() - 1;
        double reductionRatio = e.getFinalDamage() / e.getDamage();
        double newInitial = desiredFinal / reductionRatio;
        e.setDamage(newInitial);
    }

    private static final NamespacedKey DAMAGE_KEY = new NamespacedKey("yourplugin", "explosion_damage");

    @EventHandler
    public void onExplosionPlateActivate(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player player)) return;

        ItemStack chestplate = player.getInventory().getChestplate();
        if (!hasItemId(chestplate, explosionPlateId)) return;
        if (chestplate.getType() == Material.AIR) return;

        double newDamage = Math.min(20, e.getDamage());

        // Access item meta + PersistentDataContainer
        ItemMeta meta = chestplate.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        double storedDamage = data.getOrDefault(DAMAGE_KEY, PersistentDataType.DOUBLE, 0.0);
        storedDamage += newDamage;

        // Save it back
        data.set(DAMAGE_KEY, PersistentDataType.DOUBLE, storedDamage);
        chestplate.setItemMeta(meta);

        setCustomLore(chestplate, List.of(
                text("Взрывается при смерти"),
                text("Сила взрыва зависит от полученного урона"),
                text("Заряд: " + getPlatePower(storedDamage), NamedTextColor.RED)
                ));

        player.sendActionBar(text("Заряд: " + getPlatePower(storedDamage), NamedTextColor.RED));

        // Optional effects
        World world = player.getWorld();
        Location loc = player.getLocation();
        world.playSound(loc, Sound.ENTITY_TNT_PRIMED, 0.5f, 1.5f);
        world.spawnParticle(Particle.SMOKE, loc.clone().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.01);
    }

    public static void activateExplosionChest(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() == Material.AIR) return;
        if (!hasItemId(chestplate, explosionPlateId)) return;

        ItemMeta meta = chestplate.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        double totalDamage = data.getOrDefault(DAMAGE_KEY, PersistentDataType.DOUBLE, 0.0);
        if (totalDamage <= 0) return;

        float power = getPlatePower(totalDamage);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            createExplosion(player, power);
            spawnExplosionParticles(player, power * 1.2f);
            player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1, 0.8f);

            // Reset stored damage
            data.set(DAMAGE_KEY, PersistentDataType.DOUBLE, 0.0);
            chestplate.setItemMeta(meta);
        }, 1);
    }

    public static void spawnExplosionParticles(Player player, float explosionRadius) {
        spawnBeautifulExplosion(player.getEyeLocation(), explosionRadius);
    }

    private static float getPlatePower(double damage) {
        double value = Math.min(10, Math.max(2.0 + damage / 30.0, 2.0));
        return (float) Math.round(value * 100) / 100f;
    }

    private final Set<UUID> wearingTankPlate = new HashSet<>();
    private final Set<UUID> lastTimeWearingTankPlate = new HashSet<>();
    private final Set<UUID> wearingSurvivorJacketPlate = new HashSet<>();

    public void startTankPlateChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack chestplate = player.getInventory().getChestplate();

                    UUID uuid = player.getUniqueId();

                    boolean hasTank = hasItemId(chestplate, tankPlateId);
                    boolean hasAnother = hasItemId(chestplate, survivorJacketId);

                    boolean markedTank = wearingTankPlate.contains(uuid);
                    boolean markedAnother = wearingSurvivorJacketPlate.contains(uuid);

                    // Tank Plate
                    if (hasTank) {
                        lastTimeWearingTankPlate.add(uuid);
                        wearingTankPlate.add(uuid);
                        applyTankPlateEffects(player);
                    } else {
                        if (lastTimeWearingTankPlate.contains(uuid)) {
                            lastTimeWearingTankPlate.remove(uuid);
                            wearingTankPlate.remove(uuid);
                            removeTankPlateEffects(player);
                        }
                    }

                    // Another Plate
                    if (hasAnother && !markedAnother) {
                        wearingSurvivorJacketPlate.add(uuid);
                        applySurvivorJacketEffects(player);
                    } else if (!hasAnother && markedAnother) {
                        wearingSurvivorJacketPlate.remove(uuid);
                        removeSurvivorJacketEffects(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    private void applyTankPlateEffects(Player player) {
        double health = player.getHealth();

        boolean increased = health < 8;

        removeTankPlateEffects(player);

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE,
                PotionEffect.INFINITE_DURATION, increased ? 3 : 1, true, false, true));

        if (increased) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                    PotionEffect.INFINITE_DURATION, 2, true, false, true));

            World world = player.getWorld();
            Location playerLoc = player.getLocation();
            world.spawnParticle(Particle.SMOKE, playerLoc.clone().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.01);
        }
    }
    private void removeTankPlateEffects(Player player) {
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }

    private void applySurvivorJacketEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,
                PotionEffect.INFINITE_DURATION, 0, true, false, true));
    }
    private void removeSurvivorJacketEffects(Player player) {
        player.removePotionEffect(PotionEffectType.REGENERATION);
    }

    public static boolean isWearingTankPlate(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        return hasItemId(chestplate, tankPlateId);
    }

    public static void spawnBeautifulExplosion(Location center, double radius) {
        spawnExplosionFlash(center);
        spawnExplosionDebris(center, radius * 0.25);
        spawnShockwave(center, radius, 2);
        spawnExplosionSmoke(center, radius * 0.6);
    }

    private static void spawnExplosionFlash(Location center) {
        World w = center.getWorld();
        if (w == null) return;

        // White flash
        w.spawnParticle(
                Particle.FLASH,
                center,
                2,
                Color.WHITE
        );

        // Bright core
        w.spawnParticle(
                Particle.DUST,
                center,
                20,
                0.1, 0.1, 0.1,
                new Particle.DustOptions(Color.fromRGB(255, 230, 180), 2.5f)
        );
    }

    public static void spawnShockwave(Location center, double maxRadius, int durationTicks) {
        World w = center.getWorld();
        if (w == null) return;

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick > durationTicks) {
                    cancel();
                    return;
                }

                double radius = maxRadius * (tick / (double) durationTicks);
                int points = (int) (radius * 80);

                for (int i = 0; i < points; i++) {
                    double theta = Math.acos(2 * Math.random() - 1);
                    double phi = 2 * Math.PI * Math.random();

                    double x = radius * Math.sin(theta) * Math.cos(phi);
                    double y = radius * Math.cos(theta);
                    double z = radius * Math.sin(theta) * Math.sin(phi);

                    Location loc = center.clone().add(x, y, z);

                    w.spawnParticle(
                            Particle.DUST,
                            loc,
                            1,
                            0, 0, 0,
                            0,
                            new Particle.DustOptions(Color.fromRGB(255, 120, 40), 3),
                            true // ✅ FORCE VISIBILITY
                    );
                }

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void spawnExplosionDebris(Location center, double force) {
        World w = center.getWorld();
        if (w == null) return;

        for (int i = 0; i < 120; i++) {
            Vector dir = randomUnitVector().multiply(force * (0.4 + Math.random()));

            w.spawnParticle(
                    Particle.CRIT,
                    center,
                    1,
                    dir.getX(),
                    dir.getY(),
                    dir.getZ(),
                    0.15
            );
        }
    }

    private static Vector randomUnitVector() {
        double theta = Math.acos(2 * Math.random() - 1);
        double phi = 2 * Math.PI * Math.random();

        return new Vector(
                Math.sin(theta) * Math.cos(phi),
                Math.cos(theta),
                Math.sin(theta) * Math.sin(phi)
        );
    }

    private static void spawnExplosionSmoke(Location center, double radius) {
        World w = center.getWorld();
        if (w == null) return;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks++ > 15) {
                    cancel();
                    return;
                }

                w.spawnParticle(
                        Particle.SMOKE,
                        center,
                        25,
                        radius, radius * 0.7, radius,
                        0.02
                );
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }
}
