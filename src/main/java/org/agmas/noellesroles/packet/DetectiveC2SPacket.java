package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * 私家侦探审查玩家网络包
 * 
 * 从客户端发送到服务端，包含：
 * - 要审查的目标玩家UUID
 */
public record DetectiveC2SPacket(UUID targetUuid) implements CustomPayload {
    
    public static final Identifier DETECTIVE_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "detective_inspect");
    public static final Id<DetectiveC2SPacket> ID = new Id<>(DETECTIVE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, DetectiveC2SPacket> CODEC;
    
    public DetectiveC2SPacket(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.targetUuid);
    }
    
    public static DetectiveC2SPacket read(PacketByteBuf buf) {
        return new DetectiveC2SPacket(buf.readUuid());
    }
    
    public UUID targetUuid() {
        return this.targetUuid;
    }
    
    static {
        CODEC = PacketCodec.of(DetectiveC2SPacket::write, DetectiveC2SPacket::read);
    }
}