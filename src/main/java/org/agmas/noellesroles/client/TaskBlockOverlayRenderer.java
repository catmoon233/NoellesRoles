package org.agmas.noellesroles.client;

import java.awt.Color;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TaskBlockOverlayRenderer {
    public static void renderBlockOverlay(WorldRenderContext context,
            BlockPos blockPos, Color color, float alpha, boolean colorize) {
        Minecraft client = Minecraft.getInstance();
        Level world = client.level;

        if (world == null)
            return;
        VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
        if (shape.isEmpty()) {
            return; // 如果形状为空，则不渲染
        }
        // 获取相机的位位置
        Camera camera = context.camera();
        Vec3 cameraPos = camera.getPosition();
        // 计算方块相对于相机的位置
        double offsetX = blockPos.getX() - cameraPos.x;
        double offsetY = blockPos.getY() - cameraPos.y;
        double offsetZ = blockPos.getZ() - cameraPos.z;

        // 设置高亮颜色和线宽
        float red = color.getRed();
        float green = color.getGreen();
        float blue = color.getBlue();
        // 获取顶点消费者提供者
        MultiBufferSource vertexConsumers = context.consumers();
        // 获取线条渲染层
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.lines());

        // 使用WorldRenderer.drawShapeOutline来绘制形状的轮廓
        // 注意：这里我们使用context.matrixStack()提供的矩阵堆栈
        LevelRenderer.renderVoxelShape(
                context.matrixStack(),
                vertexConsumer,
                shape,
                offsetX, offsetY, offsetZ,
                red, green, blue, alpha,
                colorize);
    }

    public static void renderTaskBlockOverlay(WorldRenderContext context,
            BlockPos blockPos) {
    }

}
