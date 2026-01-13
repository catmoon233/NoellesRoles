package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.TelegrapherPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 电报员 HUD 显示
 * 显示剩余使用次数
 */
@Mixin(RoleNameRenderer.class)
public class TelegrapherHudMixin {
    
//    @Inject(method = "renderHud", at = @At("HEAD"))
//    private static void renderTelegrapherHud(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
//        Minecraft client = Minecraft.getInstance();
//        if (client.player == null) return;
//
//        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.level());
//        if (!TMMClient.isPlayerAliveAndInSurvival()) return;
//
//        TelegrapherPlayerComponent component = TelegrapherPlayerComponent.KEY.get(client.player);
//
//        context.pose().pushPose();
//
//        int screenWidth = context.guiWidth();
//        int screenHeight = context.guiHeight();
//        int yOffset = screenHeight - 40;  // 右下角
//        int xOffset = screenWidth - 150;  // 距离右边缘
//
//        // 显示剩余使用次数
//        Component usesText = Component.translatable("tip.noellesroles.telegrapher.uses", component.remainingUses)
//            .withStyle(component.remainingUses > 0 ? ChatFormatting.AQUA : ChatFormatting.RED);
//
//
//        // 显示按键提示
//        if (component.remainingUses > 0) {
//            Component hintText = Component.translatable("tip.noellesroles.telegrapher.hint")
//                .withStyle(ChatFormatting.GRAY);
//            context.drawString(renderer, hintText, xOffset, yOffset + 10, 0x888888);
//        }
//
//        context.pose().popPose();
//    }
}