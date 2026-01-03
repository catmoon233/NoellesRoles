package org.agmas.noellesroles.mixin.client.phantom;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class PhantomHudMixin {
    @Shadow public abstract Font getFont();

    @Inject(method = "render", at = @At("TAIL"))
    public void phantomHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(Minecraft.getInstance().player.level());
        AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(Minecraft.getInstance().player);
        if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.PHANTOM)) {
            int drawY = context.guiHeight();

            Component line = Component.translatable("tip.phantom", NoellesrolesClient.abilityBind.getTranslatedKeyMessage());

            if (abilityPlayerComponent.cooldown > 0) {
                line = Component.translatable("tip.noellesroles.cooldown", abilityPlayerComponent.cooldown/20);
            }

            drawY -= getFont().wordWrapHeight(line, 999999);
            context.drawString(getFont(), line, context.guiWidth() - getFont().width(line), drawY, CommonColors.RED);
        }
    }
}
