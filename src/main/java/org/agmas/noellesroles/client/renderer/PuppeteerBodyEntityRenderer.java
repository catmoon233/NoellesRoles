package org.agmas.noellesroles.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.entity.PuppeteerBodyEntity;

import java.util.UUID;

/**
 * 傀儡本体实体渲染器
 * 
 * 使用玩家皮肤渲染傀儡本体
 */
public class PuppeteerBodyEntityRenderer extends EntityRenderer<PuppeteerBodyEntity> {
    
    private final HumanoidModel<PuppeteerBodyEntity> model;
    
    public PuppeteerBodyEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER));
    }
    
    @Override
    public void render(PuppeteerBodyEntity entity, float yaw, float tickDelta, PoseStack matrices, 
            MultiBufferSource vertexConsumers, int light) {
        
        //matrices.pushPose();
        
        // 调整渲染位置和旋转
        //matrices.translate(0.0, 0.0, 0.0);
        
        // 获取玩家皮肤纹理
        ResourceLocation texture = getTextureLocation(entity);
        
        // 获取渲染层
        RenderType renderLayer = RenderType.entityTranslucent(texture);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);
        
        // 设置模型姿势（站立姿势）
        model.setupAnim(entity, 0, 0, entity.tickCount + tickDelta, 0, 0);
        
        // 渲染模型
        model.renderToBuffer(matrices, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        
        //matrices.popPose();
        
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }


    @Override
    public ResourceLocation getTextureLocation(PuppeteerBodyEntity entity) {
        // 首先尝试通过 ownerUuid 从玩家列表获取皮肤
        UUID ownerUuid = entity.getOwnerUuid().orElse(null);
        
        if (ownerUuid != null) {
            Minecraft client = Minecraft.getInstance();
            if (client.getConnection() != null) {
                // 通过 UUID 从玩家列表获取皮肤
                PlayerInfo entry = client.getConnection().getPlayerInfo(ownerUuid);
                if (entry != null) {
                    return entry.getSkin().texture();
                }
            }
            // 如果玩家不在列表中（可能离线），使用基于 UUID 的默认皮肤
            return DefaultPlayerSkin.get(ownerUuid).texture();
        }
        
        // 尝试从 GameProfile 获取（服务端设置的备选方案）
        GameProfile profile = entity.getSkinProfile();
        if (profile != null && profile.getId() != null) {
            Minecraft client = Minecraft.getInstance();
            if (client.getConnection() != null) {
                PlayerInfo entry = client.getConnection().getPlayerInfo(profile.getId());
                if (entry != null) {
                    return entry.getSkin().texture();
                }
            }
            return DefaultPlayerSkin.get(profile.getId()).texture();
        }
        
        // 最后的回退：使用固定的默认皮肤（Steve）
        return DefaultPlayerSkin.get(new UUID(0, 0)).texture();
    }
}