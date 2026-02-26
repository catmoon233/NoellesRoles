package org.agmas.noellesroles.mixin.roles.photographer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class BlindnessEffectMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableDepthTest()V",shift = At.Shift.BEFORE), cancellable = true)
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        final var player = Minecraft.getInstance().player;
        if (player.hasEffect(MobEffects.UNLUCK)){
            guiGraphics.fill(0,0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 0xFF000000);
        }
        if (player.hasEffect(MobEffects.WEAVING)){
            //屏幕变红（30%透明度）
            guiGraphics.fill(0,0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 0x4DFF0000);
        }
        if (player.hasEffect(MobEffects.RAID_OMEN)){
            // 试炼之兆效果 - 屏幕变白（仿照低san值时的效果）
            guiGraphics.fill(0,0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 0xFFFFFFFF);
        }
    }
}

