package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.game.team.GameTeamManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static me.harpervenom.hotspot.utils.Utils.*;
import static me.harpervenom.hotspot.utils.glow.GlowController.setGlow;

public class PlayerManager {

    private final Game game;
    private final GameTeamManager gameTeamManager;
    private final Map<UUID, GameProfile> profileMap = new HashMap<>();
    private final List<Player> spectators = new ArrayList<>();
    private final Team spectatorsTeam;

    public PlayerManager(Game game) {
        this.game = game;
        gameTeamManager = game.getTeamManager();

        spectatorsTeam = gameTeamManager.getScoreboard().getScoreboard().registerNewTeam("viewers");
        spectatorsTeam.color(NamedTextColor.GRAY);
    }

    public GameProfile createProfile(Player player) {
        return createProfile(player, null);
    }

    public GameProfile createProfile(Player player, GameTeam team) {
        GameProfile profile = new GameProfile(player);
        profileMap.put(player.getUniqueId(), profile);
        if (team == null) {
            assignToBestTeam(profile);
        } else {
            profile.setTeam(team);
            team.addProfile(profile);
            gameTeamManager.getScoreboard().addPlayer(player, team.getId());
        }
        return profile;
    }

    private void assignToBestTeam(GameProfile profile) {
        GameTeam smallest = gameTeamManager.getTeams().stream()
                .min(Comparator.comparingInt(GameTeam::size))
                .orElseThrow();
        profile.setTeam(smallest);
        smallest.addProfile(profile);

        gameTeamManager.getScoreboard().addPlayer(profile.getPlayer(), smallest.getId());
    }

    public boolean canConnect(Player player) {
        GameProfile profile = profileMap.get(player.getUniqueId());
        if (profile == null) {
            if (game.hasStarted() && !game.getSettings().isCanJoinMidGame()) return false;
            return profileMap.size() < game.getSettings().getMaxPlayers();
        }
        return true;
    }

    public boolean connect(Player player) {
        GameProfile profile = profileMap.get(player.getUniqueId());
        if (profile != null) {
            profile.setConnected(true);
            profile.reset();
            getTeam(player).connect(player);
            if (game.hasStarted()) {
                sendMessage(profile.getName().append(text(" вернулся в игру", NamedTextColor.GRAY)), game.getPlayers());
                playSound(Sound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 1f, getConnectedPlayers());
            }
        } else {
            if (game.hasStarted() && !game.getSettings().isCanJoinMidGame()) return false;
            if (profileMap.size() >= game.getSettings().getMaxPlayers()) return false;
            profile = createProfile(player);
            if (game.hasStarted()) {
                sendMessage( text(player.getName()).append(text(" присоединился к команде ", NamedTextColor.GRAY)
                        .append(profile.getTeam().getName())), getConnectedPlayers());
                playSound(Sound.BLOCK_NOTE_BLOCK_BIT, 0.5f, 1f, getConnectedPlayers());
            }
        }

        game.updateScoreBoardViewers();

        if (game.hasStarted()) {
            profile.getTeam().spawnFirstTime(player);
        }

        updateGlows();

        game.getUiManager().refreshBarViewers();
        return true;
    }

    public void connectSpectator(Player player) {
        spectators.add(player);
        spectatorsTeam.addPlayer(player);
        player.setScoreboard(gameTeamManager.getScoreboard().getScoreboard());
        player.teleport(game.getMap().getWorld().getBlockAt(0, 10, 0).getLocation());
        player.setGameMode(GameMode.SPECTATOR);
        game.updateScoreBoardViewers();
        game.getUiManager().refreshBarViewers();
        updateGlows();

        sendMessage(text(player.getName() + " наблюдает", NamedTextColor.GRAY), game.getPlayers());
    }

    public void disconnect(Player player) {
        disconnect(player, false);
    }

    public void disconnect(Player player, boolean silent) {
        if (spectators.contains(player)) {
            spectators.remove(player);
            spectatorsTeam.removePlayer(player);
            if (!silent) sendMessage(text(player.getName() + " больше не наблюдает", NamedTextColor.GRAY), game.getPlayers());
        } else {
            GameProfile profile = profileMap.get(player.getUniqueId());
            if (profile != null) {
                profile.setConnected(false);

                getTeam(player).disconnect(player);
                if (!silent) {
                    sendMessage(profile.getName().append(text(" покинул игру", NamedTextColor.GRAY)), game.getPlayers());
                    playSound(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.5f, 1f, getConnectedPlayers());
                }
            }
        }

        clearGlow(player);

        gameTeamManager.getScoreboard().removePlayer(player);
        game.getUiManager().refreshBarViewers();
    }

    public void updateGlows() {
        for (GameProfile gameProfile : profileMap.values()) {
            updateGlow(gameProfile);
        }
    }

    public void updateGlow(GameProfile profile) {
        GameTeam team = profile.getTeam();
        List<Player> viewers = new ArrayList<>();
        if (team != null) {
            viewers.addAll(team.getConnectedPlayers());
            viewers.addAll(getSpectators());
        }
        setGlow(profile.getPlayer(), viewers);
    }

    public void clearGlow(Player player) {
        setGlow(player, new ArrayList<>());
    }

    public GameProfile getProfile(Player player) {
        if (isSpectator(player)) return null;
        return profileMap.get(player.getUniqueId());
    }

    public GameTeam getTeam(Player player) {
        if (spectators.contains(player)) return null;
        GameProfile profile = profileMap.get(player.getUniqueId());
        return profile != null ? profile.getTeam() : null;
    }

    public List<Player> getConnectedPlayers() {
        List<Player> players = new ArrayList<>();
        for (GameTeam team : gameTeamManager.getTeams()) {
            players.addAll(team.getProfiles().stream()
                    .filter(GameProfile::isConnected)
                    .map(GameProfile::getPlayer).toList());
        }
        players.addAll(getSpectators());
        return players;
    }

    public List<Player> getSpectators() {
        return spectators;
    }

    public boolean isSpectator(Player player) {
        return getSpectators().contains(player);
    }

    public Map<UUID, GameProfile> getProfileMap() {
        return profileMap;
    }

    public boolean areSameTeam(Entity entity1, Entity entity2) {
        if (entity1 == null || entity2 == null) return false;

        if (!(entity1 instanceof Player player1) || !(entity2 instanceof Player player2)) return false;
        GameTeam team1 = getTeam(player1);
        GameTeam team2 = getTeam(player2);
        return team1 != null && team1.equals(team2);
    }
}

