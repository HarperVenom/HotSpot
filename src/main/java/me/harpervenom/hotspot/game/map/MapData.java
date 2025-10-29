package me.harpervenom.hotspot.game.map;

import org.bukkit.Material;

import java.io.File;
import java.util.List;

public class MapData {

    private final String name;
    private final String displayName;
    private final Material material;
    private final int maxPlayersPerTeam;
    private final int deathProtection;
    private final String author;
    private final int maxPlayers;
    private final int numberOfTeams;

    private final List<Loc> spawns;
    private final List<Loc> traders;
    private final List<Loc> monuments;
    private final List<Loc> vaults;

    private final File folder;

    public MapData(String name,
                   String displayName,
                   Material material,
                   int maxPlayersPerTeam,
                   int deathProtection,
                   String author,
                   int maxPlayers,
                   int numberOfTeams,
                   List<Loc> spawns,
                   List<Loc> traders,
                   List<Loc> monuments,
                   List<Loc> vaults,
                   File folder) {
        this.name = name;
        this.displayName = displayName;
        this.material = material;
        this.maxPlayersPerTeam = maxPlayersPerTeam;
        this.deathProtection = deathProtection;
        this.author = author;
        this.maxPlayers = maxPlayers;
        this.numberOfTeams = numberOfTeams;
        this.spawns = spawns;
        this.traders = traders;
        this.monuments = monuments;
        this.vaults = vaults;
        this.folder = folder;
    }

    // getters
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public int getMaxPlayersPerTeam() { return maxPlayersPerTeam; }
    public int getDeathProtection() { return deathProtection; }
    public String getAuthor() { return author; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getNumberOfTeams() { return numberOfTeams; }
    public List<Loc> getSpawns() { return spawns; }
    public List<Loc> getTraders() { return traders; }
    public List<Loc> getMonuments() { return monuments; }
    public List<Loc> getVaults() { return vaults; }
    public File getFolder() { return folder; }
}

//public String getPlayersInfo() {
//    StringBuilder playerText = new StringBuilder(maxPlayersPerTeam + "");
//
//    for (int i = 0; i < numberOfTeams - 1; i++) {
//        playerText.append("x").append(maxPlayersPerTeam);
//    }
//
//    return playerText.toString();
//}
