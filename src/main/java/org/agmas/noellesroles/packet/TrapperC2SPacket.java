package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 设陷者技能网络包
 * 用于客户端向服务端发送放置陷阱请求
 */
public record TrapperC2SPacket() implements CustomPayload {
    
    public static final Id<TrapperC2SPacket> ID = new Id<>(
        Identifier.of(Noellesroles.MOD_ID, "trapper_ability")
    );
    
    public static final PacketCodec<RegistryByteBuf, TrapperC2SPacket> CODEC = PacketCodec.of(
        (packet, buf) -> {
            // 无需写入数据，只是触发技能
        },
        buf -> new TrapperC2SPacket()
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}