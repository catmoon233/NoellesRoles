package org.agmas.noellesroles.packet.Loot;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.Noellesroles;

import java.util.List;

/**
 * 抽奖信息页发包
 * <p>
 *     NOTE:
 *      虽然目前为空，但是服务器有轮换卡池、up卡池，需要将卡池信息发给客户端用于显示
 * </p>
 * TODO : 发送卡池信息
 */
public record LootPoolsInfoCheckS2CPacket(List<Integer> poolIDs) implements CustomPacketPayload {
    public static ResourceLocation LOOT_POOLS_INFO_CHECK_PAYLOAD_ID =
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "loot_screen");
    public static final Type<LootPoolsInfoCheckS2CPacket> ID = new Type<>(LOOT_POOLS_INFO_CHECK_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, LootPoolsInfoCheckS2CPacket> CODEC;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(poolIDs, FriendlyByteBuf::writeInt);
    }

    public static LootPoolsInfoCheckS2CPacket read(FriendlyByteBuf buf) {
        return new LootPoolsInfoCheckS2CPacket(
                buf.readList(FriendlyByteBuf::readInt)
        );
    }
    static {
        CODEC = StreamCodec.ofMember(LootPoolsInfoCheckS2CPacket::write, LootPoolsInfoCheckS2CPacket::read);
    }

}
