package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import org.agmas.noellesroles.component.AdmirerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import org.agmas.noellesroles.component.MonitorPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(TMMClient.class)
public abstract class InstinctMixin {

    @Shadow
    public static KeyMapping instinctKeybind;

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void b(CallbackInfoReturnable<Boolean> cir) {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        // 检查玩家是否正在被操纵师控制 - 如果是，禁止使用杀手本能
        if (noellesroles$isPlayerBeingControlled(player)) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }
        var deathPenalty = org.agmas.noellesroles.component.ModComponents.DEATH_PENALTY.get(player);
        // 检查死亡惩罚
        if (deathPenalty != null)
            if (deathPenalty.hasPenalty()) {
                cir.setReturnValue(false);
                cir.cancel();
                return;
            }

        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.level());
        if (gameWorldComponent.isRole(player, ModRoles.JESTER)
                || gameWorldComponent.isRole(player, TMMRoles.LOOSE_END)) {
            if (instinctKeybind.isDown()) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    /**
     * 检查玩家是否正在被操纵师控制
     */
    @Unique
    private static boolean noellesroles$isPlayerBeingControlled(Player player) {
        if (player == null)
            return false;

        // 遍历所有玩家，检查是否有操纵师正在控制当前玩家
        for (Player otherPlayer : player.level().players()) {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(otherPlayer.level());
            if (gameWorldComponent.isRole(otherPlayer, ModRoles.MANIPULATOR)) {
                ManipulatorPlayerComponent manipulatorComponent = ManipulatorPlayerComponent.KEY.get(otherPlayer);
                if (manipulatorComponent.isControlling &&
                        manipulatorComponent.target != null &&
                        manipulatorComponent.target.equals(player.getUUID())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                .get(Minecraft.getInstance().player.level());
        if (target instanceof Player) {
            if (!((Player) target).isSpectator()) {
                dev.doctor4t.trainmurdermystery.cca.BartenderPlayerComponent bartenderPlayerComponent = dev.doctor4t.trainmurdermystery.cca.BartenderPlayerComponent.KEY.get((Player) target);
                PlayerPoisonComponent playerPoisonComponent = PlayerPoisonComponent.KEY.get((Player) target);
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BARTENDER)
                        && bartenderPlayerComponent.glowTicks > 0) {
                    cir.setReturnValue(Color.GREEN.getRGB());
                }
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BARTENDER)
                        && bartenderPlayerComponent.getArmor() > 0) {
                    cir.setReturnValue(Color.BLUE.getRGB());
                    cir.cancel();
                }
                if ((gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BARTENDER)
                        || gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.POISONER))
                        && playerPoisonComponent.poisonTicks > 0) {
                    cir.setReturnValue(Color.RED.getRGB());
                }
            }
        }
        if (target instanceof Player) {
            if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.EXECUTIONER)) {
                ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY
                        .get((Player) Minecraft.getInstance().player);
                if (executionerPlayerComponent != null && executionerPlayerComponent.target != null) {
                    if (executionerPlayerComponent.target.equals(target.getUUID())) {
                        cir.setReturnValue(Color.orange.getRGB());
                        cir.cancel();
                    }
                }
            }
            if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.MANIPULATOR)) {
                ManipulatorPlayerComponent manipulatorPlayerComponent = (ManipulatorPlayerComponent) ManipulatorPlayerComponent.KEY
                        .get((Player) Minecraft.getInstance().player);
                if (manipulatorPlayerComponent != null && manipulatorPlayerComponent.target != null) {
                    if (manipulatorPlayerComponent.target.equals(target.getUUID())) {
                        cir.setReturnValue(Color.orange.getRGB());
                        cir.cancel();
                    }
                }
            }
            if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.ADMIRER)) {
                AdmirerPlayerComponent admirerPlayerComponent = (AdmirerPlayerComponent) AdmirerPlayerComponent.KEY
                        .get((Player) Minecraft.getInstance().player);
                if (admirerPlayerComponent != null && admirerPlayerComponent.getBoundTarget() != null) {
                    if (admirerPlayerComponent.getBoundTarget().getUUID().equals(target.getUUID())) {
                        cir.setReturnValue(Color.PINK.getRGB());
                        cir.cancel();
                    }
                }
            }
            if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.MONITOR)) {
                MonitorPlayerComponent monitorComponent = MonitorPlayerComponent.KEY
                        .get((Player) Minecraft.getInstance().player);
                if (monitorComponent != null && monitorComponent.getMarkedTarget() != null) {
                    if (monitorComponent.getMarkedTarget().equals(target.getUUID())) {
                        cir.setReturnValue(Color.CYAN.getRGB());
                        cir.cancel();
                    }
                }
            }
            if (!((Player) target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((Player) target, ModRoles.VULTURE) && TMMClient.isKiller()
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.VULTURE.color());
                    cir.cancel();
                }
            }
            if (!((Player) target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((Player) target, ModRoles.EXECUTIONER) && TMMClient.isKiller()
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.EXECUTIONER.color());
                    cir.cancel();
                }
            }
            if (!((Player) target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.getRole(target.getUUID()) != null
                        && gameWorldComponent.getRole(target.getUUID()) != ModRoles.GHOST) {
                    if (gameWorldComponent.isRole((Player) target, ModRoles.JESTER) && TMMClient.isKiller()
                            && TMMClient.isPlayerAliveAndInSurvival()) {
                        cir.setReturnValue(Color.PINK.getRGB());
                        cir.cancel();
                    }
                }
            }
            // 小透明：杀手无法看到高亮
            if (!((Player) target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((Player) target, ModRoles.GHOST) && TMMClient.isKiller()
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(-1);
                    cir.cancel();
                }
            }
            if (!((Player) target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if ((gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.JESTER)
                        || gameWorldComponent.isRole(Minecraft.getInstance().player, TMMRoles.LOOSE_END))
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(Color.PINK.getRGB());
                    cir.cancel();
                }
            }
            if (!((Player) target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.EXECUTIONER)
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.EXECUTIONER.color());
                    cir.cancel();
                }
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.POSTMAN)
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.POSTMAN.color());
                    cir.cancel();
                }

            }
        }
    }
}
