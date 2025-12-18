package me.harpervenom.hotspot.game.profile;

import me.harpervenom.hotspot.game.team.GameTeam;
import me.harpervenom.hotspot.game.trader.TradeType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

import static me.harpervenom.hotspot.utils.Utils.text;

public class GameProfile {

    private final UUID playerId;
    private GameTeam gameTeam;
    private boolean isConnected = true;

    private final UpgradesManager upgradesManager;
    private final EconomyManager economyManager;
    private final EquipmentManager equipmentManager;
    private final LootManager lootManager;
    private final GameStats gameStats;

    private boolean isProtected = false;

    public GameProfile(Player player) {
        this.playerId = player.getUniqueId();
        upgradesManager = new UpgradesManager(this);
        economyManager = new EconomyManager(this);
        equipmentManager = new EquipmentManager(this);
        lootManager = new LootManager(this);
        gameStats = new GameStats();

        upgradesManager.setLevel(TradeType.PICKAXE, 1);
        upgradesManager.setLevel(TradeType.HELMET, 1);
        upgradesManager.setLevel(TradeType.LEGGINGS, 1);
        upgradesManager.setLevel(TradeType.BOOTS, 1);

        reset();
    }

    public void reset() {
        lootManager.reset();
        getEquipmentManager().setHasWeapon(false);
        getEquipmentManager().setHasChest(false);
    }

    public void setTeam(GameTeam gameTeam) {
        this.gameTeam = gameTeam;
    }
    public GameTeam getTeam() {
        return gameTeam;
    }
    public UUID getId() {
        return playerId;
    }
    public Player getPlayer() {
        return Bukkit.getPlayer(playerId);
    }
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(playerId);
    }
    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
        if (!isConnected) {
            isProtected = false;
        }
    }
    public boolean isConnected() {
        return isConnected;
    }
    public Component getName() {
        return text(getOfflinePlayer().getName(), gameTeam.getColor());
    }

    public UpgradesManager getUpgradesManager() {
        return upgradesManager;
    }
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    public EquipmentManager getEquipmentManager() {
        return equipmentManager;
    }
    public LootManager getLootManager() {
        return lootManager;
    }
    public GameStats getStats() {
        return gameStats;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public boolean isProtected() {
        return isProtected;
    }
}
