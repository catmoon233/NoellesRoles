package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 歌手技能网络包
 * 客户端 -> 服务端
 * 
 * 当玩家按下技能键时发送，请求播放随机音乐
 */
public record SingerAbilityC2SPacket() implements CustomPayload {
    
    public static final Id<SingerAbilityC2SPacket> ID = new Id<>(
        Identifier.of(Noellesroles.MOD_ID, "singer_ability")
    );
    
    public static final PacketCodec<RegistryByteBuf, SingerAbilityC2SPacket> CODEC = PacketCodec.unit(new SingerAbilityC2SPacket());
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}