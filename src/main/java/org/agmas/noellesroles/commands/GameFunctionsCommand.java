package org.agmas.noellesroles.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.replay.GameReplayUtils;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.WorldBlackoutComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.GameFunctions.WinStatus;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pro.fazeclan.river.stupid_express.StupidExpress;

import com.mojang.brigadier.arguments.StringArgumentType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.packet.ScanAllTaskPointsPayload;
import org.agmas.noellesroles.utils.MapScanner;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.arguments.BoolArgumentType;

public class GameFunctionsCommand {
  public static void register() {
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> {
          dispatcher.register(Commands.literal("tmm:game").requires(source -> source.hasPermission(2))
              .then(Commands.literal("win")
                  .then(Commands.argument("color", ModColorArgument.color()).then(
                      Commands.argument("id", StringArgumentType.string())
                          .executes(GameFunctionsCommand::executeWinWithOnlyId))
                      .then(Commands.argument("title", ComponentArgument.textComponent(registryAccess))
                          .then(Commands
                              .argument(
                                  "subtitle", ComponentArgument.textComponent(registryAccess))
                              .executes(GameFunctionsCommand::executeWinWithIdAndTitle)))))
              .then(Commands.literal("reset").then(Commands.literal("normal").executes((context) -> {
                GameFunctions.tryAutoTrainReset(context.getSource().getLevel());
                context.getSource().sendSuccess(() -> Component.literal("Normal Reset(copy)!"), true);
                return 1;
              })).then(Commands.literal("clean").executes((context) -> {
                GameFunctions.tryResetTrainOnlySomeBlock(context.getSource().getLevel());
                context.getSource().sendSuccess(() -> Component.literal("Clean Reset (clean only)!"), true);

                return 1;
              })).then(Commands.literal("scan").executes((context) -> {
                MapScanner.scanAllTaskBlocks(context.getSource().getLevel());
                HashMap<Integer, Boolean> map = new HashMap<>();
                for (Entry<BlockPos, Integer> entry : GameFunctions.taskBlocks.entrySet()) {
                  map.putIfAbsent(entry.getValue(), true);
                }
                context.getSource().sendSuccess(
                    () -> Component.translatable("Scanned Task points! Total %s types!", map.size()), true);

                for (var player : context.getSource().getLevel().players()) {
                  ServerPlayNetworking.send(player, new ScanAllTaskPointsPayload(GameFunctions.taskBlocks));
                }
                return 1;
              })))
              .then(Commands.literal("blackout").executes((context) -> {
                return executeBlackout(context, -1);
              }).then(Commands.literal("stop").executes((context) -> {
                return executeBlackout(context, 0);
              })))
              .then(Commands.literal("kill")
                  .then(Commands.argument("victim", EntityArgument.player())
                      .then(Commands.argument("death_reason", ResourceLocationArgument.id())
                          .suggests(DeathReasonSuggestions::suggestDeathReasons)
                          .executes((context) -> {
                            ServerPlayer victim = EntityArgument.getPlayer(context, "victim");
                            ResourceLocation deathReason = ResourceLocationArgument.getId(context,
                                "death_reason");
                            return executeKillPlayer(context, victim, null, deathReason, true);
                          })
                          .then(Commands.argument("killer", EntityArgument.player()).executes((context) -> {
                            ServerPlayer victim = EntityArgument.getPlayer(context, "victim");
                            ServerPlayer killer = EntityArgument.getPlayer(context, "killer");
                            ResourceLocation deathReason = ResourceLocationArgument.getId(context,
                                "death_reason");
                            return executeKillPlayer(context, victim, killer, deathReason, true);
                          })
                              .then(Commands.argument("spawn_body", BoolArgumentType.bool()).executes((context) -> {
                                ServerPlayer victim = EntityArgument.getPlayer(context, "victim");
                                boolean spawnBody = BoolArgumentType.getBool(context, "spawn_body");
                                ServerPlayer killer = EntityArgument.getPlayer(context, "killer");
                                ResourceLocation deathReason = ResourceLocationArgument.getId(context,
                                    "death_reason");
                                return executeKillPlayer(context, victim, killer, deathReason, spawnBody);
                              })))))));
        });
  }

  public static int executeKillPlayer(CommandContext<CommandSourceStack> context, ServerPlayer victim,
      @Nullable ServerPlayer killer, ResourceLocation deathReason, boolean spawnBody) {
    ResourceLocation deathReasonRL = deathReason;
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
    int color = ModColorArgument.getColor(context, "color");
    var roundComponent = GameRoundEndComponent.KEY.get(context.getSource().getLevel());
    roundComponent.CustomWinnerID = id;
    // roundComponent
    roundComponent.CustomWinnerSubtitle = null;
    roundComponent.CustomWinnerTitle = null;
    roundComponent.CustomWinnerColor = color;
    roundComponent.setRoundEndData(context.getSource().getLevel().players(), WinStatus.CUSTOM);

    roundComponent.sync();
    context.getSource()
        .sendSuccess(() -> Component.translatable("Stop the game with custom winner id [%s] (CUSTOM)", id), true);
    GameFunctions.stopGame(context.getSource().getLevel());
    return 1;
  }

  public static int executeWinWithIdAndTitle(CommandContext<CommandSourceStack> context) {
    Component title = ComponentArgument.getComponent(context, "title");
    Component subtitle = ComponentArgument.getComponent(context, "subtitle");
    String id = "custom";
    int color = ModColorArgument.getColor(context, "color");

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
    roundComponent.CustomWinnerColor = color;
    roundComponent.CustomWinnerSubtitle = subtitle;
    roundComponent.CustomWinnerTitle = title;
    roundComponent.setRoundEndData(context.getSource().getLevel().players(), WinStatus.CUSTOM_COMPONENT);

    roundComponent.sync();
    context.getSource().sendSuccess(
        () -> Component.translatable("Stop the game with custom winner id [%s] (CUSTOM_COMPONENT)", id), true);

    GameFunctions.stopGame(context.getSource().getLevel());
    return 1;
  }

  public static class DeathReasonSuggestions {
    private static final List<ResourceLocation> CUSTOM_DEATH_REASONS = Arrays.asList(
        Noellesroles.id("voodoo"),
        Noellesroles.id("shot_innocent"),
        Noellesroles.id("insane_killer_death"),
        Noellesroles.id("arrow"),
        Noellesroles.id("heart_attack"),
        Noellesroles.id("conspiracy_backfire"),
        Noellesroles.id("stalker_execution"),
        Noellesroles.id("bomb_death"),
        Noellesroles.id("puppeteer_puppet"),
        Noellesroles.id("recorder_mistake"),
        Noellesroles.id("gamble_self_kill"),
        Noellesroles.id("wayfarer_error"),
        Noellesroles.id("nianshou_firecrackers"),
        TMM.id("death_afk"),
        TMM.id("disconnected"),
        TMM.id("bat_hit"),
        TMM.id("fell_out_of_train"),
        TMM.id("generic"),
        TMM.id("grenade"),
        TMM.id("gun_shot"),
        TMM.id("knife_stab"),
        TMM.id("poison"),
        TMM.id("revolver_shot"),
        TMM.id("derringer_shot"),
        StupidExpress.id("broken_heart"),
        StupidExpress.id("failed_initiation"),
        StupidExpress.id("allergist"),
        StupidExpress.id("failed_ignite"),
        StupidExpress.id("ignited"));

    public static CompletableFuture<Suggestions> suggestDeathReasons(CommandContext<CommandSourceStack> context,
        SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      Set<String> suggestions = new HashSet<>();
      // 添加自定义 ID 到 Set

      CUSTOM_DEATH_REASONS.stream()
          .map(ResourceLocation::toString)
          .filter(id -> id.toLowerCase(Locale.ROOT).startsWith(remaining))
          .forEach(suggestions::add);
      if (suggestions.isEmpty()) {
        // 添加物品 ID 到 Set
        BuiltInRegistries.ITEM.keySet().stream()
            .map(ResourceLocation::toString)
            .filter(id -> id.toLowerCase(Locale.ROOT).startsWith(remaining))
            .forEach(suggestions::add);
      }
      // 最后批量建议
      suggestions.forEach((s) -> {
        var t = ResourceLocation.tryParse(s);
        if (t != null) {
          builder.suggest(s, GameReplayUtils.getItemDisplayName(t));
        }
      });

      return builder.buildFuture();
    }
  }
}
