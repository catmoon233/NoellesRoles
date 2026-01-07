package org.agmas.noellesroles.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.Noellesroles;

public record BroadcasterC2SPacket(String message) implements CustomPacketPayload {
    public static final ResourceLocation BROADCASTER_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "broadcaster");
    public static final Type<BroadcasterC2SPacket> ID = new Type<>(BROADCASTER_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, BroadcasterC2SPacket> CODEC = StreamCodec.ofMember(
        (packet, buf) -> buf.writeUtf(packet.message()),
        buf -> new BroadcasterC2SPacket(buf.readUtf())
    );

    public BroadcasterC2SPacket() {
        this("");
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}