package org.agmas.noellesroles.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 滑头鬼 HUD 显示
 * 金币显示已由 MoneyHudMixin 统一处理
 * 此 Mixin 暂时不需要额外显示
 */
@Mixin(InGameHud.class)
public abstract class SlipperyGhostHudMixin {
    
    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderSlipperyGhostHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // 金币显示已由 MoneyHudMixin 统一处理在右上角
        // 此方法暂时不需要额外渲染
    }
}