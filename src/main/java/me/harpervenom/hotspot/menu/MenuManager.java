package me.harpervenom.hotspot.menu;

import me.harpervenom.hotspot.game.*;
import me.harpervenom.hotspot.lobby.LobbyListener;
import me.harpervenom.hotspot.menu.components.Button;
import me.harpervenom.hotspot.menu.components.Window;
import me.harpervenom.hotspot.player.ButtonSet;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.QueueListener;
import me.harpervenom.hotspot.queue.QueueManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
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

    private Button queuesButton, leaveQueueButton, skipWaitingButton, cancelSkipWaitingButton;

    private final Window gamesWindow;

    private final HashMap<Player, ButtonSet> buttonSets = new HashMap<>();

    public MenuManager(QueueManager queueManager, GameManager gameManager) {
        this.queueManager = queueManager;
        this.gameManager = gameManager;

        List<GameQueue> queues = queueManager.getQueues();
        List<Game> games = gameManager.getGames();
        gamesWindow = new Window("Игры", 9);
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
        });
        updateGamesWindow();

        makeQueuesButton();
        makeLeaveQueueButton();
        makeSkipWaitingButtons();
    }

    public boolean handleHandClick(Player player) {
        Button button = buttonSets.get(player).getButtons().get(player.getInventory().getHeldItemSlot());
        if (button == null) return false;
        button.click(player, false);
        return true;
    }

    private void makeQueuesButton() {
        ItemStack itemStack = createItemStack(Material.COMPASS, text("Играть"), null);
//        itemStack.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable().build());
        queuesButton = new Button(itemStack);
        queuesButton.setOnPersonalClick(gamesWindow::open);
    }

    private void makeLeaveQueueButton() {
        ItemStack itemStack = createItemStack(Material.RED_CONCRETE, text("Выйти", NamedTextColor.RED), null);
//        itemStack.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable().build());
        leaveQueueButton = new Button(itemStack);
        leaveQueueButton.setOnPersonalClick(player -> {
            queueManager.removePlayerFromQueue(player);
            player.sendMessage(text("Вы покинули очередь", NamedTextColor.RED));
            player.sendActionBar(text(""));

//            Party party = partyManager.getParty(player);
//            if (party != null && party.isOwner(gamePlayer.getPlayer())) {
//                // Remove all members of the party from the queue
//                for (Player member : party.getMembers()) {
//                    GamePlayer gp = playerManager.get(member);
//                    if (gp != null) {
//                        queueManager.removePlayerFromQueue(gp);
//                    }
//                }
//            } else {
//                // Just remove the single player
//                queueManager.removePlayerFromQueue(gamePlayer);
//            }
        });
    }

    private void makeSkipWaitingButtons() {
        skipWaitingButton = createSkipButton(Material.LEVER, "Пропустить ожидание");
        cancelSkipWaitingButton = createSkipButton(Material.REDSTONE_TORCH, "Не пропускать ожидание");
    }

    private Button createSkipButton(Material material, String name) {
        ItemStack itemStack = createItemStack(material, text(name), null);
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
            boolean added = false;

            if (queueManager.addPlayerToQueue(player, queue)) {
                added = true;
            }

//            // Check if player is in a party and is the owner
//            Party party = partyManager.getParty(player);
//            boolean addedAny = false;
//
//            if (party != null && party.isOwner(player)) { // assuming Party has isOwner(Player) method
//                for (Player member : party.getMembers()) {
//                    GamePlayer memberGP = playerManager.get(member);
//                    memberGP.setSkipWaiting(false);
//                    if (queueManager.addPlayerToQueue(memberGP, queue)) {
//                        addedAny = true;
//                    }
//                }
//            } else {
//                if (queueManager.addPlayerToQueue(gamePlayer, queue)) {
//                    addedAny = true;
//                }
//            }

            if (added) {
                queue.playSound(Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f);
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

        ItemStack itemStack = createItemStack(
                settings.getGameMaterial(), text("Игра ")
                .append(settings.getName()), lore);

        Button button = new Button(itemStack);

        button.setOnPersonalClick(player -> {
            game.connect(player);
            updateLobbyButtons(player);
        });

        return button;
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
                if (queue.isSkipping(player)) {
                    set.setButton(6, cancelSkipWaitingButton);
                } else {
                    set.setButton(6, skipWaitingButton);
                }
                set.setButton(8, leaveQueueButton);
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

    public void clearButtonSet(Player player) {
        buttonSets.put(player, new ButtonSet());
        updateInventory(player);
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
        updateLobbyButtons(player);
        updateGamesWindow();
    }
    @Override
    public void onPlayerLeave(Player player, GameQueue queue) {
        updateLobbyButtons(player);
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
