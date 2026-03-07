package org.agmas.noellesroles.effects;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.agmas.noellesroles.commands.BroadcastCommand;
import org.agmas.noellesroles.init.ModEffects;
import org.agmas.noellesroles.packet.CanMoveInTimeStopS2CPacket;
import org.agmas.noellesroles.role.ModRoles;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TimeStopEffect extends MobEffect {
    public static List<UUID> canMovePlayers = new ArrayList<>();
    public static Map<UUID, Vec3> clientPositions = new java.util.HashMap<>();
    public static int freezeTime = 0;
    public static int freezeStatedTime = 0;
    public TimeStopEffect() {
        super(MobEffectCategory.NEUTRAL, Color.white.getRGB());
    }

    @Override
    public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return true;
    }


    public static void triggerStart(ServerPlayer serverPlayer, int time){
        canMovePlayers.clear();
        canMovePlayers.add(serverPlayer.getUUID());
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(serverPlayer.level());
        var broadcastMessage = Component
                .translatable("message.noellesroles.time_stop.the_world")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);


        serverPlayer.serverLevel().players().forEach(
                player -> {
                    BroadcastCommand.BroadcastMessage(player, broadcastMessage);
                    player.addEffect(new MobEffectInstance( (ModEffects.TIME_STOP),time,0,false,false,false));

                    if (!GameFunctions.isPlayerAliveAndSurvival(player)){
                        canMovePlayers.add(player.getUUID());
                    }else {

                        if (gameWorldComponent.isRole(player, ModRoles.CLOCKMAKER)){
                            canMovePlayers.add(player.getUUID());
                        }
                    }
                }
        );
        serverPlayer.serverLevel().players().forEach(
                player -> {
                    ServerPlayNetworking.send(player,new CanMoveInTimeStopS2CPacket(canMovePlayers ));
                }
        );
        MinecraftServer server = serverPlayer.getServer();
        ServerTickRateManager serverTickRateManager = server.tickRateManager();

        freezeTime= time;
        serverTickRateManager.setFrozen( true);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int i) {
        return super.applyEffectTick(livingEntity, i);
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributeMap) {
        super.removeAttributeModifiers(attributeMap);
        canMovePlayers.clear();
        clientPositions.clear();

    }

    @Override
    public void onEffectStarted(LivingEntity livingEntity, int i) {
        super.onEffectStarted(livingEntity, i);

    }

}
