package org.agmas.noellesroles.client.renderer;

import org.agmas.noellesroles.entity.CalamityMarkEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/**
 * 灾厄印记实体渲染器
 * 
 * 对设陷者显示半透明的紫色印记
 * 对其他玩家完全隐形
 */
public class CalamityMarkEntityRenderer extends EntityRenderer<CalamityMarkEntity> {
    
    // 使用附魔光效纹理作为印记效果
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/misc/enchanted_glint_entity.png");
    
    public CalamityMarkEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }
    
    @Override
    public void render(CalamityMarkEntity entity, float yaw, float tickDelta, MatrixStack matrices, 
                       VertexConsumerProvider vertexConsumers, int light) {
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 只对设陷者（所有者）可见
        if (!entity.isVisibleTo(client.player)) {
            return;
        }
        
        matrices.push();
        
        // 旋转使其平躺在地面上
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        
        // 缓慢旋转动画
        float rotation = (entity.age + tickDelta) * 2.0f;
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
        
        // 缩放
        float scale = 0.8f;
        // 添加脉动效果
        float pulse = (float) Math.sin((entity.age + tickDelta) * 0.1) * 0.1f + 1.0f;
        matrices.scale(scale * pulse, scale * pulse, scale);
        
        // 渲染半透明的印记
        renderMark(matrices, vertexConsumers, light);
        
        matrices.pop();
        
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
    
    /**
     * 渲染印记图形
     */
    private void renderMark(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f posMatrix = entry.getPositionMatrix();
        
        // 使用半透明渲染层
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        
        // 紫色半透明
        int red = 139;
        int green = 69;
        int blue = 200;
        int alpha = 150;
        
        float size = 0.5f;
        
        // 绘制六边形印记
        drawHexagon(vertexConsumer, posMatrix, entry, size, red, green, blue, alpha, light);
    }
    
    /**
     * 绘制六边形
     */
    private void drawHexagon(VertexConsumer consumer, Matrix4f posMatrix, MatrixStack.Entry entry,
                             float size, int r, int g, int b, int a, int light) {
        // 中心点
        float cx = 0, cy = 0;
        
        // 绘制六个三角形组成六边形
        for (int i = 0; i < 6; i++) {
            float angle1 = (float) (Math.PI / 3 * i);
            float angle2 = (float) (Math.PI / 3 * (i + 1));
            
            float x1 = (float) (size * Math.cos(angle1));
            float y1 = (float) (size * Math.sin(angle1));
            float x2 = (float) (size * Math.cos(angle2));
            float y2 = (float) (size * Math.sin(angle2));
            
            // 三角形顶点
            consumer.vertex(posMatrix, cx, cy, 0)
                    .color(r, g, b, a)
                    .texture(0.5f, 0.5f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(entry, 0, 0, 1);
            
            consumer.vertex(posMatrix, x1, y1, 0)
                    .color(r, g, b, a)
                    .texture(0.5f + x1 / size * 0.5f, 0.5f + y1 / size * 0.5f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(entry, 0, 0, 1);
            
            consumer.vertex(posMatrix, x2, y2, 0)
                    .color(r, g, b, a)
                    .texture(0.5f + x2 / size * 0.5f, 0.5f + y2 / size * 0.5f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(entry, 0, 0, 1);
            
            // 第二个三角形（背面）
            consumer.vertex(posMatrix, cx, cy, 0)
                    .color(r, g, b, a)
                    .texture(0.5f, 0.5f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(entry, 0, 0, -1);
            
            consumer.vertex(posMatrix, x2, y2, 0)
                    .color(r, g, b, a)
                    .texture(0.5f + x2 / size * 0.5f, 0.5f + y2 / size * 0.5f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(entry, 0, 0, -1);
            
            consumer.vertex(posMatrix, x1, y1, 0)
                    .color(r, g, b, a)
                    .texture(0.5f + x1 / size * 0.5f, 0.5f + y1 / size * 0.5f)
                    .overlay(OverlayTexture.DEFAULT_UV)
                    .light(light)
                    .normal(entry, 0, 0, -1);
        }
    }
    
    @Override
    public Identifier getTexture(CalamityMarkEntity entity) {
        return TEXTURE;
    }
}