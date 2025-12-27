package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * 心理学家治疗网络包
 * 客户端 -> 服务端
 * 
 * 当玩家按下技能键并瞄准玩家时发送，请求开始心理治疗
 * - 包含目标玩家UUID
 */
public record PsychologistC2SPacket(UUID targetUuid) implements CustomPayload {
    
    public static final Identifier PSYCHOLOGIST_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "psychologist_heal");
    public static final Id<PsychologistC2SPacket> ID = new Id<>(PSYCHOLOGIST_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, PsychologistC2SPacket> CODEC;
    
    public PsychologistC2SPacket(UUID targetUuid) {
        this.targetUuid = targetUuid;
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.targetUuid);
    }
    
    public static PsychologistC2SPacket read(PacketByteBuf buf) {
        return new PsychologistC2SPacket(buf.readUuid());
    }
    
    public UUID targetUuid() {
        return this.targetUuid;
    }
    
    static {
        CODEC = PacketCodec.of(PsychologistC2SPacket::write, PsychologistC2SPacket::read);
    }
}