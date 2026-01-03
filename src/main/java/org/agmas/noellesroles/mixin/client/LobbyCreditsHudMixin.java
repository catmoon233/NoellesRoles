package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在游戏开始前的右下角添加扩展职业内容制作者信息
 */
@Mixin(Gui.class)
public class LobbyCreditsHudMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "renderHotbarAndDecorations", at = @At("TAIL"))
    private void mifan$renderExtendedRolesCredits(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) return;
        
        // 检查是否有HUD组件
        GameWorldComponent game = GameWorldComponent.KEY.get(player.level());
        
        // 只在游戏未开始时显示（大厅阶段）
        if (!game.isRunning()) {
            Font renderer = this.minecraft.font;
            
            // 扩展职业内容提示信息
            Component infoLine = Component.literal("职业扩展具体可查看群公告文档");
            
            // 计算右下角位置
            int screenWidth = context.guiWidth();
            int screenHeight = context.guiHeight();
            
            // 文字颜色 - 使用白色
            int color = 0xFFFFFFFF;
            
            // 在右下角绘制文字
            int rightPadding = 10;
            int bottomPadding = 30;
            
            // 显示提示信息
            int infoWidth = renderer.width(infoLine);
            context.drawString(renderer, infoLine,
                screenWidth - infoWidth - rightPadding,
                screenHeight - bottomPadding,
                color);
        }
    }
}