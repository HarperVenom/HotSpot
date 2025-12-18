package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.game.profile.GameStats;
import me.harpervenom.hotspot.game.trader.TraderManager;
import me.harpervenom.hotspot.game.vault.VaultManager;
import me.harpervenom.hotspot.game.map.GameMap;
import me.harpervenom.hotspot.game.point.PointManager;
import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.game.team.GameTeamManager;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.players.TeamQueueOrganizer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.game.listeners.TridentListener.clearTridentTasksByGame;
import static me.harpervenom.hotspot.utils.Utils.*;

public class Game {

    private final GameManager gameManager;
    private final GameQueue queue;
    private final GameModeEnum mode;
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
        this.mode = queue.getMode();
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
                connectSpectator(owner);
//             playerManager.connectSpectator(owner);
            }
        }

        for (GameTeam team : teamManager.getTeams()) {
            team.spawnAll();
        }

        getPlayers().forEach(Player::clearTitle);

        pointManager.updateDisplay();

        sendActionBarMessage(text(""), getPlayers());
        updateScoreBoardViewers();
        uiManager.update();

        playSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1, getPlayers());

        plugin.getLogger().info("Game started");
    }

    public void end() {
        clearTridentTasksByGame(this);
        pointManager.remove();
        gameManager.removeGame(this);

        for (Player player : getPlayers()) {
            playerManager.disconnect(player, true);
        }
        updateScoreBoardViewers();
    }

    public boolean canConnect(Player player) {
        return playerManager.canConnect(player);
    }

    public void connect(Player player) {
        connect(player, false);
    }

    public void connectSpectator(Player player) {
        connect(player, true);
    }

    public boolean connect(Player player, boolean isSpectator) {
        boolean success = true;
        if (isSpectator) {
            playerManager.connectSpectator(player);
        } else {
            success = playerManager.connect(player);
        }
        pointManager.updateDisplay();
        if (!success) return false;
        gameManager.updateGames();
        return true;
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
//        if (!hasStarted) return;
        if (hasEnded) {
            uiManager.clear();
            return;
        }
        uiManager.update();
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

    public void remove() {
        if (gameTask != null) gameTask.cancel();
        deathHandler.remove();
        plugin.getLogger().info("Game removed");
    }

    public void announceWinner(GameTeam winner) {
        stop();

        if (winner == null) {
            sendMessage(text("Ничья"), getPlayers());
            sendTitle(text("Ничья"), text(""), getPlayerManager().getConnectedPlayers());
        } else {
            GameTeam loser = getTeamManager().getTeams()
                    .stream()
                    .filter(team -> team != winner)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No loser team found"));

            sendMessage(winner.getName().append(text(" одержали победу")), getPlayers());
            sendTitle(text("Поражение", NamedTextColor.RED), text(""), loser.getConnectedPlayers());
            sendTitle(text("Победа", NamedTextColor.GOLD), text(""), winner.getConnectedPlayers());
            sendTitle(winner.getName(), text("одержали победу"), getPlayerManager().getSpectators());

            winner.getProfiles().forEach(profile -> {
                double currentRank = gameManager.getStatsManager().getStats(profile.getId()).getRank();
                profile.getStats().finalizeMatch(currentRank, true, mode == GameModeEnum.RANKED);
            });
            loser.getProfiles().forEach(profile -> {
                double currentRank = gameManager.getStatsManager().getStats(profile.getId()).getRank();
                profile.getStats().finalizeMatch(currentRank, false, mode == GameModeEnum.RANKED);
            });
        }
        playSound(Sound.ENTITY_WITHER_SPAWN, 0.5f, 1f, getPlayers());

        Bukkit.getScheduler().runTaskLater(plugin, this::end, 5 * 20);

        showStats();
    }

    private void showStats() {
        List<GameProfile> profiles = playerManager.getProfileMap().values().stream().toList();

        StatsLeaderboard.Leaderboards lb =
                StatsLeaderboard.buildLeaderboards(profiles);

        for (GameProfile profile : profiles) {
            Player player = profile.getPlayer();
            if (player == null) continue;
            GameStats stats = profile.getStats();
            player.sendMessage(
                    StatsLeaderboard.buildPersonalStatsMessage(lb, profile)
            );
            if (!settings.isCustom()) {
                player.sendMessage(text("Опыт: +" + stats.getExp()));
                if (mode == GameModeEnum.RANKED) {
                    double rankChange = stats.getRankChange() * 100;
                    player.sendMessage(text("Ранг: " + (rankChange > 0 ? "+" : "") + ((rankChange * 100) / 100)));
                }
            }
        }

        for (Player spectator : playerManager.getSpectators()) {
            spectator.sendMessage(
                    StatsLeaderboard.buildPersonalStatsMessage(lb, null)
            );
        }

        if (!settings.isCustom()) {
            gameManager.getStatsManager().updateProfiles(profiles);
        }

        plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(
                StatsLeaderboard.buildPersonalStatsMessage(lb, null)));
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
    public boolean hasStarted() {
        return hasStarted;
    }
    public boolean hasEnded() {
        return hasEnded;
    }
    public GameModeEnum getMode() {
        return mode;
    }
}

