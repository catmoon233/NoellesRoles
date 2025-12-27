package org.agmas.noellesroles.mixin.client;


import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.noellesroles.component.AdmirerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 慕恋者 HUD Mixin
 * 
 * 显示慕恋者的状态：
 * - 能量值
 * - 窥视目标数
 * - 转化进度
 */
@Mixin(InGameHud.class)
public class AdmirerHudMixin {
    
    @Inject(method = "renderStatusEffectOverlay", at = @At("RETURN"))
    private void renderAdmirerHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        // 获取慕恋者组件
        AdmirerPlayerComponent admirerComp = AdmirerPlayerComponent.KEY.get(client.player);
        
        // 检查是否是慕恋者
        if (!admirerComp.isActiveAdmirer()) return;
        
        // 检查玩家是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(client.player)) return;
        
        // 渲染位置 - 左下角
        int screenHeight = client.getWindow().getScaledHeight();
        int x = 10;
        int y = screenHeight - 60;
        
        TextRenderer textRenderer = client.textRenderer;
        
        // 角色名称
        Text roleText = Text.translatable("hud.noellesroles.admirer.title")
            .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD);
        context.drawTextWithShadow(textRenderer, roleText, x, y, 0xFFFFFF);
        y += 12;
        
        // 能量条
        Text energyText = Text.translatable("hud.noellesroles.admirer.energy", 
            admirerComp.energy, AdmirerPlayerComponent.MAX_ENERGY);
        int energyColor = getEnergyColor(admirerComp.getEnergyPercent());
        context.drawTextWithShadow(textRenderer, energyText, x, y, energyColor);
        y += 12;
        
        // 能量进度条
        int barWidth = 80;
        int barHeight = 6;
        int filledWidth = (int) (barWidth * admirerComp.getEnergyPercent());
        
        // 背景
        context.fill(x, y, x + barWidth, y + barHeight, 0x88000000);
        // 填充
        context.fill(x, y, x + filledWidth, y + barHeight, energyColor | 0xFF000000);
        y += 10;
        
        // 窥视状态
        if (admirerComp.isGazing) {
            Text gazingText = Text.translatable("hud.noellesroles.admirer.gazing", admirerComp.gazingTargetCount)
                .formatted(Formatting.YELLOW);
            context.drawTextWithShadow(textRenderer, gazingText, x, y, 0xFFFFFF);
            y += 12;
        } else {
            // 提示按G开始窥视
            Text hintText = Text.translatable("hud.noellesroles.admirer.hint")
                .formatted(Formatting.GRAY);
            context.drawTextWithShadow(textRenderer, hintText, x, y, 0xFFFFFF);
        }
    }
    
    /**
     * 根据能量百分比获取颜色
     */
    private int getEnergyColor(float percent) {
        if (percent >= 0.9f) {
            return 0xFF0000; // 红色 - 即将转化
        } else if (percent >= 0.6f) {
            return 0xFF8800; // 橙色
        } else if (percent >= 0.3f) {
            return 0xFFFF00; // 黄色
        } else {
            return 0xAA00FF; // 紫色 - 正常
        }
    }
}