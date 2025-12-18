package me.harpervenom.hotspot.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.lobby.LobbyManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.text;

public class ChatManager implements Listener {

    private final LobbyManager lobbyManager;
    private final GameManager gameManager;

    private final TextColor serverColor = TextColor.color(204, 143, 143);
    private final TextColor lobbyColor = TextColor.color(143, 184, 204);
    private final TextColor gameColor = TextColor.color(230, 217, 161);
    private final TextColor teamColor = TextColor.color(184, 230, 161);
    private final TextColor spectatorColor = TextColor.color(230, 230, 230);

    public ChatManager(LobbyManager lobbyManager, GameManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onChatMessage(AsyncChatEvent e) {
        Player player = e.getPlayer();
        e.setCancelled(true);
        handleChatMessage(player, e.message());
    }

    private void handleChatMessage(Player player, Component message) {
        World world = player.getWorld();

        String raw = PlainTextComponentSerializer.plainText().serialize(message).strip();
        boolean isGlobal = raw.startsWith("!!");
        boolean isGame = !isGlobal && raw.startsWith("!");
        Component msgText = text(
                isGlobal ? raw.substring(2).strip() :
                        isGame   ? raw.substring(1).strip() :
                                raw
        );

        List<Player> viewers;
        Component prefix;
        TextColor color = NamedTextColor.WHITE;

        if (isGlobal) {
            prefix = text("[Сервер] ", serverColor);
            viewers = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        } else if (lobbyManager.isLobby(world)) {
            prefix = text("[Лобби] ", lobbyColor);
            viewers = world.getPlayers();
        } else {
            Game game = gameManager.getGame(world);
            if (game == null) return;

            boolean isSpectator = game.getPlayerManager().isSpectator(player);
            if (isSpectator) {
                color = spectatorColor;
            } else {
                color = game.getPlayerManager().getTeam(player).getColor();
            }

            if (isSpectator) {
                prefix = text("[Зритель] ", spectatorColor);
                viewers = game.getPlayers();
            } else if (isGame) {
                prefix = text("[Общий] ", gameColor);
                viewers = game.getPlayers();
            } else {
                prefix = text("[Команда] ", teamColor);
                game.getPlayerManager().getSpectators();
                viewers = game.getPlayerManager().getTeam(player).getConnectedPlayers();
            }
        }

        Component finalMessage = prefix.append(
                text("<" + player.getName() + "> ", color)
        ).append(msgText);

        sendMessage(viewers, finalMessage);
    }


    private Component handleGameMessage(Game game, Player player, Component message) {
        Component newMessage;
        Component front = text("");
        if (game.getPlayerManager().isSpectator(player)) {
            front = text("[Зритель] ", gameColor);
        }

        newMessage = front.append(text("<" + player.getName() + "> ")).append(message);

        return newMessage;

//        if (gameTeam == null || isEliminated) {
//            front = text("[Зритель] ", NamedTextColor.GRAY);
//        } else if (isGlobal || isSolo) {
//            front = text("[Общий] ", NamedTextColor.GRAY);
//        } else {
//            front = text("[Команда] ", NamedTextColor.GRAY);
//            isTeamMessage = true;
//        }
    }

//    public void handleGameChatMessage(Game game, Component message, Player player) {
//        String raw = PlainTextComponentSerializer.plainText().serialize(message);
//        boolean isGlobal = raw.startsWith("!");
//        Component messageText = text(isGlobal ? raw.substring(1).strip() : raw.strip());
//
//        Component front = text("[Общий] ", NamedTextColor.GRAY);
//        TextColor color = NamedTextColor.GRAY;
//        Component newMessage = front.append(text("<" + player.getName() + "> ", color)).append(messageText);
//
//
//        GameProfile gameProfile = game.getProfile(player);
//        boolean isEliminated = gameProfile != null && gameProfile.isEliminated();
//        GameTeam gameTeam = gameProfile == null ? null : gameProfile.getTeam();
//
//        TextColor color = NamedTextColor.GRAY;
//        if (gameTeam != null) color = gameTeam.getColor();
//        if (isEliminated) color = TextColor.color(128, 128, 128);
//
//        // Convert Component -> plain string
//        String raw = PlainTextComponentSerializer.plainText().serialize(message);
//        boolean isGlobal = raw.startsWith("!");
//        Component messageText = text(isGlobal ? raw.substring(1).strip() : raw.strip());
//
//        boolean isSolo = settings.getMode() == CustomGameMode.SOLO;
//        boolean isTeamMessage = false;
//
//        Component front;
//        if (gameTeam == null || isEliminated) {
//            front = text("[Зритель] ", NamedTextColor.GRAY);
//        } else if (isGlobal || isSolo) {
//            front = text("[Общий] ", NamedTextColor.GRAY);
//        } else {
//            front = text("[Команда] ", NamedTextColor.GRAY);
//            isTeamMessage = true;
//        }
//        Component newMessage = front.append(text("<" + player.getName() + "> ", color)).append(messageText);
//        if (isTeamMessage) {
//            showTeamMessage(newMessage, gameTeam);
//        } else {
//            showMessage(newMessage);
//        }
//    }

    public void sendMessage(List<Player> players, Component message) {
        for (Player player : players) {
            player.sendMessage(message);
        }
        plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(message));
    }
}
