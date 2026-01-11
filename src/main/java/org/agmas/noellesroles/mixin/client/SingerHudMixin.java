package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.SingerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
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
@Mixin(Gui.class)
public abstract class SingerHudMixin {
    
    @Shadow 
    public abstract Font getFont();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderSingerHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;
        
        // 检查玩家是否是歌手
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.level);
        if (!gameWorld.isRole(client.player, ModRoles.SINGER)) return;
        
        // 获取歌手组件
        SingerPlayerComponent singerComp = ModComponents.SINGER.get(client.player);
        if (!singerComp.isActive) return;
        
        Font textRenderer = this.getFont();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        
        // 基础Y位置（屏幕中下方）
        int baseX = 10;
        int baseY = screenHeight - 80;
        
        // ==================== 显示角色名称 ====================
        Component titleText = Component.translatable("hud.noellesroles.singer.title")
            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
        int titleWidth = textRenderer.width(titleText);
        context.drawString(textRenderer, titleText, 
            (screenWidth - titleWidth) / 2, baseY - 20, 0xFF69B4);
        
        // ==================== 显示技能状态 ====================
        Component abilityText;
        if (singerComp.abilityCooldown > 0) {
            // 冷却中
            abilityText = Component.translatable("hud.noellesroles.singer.cooldown", 
                String.format("%.0f", singerComp.getCooldownSeconds()))
                .withStyle(ChatFormatting.RED);
        } else {
            // 就绪
            abilityText = Component.translatable("hud.noellesroles.singer.ready")
                .withStyle(ChatFormatting.GREEN);
        }
        int abilityWidth = textRenderer.width(abilityText);
        context.drawString(textRenderer, abilityText, 
            (screenWidth - abilityWidth) / 2, baseY, 
            singerComp.abilityCooldown > 0 ? 0xFF5555 : 0x55FF55);
    }
}