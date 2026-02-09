package org.agmas.noellesroles.client;

import java.awt.Color;
import java.util.OptionalDouble;

import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.component.ModComponents;

import dev.doctor4t.trainmurdermystery.item.CocktailItem;

import dev.doctor4t.trainmurdermystery.block.FoodPlatterBlock;
import dev.doctor4t.trainmurdermystery.block.SprinklerBlock;
import dev.doctor4t.trainmurdermystery.block.TrimmedBedBlock;
import dev.doctor4t.trainmurdermystery.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.trainmurdermystery.cca.AreasWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TaskBlockOverlayRenderer {
    // 创建带厚度的永远不被遮挡线框
    public static void scanAllTaskBlocks() {
        Noellesroles.LOGGER.info("Start to scan points!");
        NoellesrolesClient.scanTaskPointsCountDown = -1;
        if (Minecraft.getInstance() == null)
            return;
        if (Minecraft.getInstance().level == null)
            return;
        if (Minecraft.getInstance().player == null)
            return;
        var game = TMMClient.gameComponent;
        if (game == null || !game.isRunning()) {
            NoellesrolesClient.scanTaskPointsCountDown = -1;
            return;
        }
        ClientLevel localLevel = Minecraft.getInstance().level;
        NoellesrolesClient.taskBlocks.clear();
        var areas = AreasWorldComponent.KEY.get(Minecraft.getInstance().level);
        BlockPos backupMinPos = BlockPos.containing(areas.getResetTemplateArea().getMinPosition());
        BlockPos backupMaxPos = BlockPos.containing(areas.getResetTemplateArea().getMaxPosition());
        BoundingBox backupTrainBox = BoundingBox.fromCorners(backupMinPos, backupMaxPos);
        BlockPos trainMinPos = BlockPos.containing(areas.getResetPasteArea().getMinPosition());
        BlockPos trainMaxPos = trainMinPos.offset(backupTrainBox.getLength());
        BoundingBox trainBox = BoundingBox.fromCorners(trainMinPos, trainMaxPos);
        int blockCounts = 0;
        for (int k = trainBox.minZ(); k <= trainBox.maxZ(); k++) {
            for (int l = trainBox.minY(); l <= trainBox.maxY(); l++) {
                for (int m = trainBox.minX(); m <= trainBox.maxX(); m++) {
                    BlockPos blockPos6 = new BlockPos(m, l, k);
                    if (!localLevel.isLoaded(blockPos6))
                        continue;
                    var blockState = localLevel.getBlockState(blockPos6);
                    if (blockState.is(Blocks.AIR))
                        continue;
                    if (blockState.is(Blocks.BLACK_CONCRETE)) {
                        NoellesrolesClient.taskBlocks.put(blockPos6, 5);
                        continue;
                    }
                    if (blockState.is(Blocks.LECTERN)) {
                        if (localLevel.getBlockEntity(blockPos6) instanceof LecternBlockEntity entity) {
                            if (!entity.getBook().isEmpty()) {
                                NoellesrolesClient.taskBlocks.put(blockPos6, 6);
                            }
                        }
                        continue;
                    }
                    blockCounts++;
                    if (blockState.getBlock() instanceof TrimmedBedBlock
                            && blockState.getValue(TrimmedBedBlock.PART).equals(BedPart.HEAD)) {
                        NoellesrolesClient.taskBlocks.put(blockPos6, 4);
                        // 暂时忽略
                    } else if (blockState.getBlock() instanceof FoodPlatterBlock) {
                        if (localLevel.getBlockEntity(blockPos6) instanceof BeveragePlateBlockEntity entity) {
                            var items = entity.getStoredItems();
                            if (items.size() > 0) {
                                ItemStack item_0 = items.get(0);
                                Item item_ = item_0.getItem();
                                if ((item_ instanceof CocktailItem)) {
                                    NoellesrolesClient.taskBlocks.put(blockPos6, 2);
                                } else {
                                    FoodProperties foodPro = item_0.get(DataComponents.FOOD);
                                    if (foodPro != null) {
                                        NoellesrolesClient.taskBlocks.put(blockPos6, 1);
                                    }
                                }
                            }

                        }
                    } else if (blockState.getBlock() instanceof SprinklerBlock) {
                        NoellesrolesClient.taskBlocks.put(blockPos6, 3);
                    }
                }
            }
        }
        if (blockCounts <= 0) {
            Noellesroles.LOGGER.warn("Failed to scan blocks. Schedule another 'scan points' event in 5s!");
            NoellesrolesClient.scanTaskPointsCountDown = 100;
        }
        Minecraft.getInstance().player.displayClientMessage(
                Component
                        .translatable("msg.noellesroles.taskpoint.available",
                                Component.keybind("key.noellesroles.taskinstinct"))
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                true);
    }

    public static final RenderType ALWAYS_VISIBLE_THICK_LINES = RenderType.create(
            "always_visible_thick_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false,
            false,
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
            switch (type) { // 1: 食物 2: 水 3: 洗澡 4: 床
                case 1:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, Color.PINK, 0.5f, true, 2f);
                    break;
                case 2:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, new Color(153, 217, 234), 0.5f,
                                true, 2f);
                    break;
                case 3:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, new Color(141, 234, 189), 0.5f,
                                true, 2f);
                    break;
                case 4:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, new Color(234, 88, 88), 0.5f,
                                true, 2f);
                    break;
                case 5:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, new Color(0, 0, 0), 0.5f,
                                true, 2f);
                    break;
                case 6:
                    if (shouldDisplay[type])
                        TaskBlockOverlayRenderer.renderBlockOverlay(renderContext, pos, new Color(0, 0, 0), 0.5f,
                                true, 2f);
                    break;
                default:
                    break;
            }
        }
        // 恢复渲染状态
    }

}
