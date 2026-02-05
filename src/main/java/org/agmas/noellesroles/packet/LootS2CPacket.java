package org.agmas.noellesroles.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.Noellesroles;

/**
 * 抽奖结果包
 * @param ansID 抽奖结果 ID
 * <p>
 * 客户端根据结果生成动画
 * </p>
 */
public record LootS2CPacket(int ansID) implements CustomPacketPayload {
    public static final ResourceLocation LOOT_PAYLOAD_ID =
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "loot");
    public static final Type<LootS2CPacket> ID = new Type<>(LOOT_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, LootS2CPacket> CODEC;
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(ansID);
    }

    public static LootS2CPacket read(FriendlyByteBuf buf) {
        return new LootS2CPacket(buf.readInt());
    }
    static {
        CODEC = StreamCodec.ofMember(LootS2CPacket::write, LootS2CPacket::read);
    }
}
