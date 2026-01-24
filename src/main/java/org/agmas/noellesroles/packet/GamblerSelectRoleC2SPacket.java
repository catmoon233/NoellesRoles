package org.agmas.noellesroles.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent;
import org.jetbrains.annotations.NotNull;

public record GamblerSelectRoleC2SPacket(ResourceLocation roleId) implements CustomPacketPayload {
    public static final Type<GamblerSelectRoleC2SPacket> ID = new Type<>(ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "gambler_select_role"));
    public static final StreamCodec<FriendlyByteBuf, GamblerSelectRoleC2SPacket> CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, GamblerSelectRoleC2SPacket::roleId,
        GamblerSelectRoleC2SPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<GamblerSelectRoleC2SPacket> {
        @Override
        public void receive(@NotNull GamblerSelectRoleC2SPacket payload, ServerPlayNetworking.@NotNull Context context) {
            context.player().server.execute(() -> {
                GamblerPlayerComponent component = GamblerPlayerComponent.KEY.get(context.player());
                component.selectRole(payload.roleId());
            });
        }
    }
}