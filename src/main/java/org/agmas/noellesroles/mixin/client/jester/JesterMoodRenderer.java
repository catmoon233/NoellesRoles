package org.agmas.noellesroles.mixin.client.jester;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.MoodRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(MoodRenderer.class)
public class JesterMoodRenderer {

    @Shadow public static float moodOffset;

    @Shadow public static float moodTextWidth;

    @Shadow public static float moodRender;

    @Shadow public static float moodAlpha;
    @Shadow public static Random random;
    @Unique private static final Identifier JESTER_MOOD = Identifier.of(Noellesroles.MOD_ID, "hud/mood_jester");

    @Inject(method = "renderKiller", at = @At("HEAD"), cancellable = true)
    private static void jesterMood(TextRenderer textRenderer, DrawContext context, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.JESTER)) {
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 3.0F * moodOffset, 0.0F);
        context.drawGuiTexture(JESTER_MOOD, 5, 6, 14, 17);
        context.getMatrices().pop();
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 10.0F * moodOffset, 0.0F);
        MatrixStack var10000 = context.getMatrices();
        var10000.translate(26.0F, (float)(8 + 9), 0.0F);
        context.getMatrices().scale((moodTextWidth - 8.0F) * moodRender, 1.0F, 1.0F);
        context.fill(0, 0, 1, 1, ModRoles.JESTER.color() | (int)(moodAlpha * 255.0F) << 24);
        context.getMatrices().pop();
        ci.cancel();
        }
    }
}
