package me.harpervenom.hotspot.menu;

import me.harpervenom.hotspot.game.*;
import me.harpervenom.hotspot.game.map.MapManager;
import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.lobby.LobbyListener;
import me.harpervenom.hotspot.menu.components.Button;
import me.harpervenom.hotspot.menu.components.Window;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.QueueListener;
import me.harpervenom.hotspot.queue.QueueManager;
import me.harpervenom.hotspot.queue.players.TeamQueueOrganizer;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import me.harpervenom.hotspot.statistics.StatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static me.harpervenom.hotspot.utils.Utils.*;


public class MenuManager implements QueueListener, LobbyListener, GameListener {

    private final QueueManager queueManager;
    private final GameManager gameManager;
    private final StatsManager statsManager;

    private final LobbyMenuController lobbyMenuController;
    private final QueueController queueController;

    private final Window gamesWindow;

    public MenuManager(QueueManager queueManager, GameManager gameManager, MapManager mapManager, StatsManager statsManager) {
        this.queueManager = queueManager;
        this.gameManager = gameManager;
        this.statsManager = statsManager;

        lobbyMenuController = new LobbyMenuController(this, queueManager, gameManager, statsManager);
        queueController = new QueueController(this, queueManager, mapManager);

        gamesWindow = new Window("Игры", 27);
        gamesWindow.setOnUpdate(() -> {
            List<GameQueue> queues = queueManager.getQueues();
            List<Game> games = gameManager.getGames();
            gamesWindow.clear();
            for (int i = 0; i < queues.size(); i++) {
                GameQueue queue = queues.get(i);
                if (queue.isReady()) continue;
                gamesWindow.addButton(makeQueueButton(queue), i);
            }
            for (int i = 0; i < games.size(); i++) {
                Game game = games.get(i);
                gamesWindow.addButton(makeGameButton(game), queues.size() + i);
            }

            gamesWindow.addButton(queueController.getCreateQueueButton(), 26);
        });

        updateGamesWindow();
    }

    private Button makeQueueButton(GameQueue queue) {
        List<Component> lore = new ArrayList<>();
        lore.add(text("Игроков: " + queue.getPlayers(false).size()+ "/" + queue.getMaxPlayers()));
        if (queue.getOrganizer() instanceof TeamQueueOrganizer organizer) {
            for (QueueTeam team : organizer.getTeamManager().getTeams()) {
                for (Player player : team.getPlayers()) {
                    lore.add(text(player.getName(), queue.getTeam(player).getColor()));
                }
            }
        } else {
            for (Player player : queue.getPlayers(false)) {
                lore.add(text(player.getName(), NamedTextColor.GRAY));
            }
        }

        if (queue.getOwner() != null) {
            lore.add(text(""));
            lore.add(text("★ " + queue.getOwner().getName(), TextColor.color(126, 89, 128)));
        }

        GameSettings settings = queue.getMode().getSettings();

        ItemStack itemStack = createItemStack(settings.getQueueMaterial(), text("Очередь ").append(settings.getName()), lore);
        Button button = new Button(itemStack);
        button.setOnPersonalClick(player -> {
            if (settings.isCustom()) {
                queueManager.getQueueWindow(queue).open(player);
                return;
            }

            boolean success = queueController.addPlayerToQueue(player, queue);
            if (!success) {
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1);
            }
        });

        return button;
    }

    private Button makeGameButton(Game game) {
        List<GameProfile> gameProfiles = game.getPlayerManager().getProfileMap().values().stream().toList();

        GameSettings settings = game.getSettings();

        List<Component> lore = new ArrayList<>();
        lore.add(text("Игроков: " + gameProfiles.size()+ "/" + settings.getMaxPlayers()));
        for (GameProfile profile : gameProfiles) {
            TextColor color = NamedTextColor.GRAY;
            if (!profile.isConnected()) color = TextColor.color(130, 35, 30);
            lore.add(text(profile.getPlayer().getName(), color));
        }

        GameQueue queue = game.getQueue();
        if (queue.getOwner() != null) {
            lore.add(text(""));
            lore.add(text("★ " + queue.getOwner().getName(), TextColor.color(126, 89, 128)));
        }

        ItemStack itemStack = createItemStack(
                settings.getGameMaterial(), text("Игра ")
                .append(settings.getName()), lore);

        Button button = new Button(itemStack);
        button.setOnPersonalClick(player -> {
            if (!game.canConnect(player)) {
                game.getPlayerManager().connectSpectator(player);
            } else {
                makeOptionWindow(game).open(player);
            }
        });

        return button;
    }

    private Window makeOptionWindow(Game game) {
        Window window = new Window("Присоединиться к игре", 27);

        ItemStack joinItemStack = createItemStack(Material.LIME_CONCRETE, text("Играть"), null);
        Button joinButton = new Button(joinItemStack);
        joinButton.setOnPersonalClick(game::connect);

        ItemStack spectateItemStack = createItemStack(Material.GLASS, text("Наблюдать"), null);
        Button spectateButton = new Button(spectateItemStack);
        spectateButton.setOnPersonalClick(player -> {
            game.getPlayerManager().connectSpectator(player);
        });

        window.setOnUpdate(() -> {
            window.addButton(joinButton, 12);
            window.addButton(spectateButton, 15);
        });
        window.update();

        return window;
    }

    public void updateGamesWindow() {
        gamesWindow.update();
    }

    public Window getGamesWindow() {
        return gamesWindow;
    }
    public LobbyMenuController getLobbyController() {
        return lobbyMenuController;
    }

    // Queue events
    @Override
    public void onQueueCreate(GameQueue queue) {
        updateGamesWindow();
    }
    @Override
    public void onQueueRemove(GameQueue queue) {
        updateGamesWindow();
    }
    @Override
    public void onQueueReady(GameQueue queue) {
        updateGamesWindow();
    }

    @Override
    public void onPlayerJoin(Player player, GameQueue queue) {
        updateGamesWindow();
    }
    @Override
    public void onPlayerLeave(Player player, GameQueue queue) {
        updateGamesWindow();
        lobbyMenuController.update(player);
    }
    @Override
    public void onTimerTick(GameQueue queue) {
        if (queue.getTimer().getTimeLeft() == 5) {
            queue.getPlayers(true).forEach(player -> lobbyMenuController.update(player, false));
        }
    }

    @Override
    public void onGamesUpdate() {
        updateGamesWindow();
    }

    @Override
    public void onLobby(Player player) {
        lobbyMenuController.update(player);
    }
}
