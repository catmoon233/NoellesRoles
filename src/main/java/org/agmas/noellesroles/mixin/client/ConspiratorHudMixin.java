package org.agmas.noellesroles.mixin.client;

import org.agmas.noellesroles.component.ConspiratorPlayerComponent;
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
 * 阴谋家 HUD 显示
 * 显示当前目标和倒计时
 */
@Mixin(RoleNameRenderer.class)
public class ConspiratorHudMixin {
    
    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void renderConspiratorHud(TextRenderer renderer, ClientPlayerEntity player, DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.getWorld());
        if (!gameWorld.isRole(client.player, ModRoles.CONSPIRATOR)) return;
        if (!TMMClient.isPlayerAliveAndInSurvival()) return;
        
        ConspiratorPlayerComponent component = ConspiratorPlayerComponent.KEY.get(client.player);
        
        context.getMatrices().push();
        
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        int yOffset = screenHeight - 28;  // 右下角
        int xOffset = screenWidth - 200;  // 距离右边缘
        
        // 如果有正在进行的诅咒
        if (component.guessCorrect && component.deathCountdown > 0 && !component.targetName.isEmpty()) {
            Text targetText = Text.translatable("tip.noellesroles.conspirator.target",
                component.targetName, component.getCountdownSeconds())
                .formatted(Formatting.DARK_PURPLE);
            context.drawTextWithShadow(renderer, targetText, xOffset, yOffset, ModRoles.CONSPIRATOR.color());
        } else {
            // 显示提示
            Text hintText = Text.translatable("tip.noellesroles.conspirator.no_target")
                .formatted(Formatting.GRAY);
            context.drawTextWithShadow(renderer, hintText, xOffset, yOffset, 0x888888);
        }
        
        context.getMatrices().pop();
    }
}