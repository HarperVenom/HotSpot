package me.harpervenom.hotspot.utils.glow;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static me.harpervenom.hotspot.utils.glow.GlowController.setGlow;
import static me.harpervenom.hotspot.utils.glow.GlowController.updateGlow;

public final class EntityMetadataListener extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) return;
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA
                && event.getPacketType() != PacketType.Play.Server.SPAWN_PLAYER) return;

        Player viewer = event.getPlayer();

        updateGlow(viewer);
    }
}

