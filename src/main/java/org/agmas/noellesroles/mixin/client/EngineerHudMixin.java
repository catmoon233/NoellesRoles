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
 * 工程师 HUD 显示
 * 工程师的金币显示由 MoneyHudMixin 统一处理
 * 此 Mixin 暂时不需要显示额外内容
 */
@Mixin(InGameHud.class)
public abstract class EngineerHudMixin {
    
    @Shadow 
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void renderEngineerHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        // 工程师的金币显示由 MoneyHudMixin 统一处理
        // 此方法暂时不需要额外渲染
    }
}