package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 拳击手技能网络包
 * 客户端 -> 服务端
 * 
 * 当玩家按下技能键时发送，请求激活钢筋铁骨技能
 */
public record BoxerAbilityC2SPacket() implements CustomPayload {
    
    public static final Id<BoxerAbilityC2SPacket> ID = new Id<>(
        Identifier.of(Noellesroles.MOD_ID, "boxer_ability")
    );
    
    public static final PacketCodec<RegistryByteBuf, BoxerAbilityC2SPacket> CODEC = PacketCodec.unit(new BoxerAbilityC2SPacket());
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}