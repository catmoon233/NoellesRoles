package org.agmas.noellesroles.mixin.roles.engineer;

import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.agmas.noellesroles.entity.LockEntityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 撬锁器小游戏启动逻辑：
 * - 当尝试拿撬锁器开带锁的门时，会进入撬锁小游戏，根据结果损坏撬锁器或开门
 */
@Mixin(SmallDoorBlock.class)
public class LockMixin {
    @Inject(
            method = "useWithoutItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void injectLockPickGame(
            BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
            )
    {
        /**
         * 判断门是否有锁，理论上应该是对于门的上下两半都应该定位到门的上半部分的格子进行查找
         * 但正常门上面应该不会有门，因此门上半部分的上面一格也将不会有锁（如果门的上一格有锁此逻辑也会阻塞）
         * 所以对于正常情况直接判断即可，如果有门上加门的特殊情况，那就改一下（虽然概率很低）
         */
        if((LockEntityManager.getInstance().getLockEntity(pos) != null ||
            LockEntityManager.getInstance().getLockEntity(pos.above()) != null) &&
                player.getMainHandItem().is(TMMItems.LOCKPICK))
        {
            // 当手持撬锁器且该门上锁时：进入撬锁小游戏

            //TODO : 让玩家执行撬锁小游戏
            player.displayClientMessage(Component.literal("开始撬锁小游戏"), true);

            // 返回 false 阻止原始方法执行
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

}
