package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在游戏开始前的右下角添加扩展职业内容制作者信息
 */
@Mixin(InGameHud.class)
public class LobbyCreditsHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "renderMainHud", at = @At("TAIL"))
    private void mifan$renderExtendedRolesCredits(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ClientPlayerEntity player = this.client.player;
        if (player == null) return;
        
        // 检查是否有HUD组件
        GameWorldComponent game = GameWorldComponent.KEY.get(player.getWorld());
        
        // 只在游戏未开始时显示（大厅阶段）
        if (!game.isRunning()) {
            TextRenderer renderer = this.client.textRenderer;
            
            // 扩展职业内容提示信息
            Text infoLine = Text.literal("职业扩展具体可查看模组页面");
            
            // 计算右下角位置
            int screenWidth = context.getScaledWindowWidth();
            int screenHeight = context.getScaledWindowHeight();
            
            // 文字颜色 - 使用白色
            int color = 0xFFFFFFFF;
            
            // 在右下角绘制文字
            int rightPadding = 10;
            int bottomPadding = 30;
            
            // 显示提示信息
            int infoWidth = renderer.getWidth(infoLine);
            context.drawTextWithShadow(renderer, infoLine,
                screenWidth - infoWidth - rightPadding,
                screenHeight - bottomPadding,
                color);
        }
    }
}