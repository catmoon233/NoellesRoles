package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.PsychologistPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
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
 * 心理学家 HUD Mixin
 * 
 * 显示心理学家的状态：
 * - 技能冷却
 * - 自己的san值状态
 * - 正在治疗时的进度
 */
@Mixin(InGameHud.class)
public class PsychologistHudMixin {
    
    @Inject(method = "renderStatusEffectOverlay", at = @At("RETURN"))
    private void renderPsychologistHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        // 检查是否是心理学家
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.world);
        if (!gameWorld.isRole(client.player, ModRoles.PSYCHOLOGIST)) return;
        
        // 检查玩家是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(client.player)) return;
        
        // 获取心理学家组件
        PsychologistPlayerComponent psychComp = ModComponents.PSYCHOLOGIST.get(client.player);
        
        // 渲染位置 - 左下角
        int screenHeight = client.getWindow().getScaledHeight();
        int x = 10;
        int y = screenHeight - 80;
        
        TextRenderer textRenderer = client.textRenderer;
        
        // 标题
        Text titleText = Text.literal("心理学家").formatted(Formatting.AQUA, Formatting.BOLD);
        context.drawTextWithShadow(textRenderer, titleText, x, y, 0xFFFFFF);
        y += 12;
        
        // 检查自己的san值（游戏中san值范围是0.0-1.0，需要转换为百分比显示）
        PlayerMoodComponent selfMood = PlayerMoodComponent.KEY.get(client.player);
        float sanity = selfMood.getMood();  // 0.0 到 1.0
        int sanityPercent = (int)(sanity * 100);  // 转换为百分比
        Formatting sanColor = sanity >= 0.99f ? Formatting.GREEN :
                             sanity >= 0.5f ? Formatting.YELLOW : Formatting.RED;
        Text sanText = Text.literal("San: " + sanityPercent + "/100").formatted(sanColor);
        context.drawTextWithShadow(textRenderer, sanText, x, y, 0xFFFFFF);
        y += 12;
        
        // 正在治疗中
        if (psychComp.isHealing) {
            int healedSeconds = (int) psychComp.getHealingSeconds();
            int totalSeconds = PsychologistPlayerComponent.HEALING_DURATION / 20;
            Text healingText = Text.translatable("hud.noellesroles.psychologist.healing",
                psychComp.healingTargetName, healedSeconds, totalSeconds)
                .formatted(Formatting.GREEN);
            context.drawTextWithShadow(textRenderer, healingText, x, y, 0xFFFFFF);
            y += 12;
            
            // 进度条
            int barWidth = 100;
            int barHeight = 5;
            float progress = (float) psychComp.healingTicks / PsychologistPlayerComponent.HEALING_DURATION;
            int filledWidth = (int) (barWidth * progress);
            
            // 背景
            context.fill(x, y, x + barWidth, y + barHeight, 0x80000000);
            // 进度
            context.fill(x, y, x + filledWidth, y + barHeight, 0xFF00FF00);
            y += barHeight + 5;
        }
        // 冷却中
        else if (psychComp.cooldown > 0) {
            Text cooldownText = Text.translatable("hud.noellesroles.psychologist.cooldown",
                psychComp.getCooldownSeconds()).formatted(Formatting.GRAY);
            context.drawTextWithShadow(textRenderer, cooldownText, x, y, 0xFFFFFF);
            y += 12;
        }
        // 技能就绪（san值需要 >= 0.99 才算满）
        else if (sanity >= 0.99f) {
            Text readyText = Text.translatable("hud.noellesroles.psychologist.ready")
                .formatted(Formatting.GREEN);
            context.drawTextWithShadow(textRenderer, readyText, x, y, 0xFFFFFF);
            y += 12;
        }
        // san值不足
        else {
            Text notReadyText = Text.translatable("hud.noellesroles.psychologist.not_ready")
                .formatted(Formatting.YELLOW);
            context.drawTextWithShadow(textRenderer, notReadyText, x, y, 0xFFFFFF);
        }
    }
}