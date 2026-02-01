package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import org.agmas.noellesroles.component.*;
import org.agmas.noellesroles.entity.CalamityMarkEntity;
import org.agmas.noellesroles.packet.PlayerResetS2CPacket;
import org.agmas.noellesroles.roles.manipulator.InControlCCA;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * 玩家重置 Mixin
 * 
 * 在游戏结束时（GameFunctions.resetPlayer 被调用）清除所有自定义组件的状态
 * 这确保了下一局游戏开始时玩家不会有残留的状态
 */
@Mixin(GameFunctions.class)
public abstract class PlayerResetMixin {

    /**
     * 在 resetPlayer 方法尾部注入，清除所有自定义组件状态
     */
    @Inject(method = "resetPlayer", at = @At("TAIL"))
    private static void clearAllComponentsOnReset(ServerPlayer player, CallbackInfo ci) {
        // 清除跟踪者组件状态
        clearAllComponents(player);
        ServerPlayNetworking.send(player, new PlayerResetS2CPacket());
    }

    /**
     * 在 initializeGame 方法头部注入，清除自定义笔记
     */
    @Inject(method = "initializeGame", at = @At("HEAD"))
    private static void clearAllComponentsOnReset(ServerLevel serverWorld, CallbackInfo ci) {
        // 清除客户端自定义笔记状态

        serverWorld.players().forEach((pl) -> {
            clearAllComponents(pl);
            ServerPlayNetworking.send(pl, new PlayerResetS2CPacket());
        });
    }

    private static void clearAllComponents(ServerPlayer player) {
        StalkerPlayerComponent stalkerComp = ModComponents.STALKER.get(player);
        stalkerComp.clearAll();
        InControlCCA inControlCCA =InControlCCA.KEY.get(player);
        inControlCCA.reset();

        ManipulatorPlayerComponent manipulatorComp = ManipulatorPlayerComponent.KEY.get(player);
        manipulatorComp.reset();
        // 清除惩罚组件状态
        DeathPenaltyComponent deathPenalty = ModComponents.DEATH_PENALTY.get(player);
        deathPenalty.clearAll();

        // 清除慕恋者组件状态
        AdmirerPlayerComponent admirerComp = ModComponents.ADMIRER.get(player);
        admirerComp.clearAll();

        // 清除其他自定义组件状态
        AbilityPlayerComponent abilityComp = ModComponents.ABILITY.get(player);
        abilityComp.reset();

        AvengerPlayerComponent avengerComp = ModComponents.AVENGER.get(player);
        avengerComp.reset();

        ConspiratorPlayerComponent conspiratorComp = ModComponents.CONSPIRATOR.get(player);
        conspiratorComp.reset();
        InsaneKillerPlayerComponent insaneKillerComp = ModComponents.INSANE_KILLER.get(player);
        insaneKillerComp.reset();

        SlipperyGhostPlayerComponent slipperyGhostComp = ModComponents.SLIPPERY_GHOST.get(player);
        slipperyGhostComp.reset();

        BroadcasterPlayerComponent broadcasterComp = ModComponents.BROADCASTER.get(player);
        broadcasterComp.reset();

        PostmanPlayerComponent postmanComp = ModComponents.POSTMAN.get(player);
        postmanComp.reset();

        DetectivePlayerComponent detectiveComp = ModComponents.DETECTIVE.get(player);
        detectiveComp.reset();

        BoxerPlayerComponent boxerComp = ModComponents.BOXER.get(player);
        boxerComp.reset();

        AthletePlayerComponent athleteComp = ModComponents.ATHLETE.get(player);
        athleteComp.reset();

        // 清除设陷者组件状态
        TrapperPlayerComponent trapperComp = ModComponents.TRAPPER.get(player);
        trapperComp.clearAll();

        // 清除傀儡师组件状态
        PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(player);
        puppeteerComp.clearAll();

        // 清除记录员组件状态
        RecorderPlayerComponent recorderComp = ModComponents.RECORDER.get(player);
        recorderComp.reset();


        // 清除该玩家放置的所有灾厄印记实体
        clearCalamityMarks(player);
    }

    /**
     * 清除指定玩家放置的所有灾厄印记实体
     */
    private static void clearCalamityMarks(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        if (world == null)
            return;

        // 收集需要移除的实体（避免在遍历时修改集合）
        List<Entity> toRemove = new ArrayList<>();

        for (Entity entity : world.getAllEntities()) {
            if (entity instanceof CalamityMarkEntity mark) {
                // 检查是否是该玩家放置的
                if (mark.getOwnerUuid().isPresent() &&
                        mark.getOwnerUuid().get().equals(player.getUUID())) {
                    toRemove.add(mark);
                }
            }
        }

        // 移除所有标记的实体
        for (Entity entity : toRemove) {
            entity.discard();
        }
    }
}