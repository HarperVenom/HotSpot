package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.profile.GameProfile;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.listeners.ExplosionListener.isInteractable;
import static me.harpervenom.hotspot.utils.Utils.text;

public class PickaxeListener implements Listener {

    private final int seconds = 6;
    private static final Map<UUID, Location> teleportOrigins = new HashMap<>();
    private static final Map<UUID, BukkitTask> teleportingPlayers = new HashMap<>();

    static {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<UUID, Location>> iterator = teleportOrigins.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, Location> entry = iterator.next();
                UUID uuid = entry.getKey();
                Player player = Bukkit.getPlayer(uuid);

                // Quick exit if player no longer exists
                if (player == null || !player.isOnline() || !player.isValid()) {
                    iterator.remove();
                    continue;
                }

                try {
                    Location currentLoc = player.getLocation();
                    Location origin = entry.getValue();

                    // Extra safety: check world still loaded (rare but possible)
                    if (currentLoc.getWorld() == null) {
                        iterator.remove();
                        continue;
                    }

                    if (currentLoc.getWorld().equals(origin.getWorld())
                            && currentLoc.distanceSquared(origin) > 0.1) {

                        cancelTeleportMessage(player);
                        iterator.remove();

                        BukkitTask task = teleportingPlayers.get(uuid);
                        if (task != null) {
                            task.cancel();
                        }
                        teleportingPlayers.remove(uuid);
                    } else {
                        // Only show particles if still in origin location
                        showParticles(player.getLocation(), player.getWorld());
                    }

                } catch (IllegalStateException e) {
                    iterator.remove();
                    // plugin.getLogger().fine("Player " + uuid + " invalid during teleport check");
                } catch (Exception e) {
                    plugin.getLogger().warning("Unexpected error in teleport check for " + uuid + ": " + e.getMessage());
                    iterator.remove();
                }
            }
        }, 0L, 5L);
    }

    private final GameManager gameManager;

    public PickaxeListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        UUID id = player.getUniqueId();

        if (teleportingPlayers.containsKey(id)) {
            cancelTeleportMessage(player);
            teleportingPlayers.get(id).cancel();
            teleportingPlayers.remove(id);
            teleportOrigins.remove(id);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        UUID id = player.getUniqueId();
        if (startTeleporting.containsKey(id)) {
            startTeleporting.get(id).cancel();
            startTeleporting.remove(id);
        }

        if (teleportingPlayers.containsKey(id)) {
            cancelTeleportMessage(player);
            teleportingPlayers.get(id).cancel();
            teleportingPlayers.remove(id);
            teleportOrigins.remove(id);
        }
    }

    private final HashMap<UUID, BukkitTask> startTeleporting = new HashMap<>();

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (!player.isSneaking()) return;
        if (item == null || !(item.getType().toString().endsWith("_PICKAXE"))) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() != null && isInteractable(e.getClickedBlock().getType())) return;

        // Already teleporting?
        if (teleportingPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            startTeleportProcess(player);
        }, 1);

        startTeleporting.put(player.getUniqueId(), task);
    }

    private static void cancelTeleportMessage(Player player) {
        player.sendActionBar(text("Телепортация отменена.", NamedTextColor.RED));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1, 1);
    }

    private void startTeleportProcess(Player player) {
        Game game = gameManager.getGame(player.getWorld());
        if (game == null) return;
        GameProfile profile = game.getPlayerManager().getProfile(player);
        if (profile == null) return;

        UUID uuid = player.getUniqueId();

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1, 1);

        player.sendActionBar(text("Телепортация на базу через " + seconds + "... Не двигайся", NamedTextColor.AQUA));

        showParticles(player.getLocation(), player.getWorld());

        BukkitTask task = new BukkitRunnable() {
            int timer = seconds - 1;

            @Override
            public void run() {
                if (profile.getTeam() == null || !teleportingPlayers.containsKey(player.getUniqueId())) {
                    if (teleportingPlayers.containsKey(uuid)) {
                        teleportingPlayers.get(uuid).cancel();
                        teleportingPlayers.remove(player.getUniqueId());
                    }

                    teleportOrigins.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                if (!teleportOrigins.containsKey(player.getUniqueId())) {
                    teleportOrigins.put(uuid, player.getLocation().clone());
                }

                // Player moved
                Location current = player.getLocation();
                Location original = teleportOrigins.get(uuid);

                if (!current.getWorld().equals(original.getWorld())) {
                    teleportingPlayers.get(uuid).cancel();
                    teleportingPlayers.remove(uuid);
                    teleportOrigins.remove(uuid);
                    cancel();
                    return;
                }

                if (current.distanceSquared(original) > 0.1) {
                    cancelTeleportMessage(player);
                    teleportingPlayers.get(uuid).cancel();
                    teleportingPlayers.remove(player.getUniqueId());
                    teleportOrigins.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                // Countdown
                if (timer > 0) {
                    player.sendActionBar(text("Телепортация на базу через " + timer + "... Не двигайся", NamedTextColor.AQUA));
                    timer--;
                } else {
                    // Teleport to fixed location
                    profile.getTeam().teleportSpawn(player);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.8f);
                    player.getWorld().playSound(teleportOrigins.get(uuid), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.8f);
                    teleportingPlayers.get(uuid).cancel();
                    teleportingPlayers.remove(uuid);
                    teleportOrigins.remove(uuid);
                    cancel();

                    player.sendActionBar(text(""));
                }
            }
        }.runTaskTimer(plugin, 5L, 20L); // Run every second

        teleportingPlayers.put(uuid, task);
    }

    private static void showParticles(Location loc, World world) {
        int count = 20;        // how many particles
        double radius = 0.3;
        double height = 0.5;

        Particle.DustOptions purple = new Particle.DustOptions(
                org.bukkit.Color.fromRGB(150, 0, 255), // purple
                1.4f // size
        );

        world.spawnParticle(Particle.DUST, loc.clone().add(0, 0.5, 0), count, radius, height, radius, purple);
    }
}
