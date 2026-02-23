package org.agmas.noellesroles.mixin.client.wayfarer;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import org.agmas.noellesroles.component.WayfarerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.client.StupidExpressClient;

@Mixin(RoleNameRenderer.class)
public class TargetEntityHudmixin {

    @Inject(method = "renderHud", at = @At("TAIL"))
    private static void replaceRoleHud(Font renderer, LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter,
            CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        if (StupidExpressClient.targetBody == null) {
            return;
        }
        var p = Minecraft.getInstance().player;
        if (gameWorldComponent.isRole(p, ModRoles.WAYFARER) && !TMMClient.isPlayerSpectatingOrCreative()) {
            context.pose().pushPose();
            context.pose().translate(context.guiWidth() / 2.0f, context.guiHeight() / 2.0f + 24.0f, 0.0f);
            context.pose().scale(0.6f, 0.6f, 1.0f);

            Component status = Component.translatable("hud.noellesroles.wayfarer.select");

            WayfarerPlayerComponent nc = WayfarerPlayerComponent.KEY.get(player);
            if (nc.phase > 1) {
                return;
            }
            context.drawString(renderer, status, -renderer.width(status) / 2, 32, 0x9457ff);

            context.pose().popPose();
        }
    }

}