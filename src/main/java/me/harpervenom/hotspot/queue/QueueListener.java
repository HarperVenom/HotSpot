package me.harpervenom.hotspot.queue;

import org.bukkit.entity.Player;

public interface QueueListener {
    default void onQueueCreate(GameQueue queue) {}
    default void onPlayerJoin(Player player, GameQueue queue) {}
    default void onPlayerLeave(Player player, GameQueue queue) {}
    default void onQueueReady(GameQueue queue) {}
    default void onQueueRemove(GameQueue queue) {}
    default void onTimerTick(GameQueue queue) {}
}
