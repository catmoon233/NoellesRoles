package org.agmas.noellesroles.mixin.client.broadcaster;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class BroadcasterHudMixin {
    @Shadow
    public abstract Font getFont();

    @Inject(method = "render", at = @At("TAIL"))
    public void broadcasterHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        if (NoellesrolesClient.currentBroadcastMessage != null && NoellesrolesClient.broadcastMessageTicks > 0) {
            Component message = NoellesrolesClient.currentBroadcastMessage;
            Font textRenderer = getFont();
            int screenWidth = context.guiWidth();
            int screenHeight = context.guiHeight();
            int textWidth = textRenderer.width(message);
            int x = (screenWidth - textWidth) / 2;
            int y = 20;
            int padding = 4;
            int bgColor = 0x80000000;
            context.fill(x - padding, y - padding, x + textWidth + padding, y + textRenderer.lineHeight + padding,
                    bgColor);
            context.drawString(textRenderer, message, x, y, 0xFFFFFF);
        }

        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                .get(Minecraft.getInstance().player.level());
        NoellesRolesAbilityPlayerComponent abilityPlayerComponent = (NoellesRolesAbilityPlayerComponent) NoellesRolesAbilityPlayerComponent.KEY
                .get(Minecraft.getInstance().player);
        PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(Minecraft.getInstance().player);

        if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BROADCASTER)) {
            int drawY = context.guiHeight();

            Component line;
            line = Component.translatable("tip.broadcaster.with_cost",
                    NoellesrolesClient.abilityBind.getTranslatedKeyMessage(), 100);

            drawY -= getFont().wordWrapHeight(line, 999999);
            context.drawString(getFont(), line, context.guiWidth() - getFont().width(line), drawY,
                    ModRoles.BROADCASTER.color());
        }
    }
}