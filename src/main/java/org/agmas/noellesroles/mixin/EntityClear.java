package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.agmas.noellesroles.entity.LockEntityManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 清除实体 Mixin
 */
@Mixin(GameFunctions.class)
public class EntityClear {
    /**
     * 在 finalizeGame 方法尾部注入，清除所有实体
     */
    @Inject(method = "finalizeGame", at = @At("TAIL"))
    private static void clearAllEntities(ServerLevel world, CallbackInfo ci) {
        // 清除所有锁实体及其映射
        LockEntityManager.getInstance().resetLockEntities();
    }
}
