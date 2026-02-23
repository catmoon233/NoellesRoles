package org.agmas.noellesroles.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.agmas.noellesroles.block_entity.VendingMachinesBlockEntity;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;

import java.util.Collection;

public class AddGoodsCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(Commands.literal("addGoods")
                            .requires(source -> source.hasPermission(2))
                            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                    .then(Commands.argument("player", EntityArgument.player())
                                            .then(Commands.argument("price", IntegerArgumentType.integer(0))
                                                    .executes(AddGoodsCommand::execute)))));
                });
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            // 获取参数
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
            ServerPlayer player = EntityArgument.getPlayer(context, "player");
            int price = IntegerArgumentType.getInteger(context, "price");

            // 获取方块实体
            BlockEntity blockEntity = context.getSource().getLevel().getBlockEntity(pos);
            
            if (!(blockEntity instanceof VendingMachinesBlockEntity vendingEntity)) {
                context.getSource().sendFailure(Component.literal("指定位置不是自动售货机方块"));
                return 0;
            }

            // 获取玩家主手物品
            ItemStack itemStack = player.getMainHandItem();
            if (itemStack.isEmpty()) {
                context.getSource().sendFailure(Component.literal("玩家主手没有物品"));
                return 0;
            }
            
            // 验证物品有效性
            if (itemStack.getItem() == null) {
                context.getSource().sendFailure(Component.literal("物品无效"));
                return 0;
            }

            // 创建商店条目
            ShopEntry shopEntry = new ShopEntry(itemStack.copy(), price, ShopEntry.Type.TOOL);
            
            // 添加到自动售货机
            vendingEntity.addItem(shopEntry);

            // 发送成功消息
            context.getSource().sendSuccess(() -> 
                Component.literal("成功添加商品: ")
                    .append(itemStack.getDisplayName())
                    .append(Component.literal(" 价格: $" + price))
                    .append(Component.literal(" 到位置: " + pos.toShortString())), 
                true);

            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            context.getSource().sendFailure(Component.literal("添加商品时发生错误: " + e.getMessage()));
            return 0;
        }
    }
}