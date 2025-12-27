package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.DetectivePlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 私家侦探 HUD Mixin
 * 
 * 显示私家侦探的技能状态：
 * - 审查技能冷却时间
 * - 技能就绪提示
 */
@Mixin(InGameHud.class)
public class DetectiveHudMixin {
    
    @Inject(method = "renderStatusEffectOverlay", at = @At("RETURN"))
    private void renderDetectiveAbilityStatus(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        // 检查是否是私家侦探
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.world);
        if (!gameWorld.isRole(client.player, ModRoles.DETECTIVE)) return;
        
        // 检查玩家是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(client.player)) return;
        
        // 获取私家侦探组件
        DetectivePlayerComponent detectiveComponent = DetectivePlayerComponent.KEY.get(client.player);
        
        // 渲染位置 - 右下角
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int x = screenWidth - 120;  // 距离右边缘
        int y = screenHeight - 30;  // 距离底部
        
        TextRenderer textRenderer = client.textRenderer;
        
        if (detectiveComponent.cooldown > 0) {
            // 显示技能冷却
            float cdSeconds = detectiveComponent.getCooldownSeconds();
            Text cdText = Text.translatable("hud.noellesroles.detective.cooldown",
                String.format("%.1f", cdSeconds));
            
            // 红色文字表示冷却中
            context.drawTextWithShadow(textRenderer, cdText, x, y, Colors.RED);
            
        } else {
            // 技能可用 - 显示金币消耗提示
            Text readyText = Text.translatable("hud.noellesroles.detective.ready_cost");
            context.drawTextWithShadow(textRenderer, readyText, x, y, Colors.GREEN);
        }
    }
}