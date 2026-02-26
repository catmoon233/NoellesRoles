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
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.arguments.BoolArgumentType;

public class GameFunctionsCommand {
  public static void register() {
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> {
          dispatcher.register(Commands.literal("tmm:game").requires(source -> source.hasPermission(2))
              .then(Commands.literal("win").then(Commands.argument("id", StringArgumentType.id()).then(
              Commands.argument("color", ModColorArgument.color())
                  .executes(GameFunctionsCommand::executeWinWithOnlyId)
                  .then(Commands.argument("title", ComponentArgument.textComponent(registryAccess))
                      .then(Commands
                          .argument(
                              "subtitle", ComponentArgument.textComponent(registryAccess))
                          .executes(GameFunctionsCommand::executeWinWithIdAndTitle))))))
              .then(Commands.literal("reset").then(Commands.literal("normal").executes((context) -> {
                GameFunctions.tryAutoTrainReset(context.getSource().getLevel());
                context.getSource().sendSuccess(() -> Component.literal("Normal Reset(copy)!"), true);
                return 1;
              })).then(Commands.literal("clean").executes((context) -> {
                GameFunctions.tryResetTrainOnlySomeBlock(context.getSource().getLevel());
                context.getSource().sendSuccess(() -> Component.literal("Clean Reset (clean only)!"), true);

                return 1;
              })))
              .then(Commands.literal("blackout").executes((context) -> {
                return executeBlackout(context, -1);
              }).then(Commands.literal("stop").executes((context) -> {
                return executeBlackout(context, 0);
              })))
              .then(Commands.literal("kill")
                  .then(Commands.argument("victim", EntityArgument.player())
                      .then(Commands.argument("death_reason", StringArgumentType.string()).executes((context) -> {
                        ServerPlayer victim = EntityArgument.getPlayer(context, "victim");
                        String deathReason = StringArgumentType.getString(context, "death_reason");
                        return executeKillPlayer(context, victim, null, deathReason, true);
                      })
                          .then(Commands.argument("killer", EntityArgument.player()).executes((context) -> {
                            ServerPlayer victim = EntityArgument.getPlayer(context, "victim");
                            ServerPlayer killer = EntityArgument.getPlayer(context, "killer");
                            String deathReason = StringArgumentType.getString(context, "death_reason");
                            return executeKillPlayer(context, victim, killer, deathReason, true);
                          })
                              .then(Commands.argument("spawn_body", BoolArgumentType.bool()).executes((context) -> {
                                ServerPlayer victim = EntityArgument.getPlayer(context, "victim");
                                boolean spawnBody = BoolArgumentType.getBool(context, "spawn_body");
                                ServerPlayer killer = EntityArgument.getPlayer(context, "killer");
                                String deathReason = StringArgumentType.getString(context, "death_reason");
                                return executeKillPlayer(context, victim, killer, deathReason, spawnBody);
                              })))))));
        });
  }

  public static int executeKillPlayer(CommandContext<CommandSourceStack> context, ServerPlayer victim,
      @Nullable ServerPlayer killer, String deathReason, boolean spawnBody) {
    ResourceLocation deathReasonRL = null;
    try {
      deathReasonRL = ResourceLocation.tryParse(deathReason);
    } catch (Exception e) {
      context.getSource().sendFailure(Component.translatable("Wrong deathReason Resource Location %s!", deathReason));
      return 0;
    }
    final String deathReasonT = deathReasonRL.toLanguageKey();
    GameFunctions.killPlayer(victim, spawnBody, killer, deathReasonRL);
    context.getSource()
        .sendSuccess(() -> Component.translatable("Killed player %s by %s with reason %s (Spawn body: %s)",
            victim.getDisplayName(), (killer == null ? Component.literal("System") : killer.getDisplayName()),
            Component.translatable("death_reason." + deathReasonT), (spawnBody ? "Yes" : "No")), true);
    return 1;
  }

  public static int executeBlackout(CommandContext<CommandSourceStack> context, int time) {
    var wbc = WorldBlackoutComponent.KEY.get(context.getSource().getLevel());
    if (time != 0) {
      wbc.triggerBlackout();
      context.getSource()
          .sendSuccess(() -> Component.translatable("Triggered Blackout!"), true);

    } else {
      context.getSource()
          .sendSuccess(() -> Component.translatable("Stopped All Blackouts!"), true);
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
    roundComponent.CustomWinnerColor = java.awt.Color.WHITE.getRGB();
    roundComponent.setWinStatus(WinStatus.CUSTOM);
    roundComponent.sync();
    context.getSource()
        .sendSuccess(() -> Component.translatable("Stop the game with custom winner id [%s] (CUSTOM)", id), true);

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
    context.getSource().sendSuccess(
        () -> Component.translatable("Stop the game with custom winner id [%s] (CUSTOM_COMPONENT)", id), true);

    GameFunctions.stopGame(context.getSource().getLevel());
    return 1;
  }
}
