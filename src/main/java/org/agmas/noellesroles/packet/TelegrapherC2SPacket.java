package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 电报员发送消息网络包
 * 
 * 从客户端发送到服务端，包含：
 * - 要发送的匿名消息内容
 */
public record TelegrapherC2SPacket(String message) implements CustomPayload {
    
    public static final Identifier TELEGRAPHER_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "telegrapher_message");
    public static final Id<TelegrapherC2SPacket> ID = new Id<>(TELEGRAPHER_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, TelegrapherC2SPacket> CODEC;
    
    public TelegrapherC2SPacket(String message) {
        this.message = message;
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public void write(PacketByteBuf buf) {
        buf.writeString(this.message);
    }
    
    public static TelegrapherC2SPacket read(PacketByteBuf buf) {
        return new TelegrapherC2SPacket(buf.readString());
    }
    
    public String message() {
        return this.message;
    }
    
    static {
        CODEC = PacketCodec.of(TelegrapherC2SPacket::write, TelegrapherC2SPacket::read);
    }
}