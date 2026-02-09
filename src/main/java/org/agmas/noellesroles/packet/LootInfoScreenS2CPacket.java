package org.agmas.noellesroles.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.Noellesroles;

/**
 * 抽奖信息页发包
 * <p>
 *     NOTE:
 *      虽然目前为空，但是服务器有轮换卡池、up卡池，需要将卡池信息发给客户端用于显示
 * </p>
 * TODO : 发送卡池信息
 */
public record LootInfoScreenS2CPacket() implements CustomPacketPayload {
    public static ResourceLocation LOOT_SCREEN_PAYLOAD_ID =
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "loot_screen");
    public static final Type<LootInfoScreenS2CPacket> ID = new Type<>(LOOT_SCREEN_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, LootInfoScreenS2CPacket> CODEC;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void write(FriendlyByteBuf buf) {
    }

    public static LootInfoScreenS2CPacket read(FriendlyByteBuf buf) {
        return new LootInfoScreenS2CPacket();
    }
    static {
        CODEC = StreamCodec.ofMember(LootInfoScreenS2CPacket::write, LootInfoScreenS2CPacket::read);
    }

}
