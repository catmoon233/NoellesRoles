package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.StarPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 明星 HUD 显示
 * 
 * 显示：
 * - 发光倒计时或发光中状态
 * - 主动技能冷却时间或就绪提示
 */
@Mixin(InGameHud.class)
public abstract class StarHudMixin {
    
    @Shadow 
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderStarHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        // 检查玩家是否是明星
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.world);
        if (!gameWorld.isRole(client.player, ModRoles.STAR)) return;
        
        // 获取明星组件
        StarPlayerComponent starComp = ModComponents.STAR.get(client.player);
        if (!starComp.isActive) return;
        
        TextRenderer textRenderer = this.getTextRenderer();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // 基础Y位置（屏幕中下方）
        int baseX = 10;
        int baseY = screenHeight - 80;
        
        // ==================== 显示角色名称 ====================
        Text titleText = Text.translatable("hud.noellesroles.star.title")
            .formatted(Formatting.GOLD, Formatting.BOLD);
        int titleWidth = textRenderer.getWidth(titleText);
        context.drawTextWithShadow(textRenderer, titleText, 
            (screenWidth - titleWidth) / 2, baseY - 20, 0xFFD700);
        
        // ==================== 显示发光状态 ====================
        Text glowText;
        if (starComp.isGlowing) {
            // 正在发光
            glowText = Text.translatable("hud.noellesroles.star.glowing")
                .formatted(Formatting.YELLOW, Formatting.BOLD);
        } else {
            // 显示下次发光倒计时
            float nextGlow = starComp.getNextGlowSeconds();
            glowText = Text.translatable("hud.noellesroles.star.next_glow", 
                String.format("%.0f", nextGlow))
                .formatted(Formatting.GRAY);
        }
        int glowWidth = textRenderer.getWidth(glowText);
        context.drawTextWithShadow(textRenderer, glowText, 
            (screenWidth - glowWidth) / 2, baseY, 
            starComp.isGlowing ? 0xFFFF00 : 0xAAAAAA);
        
        // ==================== 显示技能状态 ====================
        Text abilityText;
        if (starComp.abilityCooldown > 0) {
            // 冷却中
            abilityText = Text.translatable("hud.noellesroles.star.cooldown", 
                String.format("%.0f", starComp.getCooldownSeconds()))
                .formatted(Formatting.RED);
        } else {
            // 就绪
            abilityText = Text.translatable("hud.noellesroles.star.ready")
                .formatted(Formatting.GREEN);
        }
        int abilityWidth = textRenderer.getWidth(abilityText);
        context.drawTextWithShadow(textRenderer, abilityText, 
            (screenWidth - abilityWidth) / 2, baseY + 12, 
            starComp.abilityCooldown > 0 ? 0xFF5555 : 0x55FF55);
    }
}