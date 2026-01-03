package org.agmas.noellesroles.mixin.client.recaller;

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
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.recaller.RecallerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class RecallerHudMixin {
    @Shadow public abstract Font getFont();

    @Inject(method = "render", at = @At("TAIL"))
    public void phantomHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(Minecraft.getInstance().player.level());
        AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(Minecraft.getInstance().player);
        RecallerPlayerComponent recallerPlayerComponent = RecallerPlayerComponent.KEY.get(Minecraft.getInstance().player);
        PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(Minecraft.getInstance().player);
        if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.RECALLER)) {
            int drawY = context.guiHeight();


            Component line = Component.translatable("tip.recaller.teleport", NoellesrolesClient.abilityBind.getTranslatedKeyMessage());
            if (!recallerPlayerComponent.placed) {
                line = Component.translatable("tip.recaller.place", NoellesrolesClient.abilityBind.getTranslatedKeyMessage());
            } else {
                if (playerShopComponent.balance < 100) {
                    line = Component.translatable("tip.recaller.not_enough_money");
                }
            }

            if (abilityPlayerComponent.cooldown > 0) {
                line = Component.translatable("tip.noellesroles.cooldown", abilityPlayerComponent.cooldown/20);
            }

            drawY -= getFont().wordWrapHeight(line, 999999);
            context.drawString(getFont(), line, context.guiWidth() - getFont().width(line), drawY, ModRoles.RECALLER.color());
        }
    }
}
