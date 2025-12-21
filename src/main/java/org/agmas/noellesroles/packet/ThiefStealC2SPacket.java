package org.agmas.noellesroles.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

import java.util.UUID;

public record ThiefStealC2SPacket(UUID target) implements CustomPayload {
    public static final Identifier STEAL_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "steal");
    public static final Id<ThiefStealC2SPacket> ID = new Id<>(STEAL_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, ThiefStealC2SPacket> CODEC = PacketCodec.of(
            ThiefStealC2SPacket::write,
            ThiefStealC2SPacket::read
    );

    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.target);
    }

    public static ThiefStealC2SPacket read(PacketByteBuf buf) {
        UUID target = buf.readUuid();
        return new ThiefStealC2SPacket(target);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}