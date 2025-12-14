package org.agmas.noellesroles.repack;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;

public  record BanditRevolverShootPayload(int target) implements CustomPayload {
    public static final CustomPayload.Id<BanditRevolverShootPayload> ID = new CustomPayload.Id(Noellesroles.id("banditgunshoot"));
    public static final PacketCodec<PacketByteBuf, BanditRevolverShootPayload> CODEC;

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    static {
        CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, BanditRevolverShootPayload::target, BanditRevolverShootPayload::new);
    }

    public static class Receiver implements ServerPlayNetworking.PlayPayloadHandler<BanditRevolverShootPayload> {
        public void receive(@NotNull BanditRevolverShootPayload payload, ServerPlayNetworking.@NotNull Context context) {
            final var player = context.player();
            extracted(player, player.getServerWorld().getEntityById(payload.target()));
        }
    }

    public static void extracted(ServerPlayerEntity player, Entity var6) {

        ItemStack mainHandStack = player.getMainHandStack();
        if (mainHandStack.isIn(TMMItemTags.GUNS)) {
            if (!player.getItemCooldownManager().isCoolingDown(mainHandStack.getItem())) {
                player.getWorld().playSound((PlayerEntity)null, player.getX(), player.getEyeY(), player.getZ(), TMMSounds.ITEM_REVOLVER_CLICK, SoundCategory.PLAYERS, 0.5F, 1.0F + player.getRandom().nextFloat() * 0.1F - 0.05F);

                if (var6 instanceof PlayerEntity) {
                    PlayerEntity target = (PlayerEntity)var6;
                    if ((double)target.distanceTo(player) < (double)65.0F) {
                        GameWorldComponent game = (GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld());
                        Item banditrevolver = HSRItems.BANDIT_REVOLVER;
                        boolean backfire = false;
                        if (game.isInnocent(target) && !player.isCreative() && mainHandStack.isOf(banditrevolver)) {
                            if (game.isInnocent(player) && player.getRandom().nextFloat() <= game.getBackfireChance()) {
                                backfire = true;
                                GameFunctions.killPlayer(player, true, player, GameConstants.DeathReasons.GUN);
                            } else if (game.isRole(player, Noellesroles.BANDIT)) {
                                if (player.getRandom().nextFloat() <= 0.2F) {
                                    Scheduler.schedule(() -> {
                                        if (player.getInventory().contains((s) -> s.isIn(TMMItemTags.GUNS))) {
                                            player.getInventory().remove((s) -> s.isOf(banditrevolver), 1, player.getInventory());
                                            ItemEntity item = player.dropItem(TMMItems.REVOLVER.getDefaultStack(), false, false);
                                            if (item != null) {
                                                item.setPickupDelay(10);
                                                item.setThrower(player);
                                            }

                                            ServerPlayNetworking.send(player, new GunDropPayload());
                                        }
                                    }, 4);
                                } else {
                                    Scheduler.schedule(() -> {
                                        if (player.getInventory().contains((s) -> s.isIn(TMMItemTags.GUNS))) {
                                            player.getInventory().remove((s) -> s.isOf(banditrevolver), 1, player.getInventory());
                                        }
                                    }, 4);
                                }
                            } else {
                                Scheduler.schedule(() -> {
                                    if (player.getInventory().contains((s) -> s.isIn(TMMItemTags.GUNS))) {
                                        player.getInventory().remove((s) -> s.isOf(banditrevolver), 1, player.getInventory());
                                        ItemEntity item = player.dropItem(TMMItems.REVOLVER.getDefaultStack(), false, false);
                                        if (item != null) {
                                            item.setPickupDelay(10);
                                            item.setThrower(player);
                                        }

                                        ServerPlayNetworking.send(player, new GunDropPayload());
                                        ((PlayerMoodComponent)PlayerMoodComponent.KEY.get(player)).setMood(0.0F);
                                    }
                                }, 4);
                            }
                        }

                        if (!backfire) {
                            GameFunctions.killPlayer(target, true, player, GameConstants.DeathReasons.GUN);
                        }
                    }
                }

                player.getWorld().playSound((PlayerEntity)null, player.getX(), player.getEyeY(), player.getZ(), TMMSounds.ITEM_REVOLVER_SHOOT, SoundCategory.PLAYERS, 5.0F, 1.0F + player.getRandom().nextFloat() * 0.1F - 0.05F);

                for(ServerPlayerEntity tracking : PlayerLookup.tracking(player)) {
                    ServerPlayNetworking.send(tracking, new ShootMuzzleS2CPayload(player.getId()));
                }

                ServerPlayNetworking.send(player, new ShootMuzzleS2CPayload(player.getId()));
            }
        }
    }
}