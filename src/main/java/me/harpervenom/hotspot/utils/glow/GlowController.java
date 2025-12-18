package me.harpervenom.hotspot.utils.glow;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GlowController {

    private static final byte GLOW_BIT = 0x40;

    public static final HashMap<Player, Set<Player>> glowingPlayers = new HashMap<>();

    public static void setGlow(Player target, List<Player> viewers) {
        if (glowingPlayers.containsKey(target)) {
            Set<Player> oldViewers = glowingPlayers.get(target);

            for (Player oldViewer : oldViewers) {
                if (!viewers.contains(oldViewer)) {
                    removeGlow(oldViewer, target);
                }
            }
        }

        glowingPlayers.put(target, new HashSet<>(viewers));
        updateGlow(target);
    }

    public static void updateGlow(Player target) {
        if (!glowingPlayers.containsKey(target)) return;
        Set<Player> viewers = glowingPlayers.get(target);
        for (Player viewer : viewers) {
            addGlow(viewer, target);
        }
    }

    public static void addGlow(Player viewer, Player target) {
        int entityId = target.getEntityId();

        EntityData data = new EntityData(
                0,
                EntityDataTypes.BYTE,
                GLOW_BIT
        );

        WrapperPlayServerEntityMetadata packet =
                new WrapperPlayServerEntityMetadata(
                        entityId,
                        List.of(data)
                );

        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacketSilently(viewer, packet);
    }

    public static void removeGlow(Player viewer, Player target) {
        int entityId = target.getEntityId();

        EntityData data = new EntityData(
                0,
                EntityDataTypes.BYTE,
                (byte) 0 // no glow bit
        );

        WrapperPlayServerEntityMetadata packet =
                new WrapperPlayServerEntityMetadata(
                        entityId,
                        List.of(data)
                );

        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacketSilently(viewer, packet);
    }

//    public static void updateGlow(Player target) {
//        if (!glowingPlayers.contains(target)) return;
//        int entityId = target.getEntityId();
//
//        byte flags = EntityFlagCache.get(
//                viewer.getUniqueId(),
//                entityId
//        );
//
//        flags |= GLOW_BIT;
//
//        EntityFlagCache.set(
//                viewer.getUniqueId(),
//                entityId,
//                flags
//        );
//
//        EntityData data =
//                new EntityData(0, EntityDataTypes.BYTE, flags);
//
//        WrapperPlayServerEntityMetadata packet =
//                new WrapperPlayServerEntityMetadata(
//                        entityId,
//                        List.of(data)
//                );
//
//        PacketEvents.getAPI()
//                .getPlayerManager()
//                .sendPacketSilently(viewer, packet);
//        int entityId = target.getEntityId();
//
//        EntityData data = new EntityData(
//                0,
//                EntityDataTypes.BYTE,
//                GLOW_BIT
//        );
//
//        WrapperPlayServerEntityMetadata packet =
//                new WrapperPlayServerEntityMetadata(
//                        entityId,
//                        List.of(data)
//                );
//
//        for (Player viewer : Bukkit.getOnlinePlayers()) {
//            PacketEvents.getAPI()
//                    .getPlayerManager()
//                    .sendPacketSilently(viewer, packet);
//        }
//    }
}

