package org.agmas.noellesroles.mixin.client.insane;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.trainmurdermystery.TMM;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EmptyRender extends EntityRenderer<Entity> {
    public static final ResourceLocation DEFAULT_TEXTURE = TMM.id("textures/entity/player_body_default.png");

    protected EmptyRender(Context context) {
        super(context);
    }

    @Override
    protected int getSkyLightLevel(Entity entity, BlockPos blockPos) {
        return 0;
    }

    @Override
    protected int getBlockLightLevel(Entity entity, BlockPos blockPos) {
        return 0;
    }

    @Override
    public boolean shouldRender(Entity entity, Frustum frustum, double d, double e, double f) {
        return false;
    }

    @Override
    public Vec3 getRenderOffset(Entity entity, float f) {
        return Vec3.ZERO;
    }

    @Override
    public void render(Entity entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource,
            int i) {
    }

    protected boolean shouldShowName(Entity entity) {
        return entity.shouldShowName()
                || entity.hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity;
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return DEFAULT_TEXTURE;
    }


    protected void renderNameTag(Entity entity, Component component, PoseStack poseStack,
            MultiBufferSource multiBufferSource, int i, float f) {
    }

    protected float getShadowRadius(Entity entity) {
        return 0;
    }
}
