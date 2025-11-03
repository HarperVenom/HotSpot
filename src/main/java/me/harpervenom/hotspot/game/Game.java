package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.point.PointManager;
import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.game.team.GameTeamManager;
import me.harpervenom.hotspot.queue.GameQueue;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.*;

public class Game {

    private final GameManager gameManager;
    private final GameQueue queue;
    private final GameSettings settings;
//    private final GameDeathHandler deathHandler;

    private GameMap map;
    private BukkitTask gameTask;
    private int elapsedTicks;
    private boolean hasStarted = false;
    private boolean hasEnded = false;

    private final ScoreboardManager scoreboardManager;
    private PointManager pointManager;
    private final ScoreManager scoreManager;
    private final GameTeamManager teamManager;
    private final GamePlayerManager playerManager;

    private BukkitTask endTask;

    public Game(GameManager gameManager, GameQueue queue) {
        this.gameManager = gameManager;
        this.queue = queue;
        this.settings = queue.getSettings();
//        this.deathHandler = new GameDeathHandler(this);

        scoreManager = new ScoreManager(this);
        scoreboardManager = new ScoreboardManager(this);
        teamManager = new GameTeamManager(this);
        playerManager = new GamePlayerManager(this);
    }

    public void setup() {
        pointManager = new PointManager(this);
        pointManager.setup();
        teamManager.createTeams(map, pointManager);
    }

    public void start() {
        hasStarted = true;

        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickSecond, 0L, 1);

        for (GameTeam team : teamManager.getTeams()) {
            team.spawnAll();
        }

        sendActionBarMessage(text(""), getPlayers());
        updateScoreBoardViewers();
        scoreboardManager.update();
    }

    public void end() {
        pointManager.remove();
        gameManager.removeGame(this);
        updateScoreBoardViewers();
    }

    public void connect(Player player) {
        playerManager.connect(player);
    }

    public void disconnect(Player player) {
        playerManager.disconnect(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateScoreBoardViewers();

            if (isEmpty()) {
                if (endTask != null && !endTask.isCancelled()) {
                    endTask.cancel();
                }

                endTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (isEmpty()) {
                        end();
                    }
                }, 5 * 20);
            }
        }, 1);
    }

    public void updateScoreBoardViewers() {
        if (!hasStarted) return;
        if (hasEnded) {
            scoreboardManager.setViewers(new ArrayList<>());
            return;
        }
        scoreboardManager.setViewers(getPlayers());
    }

    private void tickSecond() {
        elapsedTicks++;

        if (elapsedTicks % 20 == 0) {
            scoreManager.updateScores();
            teamManager.checkWinner();

            scoreboardManager.update();
        }
    }

    public void stop() {
        if (hasEnded) return;
        hasEnded = true;

        if (gameTask != null) gameTask.cancel();
    }

    public void announceWinner(GameTeam winner) {
        stop();

        if (winner == null) {
            sendMessage(text("Ничья!"), getPlayers());
        } else {
            sendMessage(text("Победитель - ").append(winner.getName()), getPlayers());
        }
        playSound(Sound.ENTITY_WITHER_SPAWN, 0.5f, 1f, getPlayers());

        Bukkit.getScheduler().runTaskLater(plugin, this::end, 4 * 20);
    }

    public List<Player> getPlayers() {
        return playerManager.getConnectedPlayers();
    }

    public boolean isEmpty() {
        return getPlayers().isEmpty();
    }

    public List<GameTeam> getTeams() {
        return teamManager.getTeams();
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
    public PointManager getPointManager() {
        return pointManager;
    }
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    public GamePlayerManager getPlayerManager() {
        return playerManager;
    }
    public GameTeamManager getTeamManager() {
        return teamManager;
    }
    public ScoreManager getScoreManager() {
        return scoreManager;
    }
    public int getElapsedTicks() {
        return elapsedTicks;
    }
}

