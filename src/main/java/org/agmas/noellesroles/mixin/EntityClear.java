package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.entity.NoteEntity;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.item.ItemEntity;

import org.agmas.noellesroles.entity.LockEntity;
import org.agmas.noellesroles.entity.LockEntityManager;
import org.agmas.noellesroles.utils.RoleUtils;
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
        // 先清除所有锁实体及其映射
        LockEntityManager.getInstance().resetLockEntities();

        try {


            // 清除玩家属性
            for (var pl : world.players()) {
                RoleUtils.RemoveAllPlayerAttributes(pl);
            }

            // 收集需要删除的实体列表，避免在遍历过程中修改集合
            java.util.List<net.minecraft.world.entity.Entity> entitiesToRemove = new java.util.ArrayList<>();

            world.getAllEntities().forEach((entity) -> {
                if (entity instanceof LockEntity ||
                        entity instanceof AreaEffectCloud ||
                        entity instanceof ItemEntity ||
                        entity instanceof PlayerBodyEntity ||
                        entity instanceof NoteEntity) {
                    entitiesToRemove.add(entity);
                }
            });

            // 安全地删除收集到的实体
            for (net.minecraft.world.entity.Entity entity : entitiesToRemove) {
                if (!entity.isRemoved()) { // 双重检查确保实体未被其他地方删除
                    entity.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                }
            }
        }catch (Exception ignored){
            ignored.printStackTrace();
        }
    }
}
