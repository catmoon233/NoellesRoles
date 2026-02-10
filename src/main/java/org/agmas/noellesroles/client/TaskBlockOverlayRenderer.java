package org.agmas.noellesroles.client;

import java.awt.Color;
import java.util.OptionalDouble;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TaskBlockOverlayRenderer {
    // 创建带厚度的永远不被遮挡线框

    public static final RenderType ALWAYS_VISIBLE_THICK_LINES = RenderType.create("always_visible_thick_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(4.0))) // 线宽4.0
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .createCompositeState(false));

    public static void renderBlockOverlay(WorldRenderContext context,
            BlockPos blockPos, Color color, float alpha, boolean colorize, float lineWidth) {
        Minecraft client = Minecraft.getInstance();
        Level world = client.level;

        if (world == null)
            return;
        VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
        if (shape.isEmpty()) {
            return; // 如果形状为空，则不渲染
        }
        // RenderSystem.line
        // 设置矩阵
        PoseStack matrices = context.matrixStack();
        matrices.pushPose();
        // 获取相机的位位置
        Camera camera = context.camera();
        Vec3 cameraPos = camera.getPosition();
        // 计算方块相对于相机的位置
        double offsetX = blockPos.getX() - cameraPos.x;
        double offsetY = blockPos.getY() - cameraPos.y;
        double offsetZ = blockPos.getZ() - cameraPos.z;
        matrices.translate(offsetX, offsetY, offsetZ);
        // 设置高亮颜色和线宽
        float red = color.getRed();
        float green = color.getGreen();
        float blue = color.getBlue();

        // 获取顶点消费者提供者
        MultiBufferSource vertexConsumers = context.consumers();
        // 获取线条渲染层
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(ALWAYS_VISIBLE_THICK_LINES);

        // 使用WorldRenderer.drawShapeOutline来绘制形状的轮廓
        // 注意：这里我们使用context.matrixStack()提供的矩阵堆栈
        RenderSystem.lineWidth(lineWidth);
        LevelRenderer.renderVoxelShape(
                context.matrixStack(),
                vertexConsumer,
                shape,
                0, 0, 0,
                red, green, blue, alpha,
                false);
        // 结束渲染

        matrices.popPose();
    }

    public static void renderTaskBlockOverlay(WorldRenderContext context,
            BlockPos blockPos) {
    }

    public static void render(WorldRenderContext renderContext) {
        if (!NoellesrolesClient.isTaskInstinctEnabled)
            return;
        var instance = Minecraft.getInstance();
        if (instance == null)
            return;
        if (instance.player == null)
            return;
        if (instance.level == null)
            return;
        if (TMMClient.gameComponent == null)
            return;
        if (!TMMClient.gameComponent.isRunning())
            return;

        /**
         * 1: 食物 2: 水 3: 洗澡 4: 床 5: 跑步机 6: 讲台
         */
        boolean shouldDisplay[] = { false, false, false, false, false, false, false, false, false, false };
        if (TMMClient.isPlayerSpectatingOrCreative()) {
            for (int i = 0; i < shouldDisplay.length; i++) {
                shouldDisplay[i] = true;
            }
        }
        var playerMood = PlayerMoodComponent.KEY.get(Minecraft.getInstance().player);
        if (playerMood != null) {
            for (var task : playerMood.tasks.values()) {
                switch (task.getType()) {
                    case BATHE:
                        shouldDisplay[3] = true;
                        break;
                    case DRINK:
                        shouldDisplay[2] = true;
                        break;
                    case EAT:
                        shouldDisplay[1] = true;
                        break;
                    case EXERCISE:
                        shouldDisplay[5] = true;
                        break;
                    case MEDITATE:
                        // 无
                        break;
                    case OUTSIDE:
                        // 无
                        break;
                    case RAED_BOOK:
                        shouldDisplay[6] = true;
                        break;
                    case SLEEP:
                        shouldDisplay[4] = true;
                        break;
                    default:
                        break;

                }
            }
        }
        for (var set : NoellesrolesClient.taskBlocks.entrySet()) {
            var pos = set.getKey();
            int type = set.getValue();
            switch (type) { // 1: 食物 2: 水 3: 洗澡 4: 床 5: 跑步机 6: 讲台
                case 1:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, Color.GREEN, 1f, true, 2f);
                    break;
                case 2:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, new Color(0, 255, 220), 1f,
                                true, 2f);
                    break;
                case 3:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, new Color(141, 234, 189), 1f,
                                true, 2f);
                    break;
                case 4:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, new Color(234, 88, 88), 1f,
                                true, 2f);
                    break;
                case 5:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, new Color(255, 242, 0), 0.5f,
                                true, 2f);
                    break;
                case 6:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos,
                                new Color(255, 127, 39), 0.5f,
                                true, 2f);
                    break;
                default:
                    break;
            }
        }
        // 恢复渲染状态
    }

}
