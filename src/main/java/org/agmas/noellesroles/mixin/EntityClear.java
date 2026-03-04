package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.api.GameMode;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerLevel;
import org.agmas.noellesroles.utils.EntityClearUtils;
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
    @Inject(method = "trueStartGame", at = @At("HEAD"))
    private static void clearAllEntities$start(ServerLevel world, GameMode gameMode, int time) {
        EntityClearUtils.clearAllEntities(world);
    }
    /**
     * 在 finalizeGame 方法尾部注入，清除所有实体
     */
    @Inject(method = "finalizeGame", at = @At("TAIL"))
    private static void clearAllEntities$end(ServerLevel world, CallbackInfo ci) {
        EntityClearUtils.clearAllEntities(world);
    }
}
