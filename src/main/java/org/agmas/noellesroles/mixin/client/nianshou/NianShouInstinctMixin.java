package org.agmas.noellesroles.mixin.client.nianshou;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

/**
 * 年兽在杀手本能中显示为绿色（好人）
 * 参考纵火犯的实现
 */
@Mixin(TMMClient.class)
public class NianShouInstinctMixin {

    @Shadow
    public static KeyMapping instinctKeybind;

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void nianShouGreenGlow(Entity target, CallbackInfoReturnable<Integer> cir) {
        var player = Minecraft.getInstance().player;
        if (player == null || !(target instanceof Player targettedPlayer)) {
            return;
        }

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        if (gameWorldComponent == null) {
            return;
        }

        // 如果当前玩家是杀手（可以使用杀手能力）
        if (!gameWorldComponent.getRole(player).canUseKiller()) {
            return;
        }

        // 如果在观察者或创造模式，不处理
        if (TMMClient.isPlayerSpectatingOrCreative()) {
            return;
        }

        // 如果没有启用杀手本能，不处理
        if (!TMMClient.isInstinctEnabled()) {
            return;
        }

        // 如果目标是年兽，显示为绿色（好人）
        if (gameWorldComponent.isRole(targettedPlayer, ModRoles.NIAN_SHOU)) {
            cir.setReturnValue(Color.GREEN.getRGB());
        }
    }
}
