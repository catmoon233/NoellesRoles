package org.agmas.noellesroles.mixin.client.broadcaster;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class BroadcasterHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void broadcasterHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        if (NoellesrolesClient.currentBroadcastMessage != null && NoellesrolesClient.broadcastMessageTicks > 0) {
            String message = NoellesrolesClient.currentBroadcastMessage;
            TextRenderer textRenderer = getTextRenderer();
            int screenWidth = context.getScaledWindowWidth();
            int screenHeight = context.getScaledWindowHeight();
            int textWidth = textRenderer.getWidth(message);
            int x = (screenWidth - textWidth) / 2;
            int y = 20;
            int padding = 4;
            int bgColor = 0x80000000;
            context.fill(x - padding, y - padding, x + textWidth + padding, y + textRenderer.fontHeight + padding, bgColor);
            context.drawTextWithShadow(textRenderer, message, x, y, 0xFFFFFF);
        }
        
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(MinecraftClient.getInstance().player);
        
        if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.BROADCASTER)) {
            int drawY = context.getScaledWindowHeight();

            Text line;
            line = Text.translatable("tip.broadcaster.with_cost", NoellesrolesClient.abilityBind.getBoundKeyLocalizedText(), 150);

            drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
            context.drawTextWithShadow(getTextRenderer(), line, context.getScaledWindowWidth() - getTextRenderer().getWidth(line), drawY, ModRoles.BROADCASTER.color());
        }
    }
}