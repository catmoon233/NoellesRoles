package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 明星技能网络包
 * 客户端 -> 服务端
 * 
 * 当玩家按下技能键时发送，请求激活"聚光灯"技能
 * 让10格范围内的玩家视野都看向明星
 */
public record StarAbilityC2SPacket() implements CustomPayload {
    
    public static final Id<StarAbilityC2SPacket> ID = new Id<>(
        Identifier.of(Noellesroles.MOD_ID, "star_ability")
    );
    
    public static final PacketCodec<RegistryByteBuf, StarAbilityC2SPacket> CODEC = PacketCodec.unit(new StarAbilityC2SPacket());
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}