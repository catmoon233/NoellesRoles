package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.StalkerPlayerComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 跟踪者 HUD Mixin
 * 
 * 显示跟踪者的状态：
 * - 当前阶段
 * - 能量值
 * - 击杀数（二阶段）
 * - 免疫状态（二阶段）
 * - 倒计时（三阶段）
 * - 窥视目标数
 * - 蓄力进度（三阶段）
 */
@Mixin(InGameHud.class)
public class StalkerHudMixin {
    
    @Inject(method = "renderStatusEffectOverlay", at = @At("RETURN"))
    private void renderStalkerHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        // 获取跟踪者组件
        StalkerPlayerComponent stalkerComp = StalkerPlayerComponent.KEY.get(client.player);
        
        // 检查是否是跟踪者
        if (!stalkerComp.isActiveStalker()) return;
        
        // 检查玩家是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(client.player)) return;
        
        // 渲染位置 - 左下角
        int screenHeight = client.getWindow().getScaledHeight();
        int x = 10;
        int y = screenHeight - 80;
        
        TextRenderer textRenderer = client.textRenderer;
        
        // 阶段显示
        Text phaseText = switch (stalkerComp.phase) {
            case 1 -> Text.translatable("hud.noellesroles.stalker.phase1").formatted(Formatting.DARK_PURPLE);
            case 2 -> Text.translatable("hud.noellesroles.stalker.phase2").formatted(Formatting.RED);
            case 3 -> Text.translatable("hud.noellesroles.stalker.phase3").formatted(Formatting.DARK_RED);
            default -> Text.empty();
        };
        context.drawTextWithShadow(textRenderer, phaseText, x, y, 0xFFFFFF);
        y += 12;
        
        // 能量条
        int maxEnergy = stalkerComp.phase == 1 ? StalkerPlayerComponent.PHASE_1_ENERGY : StalkerPlayerComponent.PHASE_2_ENERGY;
        Text energyText = Text.translatable("hud.noellesroles.stalker.energy", stalkerComp.energy, maxEnergy);
        context.drawTextWithShadow(textRenderer, energyText, x, y, 0xAAAAAA);
        y += 12;
        
        // 二阶段：击杀数和免疫状态
        if (stalkerComp.phase >= 2) {
            Text killsText = Text.translatable("hud.noellesroles.stalker.kills", 
                stalkerComp.phase2Kills, StalkerPlayerComponent.PHASE_2_KILLS);
            context.drawTextWithShadow(textRenderer, killsText, x, y, 0xFF6666);
            y += 12;
            
            Text immunityText = stalkerComp.immunityUsed ? 
                Text.translatable("hud.noellesroles.stalker.immunity_used").formatted(Formatting.GRAY) : 
                Text.translatable("hud.noellesroles.stalker.immunity_available").formatted(Formatting.GREEN);
            context.drawTextWithShadow(textRenderer, immunityText, x, y, 0xFFFFFF);
            y += 12;
        }
        
        // 三阶段：倒计时
        if (stalkerComp.phase == 3) {
            int seconds = stalkerComp.phase3Timer / 20;
            int minutes = seconds / 60;
            seconds %= 60;
            Text timerText = Text.translatable("hud.noellesroles.stalker.timer", 
                String.format("%d:%02d", minutes, seconds));
            int color = stalkerComp.phase3Timer < 600 ? 0xFF0000 : 0xFFAA00; // 30秒以下变红
            context.drawTextWithShadow(textRenderer, timerText, x, y, color);
            y += 12;
        }
        
        // 窥视状态
        if (stalkerComp.isGazing) {
            Text gazingText = Text.translatable("hud.noellesroles.stalker.gazing", stalkerComp.gazingTargetCount)
                .formatted(Formatting.YELLOW);
            context.drawTextWithShadow(textRenderer, gazingText, x, y, 0xFFFFFF);
            y += 12;
        }
        
        // 蓄力进度（三阶段）
        if (stalkerComp.isCharging) {
            float chargeSeconds = stalkerComp.getChargeSeconds();
            float maxSeconds = StalkerPlayerComponent.MAX_CHARGE_TIME / 20.0f;
            Text chargeText = Text.translatable("hud.noellesroles.stalker.charging", 
                String.format("%.1f", chargeSeconds), String.format("%.1f", maxSeconds));
            int chargeColor = chargeSeconds >= 1.0f ? 0x00FF00 : 0xFFFF00;
            context.drawTextWithShadow(textRenderer, chargeText, x, y, chargeColor);
        }
        
        // 突进状态
        if (stalkerComp.isDashing) {
            Text dashText = Text.translatable("hud.noellesroles.stalker.dashing")
                .formatted(Formatting.AQUA, Formatting.BOLD);
            context.drawTextWithShadow(textRenderer, dashText, x, y, 0xFFFFFF);
        }
    }
}