package me.harpervenom.hotspot.lobby;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.getLocationFromConfig;
import static me.harpervenom.hotspot.utils.Utils.text;

public class LobbyManager implements GameListener {

    private final World world;
    private final Location spawnLoc;

    private final List<LobbyListener> listeners = new ArrayList<>();

    public LobbyManager() {
        World world = Bukkit.getWorld("lobby");
        if (world == null) world = Bukkit.getWorlds().getFirst();
        this.world = world;

        Location spawn = getLocationFromConfig("lobby-spawn", true);
        if (spawn == null) spawn = world.getBlockAt(0, 100, 0).getLocation();
        this.spawnLoc = spawn;

        this.world.setDifficulty(Difficulty.PEACEFUL);
        this.world.setTime(6000);
        this.world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        this.world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
    }

    public void sendToLobby(Player player) {
        if (spawnLoc != null) {
            player.teleport(spawnLoc);
        }
        setupPlayer(player);
        player.getInventory().setHeldItemSlot(0);

        for (LobbyListener l : listeners) l.onLobby(player);
    }

    private void setupPlayer(Player player) {
        player.setFoodLevel(20);
        player.setHealth(20);
        player.setLevel(0);
        player.setExp(0);
        player.clearActivePotionEffects();
        player.setGameMode(GameMode.ADVENTURE);
    }

    public World getLobbyWorld() {
        return world;
    }

    public Location getSpawnLoc() {
        return spawnLoc;
    }

    public boolean isLobby(World world) {
        return world.getUID().equals(this.world.getUID());
    }

//    @Override
//    public void onGameStart(Game game) {
//
//    }

    @Override
    public void onGameEnd(Game game) {
        for (Player player : game.getPlayers()) {
            if (player.getWorld().getUID() != game.getMap().getWorld().getUID()) continue;
            sendToLobby(player);
        }
    }

    public void addListener(LobbyListener lobbyListener) {
        listeners.add(lobbyListener);
    }
}
