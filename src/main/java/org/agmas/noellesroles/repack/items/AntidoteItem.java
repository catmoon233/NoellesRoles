package org.agmas.noellesroles.repack.items;

import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;


import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.agmas.noellesroles.repack.AntidoteUsePayload;
import org.agmas.noellesroles.repack.HSRConstants;
import org.agmas.noellesroles.repack.HSRItems;
import org.agmas.noellesroles.repack.HSRSounds;
import org.jetbrains.annotations.NotNull;

public class AntidoteItem extends Item {
    public AntidoteItem(Settings settings) {
        super(settings);
    }

    public TypedActionResult<ItemStack> use(World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.setCurrentHand(hand);
        return TypedActionResult.consume(itemStack);
    }

    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!user.isSpectator()) {
            if (remainingUseTicks < this.getMaxUseTime(stack, user) - 10 && user instanceof PlayerEntity) {
                PlayerEntity attacker = (PlayerEntity)user;
                    HitResult collision = getAntidoteTarget(attacker);
                    if (collision instanceof EntityHitResult) {
                        EntityHitResult entityHitResult = (EntityHitResult) collision;
                        Entity target = entityHitResult.getEntity();
                        if (attacker instanceof ServerPlayerEntity player) {


                                if (!((double)target.distanceTo(player) > (double)3.0F)) {
                                    ((PlayerPoisonComponent)PlayerPoisonComponent.KEY.get(target)).reset();
                                    target.playSound(HSRSounds.ITEM_SYRINGE_STAB, 0.4F, 1.0F);
                                    final var blockPos = target.getBlockPos();
                                    ((ServerWorld) world).playSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS,1.4F, 1.0F,false);
                                    player.swingHand(Hand.MAIN_HAND);
                                    if (!player.isCreative()) {
                                        player.getItemCooldownManager().set(HSRItems.ANTIDOTE, (Integer) HSRConstants.ITEM_COOLDOWNS.get(HSRItems.ANTIDOTE));
                                    }


                            }                        }


                    return;
                }
            }

        }
    }

    public static HitResult getAntidoteTarget(PlayerEntity user) {
        return ProjectileUtil.getCollision(user, (entity) -> {
            boolean var10000;
            if (entity instanceof PlayerEntity player) {
                if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                    var10000 = true;
                    return var10000;
                }
            }

            var10000 = false;
            return var10000;
        }, (double)3.0F);
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }
}
