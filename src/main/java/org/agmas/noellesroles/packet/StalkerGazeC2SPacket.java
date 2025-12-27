package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 跟踪者窥视网络包
 * 客户端 -> 服务端
 * 
 * 当玩家按下/松开技能键时发送，用于控制窥视状态
 */
public record StalkerGazeC2SPacket(boolean gazing) implements CustomPayload {
    
    public static final Id<StalkerGazeC2SPacket> ID = new Id<>(
        Identifier.of(Noellesroles.MOD_ID, "stalker_gaze")
    );
    
    public static final PacketCodec<RegistryByteBuf, StalkerGazeC2SPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOL, StalkerGazeC2SPacket::gazing,
        StalkerGazeC2SPacket::new
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}