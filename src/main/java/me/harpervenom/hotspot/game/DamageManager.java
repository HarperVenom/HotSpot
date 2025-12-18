package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.profile.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.listeners.ArmorListener.isWearingTankPlate;
import static me.harpervenom.hotspot.game.vault.loot.CustomItems.damageReduction;

public class DamageManager {

    private final Game game;

    private final Map<UUID, BukkitTask> lastDamagerTasks = new HashMap<>();
    private final Map<UUID, GameProfile> lastDamager = new HashMap<>(); // <victim, damager>

    public DamageManager(Game game) {
        this.game = game;
    }

    public void handleHandDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player damager)) return;

        double damage = e.getFinalDamage();
        if (isWearingTankPlate(damager)) {
            damage -= damageReduction * damage;
            e.setDamage(damage);
        }

        GameProfile damagerProfile = game.getPlayerManager().getProfile(damager);
        if (damagerProfile == null) return;

        if (!e.isCancelled()) {
            assignLastDamager(e.getEntity(), damagerProfile);
        }
    }

    public void handleProjectileDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof LivingEntity entity)) return;
        if (!(e.getDamager() instanceof Projectile projectile)) return;
        if (!(projectile.getShooter() instanceof Player shooter)) return;

        double damage = e.getFinalDamage();

        if (isWearingTankPlate(shooter)) {
            damage -= damageReduction * damage;
            e.setDamage(damage);

            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 10, true, false, true));
            entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_BELL_USE, 0.5f, 1.3f);

            shooter.playSound(shooter, Sound.BLOCK_BELL_USE, 0.5f, 1.3f);
        }

        GameProfile shooterProfile = game.getPlayerManager().getProfile(shooter);
        if (shooterProfile == null) return;

        if (entity instanceof Player victim) {
            GameProfile victimProfile = game.getPlayerManager().getProfile(victim);
            if (shooterProfile.equals(victimProfile)) return;
        }

        if (!e.isCancelled()) {
            assignLastDamager(entity, shooterProfile);
        }
    }

    public void handleExplosionDamage(EntityDamageByEntityEvent e, Player exploder) {
        GameProfile exploderProfile = game.getPlayerManager().getProfile(exploder);
        if (exploderProfile == null) return;

        if (!exploder.equals(e.getEntity()) && game.getPlayerManager().areSameTeam(exploder.getPlayer(), e.getEntity())) {
            e.setCancelled(true);
            return;
        }

        // === CANCEL if entity has a teammate rider ===
        for (Entity passenger : e.getEntity().getPassengers()) {
            if (passenger instanceof Player rider) {
                if (game.getPlayerManager().areSameTeam(exploder.getPlayer(), rider)) {
                    e.setCancelled(true);
                }
            }
        }

        Entity entity = e.getEntity();

        if (!e.isCancelled()) {
            assignLastDamager(entity, exploderProfile);
        }
    }

    public void processDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof LivingEntity entity)) return;

        if (game.hasEnded()) {
            e.setCancelled(true);
            return;
        }

        GameProfile damagerProfile = lastDamager.get(entity.getUniqueId());
        if (damagerProfile == null) return;

        double damage = Math.min(entity.getHealth(), e.getFinalDamage());

        if (entity instanceof Player victim) {
            victim.setKiller(damagerProfile.getPlayer());

            GameProfile victimProfile = game.getPlayerManager().getProfile(victim);
            if (victimProfile != null) {
                if (victimProfile.equals(damagerProfile)) return;
                victimProfile.getStats().addTakenDamage(damage);
                victimProfile.getStats().addPreventedDamage(e.getDamage() - damage);
            }
        }

        Player damager = damagerProfile.getPlayer();
        if (isWearingTankPlate(damager)) {
            damage -= damageReduction * damage;
            e.setDamage(damage);
        }

        if (e.getCause() == EntityDamageEvent.DamageCause.KILL) return;

        damagerProfile.getEconomyManager().transferToBalance(damage);
        damagerProfile.getStats().addDealtDamage(damage);
    }

    public void assignLastDamager(Entity entity, Player player) {
        GameProfile profile = game.getPlayerManager().getProfile(player);
        if (profile == null) return;
        assignLastDamager(entity, profile);
    }

    public void assignLastDamager(Entity entity, GameProfile profile) {
        UUID uuid = entity.getUniqueId();

        if (profile.getPlayer().getUniqueId().equals(uuid)) return;

        // Store the attacker
        lastDamager.put(uuid, profile);

        // Cancel and remove any previous scheduled removal
        BukkitTask previousTask = lastDamagerTasks.remove(uuid);
        if (previousTask != null) {
            previousTask.cancel();
        }

        // Schedule a new task to remove the damager after 10 seconds
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            lastDamager.remove(uuid);
            lastDamagerTasks.remove(uuid); // Clean up task entry
        }, 10 * 20); // 10 seconds

        // Store the new task
        lastDamagerTasks.put(uuid, task);
    }

    public GameProfile getLastDamager(Player player) {
        return lastDamager.get(player.getUniqueId());
    }
}
