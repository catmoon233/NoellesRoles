package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.BoxerPlayerComponent;
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
 * 拳击手 HUD Mixin
 * 
 * 显示拳击手的技能状态：
 * - 技能冷却时间
 * - 无敌状态激活提示
 * - 技能就绪提示
 */
@Mixin(InGameHud.class)
public class BoxerHudMixin {
    
    @Inject(method = "renderStatusEffectOverlay", at = @At("RETURN"))
    private void renderBoxerAbilityStatus(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        // 检查是否是拳击手
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.world);
        if (!gameWorld.isRole(client.player, ModRoles.BOXER)) return;
        
        // 检查玩家是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(client.player)) return;
        
        // 获取拳击手组件
        BoxerPlayerComponent boxerComponent = BoxerPlayerComponent.KEY.get(client.player);
        
        // 渲染位置 - 右下角
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int x = screenWidth - 150;  // 距离右边缘
        int y = screenHeight - 30;  // 距离底部
        
        TextRenderer textRenderer = client.textRenderer;
        
        // 检查无敌状态
        if (boxerComponent.isInvulnerable) {
            // 无敌激活 - 显示黄色闪烁文字
            Text activeText = Text.translatable("hud.noellesroles.boxer.active",
                String.format("%.1f", boxerComponent.getInvulnerabilitySeconds()));
            
            // 使用黄色表示无敌激活
            int color = 0xFFFF00; // 黄色
            context.drawTextWithShadow(textRenderer, activeText, x, y, color);
            
        } else if (boxerComponent.cooldown > 0) {
            // 显示技能冷却
            float cdSeconds = boxerComponent.getCooldownSeconds();
            Text cdText = Text.translatable("hud.noellesroles.boxer.cooldown",
                String.format("%.1f", cdSeconds));
            
            // 红色文字表示冷却中
            context.drawTextWithShadow(textRenderer, cdText, x, y, Colors.RED);
            
        } else {
            // 技能就绪 - 显示绿色提示
            Text readyText = Text.translatable("hud.noellesroles.boxer.ready");
            context.drawTextWithShadow(textRenderer, readyText, x, y, Colors.GREEN);
        }
    }
}