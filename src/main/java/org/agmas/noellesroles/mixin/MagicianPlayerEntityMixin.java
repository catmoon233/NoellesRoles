package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 魔术师玩家实体Mixin
 * - 处理假球棒的攻击（只击退，不击杀）
 */
@Mixin(Player.class)
public class MagicianPlayerEntityMixin {

    /**
     * 拦截玩家攻击逻辑
     * 如果玩家手持假球棒且目标是玩家，则只造成击退而不击杀
     */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void noellesroles$handleFakeBatAttack(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());

        // 检查是否是魔术师手持假球棒攻击玩家
        var magicianRole = TMMRoles.ROLES.get(ModRoles.MAGICIAN_ID);
        if (magicianRole != null && gameWorld.isRole(player, magicianRole) &&
                player.getMainHandItem().is(ModItems.FAKE_BAT) &&
                target instanceof Player) {

            // 假球棒只造成击退效果
            Player targetPlayer = (Player) target;

            // 击退目标
            double knockbackStrength = 0.8;
            double dx = targetPlayer.getX() - player.getX();
            double dy = targetPlayer.getY() - player.getY();
            double dz = targetPlayer.getZ() - player.getZ();

            // 归一化方向向量
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance > 0) {
                dx /= distance;
                dy /= distance;
                dz /= distance;

                // 应用击退
                targetPlayer.setDeltaMovement(
                        targetPlayer.getDeltaMovement().x() + dx * knockbackStrength,
                        targetPlayer.getDeltaMovement().y() + dy * knockbackStrength + 0.2,
                        targetPlayer.getDeltaMovement().z() + dz * knockbackStrength
                );
                targetPlayer.hurtMarked = true;
            }

            // 取消原始攻击逻辑
            ci.cancel();
        }
    }
}
