package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.utils.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.formatTime;
import static me.harpervenom.hotspot.utils.Utils.text;

public class Game {

    private final GameManager gameManager;
    private final GameQueue queue;
    private final GameSettings settings;
//    private final List<GameTeam> teams = new ArrayList<>();
//    private final HashMap<UUID, GameProfile> profileMap = new HashMap<>();
//    private final GameDeathHandler deathHandler;

    private GameMap map;
    private BukkitTask gameTask;
    private int elapsedTicks;
    private boolean hasStarted = false;
    private boolean hasEnded = false;

    private final CustomScoreboard customScoreboard;

    private final Team spectators;

    private BukkitTask endTask;

    public Game(GameManager gameManager, GameQueue queue) {
        this.gameManager = gameManager;
        this.queue = queue;
        this.settings = queue.getSettings();
//        this.deathHandler = new GameDeathHandler(this);

        customScoreboard = new CustomScoreboard("game", text("Игра"));
        customScoreboard.showHealth();

        spectators = customScoreboard.getScoreboard().registerNewTeam("viewers");
        spectators.color(NamedTextColor.GRAY);
    }

    public void start() {
        hasStarted = true;

        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickSecond, 0L, 1);

        for (Player viewer : getSpectators()) {
            spawnPlayer(viewer);
        }

        updateScoreBoardViewers();
    }

    public void end() {
        if (hasEnded) return;
        hasEnded = true;
        gameManager.removeGame(this);
        if (gameTask != null) gameTask.cancel();
        updateScoreBoardViewers();
    }

    public void connect(Player player) {
        addViewer(player);

        if (hasStarted) {
            spawnPlayer(player);
        }
    }

    public void disconnect(Player player) {
        removeViewer(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateScoreBoardViewers();

            if (isEmpty()) {
                // Cancel old task if it exists
                if (endTask != null && !endTask.isCancelled()) {
                    endTask.cancel();
                }

                // Schedule new task
                endTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (isEmpty()) {
                        end();
                    }
                }, 5 * 20);
            }
        }, 1);
    }

    public void addViewer(Player player) {
        spectators.addPlayer(player);
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        updateScoreBoardViewers();
    }

    public void removeViewer(Player player) {
        spectators.removePlayer(player);

    }

    public void spawnPlayer(Player player) {
        if (spectators.hasPlayer(player)) {
            player.teleport(new Location(map.getWorld(), 0, 10, 0));
        }
    }

    public void updateScoreBoardViewers() {
        if (!hasStarted) return;
        if (hasEnded) {
            customScoreboard.setViewers(new ArrayList<>());
            return;
        }
//        Bukkit.broadcastMessage("3");

        List<Player> players = new ArrayList<>();

//                profileMap.values().stream()
//                .map(GameProfile::getGamePlayer)
//                .map(GamePlayer::getPlayer)
//                .filter(this::isInGame)
//                .filter(Player::isOnline)
//                .collect(Collectors.toList());

        players.addAll(getSpectators());

        customScoreboard.setViewers(players);
    }

    public void updateScoreboard() {
//        List<GameTeam> activeTeams = teams.stream()
//                .filter(team -> !team.isDestroyed())
//                .toList();

        List<Component> lines = new ArrayList<>();
        lines.add(text(""));
        lines.add(text(formatTime(elapsedTicks/20)));
        lines.add(text(""));

//        if (elapsedTicks < virusStartTime) {
//            lines.add(text("Чума: " + formatTime((virusStartTime - elapsedTicks)/20), NamedTextColor.YELLOW));
//        } else {
//            lines.add(text("Чума!", NamedTextColor.RED));
//        }
//        lines.add(text(""));
//
//        lines.add(text("Команд: " + activeTeams.size()));

        customScoreboard.updateLines(lines);
    }

    private void tickSecond() {
        elapsedTicks++;

        if (elapsedTicks % 20 == 0) {
            updateScoreboard();

//            if (elapsedTicks > virusStartTime) {
//                for (GameVillager villager : villagerMap.values()) {
//                    LivingEntity entity = (LivingEntity) villager.getEntity();
//                    if (entity == null || !entity.isValid()) continue;
//
//                    // Check if not already withered
//                    if (!entity.hasPotionEffect(PotionEffectType.WITHER)) {
//                        entity.addPotionEffect(
//                                new PotionEffect(
//                                        PotionEffectType.WITHER,
//                                        Integer.MAX_VALUE, // effectively infinite
//                                        0,                 // amplifier (0 = Wither I)
//                                        false,             // ambient (true makes particles smaller)
//                                        true,             // showParticles
//                                        false              // showIcon
//                                )
//                        );
//                    }
//                }
//            }
        }
    }

    public List<Player> getPlayers() {
        return getSpectators();
    }

    public List<Player> getSpectators() {
        List<Player> players = new ArrayList<>();
        for (String entry : spectators.getEntries()) {
            Player player = Bukkit.getPlayer(entry); // get online player by name
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public boolean isEmpty() {
        if (!spectators.getEntries().isEmpty()) return false;

//        for (GameTeam team : teams) {
//            for (GameProfile gameProfile : team.getProfiles()) {
//                Player player = gameProfile.getGamePlayer().getPlayer();
//                if (player != null && player.isOnline()
//                        && player.getWorld().getUID().equals(map.getWorld().getUID())) {
//                    return false;
//                }
//            }
//        }
        return true;
    }

    public void setMap(GameMap map) {
        this.map = map;
    }
    public GameMap getMap() {
        return map;
    }
    public GameQueue getQueue() {
        return queue;
    }
}

