package me.harpervenom.hotspot.game;

import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.game.team.GameTeamManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class GamePlayerManager {

    private final GameTeamManager teamManager;
    private final Map<UUID, GameProfile> profileMap = new HashMap<>();
    private final Team spectators;

    public GamePlayerManager(Game game) {
        teamManager = game.getTeamManager();

        spectators = game.getScoreboardManager().getScoreboard().registerNewTeam("viewers");
        spectators.color(NamedTextColor.GRAY);
    }

    public void addPlayer(Player player) {
        GameProfile profile = new GameProfile(player);
        profileMap.put(player.getUniqueId(), profile);
        assignToBestTeam(profile);
    }

    private void assignToBestTeam(GameProfile profile) {
        GameTeam smallest = teamManager.getTeams().stream()
                .min(Comparator.comparingInt(GameTeam::size))
                .orElseThrow();
        profile.setTeam(smallest);
        smallest.addProfile(profile);
    }

    public void connect(Player player) {
        GameProfile profile = profileMap.get(player.getUniqueId());
        if (profile != null) {
            profile.setConnected(true);
        } else {
            addPlayer(player);
        }
    }

    public void disconnect(Player player) {
        GameProfile profile = profileMap.get(player.getUniqueId());
        if (profile != null) profile.setConnected(false);
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
        for (GameTeam team : teamManager.getTeams()) {
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
}

