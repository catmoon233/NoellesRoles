package org.agmas.noellesroles.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.WorldBlackoutComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.GameFunctions.WinStatus;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;

public class GameFunctionsCommand {
  public static void register() {
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> {
          dispatcher.register(Commands.literal("tmm:game").requires(source -> source.hasPermission(2))
              .then(Commands.literal("win").then(Commands.argument("id", StringArgumentType.string())
                  .executes(GameFunctionsCommand::executeWinWithOnlyId)
                  .then(Commands.argument("title", ComponentArgument.textComponent(registryAccess))
                      .then(Commands
                          .argument(
                              "subtitle", ComponentArgument.textComponent(registryAccess))
                          .executes(GameFunctionsCommand::executeWinWithIdAndTitle)))))
              .then(Commands.literal("reset").then(Commands.literal("normal").executes((context) -> {
                GameFunctions.tryResetTrain(context.getSource().getLevel());
                return 1;
              })).then(Commands.literal("clean").executes((context) -> {
                GameFunctions.tryResetTrainOnlySomeBlock(context.getSource().getLevel());
                return 1;
              })))
              .then(Commands.literal("blackout").executes((context) -> {
                return executeBlackout(context, -1);
              }).then(Commands.literal("stop").executes((context) -> {
                return executeBlackout(context, 0);
              }))));
        });
  }

  public static int executeBlackout(CommandContext<CommandSourceStack> context, int time) {
    var wbc = WorldBlackoutComponent.KEY.get(context.getSource().getLevel());
    if (time != 0) {
      wbc.triggerBlackout();
    }else{
      wbc.reset();
    }
    return 1;
  }

  public static int executeWinWithOnlyId(CommandContext<CommandSourceStack> context) {
    String id = StringArgumentType.getString(context, "id");
    var roundComponent = GameRoundEndComponent.KEY.get(context.getSource().getLevel());
    roundComponent.CustomWinnerID = id;
    // roundComponent
    roundComponent.CustomWinnerSubtitle = null;
    roundComponent.CustomWinnerTitle = null;
    roundComponent.setWinStatus(WinStatus.CUSTOM);
    roundComponent.sync();
    GameFunctions.stopGame(context.getSource().getLevel());
    return 1;
  }

  public static int executeWinWithIdAndTitle(CommandContext<CommandSourceStack> context) {
    Component title = ComponentArgument.getComponent(context, "title");
    Component subtitle = ComponentArgument.getComponent(context, "subtitle");
    String id = StringArgumentType.getString(context, "id");
    ServerPlayer serverPlayer = context.getSource().getPlayer();
    if (serverPlayer != null) {
      try {
        title = ComponentUtils.updateForEntity(
            (CommandSourceStack) context.getSource(),
            title,
            serverPlayer, 0);
        subtitle = ComponentUtils.updateForEntity(
            (CommandSourceStack) context.getSource(),
            subtitle,
            serverPlayer, 0);
      } catch (CommandSyntaxException e) {
        e.printStackTrace();
        context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        return 0;
      }
    }
    var roundComponent = GameRoundEndComponent.KEY.get(context.getSource().getLevel());
    roundComponent.CustomWinnerID = id;
    // roundComponent
    roundComponent.CustomWinnerSubtitle = subtitle;
    roundComponent.CustomWinnerTitle = title;
    roundComponent.setWinStatus(WinStatus.CUSTOM_COMPONENT);
    roundComponent.sync();
    GameFunctions.stopGame(context.getSource().getLevel());
    return 1;
  }
}
