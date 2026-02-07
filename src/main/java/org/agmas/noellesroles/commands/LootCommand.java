package org.agmas.noellesroles.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.noellesroles.packet.LootS2CPacket;
import org.agmas.noellesroles.utils.LotteryManager;

/**
 * 抽奖命令
 * 玩家调用抽奖命令，服务器进行抽奖并向玩家返回结果
 * 玩家根据结果包播放相关动画
 */
public class LootCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(Commands.literal("Loot")
                        .then(Commands.literal("Start")
                            .executes(LootCommand::startLoot)
                        )
                        .then(Commands.literal("SetData")
                            .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("AddOrDegreeChance")
                                    .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("chance", IntegerArgumentType.integer())
                                            .executes(LootCommand::addOrDegreeChance))))
                        )
                    );
        });
    }

    /** 进行抽奖，目前是基于玩家进行随机*/
    protected static int startLoot(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null)
            return 0;
        if(LotteryManager.getInstance().canRoll(player))
        {
            // TODO : 遇上过一次动画停一半，当时在录视频不知道是不是受到了帧率影响，但是应该是不会影响结果的，毕竟如果没抽的话不会有动画
            ServerPlayNetworking.send(player, new LootS2CPacket(LotteryManager.getInstance().rollOnce(player)));
            // 抽一次减一次
            LotteryManager.getInstance().addOrDegreeLotteryChance(player, -1);
        }
        else {
            // 抽奖次数 = 0 限制
            player.sendSystemMessage(Component.translatable("message.noellesroles.loot.limit"));
        }
        return 1;
    }
    protected static int addOrDegreeChance(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null)
            return 0;
        LotteryManager.getInstance().addOrDegreeLotteryChance(player, IntegerArgumentType.getInteger(context, "chance"));
        return 1;
    }
}