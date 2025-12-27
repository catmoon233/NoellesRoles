package org.agmas.noellesroles.client.renderer;

import org.agmas.noellesroles.entity.PuppeteerBodyEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * 傀儡本体实体渲染器
 * 
 * 使用玩家皮肤渲染傀儡本体
 */
public class PuppeteerBodyEntityRenderer extends EntityRenderer<PuppeteerBodyEntity> {
    
    private final BipedEntityModel<PuppeteerBodyEntity> model;
    
    public PuppeteerBodyEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        this.model = new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER));
    }
    
    @Override
    public void render(PuppeteerBodyEntity entity, float yaw, float tickDelta, MatrixStack matrices, 
            VertexConsumerProvider vertexConsumers, int light) {
        
        matrices.push();
        
        // 调整渲染位置和旋转
        matrices.translate(0.0, 0.0, 0.0);
        
        // 获取玩家皮肤纹理
        Identifier texture = getTexture(entity);
        
        // 获取渲染层
        RenderLayer renderLayer = RenderLayer.getEntityTranslucent(texture);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);
        
        // 设置模型姿势（站立姿势）
        model.setAngles(entity, 0, 0, entity.age + tickDelta, 0, 0);
        
        // 渲染模型
        model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
        
        matrices.pop();
        
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
    
    @Override
    public Identifier getTexture(PuppeteerBodyEntity entity) {
        // 首先尝试通过 ownerUuid 从玩家列表获取皮肤
        UUID ownerUuid = entity.getOwnerUuid().orElse(null);
        
        if (ownerUuid != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() != null) {
                // 通过 UUID 从玩家列表获取皮肤
                PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(ownerUuid);
                if (entry != null) {
                    return entry.getSkinTextures().texture();
                }
            }
            // 如果玩家不在列表中（可能离线），使用基于 UUID 的默认皮肤
            return DefaultSkinHelper.getSkinTextures(ownerUuid).texture();
        }
        
        // 尝试从 GameProfile 获取（服务端设置的备选方案）
        GameProfile profile = entity.getSkinProfile();
        if (profile != null && profile.getId() != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getNetworkHandler() != null) {
                PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(profile.getId());
                if (entry != null) {
                    return entry.getSkinTextures().texture();
                }
            }
            return DefaultSkinHelper.getSkinTextures(profile.getId()).texture();
        }
        
        // 最后的回退：使用固定的默认皮肤（Steve）
        return DefaultSkinHelper.getSkinTextures(new UUID(0, 0)).texture();
    }
}