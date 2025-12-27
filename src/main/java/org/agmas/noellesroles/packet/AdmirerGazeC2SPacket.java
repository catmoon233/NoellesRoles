package org.agmas.noellesroles.packet;


import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

/**
 * 慕恋者窥视网络包
 * 客户端 -> 服务端
 * 
 * 当玩家按下/松开技能键时发送，用于控制窥视状态
 */
public record AdmirerGazeC2SPacket(boolean gazing) implements CustomPayload {
    
    public static final Id<AdmirerGazeC2SPacket> ID = new Id<>(
        Identifier.of(Noellesroles.MOD_ID, "admirer_gaze")
    );
    
    public static final PacketCodec<RegistryByteBuf, AdmirerGazeC2SPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOL, AdmirerGazeC2SPacket::gazing,
        AdmirerGazeC2SPacket::new
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}