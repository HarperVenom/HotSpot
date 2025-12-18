package me.harpervenom.hotspot.game.team;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;
import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.game.point.Point;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.text;

public class GameTeam {

    private final String id;
    private final NamedTextColor color;
    private final String name;
    private Team team;

    private final HashMap<UUID, GameProfile> profiles = new HashMap<>();
    private final Location spawn;

    private Point firstPoint;

    private int score = 500;

    public GameTeam(NamedTextColor color, String name, Location spawn) {
        this.id = "team_" + color.toString().toLowerCase();
        this.color = color;
        this.name = name;
        this.spawn = spawn;
    }

//    public void register(Scoreboard scoreboard) {
//        team = scoreboard.registerNewTeam(id + "");
//        team.displayName(getName());
//        team.color(color);
//        team.setAllowFriendlyFire(false);
//    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void addProfile(GameProfile profile) {
        profiles.put(profile.getPlayer().getUniqueId(), profile);

        Player player = profile.getPlayer();
        if (player != null) {
            connect(player);
        }
    }

    public void connect(Player player) {
        team.addEntity(player);
        Scoreboard scoreboard = team.getScoreboard();
        if (scoreboard != null) {
            player.setScoreboard(team.getScoreboard());
        }

        player.setHealth(1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setHealth(20);
        }, 2);
    }

    public void disconnect(Player player) {
        team.removePlayer(player);
    }

    // Move to player manager
    public void spawnAll() {
        for (GameProfile profile : profiles.values()) {
            Player player = profile.getPlayer();
            spawn(player);
        }
    }

    public void spawn(Player player) {
        spawn(player, true);
    }

    public void spawn(Player player, boolean reset) {
        player.teleport(spawn);

        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        if (reset) {
            player.setSaturation(20);
            player.setFoodLevel(20);
            player.getInventory().clear();
            player.getInventory().setHeldItemSlot(0);
            profiles.get(player.getUniqueId()).getEquipmentManager().giveItems();
        }
    }

    public void setFirstPoint(Point point) {
        firstPoint = point;
    }
    public Point getFirstPoint() {
        return firstPoint;
    }
    public Component getName() {
        return text(name, color);
    }
    public int size() {
        return profiles.size();
    }
    public List<GameProfile> getProfiles() {
        return profiles.values().stream().toList();
    }
    public List<Player> getConnectedPlayers() {
        return profiles.values().stream()
                .filter(GameProfile::isConnected)
                .map(GameProfile::getPlayer).toList();
    }
    public NamedTextColor getColor() {
        return color;
    }
    public void setScore(int score) {
        this.score = Math.max(0, score);
    }
    public int getScore() {
        return score;
    }

    public Material getMaterial() {
        return switch (color.toString().toUpperCase()) {
            case "RED" -> Material.RED_CONCRETE;
            case "BLUE" -> Material.BLUE_CONCRETE;
            default -> Material.WHITE_CONCRETE;
        };
    }
    public String getId() {
        return id;
    }
}
