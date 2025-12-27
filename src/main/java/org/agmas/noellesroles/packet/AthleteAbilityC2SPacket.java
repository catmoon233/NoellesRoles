package org.agmas.noellesroles.packet;


import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

/**
 * 运动员技能网络包
 * 客户端 -> 服务端
 * 
 * 当玩家按下技能键时发送，请求激活疾跑技能
 */
public record AthleteAbilityC2SPacket() implements CustomPayload {
    
    public static final Id<AthleteAbilityC2SPacket> ID = new Id<>(
        Identifier.of(Noellesroles.MOD_ID, "athlete_ability")
    );
    
    public static final PacketCodec<RegistryByteBuf, AthleteAbilityC2SPacket> CODEC = PacketCodec.unit(new AthleteAbilityC2SPacket());
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}