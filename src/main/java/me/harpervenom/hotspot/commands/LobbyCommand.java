package me.harpervenom.hotspot.commands;

import me.harpervenom.hotspot.lobby.LobbyManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.harpervenom.hotspot.utils.Utils.text;

public class LobbyCommand implements CommandExecutor {

    private final LobbyManager lobbyManager;

    public LobbyCommand(LobbyManager lobbyManager) {
        this.lobbyManager = lobbyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        player.sendMessage(text("Перемещение в Лобби", NamedTextColor.WHITE));
        lobbyManager.sendToLobby(player);
        return true;
    }
}
