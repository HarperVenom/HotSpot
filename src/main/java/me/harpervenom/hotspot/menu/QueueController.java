package me.harpervenom.hotspot.menu;

import me.harpervenom.hotspot.game.GameModeEnum;
import me.harpervenom.hotspot.game.GameSettings;
import me.harpervenom.hotspot.game.map.MapData;
import me.harpervenom.hotspot.game.map.MapManager;
import me.harpervenom.hotspot.menu.components.Button;
import me.harpervenom.hotspot.menu.components.Window;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.QueueManager;
import me.harpervenom.hotspot.queue.players.TeamQueueOrganizer;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static me.harpervenom.hotspot.utils.Utils.*;

public class QueueController {

    private final MenuManager menuManager;
    private final QueueManager queueManager;
    private final MapManager mapManager;

    public QueueController(MenuManager menuManager, QueueManager queueManager, MapManager mapManager) {
        this.menuManager = menuManager;
        this.queueManager = queueManager;
        this.mapManager = mapManager;
    }

    public Button getCreateQueueButton() {
        ItemStack itemStack = createItemStack(Material.GLASS_PANE, text("Создать игру"), null);
        Button button = new Button(itemStack);
        button.setOnPersonalClick(makeCreateGameWindow()::open);
        return button;
    }

    private Window makeCreateGameWindow() {
        Window window = new Window("Создать игру", 27);

        GameModeEnum mode = GameModeEnum.CUSTOM;
        GameSettings settings = mode.getSettings();
        settings.setMapData(mapManager.getMaps().getFirst());

        window.setOnUpdate(() -> {
            ItemStack createItemStack = createItemStack(Material.LIME_CONCRETE, text("Создать", NamedTextColor.GREEN), null);
            Button createButton = new Button(createItemStack);
            createButton.setOnPersonalClick(player -> {
                GameQueue queue = queueManager.createQueue(mode, player);
                if (settings.isCustom()) {
                    queueManager.setWindow(queue, makeQueueWindow(queue));
                } else {
                    addPlayerToQueue(player, queue);
                }
                menuManager.getLobbyController().update(player);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BANJO, 0.5f, 1);
                queueManager.getQueueWindow(queue).open(player);
            });

            ItemStack mapItemStack = createItemStack(settings.getMapData().getMaterial(),
                    text("Карта: " + settings.getMapData().getDisplayName()), null);
            Button mapButton = new Button(mapItemStack);
            mapButton.setOnPersonalClick(player -> {
                makeMapWindow(player, settings, window).open(player);
            });

            window.addButton(mapButton, 4);
            window.addButton(createButton, 22);
        });
        window.update();

        return window;
    }

    private Window makeMapWindow(Player player, GameSettings setting, Window lastWindow) {
        Window window = new Window("Выбор карты", InventoryType.CHEST);
        List<MapData> maps = mapManager.getMaps();
        for (int i = 0; i < maps.size(); i++) {
            MapData mapData = maps.get(i);
            ItemStack mapItemStack = createItemStack(mapData.getMaterial(),
                    text(mapData.getDisplayName()), null);
            Button button = new Button(mapItemStack);
            button.setOnClick(() -> {
                setting.setMapData(mapData);
                lastWindow.update();
                lastWindow.open(player);
            });
            window.addButton(button, i);
        }
        return window;
    }

    private Window makeQueueWindow(GameQueue queue) {
        Window window = new Window("Команды", 27);

        window.setOnUpdate(() -> {
            TeamQueueOrganizer organizer = (TeamQueueOrganizer) queue.getOrganizer();

            window.addButton(makeTeamButton(window, queue, organizer.getTeamManager().getTeams().getFirst()), 12);
            window.addButton(makeTeamButton(window, queue, organizer.getTeamManager().getTeams().getLast()), 14);
        });
        window.update();

        return window;
    }

    private Button makeTeamButton(Window window, GameQueue queue, QueueTeam team) {
        List<Component> lore = new ArrayList<>();
        for (Player player : team.getPlayers()) {
            lore.add(text(player.getName()));
        }
        ItemStack itemStack = createItemStack(team.getMaterial(), text(team.getName(), team.getColor()), lore);
        Button button = new Button(itemStack);
        button.setOnPersonalClick(player -> {
            QueueTeam lastTeam = queue.getTeam(player);

            if (queue.getTimer().getTimeLeft() > 5) {
                if (lastTeam != null && lastTeam.equals(team) && queue.isOwner(player)) {
                    queueManager.removePlayerFromQueue(player, true);
                    menuManager.getLobbyController().update(player, false);
                } else {
                    boolean success = addPlayerToQueue(player, queue, team);
                    if (!success) {
                        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1);
                        return;
                    }
                }
            }

            window.update();
        });
        return button;
    }

    public boolean addPlayerToQueue(Player player, GameQueue queue) {
        return addPlayerToQueue(player, queue, null);
    }

    public boolean addPlayerToQueue(Player player, GameQueue queue, QueueTeam team) {
        boolean added = queueManager.addPlayerToQueue(player, queue, team);

        if (added) {
            menuManager.getLobbyController().update(player, false);
            playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f, queue.getPlayers());
        }
        return added;
    }
}
