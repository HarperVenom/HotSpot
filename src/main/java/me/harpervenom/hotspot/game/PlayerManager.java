package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.game.team.GameTeamManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;

import static me.harpervenom.hotspot.utils.Utils.*;

public class PlayerManager {

    private final Game game;
    private final GameTeamManager gameTeamManager;
    private final Map<UUID, GameProfile> profileMap = new HashMap<>();
    private final Team spectators;

    public PlayerManager(Game game) {
        this.game = game;
        gameTeamManager = game.getTeamManager();

        spectators = game.getUiManager().getScoreboard().registerNewTeam("viewers");
        spectators.color(NamedTextColor.GRAY);
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
        }
        return profile;
    }

    private void assignToBestTeam(GameProfile profile) {
        GameTeam smallest = gameTeamManager.getTeams().stream()
                .min(Comparator.comparingInt(GameTeam::size))
                .orElseThrow();
        profile.setTeam(smallest);
        smallest.addProfile(profile);
    }

    public boolean connect(Player player) {
        GameProfile profile = profileMap.get(player.getUniqueId());
        if (profile != null) {
            profile.getTeam().spawn(player);
            profile.setConnected(true);
            profile.reset();
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
            profile.getTeam().spawn(player);
        }

        game.getUiManager().refreshBarViewers();
        return true;
    }

    public void connectSpectator(Player player) {
        spectators.addPlayer(player);
        player.teleport(game.getMap().getWorld().getBlockAt(0, 10, 0).getLocation());
        player.setGameMode(GameMode.SPECTATOR);
        game.updateScoreBoardViewers();
        game.getUiManager().refreshBarViewers();
    }

    public void disconnect(Player player) {
        GameProfile profile = profileMap.get(player.getUniqueId());
        if (profile != null) {
            profile.setConnected(false);
            sendMessage(profile.getName().append(text(" покинул игру", NamedTextColor.GRAY)), game.getPlayers());
            playSound(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 0.5f, 1f, getConnectedPlayers());
        } else {
            spectators.removePlayer(player);
            sendMessage(text(player.getName() + " больше не наблюдает", NamedTextColor.GRAY), game.getPlayers());
        }

        game.getUiManager().refreshBarViewers();
    }

    public GameProfile getProfile(Player player) {
        return profileMap.get(player.getUniqueId());
    }

    public GameTeam getTeam(Player player) {
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
        List<Player> players = new ArrayList<>();
        for (String entry : spectators.getEntries()) {
            Player player = Bukkit.getPlayer(entry); // get online player by name
            if (player != null) {
                players.add(player);
            }
        }
        return players;
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

