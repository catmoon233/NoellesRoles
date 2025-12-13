package org.agmas.noellesroles.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

public record BroadcastMessageS2CPacket(String message) implements CustomPayload {
    public static final Identifier BROADCAST_MESSAGE_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "broadcast_message");
    public static final Id<BroadcastMessageS2CPacket> ID = new Id<>(BROADCAST_MESSAGE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, BroadcastMessageS2CPacket> CODEC = PacketCodec.of(
        (packet, buf) -> buf.writeString(packet.message()),
        buf -> new BroadcastMessageS2CPacket(buf.readString())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public BroadcastMessageS2CPacket(Text message) {
        this(message.getString());
    }
}