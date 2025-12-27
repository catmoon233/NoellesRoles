package org.agmas.noellesroles.mixin.client.morphling;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.roles.recaller.RecallerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MorphilingHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void phantomHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        RecallerPlayerComponent recallerPlayerComponent = RecallerPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(MinecraftClient.getInstance().player);
        if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.MORPHLING)) {
            final var morphTicks = MorphlingPlayerComponent.KEY.get(MinecraftClient.getInstance().player).getMorphTicks();
            context.drawTextWithShadow(getTextRenderer(), Text.translatable("Morphling.tip" ,morphTicks), context.getScaledWindowWidth() - getTextRenderer().getWidth(Text.of("Morphing in " + morphTicks)), context.getScaledWindowHeight() - 20, ModRoles.MORPHLING.color());
        }
    }
}
