package org.agmas.noellesroles.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class InsaneKillerRenderMixin
        extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public InsaneKillerRenderMixin(EntityRendererProvider.Context context,
            PlayerModel<AbstractClientPlayer> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "setupRotations", at = @At("RETURN"))
    protected void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float f, float g, float h, float i,
            CallbackInfo ci) {
        if (player.isSpectator())
            return;

        InsaneKillerPlayerComponent component = InsaneKillerPlayerComponent.KEY.get(player);
        if (component.isActive) {
            // 模拟尸体渲染：绕 Z 轴旋转 90 度，并下移到地面
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            poseStack.translate(0.0F, -0.85F, 0.0F);
        }
    }
}