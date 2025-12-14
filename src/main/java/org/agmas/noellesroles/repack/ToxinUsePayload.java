package org.agmas.noellesroles.repack;

import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;

public record ToxinUsePayload(int target) implements CustomPayload {
    public static final CustomPayload.Id<ToxinUsePayload> ID = new CustomPayload.Id(Noellesroles.id("toxinuse"));
    public static final PacketCodec<PacketByteBuf, ToxinUsePayload> CODEC;

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    static {
        CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, ToxinUsePayload::target, ToxinUsePayload::new);
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<ToxinUsePayload> {
        public void receive(@NotNull ToxinUsePayload payload, ServerPlayNetworking.@NotNull Context context) {
            ServerPlayerEntity player = context.player();
            Entity var5 = player.getServerWorld().getEntityById(payload.target());
            if (var5 instanceof PlayerEntity target) {
                if (!((double)target.distanceTo(player) > (double)3.0F)) {
                    ((PlayerPoisonComponent)PlayerPoisonComponent.KEY.get(target)).setPoisonTicks(HSRConstants.toxinPoisonTime, player.getUuid());
                    player.playSound(HSRSounds.ITEM_SYRINGE_STAB, 0.15F, 1.0F);
                    player.swingHand(Hand.MAIN_HAND);
                    if (!player.isCreative()) {
                        player.getMainHandStack().decrement(1);
                        player.getItemCooldownManager().set(HSRItems.TOXIN, (Integer) HSRConstants.ITEM_COOLDOWNS.get(HSRItems.TOXIN));
                    }

                }
            }
        }
    }
}
