package org.agmas.noellesroles.utils;

import java.util.HashMap;

import org.agmas.noellesroles.packet.ScanAllTaskPointsPayload;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.block.FoodPlatterBlock;
import dev.doctor4t.trainmurdermystery.block.MountableBlock;
import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.block.SprinklerBlock;
import dev.doctor4t.trainmurdermystery.block.ToiletBlock;
import dev.doctor4t.trainmurdermystery.block.TrimmedBedBlock;
import dev.doctor4t.trainmurdermystery.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.trainmurdermystery.block_entity.SmallDoorBlockEntity;
import dev.doctor4t.trainmurdermystery.cca.AreasWorldComponent;
import dev.doctor4t.trainmurdermystery.event.OnTrainAreaHaveReseted;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.item.CocktailItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class MapScanner {
    public static void registerMapScanEvent() {
        OnTrainAreaHaveReseted.EVENT.register((serverLevel) -> {
            scanAllTaskBlocks(serverLevel);
            for (var player : serverLevel.players()) {
                ServerPlayNetworking.send(player, new ScanAllTaskPointsPayload(GameFunctions.taskBlocks));
            }
        });
    }

    public static void scanAllTaskBlocks(ServerLevel serverLevel) {
        TMM.LOGGER.info("Start to scan points!");
        ServerLevel localLevel = serverLevel;
        if (GameFunctions.taskBlocks == null) {
            GameFunctions.taskBlocks = new HashMap<>();
        }
        GameFunctions.taskBlocks.clear();
        var areas = AreasWorldComponent.KEY.get(serverLevel);
        BlockPos backupMinPos = BlockPos.containing(areas.getResetTemplateArea().getMinPosition());
        BlockPos backupMaxPos = BlockPos.containing(areas.getResetTemplateArea().getMaxPosition());
        BoundingBox backupTrainBox = BoundingBox.fromCorners(backupMinPos, backupMaxPos);
        BlockPos trainMinPos = BlockPos.containing(areas.getResetPasteArea().getMinPosition());
        BlockPos trainMaxPos = trainMinPos.offset(backupTrainBox.getLength());
        BoundingBox trainBox = BoundingBox.fromCorners(trainMinPos, trainMaxPos);
        for (int k = trainBox.minZ(); k <= trainBox.maxZ(); k++) {
            for (int l = trainBox.minY(); l <= trainBox.maxY(); l++) {
                for (int m = trainBox.minX(); m <= trainBox.maxX(); m++) {
                    BlockPos blockPos6 = new BlockPos(m, l, k);
                    var blockState = localLevel.getBlockState(blockPos6);
                    if (blockState.is(BlockTags.AIR))
                        continue;
                    // blockCounts++;
                    if (blockState.is(Blocks.NOTE_BLOCK)) {
                        GameFunctions.taskBlocks.put(blockPos6, 10);
                    } else if (blockState.is(Blocks.BLACK_CONCRETE)) {
                        BlockPos blockPos7 = new BlockPos(m, l + 1, k);
                        var blockState2 = localLevel.getBlockState(blockPos7);
                        if (blockState2.is(BlockTags.WOOL_CARPETS) || blockState2.is(BlockTags.AIR)) {
                            GameFunctions.taskBlocks.put(blockPos6, 5);
                        }
                    } else if (blockState.getBlock() instanceof TrimmedBedBlock) {
                        GameFunctions.taskBlocks.put(blockPos6, 4);
                    } else if (blockState.getBlock() instanceof ToiletBlock) {
                        GameFunctions.taskBlocks.put(blockPos6, 8);
                    } else if (blockState.getBlock() instanceof MountableBlock) {
                        GameFunctions.taskBlocks.put(blockPos6, 9);
                    } else if (blockState.getBlock() instanceof SmallDoorBlock
                            && blockState.getValue(SmallDoorBlock.HALF).equals(DoubleBlockHalf.LOWER)) {
                        if (localLevel.getBlockEntity(blockPos6) instanceof SmallDoorBlockEntity entity) {
                            if (entity.getKeyName() != null && !entity.getKeyName().isEmpty())
                                GameFunctions.taskBlocks.put(blockPos6, 7);
                        }
                    } else if (blockState.getBlock() instanceof FoodPlatterBlock) {
                        if (localLevel.getBlockEntity(blockPos6) instanceof BeveragePlateBlockEntity entity) {
                            var items = entity.getStoredItems();
                            if (items.size() > 0) {
                                ItemStack item_0 = items.get(0);
                                Item item_ = item_0.getItem();
                                if ((item_ instanceof CocktailItem)) {
                                    GameFunctions.taskBlocks.put(blockPos6, 2);
                                } else {
                                    FoodProperties foodPro = item_0.get(DataComponents.FOOD);
                                    if (foodPro != null) {
                                        GameFunctions.taskBlocks.put(blockPos6, 1);
                                    }
                                }
                            }

                        }
                    } else if (blockState.getBlock() instanceof LecternBlock) {
                        if (blockState.getValue(LecternBlock.HAS_BOOK)) {
                            GameFunctions.taskBlocks.put(blockPos6, 6);
                        }
                    } else if (blockState.getBlock() instanceof SprinklerBlock) {
                        GameFunctions.taskBlocks.put(blockPos6, 3);
                    }
                }
            }
        }
        TMM.LOGGER.info("Successed scanned task points! Total {}.", GameFunctions.taskBlocks.size());
        // Minecraft.getInstance().player.displayClientMessage(
        // Component
        // .translatable("msg.noellesroles.taskpoint.available",
        // Component.keybind("key.noellesroles.taskinstinct"))
        // .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
        // true);服务端扫描点位
    }

}
