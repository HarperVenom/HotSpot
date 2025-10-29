package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.utils.CustomScoreboard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

import static me.harpervenom.hotspot.HotSpot.plugin;
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

    private final Team viewers;

    private BukkitTask endTask;

    public Game(GameManager gameManager, GameQueue queue) {
        this.gameManager = gameManager;
        this.queue = queue;
        this.settings = queue.getSettings();
//        this.deathHandler = new GameDeathHandler(this);

        customScoreboard = new CustomScoreboard("game", text("Игра"));
        customScoreboard.showHealth();

        viewers = customScoreboard.getScoreboard().registerNewTeam("viewers");
        viewers.color(NamedTextColor.GRAY);
    }

    public void start() {
        hasStarted = true;

        for (Player viewer : getViewers()) {
            spawnPlayer(viewer);
        }
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
        viewers.addPlayer(player);
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        updateScoreBoardViewers();
    }

    public void removeViewer(Player player) {
        viewers.removePlayer(player);

    }

    public void spawnPlayer(Player player) {
        if (viewers.hasPlayer(player)) {
            player.teleport(new Location(map.getWorld(), 0, 10, 0));
        }
    }

    public void updateScoreBoardViewers() {
        if (!hasStarted) return;
        if (hasEnded) {
            customScoreboard.setViewers(new ArrayList<>());
            return;
        }

        List<Player> players = new ArrayList<>();

//                profileMap.values().stream()
//                .map(GameProfile::getGamePlayer)
//                .map(GamePlayer::getPlayer)
//                .filter(this::isInGame)
//                .filter(Player::isOnline)
//                .collect(Collectors.toList());

        players.addAll(getViewers());

        customScoreboard.setViewers(players);
    }

    public List<Player> getPlayers() {
        return getViewers();
    }

    public List<Player> getViewers() {
        List<Player> players = new ArrayList<>();
        for (String entry : viewers.getEntries()) {
            Player player = Bukkit.getPlayer(entry); // get online player by name
            if (player != null) {
                players.add(player);
            }
        }
        return players;
    }

    public boolean isEmpty() {
        if (!viewers.getEntries().isEmpty()) return false;

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

