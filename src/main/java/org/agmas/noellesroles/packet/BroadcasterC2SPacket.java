package org.agmas.noellesroles.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

public record BroadcasterC2SPacket(String message) implements CustomPayload {
    public static final Identifier BROADCASTER_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "broadcaster");
    public static final Id<BroadcasterC2SPacket> ID = new Id<>(BROADCASTER_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, BroadcasterC2SPacket> CODEC = PacketCodec.of(
        (packet, buf) -> buf.writeString(packet.message()),
        buf -> new BroadcasterC2SPacket(buf.readString())
    );

    public BroadcasterC2SPacket() {
        this("");
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}