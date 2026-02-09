package org.agmas.noellesroles.mixin.roles.nianshou;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import org.agmas.noellesroles.component.NianShouPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.player.Player;

/**
 * 处理任务完成，年兽每完成2个任务获得1个红包
 */
@Mixin(targets = {"dev.doctor4t.trainmurdermystery.util.TaskManager$TaskState"}, remap = false)
public class TaskMixin {

    @Inject(method = "complete", at = @At("HEAD"), remap = false)
    private void onTaskComplete(CallbackInfo ci) {
        // 获取玩家实例
        Object taskState = this;
        try {
            java.lang.reflect.Field playerField = taskState.getClass().getDeclaredField("player");
            playerField.setAccessible(true);
            Player player = (Player) playerField.get(taskState);

            if (player == null)
                return;

            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());

            // 检查是否是年兽
            if (gameWorld.isRole(player, ModRoles.NIAN_SHOU)) {
                NianShouPlayerComponent nianShouComponent = NianShouPlayerComponent.KEY.get(player);
                nianShouComponent.onTaskCompleted();
            }
        } catch (Exception e) {
            // 忽略错误
        }
    }
}
