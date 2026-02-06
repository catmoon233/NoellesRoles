package org.agmas.noellesroles.mixin.roles.engineer;

import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.block_entity.SmallDoorBlockEntity;
import dev.doctor4t.trainmurdermystery.index.TMMItems;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.entity.LockEntityManager;
import org.agmas.noellesroles.packet.OpenLockGuiS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.doctor4t.trainmurdermystery.block.SmallDoorBlock.HALF;

/**
 * 撬锁器小游戏启动逻辑：
 * - 当尝试拿撬锁器开带锁的门时，会进入撬锁小游戏，根据结果损坏撬锁器或开门
 */
@Mixin(SmallDoorBlock.class)
public class LockMixin {
    @Inject(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z", ordinal = 0), cancellable = true)
    private void injectLockPickGame(
            BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        // 用于小游戏结束后查询锁的位置
        BlockPos lockPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos;
        boolean canBeAffectedByLock =
                // 如果该物品会被锁影响，就继续加|| xxx 即可，然后在下面的if语句里具体操作
                player.getMainHandItem().is(TMMItems.LOCKPICK)
                        || player.getMainHandItem().is(ModItems.MASTER_KEY_P)
                        || player.getMainHandItem().is(ModItems.MASTER_KEY)
                        || player.getMainHandItem().is(TMMItems.KEY);
        if (canBeAffectedByLock) {
            // 当当前门上无锁时，检查附近门的情况：实现锁对附近门的影响
            if (LockEntityManager.getInstance().getLockEntity(lockPos) == null) {
                if (world.getBlockEntity(lockPos.below()) instanceof SmallDoorBlockEntity entity) {
                    switch (entity.getFacing()) {
                        case NORTH:
                        case SOUTH:
                            if (LockEntityManager.getInstance().getLockEntity(lockPos.east()) != null)
                                lockPos = lockPos.east();
                            else if (LockEntityManager.getInstance().getLockEntity(lockPos.west()) != null) {
                                lockPos = lockPos.west();
                            }
                            break;
                        case EAST:
                        case WEST:
                            if (LockEntityManager.getInstance().getLockEntity(lockPos.north()) != null)
                                lockPos = lockPos.north();
                            else if (LockEntityManager.getInstance().getLockEntity(lockPos.south()) != null) {
                                lockPos = lockPos.south();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }

            if (LockEntityManager.getInstance().getLockEntity(lockPos) != null) {
                // 根据手中物品决定锁的影响
                if (player.getMainHandItem().is(TMMItems.LOCKPICK)) {
                    // 当手持撬锁器且该门上锁时：进入撬锁小游戏
                    player.displayClientMessage(
                            Component.translatable("message.lock.game.start").withStyle(ChatFormatting.AQUA), true);
                    player.playNotifySound(SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.5f, 1.5f);
                    // 客户端：打开GUI
                    var lockEntity = LockEntityManager.getInstance().getLockEntity(lockPos);
                    if (lockEntity != null) {
                        if (player instanceof ServerPlayer serverPlayer) {
                            ServerPlayNetworking.send(serverPlayer, new OpenLockGuiS2CPacket(lockPos,
                                    lockEntity.getUUID(), lockEntity.getLength()));
                        }
                    }
                    // 返回 false 阻止原始方法执行
                    cir.setReturnValue(InteractionResult.FAIL);
                } else {
                    // 默认行为：阻止原操作
                    cir.setReturnValue(InteractionResult.FAIL);
                }

            }
        }
    }

}
