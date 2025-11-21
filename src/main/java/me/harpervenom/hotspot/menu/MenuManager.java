package me.harpervenom.hotspot.menu;

import me.harpervenom.hotspot.game.*;
import me.harpervenom.hotspot.game.profile.GameProfile;
import me.harpervenom.hotspot.lobby.LobbyListener;
import me.harpervenom.hotspot.menu.components.Button;
import me.harpervenom.hotspot.menu.components.Window;
import me.harpervenom.hotspot.player.ButtonSet;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.QueueListener;
import me.harpervenom.hotspot.queue.QueueManager;
import me.harpervenom.hotspot.queue.players.TeamQueueOrganizer;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static me.harpervenom.hotspot.utils.Utils.createItemStack;
import static me.harpervenom.hotspot.utils.Utils.text;


public class MenuManager implements QueueListener, LobbyListener, GameListener {

    private final QueueManager queueManager;
    private final GameManager gameManager;
//    private final PartyManager partyManager;

    private Button queuesButton, createQueueButton, leaveQueueButton, removeQueueButton;

    private final Window gamesWindow;

    private final HashMap<Player, ButtonSet> buttonSets = new HashMap<>();

    public MenuManager(QueueManager queueManager, GameManager gameManager) {
        this.queueManager = queueManager;
        this.gameManager = gameManager;

        makeCreateQueueButton();

        List<GameQueue> queues = queueManager.getQueues();
        List<Game> games = gameManager.getGames();
        gamesWindow = new Window("Игры", 27);
        gamesWindow.setOnUpdate(() -> {
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

            gamesWindow.addButton(createQueueButton, 26);
        });
        updateGamesWindow();

        makeQueuesButton();
        makeLeaveQueueButton();
        makeRemoveQueueButton();
    }

    public boolean handleHandClick(Player player) {
        Button button = buttonSets.get(player).getButtons().get(player.getInventory().getHeldItemSlot());
        if (button == null) return false;
        button.click(player, false);
        return true;
    }

    private void makeQueuesButton() {
        ItemStack itemStack = createItemStack(Material.COMPASS, text("Играть"), null);
        queuesButton = new Button(itemStack);
        queuesButton.setOnPersonalClick(gamesWindow::open);
    }

    private void makeCreateQueueButton() {
        ItemStack itemStack = createItemStack(Material.GLASS_PANE, text("Создать игру"), null);
        createQueueButton = new Button(itemStack);
        createQueueButton.setOnPersonalClick(makeCreateGameWindow()::open);
    }

    private Window makeCreateGameWindow() {
        Window window = new Window("Создать игру", 27);

        GameModeEnum mode = GameModeEnum.CUSTOM;

        window.setOnUpdate(() -> {
            ItemStack createItemStack = createItemStack(Material.LIME_CONCRETE, text("Создать", NamedTextColor.GREEN), null);
            Button createButton = new Button(createItemStack);
            createButton.setOnPersonalClick(player -> {
                GameQueue queue = queueManager.createQueue(mode, player);
                if (mode.getSettings().isCustom()) {
                    queueManager.setWindow(queue, makeQueueWindow(queue));
                } else {
                    addPlayerToQueue(player, queue);
                }
                updateLobbyButtons(player);
                queueManager.getQueueWindow(queue).open(player);
            });

            window.addButton(createButton, 22);
        });
        window.update();

        return window;
    }

    private Window makeQueueWindow(GameQueue queue) {
        Window window = new Window("Очередь", 27);

        window.setOnUpdate(() -> {
            TeamQueueOrganizer organizer = (TeamQueueOrganizer) queue.getOrganizer();

            window.addButton(makeTeamButton(window, queue, organizer.getTeamManager().getTeams().getFirst()), 12);
            window.addButton(makeTeamButton(window, queue,organizer.getTeamManager().getTeams().getLast()), 14);
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
            if (lastTeam != null && lastTeam.equals(team) && queue.isOwner(player)) {
                queueManager.removePlayerFromQueue(player);
            } else {
                addPlayerToQueue(player, queue, team);
            }
            window.update();
            updateLobbyButtons(player, false);
        });
        return button;
    }

    private void makeLeaveQueueButton() {
        ItemStack itemStack = createItemStack(Material.RED_CONCRETE, text("Выйти", NamedTextColor.RED), null);
        leaveQueueButton = new Button(itemStack);
        leaveQueueButton.setOnPersonalClick(player -> {
            queueManager.removePlayerFromQueue(player);
            updateLobbyButtons(player);
        });
    }

    private void makeRemoveQueueButton() {
        ItemStack itemStack = createItemStack(Material.RED_CONCRETE, text("Удалить очередь", NamedTextColor.RED), null);
        removeQueueButton = new Button(itemStack);
        removeQueueButton.setOnPersonalClick(player -> {
            queueManager.removeQueue(player);
            updateLobbyButtons(player);
        });
    }

    private Button createSkipButton(Material material, String name) {
        Button button = new Button(createItemStack(material, text(name), null));
        button.setOnPersonalClick(player -> {
            GameQueue queue = queueManager.getQueue(player);
            if (queue == null) return;
            queue.toggleSkip(player);
            updateLobbyButtons(player, false);
            queue.checkSkips();
        });
        return button;
    }

    private Button makeQueueButton(GameQueue queue) {
        List<Component> lore = new ArrayList<>();
        lore.add(text("Игроков: " + queue.getPlayers().size()+ "/" + queue.getMaxPlayers()));
        for (Player player : queue.getPlayers()) {
            lore.add(text(player.getName(), NamedTextColor.GRAY));
        }

        GameSettings settings = queue.getGameMode().getSettings();

        ItemStack itemStack = createItemStack(settings.getQueueMaterial(), text("Очередь ").append(settings.getName()), lore);

        Button button = new Button(itemStack);

        button.setOnPersonalClick(player -> {
            if (settings.isCustom()) {
                onCustomQueueClick(player, queue);
                return;
            }

            addPlayerToQueue(player, queue);
        });

        return button;
    }

    private void addPlayerToQueue(Player player, GameQueue queue) {
        addPlayerToQueue(player, queue, null);
    }

    private void addPlayerToQueue(Player player, GameQueue queue, QueueTeam team) {
        boolean added = queueManager.addPlayerToQueue(player, queue, team);

        if (added) {
            updateLobbyButtons(player, false);
            queue.playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f);
        }
    }

    private void onCustomQueueClick(Player player, GameQueue queue) {
        queueManager.getQueueWindow(queue).open(player);
//        makeQueueWindow(queue).open(player);
    }

//    private Window makeQueueWindow(GameQueue queue) {
//        Window window = new Window("Очередь", 27);
//
//        window.setOnUpdate(() -> {
//            TeamQueueOrganizer organizer = (TeamQueueOrganizer) queue.getOrganizer();
//
//            window.addButton(makeTeamButton(window, queue, organizer.getTeamManager().getTeams().getFirst()), 12);
//            window.addButton(makeTeamButton(window, queue,organizer.getTeamManager().getTeams().getLast()), 14);
//        });
//        window.update();
//
//        return window;
//    }
//
//    private Button makeTeamButton(Window window, GameQueue queue, QueueTeam team) {
//        List<Component> lore = new ArrayList<>();
//        for (Player player : team.getPlayers()) {
//            lore.add(text(player.getName()));
//        }
//        ItemStack itemStack = createItemStack(team.getMaterial(), text(team.getName(), team.getColor()), lore);
//        Button button = new Button(itemStack);
//        button.setOnPersonalClick(player -> {
//            addPlayerToQueue(player, queue, team);
//            window.update();
//        });
//        return button;
//    }

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

        ItemStack itemStack = createItemStack(
                settings.getGameMaterial(), text("Игра ")
                .append(settings.getName()), lore);

        Button button = new Button(itemStack);

        button.setOnPersonalClick(player -> {
            updateLobbyButtons(player);
            Bukkit.broadcastMessage("try");
//            game.connect(player);
        });

        return button;
    }

    private Button makeTeamsButton(GameQueue queue, Player player) {
        QueueTeam team = queue.getTeam(player);

        ItemStack itemStack = createItemStack(team == null ? Material.WHITE_WOOL : team.getMaterial(), text("Команды"), null);
        Button button = new Button(itemStack);

        button.setOnPersonalClick(p -> {
            queueManager.getQueueWindow(queue).open(p);
        });

        return button;
    }

    private Button makeTimerButton(GameQueue queue, Player player) {
        boolean isCustom = queue.getSettings().isCustom();
        if (queue.isSkipping(player)) {
            return createSkipButton(Material.REDSTONE_TORCH, isCustom ? "Запустить таймер" : "Не пропускать ожидание");
        } else {
            return createSkipButton(Material.LEVER, isCustom ? "Отменить таймер" : "Пропустить ожидание");
        }
    }

    public void updateLobbyButtons(Player player) {
        updateLobbyButtons(player, true);
    }

    public void updateLobbyButtons(Player player, boolean changeHand) {
        ButtonSet set = new ButtonSet();

        Game game = gameManager.getGame(player);
        if (game == null) {
            set.setButton(0, queuesButton);
            GameQueue queue = queueManager.getQueue(player);
            if (queue != null) {
                if (queue.getSettings().isCustom()) {
                    set.setButton(4, makeTeamsButton(queue, player));
                }

                if (!queue.getSettings().isCustom() || queue.isOwner(player)) {
                    set.setButton(6, makeTimerButton(queue, player));
                }

                if (queue.isOwner(player)) {
                    set.setButton(8, removeQueueButton);
                } else {
                    set.setButton(8, leaveQueueButton);
                }
            }
        }

        buttonSets.put(player, set);
        updateInventory(player);
        if (changeHand) {
            player.getInventory().setHeldItemSlot(0);
        }
    }

    private void updateInventory(Player player) {
        Map<Integer, Button> buttons = buttonSets.get(player).getButtons();
        Inventory inventory = player.getInventory();

        // iterate through all button slots
        for (int i = 0; i < inventory.getSize(); i++) {
            Button button = buttons.get(i);
            ItemStack item = inventory.getItem(i);
            if (button == null) {
                // only clear the slot if it’s not already air
                if (item != null && item.getType() != Material.AIR) {
                    inventory.setItem(i, new ItemStack(Material.AIR));
                }
                continue;
            }

            // skip if the item is already identical
            if (button.getItemStack().isSimilar(inventory.getItem(i))) continue;

            // update the slot
            inventory.setItem(i, button.getItemStack());
        }
    }

    public void updateGamesWindow() {
        gamesWindow.update();
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
    }

    @Override
    public void onGamesUpdate() {
        updateGamesWindow();
    }

    @Override
    public void onLobby(Player player) {
        updateLobbyButtons(player);
    }
}
