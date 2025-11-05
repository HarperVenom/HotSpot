package me.harpervenom.hotspot.game.team;

import me.harpervenom.hotspot.game.GameProfile;
import me.harpervenom.hotspot.game.point.Point;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

import static me.harpervenom.hotspot.utils.Utils.text;

public class GameTeam {

    private static int lastId;

    private final int id;
    private final NamedTextColor color;
    private final String name;
    private Team team;

    private final List<GameProfile> profiles = new ArrayList<>();
    private final TeamBase base;

    private Point firstPoint;

    private int score = 100;

    public GameTeam(NamedTextColor color, String name, TeamBase base) {
        this.id = lastId;
        lastId++;
        this.color = color;
        this.name = name;
        this.base = base;
    }

    public void register(Scoreboard scoreboard) {
        team = scoreboard.registerNewTeam(id + "");
        team.displayName(getName());
        team.color(color);
        team.setAllowFriendlyFire(false);
    }

    public void addProfile(GameProfile profile) {
        profiles.add(profile);

        Player player = profile.getPlayer();
        if (player != null) {
            team.addEntity(player);
        }
    }

    public void spawnAll() {
        for (GameProfile profile : profiles) {
            Player player = profile.getPlayer();
            spawn(player);
        }
    }

    public void spawn(Player player) {
        spawn(player, true);
    }

    public void spawn(Player player, boolean reset) {
        player.teleport(base.getSpawn());

        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        if (reset) {
            player.setSaturation(20);
            player.setFoodLevel(20);
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
        return profiles;
    }
    public List<Player> getPlayers() {
        return profiles.stream().map(GameProfile::getPlayer).toList();
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
            case "GREEN" -> Material.LIME_CONCRETE;
            case "YELLOW" -> Material.YELLOW_CONCRETE;
            default -> Material.WHITE_CONCRETE; // Default fallback
        };
    }
}
