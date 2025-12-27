package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * 邮差传递网络包
 * 
 * 从客户端发送到服务端，包含：
 * - 操作类型（打开界面、放入物品、确认交换、取消）
 * - 目标玩家 UUID
 * - 物品数据（如果有）
 */
public record PostmanC2SPacket(
    Action action,
    UUID targetPlayer,
    ItemStack item
) implements CustomPayload {
    
    public static final Identifier POSTMAN_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "postman_delivery");
    public static final Id<PostmanC2SPacket> ID = new Id<>(POSTMAN_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, PostmanC2SPacket> CODEC;
    
    public enum Action {
        OPEN_DELIVERY,      // 打开传递界面
        SET_ITEM,           // 放入物品
        CONFIRM,            // 确认交换
        CANCEL              // 取消传递
    }
    
    public PostmanC2SPacket(Action action, UUID targetPlayer, ItemStack item) {
        this.action = action;
        this.targetPlayer = targetPlayer;
        this.item = item != null ? item : ItemStack.EMPTY;
    }
    
    // 简化构造函数（无物品）
    public PostmanC2SPacket(Action action, UUID targetPlayer) {
        this(action, targetPlayer, ItemStack.EMPTY);
    }
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public void write(RegistryByteBuf buf) {
        buf.writeEnumConstant(this.action);
        buf.writeUuid(this.targetPlayer);
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, this.item);
    }
    
    public static PostmanC2SPacket read(RegistryByteBuf buf) {
        Action action = buf.readEnumConstant(Action.class);
        UUID targetPlayer = buf.readUuid();
        ItemStack item = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
        return new PostmanC2SPacket(action, targetPlayer, item);
    }
    
    public Action action() {
        return this.action;
    }
    
    public UUID targetPlayer() {
        return this.targetPlayer;
    }
    
    public ItemStack item() {
        return this.item;
    }
    
    static {
        CODEC = PacketCodec.of(PostmanC2SPacket::write, PostmanC2SPacket::read);
    }
}