package org.agmas.noellesroles.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.Noellesroles;

public record BroadcastMessageS2CPacket(String message) implements CustomPacketPayload {
    public static final ResourceLocation BROADCAST_MESSAGE_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "broadcast_message");
    public static final Type<BroadcastMessageS2CPacket> ID = new Type<>(BROADCAST_MESSAGE_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BroadcastMessageS2CPacket> CODEC = StreamCodec.ofMember(
        (packet, buf) -> buf.writeUtf(packet.message()),
        buf -> new BroadcastMessageS2CPacket(buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public BroadcastMessageS2CPacket(Component message) {
        this(message.getString());
    }
}