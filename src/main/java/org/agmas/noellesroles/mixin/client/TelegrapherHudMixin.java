package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.TelegrapherPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 电报员 HUD 显示
 * 显示剩余使用次数
 */
@Mixin(RoleNameRenderer.class)
public class TelegrapherHudMixin {
    
    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void renderTelegrapherHud(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        if (!gameWorld.isRole(client.player, ModRoles.TELEGRAPHER)) return;
        if (!TMMClient.isPlayerAliveAndInSurvival()) return;
        
        TelegrapherPlayerComponent component = TelegrapherPlayerComponent.KEY.get(client.player);
        
        context.getMatrices().push();
        
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int yOffset = screenHeight - 40;  // 右下角
        int xOffset = screenWidth - 150;  // 距离右边缘
        
        // 显示剩余使用次数
        Text usesText = Text.translatable("tip.noellesroles.telegrapher.uses", component.remainingUses)
            .formatted(component.remainingUses > 0 ? Formatting.AQUA : Formatting.RED);
        context.drawTextWithShadow(renderer, usesText, xOffset, yOffset, ModRoles.TELEGRAPHER.color());
        
        // 显示按键提示
        if (component.remainingUses > 0) {
            Text hintText = Text.translatable("tip.noellesroles.telegrapher.hint")
                .formatted(Formatting.GRAY);
            context.drawTextWithShadow(renderer, hintText, xOffset, yOffset + 10, 0x888888);
        }
        
        context.getMatrices().pop();
    }
}