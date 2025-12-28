package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.client.gui.StoreRenderer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
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
    private static void renderCoinsForCustomRoles(TextRenderer renderer,ClientPlayerEntity player, DrawContext context, float delta, CallbackInfo ci) {
        if (((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.BARTENDER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.RECALLER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.EXECUTIONER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.JESTER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.NOISEMAKER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.BROADCASTER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.PHOTOGRAPHER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.PUPPETEER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.TELEGRAPHER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.AVENGER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.BOXER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.SLIPPERY_GHOST)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.CONSPIRATOR)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.DETECTIVE)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.POSTMAN)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.ENGINEER)
        || ((GameWorldComponent)GameWorldComponent.KEY.get(player.getWorld())).isRole(player.getUuid(), ModRoles.THIEF)) {
            int balance = ((PlayerShopComponent)PlayerShopComponent.KEY.get(player)).balance;
            if (view.getTarget() != (float)balance) {
                offsetDelta = (float)balance > view.getTarget() ? 0.6F : -0.6F;
                view.setTarget((float)balance);
            }

            float r = offsetDelta > 0.0F ? 1.0F - offsetDelta : 1.0F;
            float g = offsetDelta < 0.0F ? 1.0F + offsetDelta : 1.0F;
            float b = 1.0F - Math.abs(offsetDelta);
            int colour = MathHelper.packRgb(r, g, b) | -16777216;
            context.getMatrices().push();
            context.getMatrices().translate((float)(context.getScaledWindowWidth() - 12), 6.0F, 0.0F);
            view.render(renderer, context, 0, 0, colour, delta);
            context.getMatrices().pop();
            offsetDelta = MathHelper.lerp(delta / 16.0F, offsetDelta, 0.0F);
        }
    }
}
