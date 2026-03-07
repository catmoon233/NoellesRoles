package org.agmas.noellesroles.client.renderer;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class ThrowingKnifeRenderer extends ArrowRenderer {
    public ThrowingKnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @Nullable ResourceLocation getTextureLocation(Entity entity) {
        return ResourceLocation.tryParse("noellesroles:textures/entity/throwing_knife.png");
    }
}
