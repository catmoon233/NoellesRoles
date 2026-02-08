package org.agmas.noellesroles.client;

import org.agmas.noellesroles.component.AdmirerPlayerComponent;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.MonitorPlayerComponent;
import org.agmas.noellesroles.component.RecorderPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.BartenderPlayerComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.event.OnGetInstinctHighlight;
import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class InstinctRenderer {
    public static void registerInstinctEvents() {
        OnGetInstinctHighlight.EVENT.register((target, hasInstinct) -> {
            if (Minecraft.getInstance() == null)
                return -1;
            var self = Minecraft.getInstance().player;
            if (self == null)
                return -1;

            GameWorldComponent gameWorldComponent = TMMClient.gameComponent;
            if (gameWorldComponent == null)
                return -1;
            if (target instanceof Player target_player) {
                // 不开直觉，默认有
                BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(target_player);
                PlayerPoisonComponent playerPoisonComponent = PlayerPoisonComponent.KEY.get(target_player);
                if (gameWorldComponent.isRole(self, ModRoles.BARTENDER)) {
                    // LoggerFactory.getLogger("renderer").info("glowTick {}",
                    // bartenderPlayerComponent.glowTicks);
                    if (bartenderPlayerComponent.getArmor() > 0 && playerPoisonComponent.poisonTicks > 0) {
                        return (new Color(186, 255, 65).getRGB());
                    }
                    if (bartenderPlayerComponent.getArmor() > 0) {
                        return (Color.BLUE.getRGB());
                    }
                    if (bartenderPlayerComponent.glowTicks > 0) {
                        return (Color.GREEN.getRGB());
                    }

                }
                if ((gameWorldComponent.isRole(self, ModRoles.BARTENDER)
                        || gameWorldComponent.isRole(self, ModRoles.POISONER))
                        && playerPoisonComponent.poisonTicks > 0) {
                    return (Color.RED.getRGB());
                }

                if (gameWorldComponent.isRole(self, ModRoles.EXECUTIONER)) {
                    ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY
                            .get(self);
                    if (executionerPlayerComponent != null && executionerPlayerComponent.target != null) {
                        if (executionerPlayerComponent.target.equals(target.getUUID())) {
                            return (Color.orange.getRGB());
                        }
                    }
                }
                if (gameWorldComponent.isRole(self, ModRoles.MANIPULATOR)) {
                    ManipulatorPlayerComponent manipulatorPlayerComponent = (ManipulatorPlayerComponent) ManipulatorPlayerComponent.KEY
                            .get(self);
                    if (manipulatorPlayerComponent != null && manipulatorPlayerComponent.target != null) {
                        if (manipulatorPlayerComponent.target.equals(target.getUUID())) {
                            return (Color.orange.getRGB());
                        }
                    }
                }
                if (gameWorldComponent.isRole(self, ModRoles.ADMIRER)) {
                    AdmirerPlayerComponent admirerPlayerComponent = (AdmirerPlayerComponent) AdmirerPlayerComponent.KEY
                            .get(self);
                    if (admirerPlayerComponent != null && admirerPlayerComponent.getBoundTarget() != null) {
                        if (admirerPlayerComponent.getBoundTarget().getUUID().equals(target.getUUID())) {
                            // LoggerFactory.getLogger("Instinct").info("PINK");
                            return (Color.PINK.getRGB());
                        }
                    }
                }
                if (gameWorldComponent.isRole(self, ModRoles.MONITOR)) {
                    MonitorPlayerComponent monitorComponent = MonitorPlayerComponent.KEY
                            .get(self);
                    if (monitorComponent != null && monitorComponent.getMarkedTarget() != null) {
                        if (monitorComponent.getMarkedTarget().equals(target.getUUID())) {
                            return (Color.CYAN.getRGB());
                        }
                    }
                }

                if (!hasInstinct)
                    return -1;

                // 直觉看不到旁观

                if ((target_player).isSpectator())
                    return -1;
                // 需要开启直觉

                if (gameWorldComponent.isRole(self, ModRoles.RECORDER)) {
                    if (target instanceof Player targetPlayer) {
                        if (targetPlayer == self)
                            return -1;

                        RecorderPlayerComponent recorder = ModComponents.RECORDER.get(self);
                        if (recorder.getGuesses().containsKey(targetPlayer.getUUID())) {
                            // 已记录（猜测过）：亮黄色
                            return (0xFFFF55);
                        } else {
                            // 未记录：暗蓝色
                            return (0x0000AA);
                        }
                    }
                }

                if (gameWorldComponent.isRole(target_player, ModRoles.VULTURE) && TMMClient.isKiller()
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    return (ModRoles.VULTURE.color());
                }

                if (gameWorldComponent.isRole(target_player, ModRoles.EXECUTIONER) && TMMClient.isKiller()
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    return (ModRoles.EXECUTIONER.color());
                }

                if (gameWorldComponent.getRole(target.getUUID()) != null
                        && gameWorldComponent.getRole(target.getUUID()) != ModRoles.GHOST) {
                    if (gameWorldComponent.isRole(target_player, ModRoles.JESTER) && TMMClient.isKiller()
                            && TMMClient.isPlayerAliveAndInSurvival()) {
                        return (Color.PINK.getRGB());
                    }
                }

                // 小透明：杀手无法看到高亮

                if (gameWorldComponent.isRole(target_player, ModRoles.GHOST) && TMMClient.isKiller()
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    return (-1);
                }

                if ((gameWorldComponent.isRole(self, ModRoles.JESTER)
                        || gameWorldComponent.isRole(self, TMMRoles.LOOSE_END))
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    return (Color.PINK.getRGB());
                }

                if (gameWorldComponent.isRole(self, ModRoles.EXECUTIONER)
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    return (ModRoles.EXECUTIONER.color());
                }
                if (gameWorldComponent.isRole(self, ModRoles.POSTMAN)
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    return (ModRoles.POSTMAN.color());
                }

            }

            return -1;
        });
    }
}