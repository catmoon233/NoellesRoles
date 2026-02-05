package org.agmas.noellesroles.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.agmas.noellesroles.packet.LootS2CPacket;

/**
 * 抽奖命令
 * 玩家调用抽奖命令，服务器进行抽奖并向玩家返回结果
 * 玩家根据结果包播放相关动画
 */
public class LootCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> {
                    dispatcher.register(Commands.literal("StartLoot").executes(context -> {
                        // TODO : Start loot
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        ServerPlayNetworking.send(player, new LootS2CPacket(0));
                        return 1;
                    }));
        });
    }
}