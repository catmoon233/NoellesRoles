package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.RecorderPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TMMClient.class)
public class RecorderInstinctMixin {

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void recorderCanUseInstinct(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null)
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.level);
        if (gameWorld.isRole(client.player, ModRoles.RECORDER)) {
            if (TMMClient.instinctKeybind.isDown()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void recorderInstinctHighlight(Entity target, CallbackInfoReturnable<Integer> cir) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null)
            return;

        // 只有在杀手本能激活时才处理
        if (!TMMClient.isInstinctEnabled())
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.level);
        if (gameWorld.isRole(client.player, ModRoles.RECORDER)) {
            if (target instanceof Player targetPlayer) {
                if (targetPlayer == client.player)
                    return;

                RecorderPlayerComponent recorder = ModComponents.RECORDER.get(client.player);
                if (recorder.getGuesses().containsKey(targetPlayer.getUUID())) {
                    // 已记录（猜测过）：亮黄色
                    cir.setReturnValue(0xFFFF55);
                } else {
                    // 未记录：暗蓝色
                    cir.setReturnValue(0x0000AA);
                }
            }
        }
    }
}