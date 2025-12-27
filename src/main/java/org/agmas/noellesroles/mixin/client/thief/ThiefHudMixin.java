package org.agmas.noellesroles.mixin.client.thief;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.thief.ThiefPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class ThiefHudMixin {
    @Shadow public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("TAIL"))
    public void thiefHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) return;
        
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        AbilityPlayerComponent abilityPlayerComponent = AbilityPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        ThiefPlayerComponent thiefComponent = ThiefPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        
        if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.THIEF)) {
            int drawY = context.getScaledWindowHeight();

            Text hint = Text.translatable("tip.thief.steal");
            drawY -= getTextRenderer().getWrappedLinesHeight(hint, 999999);
            context.drawTextWithShadow(getTextRenderer(), hint, context.getScaledWindowWidth() - getTextRenderer().getWidth(hint), drawY, 0xFFFFFF);

            if (thiefComponent.hasBlackoutEffect) {
                Text blackout = Text.translatable("tip.thief.blackout_active");
                drawY -= getTextRenderer().getWrappedLinesHeight(blackout, 999999);
                context.drawTextWithShadow(getTextRenderer(), blackout, context.getScaledWindowWidth() - getTextRenderer().getWidth(blackout), drawY, 0x00FF00);
            }

            if (abilityPlayerComponent.cooldown > 0) {
                Text line = Text.translatable("tip.noellesroles.cooldown", abilityPlayerComponent.cooldown / 20);
                drawY -= getTextRenderer().getWrappedLinesHeight(line, 999999);
                context.drawTextWithShadow(getTextRenderer(), line, context.getScaledWindowWidth() - getTextRenderer().getWidth(line), drawY, ModRoles.THIEF.color());
            }
        }
    }
}