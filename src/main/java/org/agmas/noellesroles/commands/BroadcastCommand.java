package org.agmas.noellesroles.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import java.util.Iterator;

public class BroadcastCommand {
    @SuppressWarnings("rawtypes")
    public static void register() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, registryAccess, environment) -> {
            commandDispatcher.register((Commands.literal("broadcast")
                    .requires((commandSourceStack) -> {
                        return commandSourceStack.hasPermission(2);
                    })).then(Commands.argument("targets", EntityArgument.players())
                            .then(Commands.argument("message", ComponentArgument.textComponent(registryAccess))
                                    .executes((commandContext) -> {
                                        int i = 0;

                                        for (Iterator var2 = EntityArgument.getPlayers(commandContext, "targets")
                                                .iterator(); var2.hasNext(); ++i) {
                                            ServerPlayer serverPlayer = (ServerPlayer) var2.next();

                                            org.agmas.noellesroles.packet.BroadcastMessageS2CPacket packet = new org.agmas.noellesroles.packet.BroadcastMessageS2CPacket(
                                                    ComponentUtils.updateForEntity(
                                                            (CommandSourceStack) commandContext.getSource(),
                                                            ComponentArgument.getComponent(commandContext, "message"),
                                                            serverPlayer, 0));
                                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
                                                    .send(serverPlayer, packet);
                                        }
                                        return i;
                                    }))));
        });
    }
}