package org.agmas.noellesroles.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 工程师 HUD 显示
 * 工程师的金币显示由 MoneyHudMixin 统一处理
 * 此 Mixin 暂时不需要显示额外内容
 */
@Mixin(Gui.class)
public abstract class EngineerHudMixin {
    
    @Shadow 
    public abstract Font getFont();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderEngineerHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        // 工程师的金币显示由 MoneyHudMixin 统一处理
        // 此方法暂时不需要额外渲染
    }
}