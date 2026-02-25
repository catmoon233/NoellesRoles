package org.agmas.noellesroles.game;

import dev.doctor4t.trainmurdermystery.api.GameMode;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameRoundEndComponent;
import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.TrainWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

import org.agmas.harpymodloader.events.GameInitializeEvent;
import org.agmas.noellesroles.entity.WheelchairEntity;
import org.agmas.noellesroles.init.ModEntities;

import java.util.ArrayList;
import java.util.List;

public class ChairWheelRaceGame extends GameMode {
    public static final ResourceLocation identifier = ResourceLocation.tryBuild("noellesroles", "chair_wheel_race");
    public static final int defaultStartTime = 5;
    public static final int minPlayerCount = 2;
    public ChairWheelRaceGame() {
        super(identifier, defaultStartTime, minPlayerCount);
    }
    private static void executeFunction(CommandSourceStack source, String function) {
        try {
            source.getServer().getCommands().performPrefixedCommand(source, "function " + function);
        } catch (Exception e) {
            Log.warn(LogCategory.GENERAL, "Failed to execute function: " + function + ", error: " + e.getMessage());
        }
    }
    public List<ServerPlayer> isWin = new ArrayList<>() ;

    @Override
    public void tickServerGameLoop(ServerLevel serverLevel, GameWorldComponent gameWorldComponent) {
        serverLevel.players().forEach(player -> {
            if (GameFunctions.isPlayerAliveAndSurvival(player)){
                if (player.getVehicle() instanceof WheelchairEntity wheelchairEntity){
                    if (serverLevel.getBlockState(player.getOnPos().above(-1)).getBlock() == Blocks.DIAMOND_BLOCK){
                        isWin.add(player);
                        player.startRiding(wheelchairEntity);
                        wheelchairEntity.remove(Entity.RemovalReason.DISCARDED);
                        player.setGameMode(GameType.SPECTATOR);
                        serverLevel.getServer().getCommands().performPrefixedCommand(serverLevel.getServer().createCommandSourceStack(), "broadcast @a \"\\u00a76玩家 " + player.getScoreboardName() + " 到达了终点！ 排名"+( isWin.indexOf(player)+1)+"\"");
                    }   
                }

            }
        });
        if (!((GameTimeComponent)GameTimeComponent.KEY.get(serverLevel)).hasTime() || isWin.size()>=serverLevel.getPlayers(GameFunctions::isPlayerAliveAndSurvival).size()) {
            endGame(serverLevel, gameWorldComponent);

        }
    }
    public void endGame(ServerLevel serverLevel, GameWorldComponent gameWorldComponent){
        var roundComponent = GameRoundEndComponent.KEY.get(serverLevel);
        roundComponent.CustomWinnerID = "chiar_wheel_race";
        // roundComponent
        var player = isWin.isEmpty() ? null : isWin.getFirst();
        roundComponent.CustomWinnerSubtitle = Component.translatable("game.win.chair_wheel_race.subtitle");
        roundComponent.CustomWinnerTitle = Component.translatable("game.win.chair_wheel_race",player == null ? "滚木" : player.getScoreboardName());
        roundComponent.setWinStatus(GameFunctions.WinStatus.CUSTOM_COMPONENT);
        roundComponent.sync();

        GameFunctions.stopGame(serverLevel);
    }
    @Override
    public void initializeGame(ServerLevel serverLevel, GameWorldComponent gameWorldComponent, List<ServerPlayer> list) {
        GameInitializeEvent.EVENT.invoker().initializeGame(serverLevel, gameWorldComponent, list);
        ((TrainWorldComponent)TrainWorldComponent.KEY.get(serverLevel)).setTimeOfDay(TrainWorldComponent.TimeOfDay.DAY);
        isWin.clear();
        executeFunction(serverLevel.getServer().createCommandSourceStack(), "harpymodloader:chair_wheel_race/init");
        for(ServerPlayer player : list) {
            gameWorldComponent.addRole(player, TMMRoles.DISCOVERY_CIVILIAN);
            var chair = new WheelchairEntity(ModEntities.WHEELCHAIR, serverLevel);
            chair.setPos(player.getX(), player.getY(), player.getZ());
            serverLevel.addFreshEntity(chair);
            player.startRiding(chair,true);
        }
    }
}
