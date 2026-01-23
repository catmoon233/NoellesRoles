package org.agmas.noellesroles.mixin.client.morphling;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.roles.recaller.RecallerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MorphilingHudMixin {
    @Shadow public abstract Font getFont();

    @Inject(method = "render", at = @At("TAIL"))
    public void phantomHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(Minecraft.getInstance().player.level());

        if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.MORPHLING)) {
            final var morphTicks = MorphlingPlayerComponent.KEY.get(Minecraft.getInstance().player).getMorphTicks();
            context.drawString(getFont(), Component.translatable("morphling.tip" ,((int) (morphTicks * 0.05))), context.guiWidth() - getFont().width(Component.nullToEmpty("Morphing in " + morphTicks)), context.guiHeight() - 20, ModRoles.MORPHLING.color());
        }
    }
}
