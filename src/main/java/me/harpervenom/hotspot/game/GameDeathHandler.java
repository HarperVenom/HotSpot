package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.game.team.GameTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.listeners.ArmorListener.activateExplosionChest;
import static me.harpervenom.hotspot.utils.Utils.*;

public class GameDeathHandler {

    private final Game game;

    public GameDeathHandler(Game game) {
        this.game = game;
        startProtectionParticles();
    }

    public void handlePlayerDeath(PlayerDeathEvent e, Player player) {
        GameProfile gameProfile = game.getPlayerManager().getProfile(player);
        handleDeath(e, gameProfile);

        gameProfile.getStats().addDeath();

        Component victimName = gameProfile.getName();

        GameProfile killerProfile = game.getDamageManager().getLastDamager(player);
        Component deathMessage;
        if (killerProfile != null) {
            killerProfile.getStats().addKill();

            int bounty = gameProfile.getStats().getBounty();
            gameProfile.getStats().resetBounty();

            killerProfile.getEconomyManager().addBalance(bounty);

            deathMessage = victimName.append(text(" был убит ", NamedTextColor.GRAY)).append(killerProfile.getName());
            if (bounty >= 10) {
                deathMessage = deathMessage.append(text(" (Награда: " + bounty + ")", NamedTextColor.GOLD));
            }

            if (bounty > 0) {
                Player killer = killerProfile.getPlayer();
                killer.sendActionBar(text("+" + bounty, NamedTextColor.GOLD));
                killer.playSound(killer, Sound.BLOCK_CHAIN_BREAK, 1, 0.8f);
            }
        } else {
            deathMessage = victimName.append(text(" погиб", NamedTextColor.GRAY));
        }
        sendMessage(deathMessage, game.getPlayers());

        String logMessage = PlainTextComponentSerializer.plainText().serialize(deathMessage);
        plugin.getLogger().info(logMessage);
    }

    public void handleDeath(PlayerDeathEvent e, GameProfile gameProfile) {
        if (gameProfile == null) return;

        Player player = gameProfile.getPlayer();

        activateExplosionChest(player);

        handleDeathState(e, player);

        killPlayer(gameProfile);
        playDeathEffects(player);
    }

    private void handleDeathState(PlayerDeathEvent e, Player player) {
        e.setKeepInventory(true); // keep inventory, we’ll remove items manually
        e.setShouldDropExperience(false); // optional
        e.setCancelled(true);
        player.setVelocity(new Vector(0, 0, 0));

        player.setItemOnCursor(null);
        player.getInventory().clear();
    }

    private void killPlayer(GameProfile gameProfile) {
        Player player = gameProfile.getPlayer();

        player.clearActivePotionEffects();
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);

        new BukkitRunnable() {
            int seconds = 3;

            @Override
            public void run() {
                if (!gameProfile.isConnected()) {
                    cancel();
                    player.sendActionBar(text(""));
                    return;
                }
                if (seconds == 0) {
                    cancel();
                    player.sendActionBar(text(""));
                    respawn(gameProfile);
                    return;
                }
                player.sendActionBar(text("Респавн через " + seconds));
                seconds--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void respawn(GameProfile profile) {
        Player player = profile.getPlayer();

        if (player.getSpectatorTarget() != null) {
            player.setSpectatorTarget(null);
        }

        profile.reset();
        addProtection(profile, 15 * 20);

        GameTeam gameTeam = profile.getTeam();
        gameTeam.spawn(player);
    }

    private void addProtection(GameProfile profile, int duration) {
        profile.setProtected(true);

        Player player = profile.getPlayer();

        AttributeInstance attr = player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (attr != null) {
            attr.setBaseValue(0.99);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.getWorld().playSound(player.getLocation(), Sound.EVENT_MOB_EFFECT_RAID_OMEN, 1, 1.5f);

            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, duration, 2, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, 0, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, duration, 1, false, true));
        }, 1);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeProtection(profile);
        }, duration);
    }

    public static void removeProtection(GameProfile profile) {
        profile.setProtected(false);

        Player player = profile.getPlayer();

        player.getAttribute(Attribute.KNOCKBACK_RESISTANCE).setBaseValue(
                player.getAttribute(Attribute.KNOCKBACK_RESISTANCE).getDefaultValue());
    }

    private void playDeathEffects(Player player) {
        GameTeam team = game.getPlayerManager().getTeam(player);
        if (team == null) return;

        Location loc = player.getLocation().clone().add(0, 1, 0);
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions dust = new Particle.DustOptions(toBukkitColor(team.getColor()), 3);
        world.spawnParticle(Particle.DUST, loc, 12, 0.3, 0.5, 0.3, 0, dust, true);
        world.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 0.8f);
//        player.playSound(player, Sound.ENTITY_SHULKER_DEATH, 0.6f, 1.2f);

        playSound(Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.2f, 0.8f, game.getPlayers());

        player.showTitle(Title.title(text("СМЭРТЬ", NamedTextColor.RED), text(""), Title.Times.times(
                Duration.ofSeconds(0),   // fade-in
                Duration.ofSeconds(1),    // stay
                Duration.ofSeconds(1)    // fade-out
        )));
    }

    private int particleTaskId = -1;

    public void startProtectionParticles() {
        if (particleTaskId != -1) return; // Already running

        particleTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (GameProfile profile : game.getPlayerManager().getProfileMap().values()) {
                if (!profile.isProtected()) continue;
                Player player = profile.getPlayer();
                if (player != null && player.isOnline()) {
                    player.getWorld().spawnParticle(
                            Particle.RAID_OMEN,
                            player.getLocation().clone().add(0, 1, 0),
                            5,
                            0.2, 0.5, 0.2
                    );
                }
            }
        }, 0L, 5L);
    }

    public void remove() {
        Bukkit.getScheduler().cancelTask(particleTaskId);
    }
}

