package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.ghost.GhostPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GhostHudMixin {

    @Shadow
    public abstract Font getFont();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderGhostHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null)
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.level);
        if (!gameWorld.isRole(client.player, ModRoles.GHOST))
            return;
        if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
            return;

        GhostPlayerComponent ghostComponent = GhostPlayerComponent.KEY.get(client.player);

        Component text;
        int color;

        if (ghostComponent.cooldown > 0) {
            int seconds = (ghostComponent.cooldown + 19) / 20;
            text = Component.translatable("gui.noellesroles.ghost.cooldown", seconds);
            color = 0xFF5555; // 红色
        } else {
            text = Component.translatable("gui.noellesroles.ghost.ready");
            color = 0x55FF55; // 绿色
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int textWidth = getFont().width(text);

        // 右下角显示，留出一些边距
        int x = screenWidth - textWidth - 10;
        int y = screenHeight - 20;

        context.drawString(getFont(), text, x, y, color);
    }
}