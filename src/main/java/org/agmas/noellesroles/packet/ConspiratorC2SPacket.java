package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * 阴谋家猜测网络包
 * 
 * 从客户端发送到服务端，包含：
 * - 目标玩家 UUID
 * - 猜测的角色 ID
 */
public record ConspiratorC2SPacket(UUID targetPlayer, String roleId) implements CustomPayload {
    
    public static final Identifier CONSPIRATOR_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "conspirator_guess");
    public static final Id<ConspiratorC2SPacket> ID = new Id<>(CONSPIRATOR_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, ConspiratorC2SPacket> CODEC;
    
    public ConspiratorC2SPacket(UUID targetPlayer, String roleId) {
        this.targetPlayer = targetPlayer;
        this.roleId = roleId;
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.targetPlayer);
        buf.writeString(this.roleId);
    }
    
    public static ConspiratorC2SPacket read(PacketByteBuf buf) {
        return new ConspiratorC2SPacket(buf.readUuid(), buf.readString());
    }
    
    public UUID targetPlayer() {
        return this.targetPlayer;
    }
    
    public String roleId() {
        return this.roleId;
    }
    
    static {
        CODEC = PacketCodec.of(ConspiratorC2SPacket::write, ConspiratorC2SPacket::read);
    }
}