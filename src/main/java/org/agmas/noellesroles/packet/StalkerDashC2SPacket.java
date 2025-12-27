package org.agmas.noellesroles.packet;


import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

/**
 * 跟踪者突进网络包
 * 客户端 -> 服务端
 * 
 * 用于控制三阶段的蓄力突进
 * - charging=true: 开始蓄力
 * - charging=false: 释放突进
 */
public record StalkerDashC2SPacket(boolean charging) implements CustomPayload {
    
    public static final Id<StalkerDashC2SPacket> ID = new Id<>(
        Identifier.of(Noellesroles.MOD_ID, "stalker_dash")
    );
    
    public static final PacketCodec<RegistryByteBuf, StalkerDashC2SPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOL, StalkerDashC2SPacket::charging,
        StalkerDashC2SPacket::new
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}