package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.roles.recaller.RecallerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InsaneHudMixin {
    @Shadow public abstract Font getFont();

    @Inject(method = "render", at = @At("TAIL"))
    public void phantomHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(Minecraft.getInstance().player.level());
        if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {

            final var insaneKillerPlayerComponent = InsaneKillerPlayerComponent.KEY.get(Minecraft.getInstance().player);
            if (insaneKillerPlayerComponent.isActive) {
                context.drawString(getFont(), Component.translatable("insane.tip.over", NoellesrolesClient.abilityBind.getTranslatedKeyMessage().getString()), context.guiWidth() - getFont().width(Component.nullToEmpty("Morphing in    a" )), context.guiHeight() - 20, ModRoles.MORPHLING.color());

            } else {
                final var morphTicks = insaneKillerPlayerComponent.cooldown;
                if (morphTicks > 0) {
                    context.drawString(getFont(), Component.translatable("insane.tip", ((int) (morphTicks * 0.05))), context.guiWidth() - getFont().width(Component.nullToEmpty("Morphing in   a " + morphTicks)), context.guiHeight() - 20, ModRoles.MORPHLING.color());
                }else {
                    context.drawString(getFont(), Component.translatable("insane.tip.ready", NoellesrolesClient.abilityBind.getTranslatedKeyMessage().getString()),context.guiWidth() - getFont().width(Component.nullToEmpty("Morphing in  adaada   " + morphTicks)), context.guiHeight() - 20, ModRoles.MORPHLING.color());
                }
            }
        }
    }
}
