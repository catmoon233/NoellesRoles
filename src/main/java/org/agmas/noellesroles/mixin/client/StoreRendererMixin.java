package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.client.gui.StoreRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StoreRenderer.class)
public abstract class StoreRendererMixin {

    @Shadow public static StoreRenderer.MoneyNumberRenderer view;

    @Shadow public static float offsetDelta;

    @Inject(method = "renderHud", at = @At("HEAD"))
    private static void renderCoinsForCustomRoles(Font renderer,LocalPlayer player, GuiGraphics context, float delta, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        Role role = gameWorldComponent.getRole(player);
        if(role == null){
            return;
        }
        if (ModRoles.SHOW_MONEY_ROLES.contains(role)) // 显示金币
        {
            int balance = ((PlayerShopComponent)PlayerShopComponent.KEY.get(player)).balance;
            if (view.getTarget() != (float)balance) {
                offsetDelta = (float)balance > view.getTarget() ? 0.6F : -0.6F;
                view.setTarget((float)balance);
            }

            float r = offsetDelta > 0.0F ? 1.0F - offsetDelta : 1.0F;
            float g = offsetDelta < 0.0F ? 1.0F + offsetDelta : 1.0F;
            float b = 1.0F - Math.abs(offsetDelta);
            int colour = Mth.color(r, g, b) | -16777216;
            context.pose().pushPose();
            context.pose().translate((float)(context.guiWidth() - 12), 6.0F, 0.0F);
            view.render(renderer, context, 0, 0, colour, delta);
            context.pose().popPose();
            offsetDelta = Mth.lerp(delta / 16.0F, offsetDelta, 0.0F);
        }
    }
}
