package me.harpervenom.hotspot.menu;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.menu.components.Button;
import me.harpervenom.hotspot.menu.components.Window;
import me.harpervenom.hotspot.player.ButtonSet;
import me.harpervenom.hotspot.queue.GameQueue;
import me.harpervenom.hotspot.queue.QueueManager;
import me.harpervenom.hotspot.queue.players.team.QueueTeam;
import me.harpervenom.hotspot.statistics.Stats;
import me.harpervenom.hotspot.statistics.StatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.harpervenom.hotspot.statistics.Stats.*;
import static me.harpervenom.hotspot.utils.Utils.createItemStack;
import static me.harpervenom.hotspot.utils.Utils.text;

public final class LobbyMenuController {

    private final MenuManager menuManager;
    private final QueueManager queueManager;
    private final GameManager gameManager;
    private final StatsManager statsManager;
    private final Map<Player, ButtonSet> buttonSets = new HashMap<>();

    public LobbyMenuController(MenuManager menuManager, QueueManager queueManager, GameManager gameManager, StatsManager statsManager) {
        this.menuManager = menuManager;
        this.queueManager = queueManager;
        this.gameManager = gameManager;
        this.statsManager = statsManager;
    }

    public boolean handleHandClick(Player player) {
        Button button = buttonSets.get(player).getButtons().get(player.getInventory().getHeldItemSlot());
        if (button == null) return false;
        button.click(player, false);
        return true;
    }

    public void update(Player player) {
        update(player, true);
    }

    public void update(Player player, boolean changeHand) {
        ButtonSet set = new ButtonSet();

        Game game = gameManager.getGame(player);
        GameQueue queue = queueManager.getQueue(player);

        if (game == null) {
            set.setButton(0, getQueuesButton());
        }

        set.setButton(2, getStatsButton());

        if (queue != null) {
            if (queue.getSettings().canChooseTeam()) {
                set.setButton(4, getTeamsButton(queue, player));
            }

            if ((!queue.isSkipped() && !queue.getSettings().isCustom()) || queue.isOwner(player)) {
                set.setButton(6, getTimerButton(queue, player));
            }

            set.setButton(8, getQueueExitButton(queue.isOwner(player)));
        }

        buttonSets.put(player, set);
        syncInventory(player);

        if (changeHand) {
            player.getInventory().setHeldItemSlot(0);
        }
    }

    private void syncInventory(Player player) {
        Inventory inv = player.getInventory();
        ButtonSet set = buttonSets.get(player);

        for (int i = 0; i < inv.getSize(); i++) {
            Button b = set.getButtons().get(i);
            if (b == null) {
                inv.setItem(i, null);
            } else if (!b.getItemStack().isSimilar(inv.getItem(i))) {
                inv.setItem(i, b.getItemStack());
            }
        }
    }

    private Button getQueuesButton() {
        ItemStack itemStack = createItemStack(Material.COMPASS, text("Играть"), null);
        Button button = new Button(itemStack);
        button.setOnPersonalClick(menuManager.getGamesWindow()::open);
        return button;
    }

    private Button getTeamsButton(GameQueue queue, Player player) {
        QueueTeam team = queue.getTeam(player);

        ItemStack itemStack = createItemStack(team == null ? Material.WHITE_WOOL : team.getMaterial(), text("Команды"), null);
        Button button = new Button(itemStack);

        button.setOnPersonalClick(p -> {
            queueManager.getQueueWindow(queue).open(p);
        });

        return button;
    }

    private Button getTimerButton(GameQueue queue, Player player) {
        boolean isCustom = queue.getSettings().isCustom();
        if (queue.isSkipping(player)) {
            return createTimerButton(Material.REDSTONE_TORCH, isCustom ? "Отменить таймер" : "Не пропускать ожидание");
        } else {
            return createTimerButton(Material.LEVER, isCustom ? "Запустить таймер" : "Пропустить ожидание");
        }
    }

    private Button createTimerButton(Material material, String name) {
        Button button = new Button(createItemStack(material, text(name), null));
        button.setOnPersonalClick(player -> {
            GameQueue queue = queueManager.getQueue(player);
            if (queue == null) return;
            queue.toggleSkip(player);
            queue.checkSkips();
            for (Player queuePlayer : queue.getPlayers()) {
                update(queuePlayer, false);
            }
        });
        return button;
    }

    private Button getQueueExitButton(boolean owner) {
        ItemStack itemStack = createItemStack(
                Material.RED_CONCRETE,
                owner
                        ? text("Удалить очередь", NamedTextColor.RED)
                        : text("Выйти", NamedTextColor.RED),
                null
        );

        Button button = new Button(itemStack);

        button.setOnPersonalClick(player -> {
            if (owner) {
                queueManager.removeQueue(player);
            } else {
                queueManager.removePlayerFromQueue(player);
            }
            update(player);
        });

        return button;
    }

    private Button getStatsButton() {
        ItemStack itemStack = createItemStack(Material.PAPER, text("Статистика"), null);
        Button button = new Button(itemStack);
        button.setOnPersonalClick(player -> {
            Stats stats = statsManager.getStats(player.getUniqueId());
            Window window = new Window("Статистика", 27);
            window.setOnUpdate(() -> {
                window.clear();

                TextColor fieldColor = TextColor.color(125, 134, 179);

                List<Component> lore = new ArrayList<>();
                lore.add(text("Уровень: ", fieldColor)
                        .append(levelIconFromExp(stats.getExp()))
                        .append(text(" " + getLevelProgressString(stats.getExp()) + " "))
                        .append(levelIcon(getLevelFromPoints(stats.getExp()) + 1)));

                lore.add(text("Ранг: ", fieldColor)
                        .append(getRankIcon(stats.getRank()))
                        .append(text(" " + getRankProgressString(stats.getRank()) + " "))
                        .append(getRankIcon(stats.getRank() + 1)));

                lore.add(text("Скилл: ", fieldColor).append(stats.getSkillIcon()));

                lore.add(text(""));
                lore.add(text("Всего игр: ", fieldColor).append(text("" + stats.getGamesPlayed(), NamedTextColor.WHITE)));
                lore.add(text("Побед: ", fieldColor).append(text("" + stats.getGamesWon(), NamedTextColor.WHITE)));
                lore.add(text("Убийств: ", fieldColor).append(text("" + stats.getKills(), NamedTextColor.WHITE)));
                lore.add(text("Смертей: ", fieldColor).append(text("" + stats.getDeaths(), NamedTextColor.WHITE)));
                lore.add(text("Нанесено урона: ", fieldColor)
                        .append(text("" + Math.round(stats.getDealtDamage() * 100.0) / 100.0, NamedTextColor.WHITE)));
                lore.add(text("Получено урона: ", fieldColor)
                        .append(text("" + Math.round(stats.getTakenDamage() * 100.0) / 100.0, NamedTextColor.WHITE)));
                lore.add(text("Предотвращено урона: ", fieldColor)
                        .append(text("" + Math.round(stats.getPreventedDamage() * 100.0) / 100.0, NamedTextColor.WHITE)));
                lore.add(text("Захвачено точек: ", fieldColor).append(text("" + stats.getCapturedPoints(), NamedTextColor.WHITE)));

                ItemStack statsItemStack = createItemStack(Material.PAPER, text("Твоя статистика:", NamedTextColor.GOLD), lore);

                Button statsButton = new Button(statsItemStack);

                window.addButton(statsButton, 4);
            });
            window.update();
            window.open(player);
        });

        return button;
    }

}