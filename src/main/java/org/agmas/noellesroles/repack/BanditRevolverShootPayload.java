package org.agmas.noellesroles.repack;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.index.tag.TMMItemTags;
import dev.doctor4t.trainmurdermystery.util.GunDropPayload;
import dev.doctor4t.trainmurdermystery.util.Scheduler;
import dev.doctor4t.trainmurdermystery.util.ShootMuzzleS2CPayload;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;

public record BanditRevolverShootPayload(int target) implements CustomPacketPayload {
    public static final Type<BanditRevolverShootPayload> ID = new Type<>(Noellesroles.id("banditgunshoot"));;
    public static final StreamCodec<FriendlyByteBuf, BanditRevolverShootPayload> CODEC;

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    static {
        CODEC = StreamCodec.composite(ByteBufCodecs.INT, BanditRevolverShootPayload::target,
                BanditRevolverShootPayload::new);
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<BanditRevolverShootPayload> {
        @Override
        public void receive(@NotNull BanditRevolverShootPayload payload,
                ServerPlayNetworking.@NotNull Context context) {
            final var player = context.player();
            extracted(player, player.serverLevel().getEntity(payload.target()));
        }
    }

    public static void extracted(ServerPlayer player, Entity var6) {

        ItemStack mainHandStack = player.getMainHandItem();
        if (mainHandStack.is(TMMItemTags.GUNS)) {
            if (!player.getCooldowns().isOnCooldown(mainHandStack.getItem())) {
                player.level().playSound((Player) null, player.getX(), player.getEyeY(), player.getZ(),
                        TMMSounds.ITEM_REVOLVER_CLICK, SoundSource.PLAYERS, 0.5F,
                        1.0F + player.getRandom().nextFloat() * 0.1F - 0.05F);

                if (var6 instanceof Player) {
                    Player target = (Player) var6;
                    if ((double) target.distanceTo(player) < (double) 70) {
                        GameWorldComponent game = (GameWorldComponent) GameWorldComponent.KEY.get(player.level());
                        Item banditrevolver = HSRItems.BANDIT_REVOLVER;
                        boolean backfire = false;
                        if (game.isInnocent(target) && !player.isCreative() && mainHandStack.is(banditrevolver)) {
                            //

                            if (player.getRandom().nextFloat() <= 0.2F) {
                                Scheduler.schedule(() -> {
                                    if (player.getInventory().contains((s) -> s.is(TMMItemTags.GUNS))) {
                                        player.getInventory().clearOrCountMatchingItems((s) -> s.is(banditrevolver), 1,
                                                player.getInventory());
                                        ItemEntity item = player.drop(TMMItems.REVOLVER.getDefaultInstance(), false,
                                                false);
                                        if (item != null) {
                                            item.setPickUpDelay(10);
                                            item.setThrower(player);
                                        }

                                        ServerPlayNetworking.send(player, new GunDropPayload());
                                    }
                                }, 4);
                            }
                        }
                        if (!backfire) {
                            GameFunctions.killPlayer(target, true, player, GameConstants.DeathReasons.REVOLVER);
                        }
                    }
                }

                player.level().playSound((Player) null, player.getX(), player.getEyeY(), player.getZ(),
                        TMMSounds.ITEM_REVOLVER_SHOOT, SoundSource.PLAYERS, 5.0F,
                        1.0F + player.getRandom().nextFloat() * 0.1F - 0.05F);

                for (ServerPlayer tracking : PlayerLookup.tracking(player)) {
                    ServerPlayNetworking.send(tracking, new ShootMuzzleS2CPayload(player.getId()));
                }

                ServerPlayNetworking.send(player, new ShootMuzzleS2CPayload(player.getId()));
            }
        }
    }
}