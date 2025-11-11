package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.game.team.GameTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.*;

public class GameDeathHandler {

    private final Game game;

    public GameDeathHandler(Game game) {
        this.game = game;
    }

    public void handlePlayerDeath(PlayerDeathEvent e, Player player) {
        GameProfile gameProfile = game.getPlayerManager().getProfile(player);
        handleDeath(e, gameProfile);

        Component victimName = gameProfile.getName();

        Component killerName = null;
        Entity entity = e.getDamageSource().getCausingEntity();
        if (entity instanceof Player killer) {
            GameProfile killerProfile = game.getPlayerManager().getProfile(killer);
            if (killerProfile != null) {
                killerName = killerProfile.getName();
            }
        }

        Component deathMessage;
        if (killerName != null) {
            deathMessage = victimName.append(text(" был убит ", NamedTextColor.GRAY)).append(killerName);
        } else {
            deathMessage = victimName.append(text(" погиб", NamedTextColor.GRAY));
        }
        sendMessage(deathMessage, game.getPlayers());
    }

    public void handleDeath(PlayerDeathEvent e, GameProfile gameProfile) {
        if (gameProfile == null) return;

        Player player = gameProfile.getPlayer();

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
                if (gameProfile.isConnected() && seconds == 0) {
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

        GameTeam gameTeam = profile.getTeam();
        gameTeam.spawn(player);
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
}

