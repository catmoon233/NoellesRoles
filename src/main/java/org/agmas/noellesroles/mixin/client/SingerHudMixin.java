package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.SingerPlayerComponent;
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
 * 歌手 HUD 显示
 * 
 * 显示：
 * - 技能冷却时间或就绪提示
 */
@Mixin(InGameHud.class)
public abstract class SingerHudMixin {
    
    @Shadow 
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderSingerHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        // 检查玩家是否是歌手
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.world);
        if (!gameWorld.isRole(client.player, ModRoles.SINGER)) return;
        
        // 获取歌手组件
        SingerPlayerComponent singerComp = ModComponents.SINGER.get(client.player);
        if (!singerComp.isActive) return;
        
        TextRenderer textRenderer = this.getTextRenderer();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        // 基础Y位置（屏幕中下方）
        int baseX = 10;
        int baseY = screenHeight - 80;
        
        // ==================== 显示角色名称 ====================
        Text titleText = Text.translatable("hud.noellesroles.singer.title")
            .formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD);
        int titleWidth = textRenderer.getWidth(titleText);
        context.drawTextWithShadow(textRenderer, titleText, 
            (screenWidth - titleWidth) / 2, baseY - 20, 0xFF69B4);
        
        // ==================== 显示技能状态 ====================
        Text abilityText;
        if (singerComp.abilityCooldown > 0) {
            // 冷却中
            abilityText = Text.translatable("hud.noellesroles.singer.cooldown", 
                String.format("%.0f", singerComp.getCooldownSeconds()))
                .formatted(Formatting.RED);
        } else {
            // 就绪
            abilityText = Text.translatable("hud.noellesroles.singer.ready")
                .formatted(Formatting.GREEN);
        }
        int abilityWidth = textRenderer.getWidth(abilityText);
        context.drawTextWithShadow(textRenderer, abilityText, 
            (screenWidth - abilityWidth) / 2, baseY, 
            singerComp.abilityCooldown > 0 ? 0xFF5555 : 0x55FF55);
    }
}