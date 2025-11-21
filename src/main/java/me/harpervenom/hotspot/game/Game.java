package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.trader.TraderManager;
import me.harpervenom.hotspot.game.vault.VaultManager;
import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.point.PointManager;
import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.game.team.GameTeamManager;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.players.TeamQueueOrganizer;
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

    private GameMap map;
    private BukkitTask gameTask;
    private int elapsedTicks;
    private boolean hasStarted = false;
    private boolean hasEnded = false;

    private final UIManager uiManager;
    private PointManager pointManager;
    private VaultManager vaultManager;
    private final ScoreManager scoreManager;
    private final GameTeamManager teamManager;
    private final PlayerManager playerManager;
    private TraderManager traderManager;
    private DamageManager damageManager;

    private final GameDeathHandler deathHandler;

    private BukkitTask endTask;

    private final UUID ownerId;

    public Game(GameManager gameManager, GameQueue queue) {
        this.gameManager = gameManager;
        this.queue = queue;
        this.settings = queue.getSettings();
        this.ownerId = queue.getOwner() == null ? null : queue.getOwner().getUniqueId();

        scoreManager = new ScoreManager(this);
        uiManager = new UIManager(this);
        teamManager = new GameTeamManager(this);
        playerManager = new PlayerManager(this);
        damageManager = new DamageManager(this);

        deathHandler = new GameDeathHandler(this);
    }

    public void setup() {
        pointManager = new PointManager(this);
        pointManager.setup();

        vaultManager = new VaultManager(this);
        vaultManager.setup();

        traderManager = new TraderManager(this);
        traderManager.setup();

        if (queue.getOrganizer() instanceof TeamQueueOrganizer teamQueueOrganizer) {
            teamManager.createTeams(map, pointManager, teamQueueOrganizer.getTeamManager().getTeams());
        } else {
            teamManager.createTeams(map, pointManager, null);
        }
    }

    public void start() {
        hasStarted = true;

        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickSecond, 0L, 1);

        if (ownerId != null) {
            Player owner = Bukkit.getPlayer(ownerId);
            if (owner != null && playerManager.getTeam(owner) == null) {
             playerManager.connectSpectator(owner);
            }
        }

        for (GameTeam team : teamManager.getTeams()) {
            team.spawnAll();
        }

        pointManager.updateDisplay();

        sendActionBarMessage(text(""), getPlayers());
        updateScoreBoardViewers();
        uiManager.update();
    }

    public void end() {
        pointManager.remove();
        gameManager.removeGame(this);

        updateScoreBoardViewers();
    }

    public void connect(Player player) {
        playerManager.connect(player);
        gameManager.updateGames();
        pointManager.updateDisplay();
    }

    public void disconnect(Player player) {
        playerManager.disconnect(player);
        gameManager.updateGames();

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
            uiManager.setViewers(new ArrayList<>());
            uiManager.removeBar();
            return;
        }

        uiManager.setViewers(getPlayers());
    }

    private void tickSecond() {
        elapsedTicks++;

        if (elapsedTicks % 20 == 0) {
            scoreManager.updateScores();
            teamManager.checkWinner();
            vaultManager.update();

            uiManager.update();
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

        Bukkit.getScheduler().runTaskLater(plugin, this::end, 5 * 20);
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
    public VaultManager getVaultManager() {
        return vaultManager;
    }
    public TraderManager getTraderManager() {
        return traderManager;
    }
    public UIManager getUiManager() {
        return uiManager;
    }
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    public GameTeamManager getTeamManager() {
        return teamManager;
    }
    public ScoreManager getScoreManager() {
        return scoreManager;
    }
    public GameDeathHandler getDeathHandler() {
        return deathHandler;
    }
    public DamageManager getDamageManager() {
        return damageManager;
    }
    public int getElapsedTicks() {
        return elapsedTicks;
    }
    public GameSettings getSettings() {
        return settings;
    }
    public boolean hasEnded() {
        return hasEnded;
    }
}

