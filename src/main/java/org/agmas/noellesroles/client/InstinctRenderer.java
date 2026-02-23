package org.agmas.noellesroles.client;

import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.noellesroles.component.AdmirerPlayerComponent;
import org.agmas.noellesroles.component.AwesomePlayerComponent;
import org.agmas.noellesroles.component.BetterVigilantePlayerComponent;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.agmas.noellesroles.component.MagicianPlayerComponent;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.MonitorPlayerComponent;
import org.agmas.noellesroles.component.PuppeteerPlayerComponent;
import org.agmas.noellesroles.component.RecorderPlayerComponent;
import org.agmas.noellesroles.component.WayfarerPlayerComponent;
import org.agmas.noellesroles.component.BanditPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import org.agmas.noellesroles.utils.RoleUtils;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.BartenderPlayerComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.event.OnGetInstinctHighlight;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.tag.TMMItemTags;
import dev.doctor4t.trainmurdermystery.util.TMMItemUtils;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import pro.fazeclan.river.stupid_express.StupidExpress;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import pro.fazeclan.river.stupid_express.role.arsonist.cca.DousedPlayerComponent;

public class InstinctRenderer {
    public static void registerInstinctEvents() {
        // 验尸官
        OnGetInstinctHighlight.EVENT.register((target, hasInstinct) -> {
            if (Minecraft.getInstance() == null)
                return -1;
            var self = Minecraft.getInstance().player;
            if (GameFunctions.isPlayerSpectatingOrCreative(self))
                return -1;
            if (self == null)
                return -1;
            if (TMMClient.gameComponent == null) {
                return -1;
            }
            if (!TMMClient.gameComponent.isRole(self, ModRoles.CORONER)) {
                return -1;
            }

            long time = self.level().getGameTime();
            if (time % 400 >= 100) {
                return -1;

            }

            if (target instanceof PlayerBodyEntity) {
                return (ModRoles.CORONER.color());
            }

            if (target instanceof Player targetPlayer) {
                InsaneKillerPlayerComponent component = InsaneKillerPlayerComponent.KEY.get(targetPlayer);
                if (component.isActive) {
                    return (ModRoles.CORONER.color());
                }
            }
            return -1;
        });
        // 傀儡师
        OnGetInstinctHighlight.EVENT.register((target, hasInstinct) -> {
            var client = Minecraft.getInstance();
            if (client == null || client.player == null)
                return -1;
            if (TMMClient.gameComponent == null) {
                return -1;
            }
            if (GameFunctions.isPlayerSpectatingOrCreative(client.player))
                return -1;
            if (target instanceof Player target_player) {
                PuppeteerPlayerComponent selfPuppeteerComp = ModComponents.PUPPETEER.get(client.player);
                if (selfPuppeteerComp.isControllingPuppet && TMMClient.isPlayerAliveAndInSurvival()) {
                    int entityOffset = target_player.getId() * 7;
                    return (getGradientColor(entityOffset));
                }
            }
            return -1;
        });
        // 初学者
        OnGetInstinctHighlight.EVENT.register((target, hasInstinct) -> {
            if (TMMClient.gameComponent == null) {
                return -1;
            }
            if (Minecraft.getInstance() == null)
                return -1;
            if (Minecraft.getInstance().player == null)
                return -1;
            if (GameFunctions.isPlayerSpectatingOrCreative(Minecraft.getInstance().player))
                return -1;
            Player player = Minecraft.getInstance().player;
            if (TMMItemUtils.hasItem(player, TMMItems.KNIFE) <= 0) {
                return -2;
            }
            if (target instanceof Player targettedPlayer) {
                if (TMMClient.gameComponent.isRole(targettedPlayer, SERoles.INITIATE)
                        && TMMClient.gameComponent.isRole(Minecraft.getInstance().player, SERoles.INITIATE)) {
                    return (SERoles.INITIATE.color());
                }
            }
            return -1;
        });
        // 纵火犯
        OnGetInstinctHighlight.EVENT.register((target, hasInstinct) -> {
            var player = Minecraft.getInstance().player;
            if (!(target instanceof Player targettedPlayer)) {
                return -1;
            }
            if (Minecraft.getInstance() == null)
                return -1;
            if (Minecraft.getInstance().player == null)
                return -1;
            if (GameFunctions.isPlayerSpectatingOrCreative(Minecraft.getInstance().player))
                return -1;
            if (TMMClient.gameComponent == null) {
                return -1;
            }
            if (!TMMClient.gameComponent.isRole(player, SERoles.ARSONIST)) {
                return -1;
            }
            if (TMMClient.isPlayerSpectatingOrCreative()) {
                return -1;
            }
            if (!TMMClient.isInstinctEnabled()) {
                return -1;
            }
            var douse = DousedPlayerComponent.KEY.get(targettedPlayer);
            if (douse.getDoused()) {
                return (SERoles.ARSONIST.color());
            } else {
                return (Color.GRAY.getRGB());
            }
        });
        // 失忆患者
        OnGetInstinctHighlight.EVENT.register((target, hasInstinct) -> {
            if (Minecraft.getInstance() == null)
                return -1;
            var self = Minecraft.getInstance().player;
            if (self == null)
                return -1;
            if (GameFunctions.isPlayerSpectatingOrCreative(self))
                return -1;
            if (TMMClient.gameComponent == null) {
                return -1;
            }
            if (!(target instanceof Player targetPlayer)) {
                return -1;
            }
            if (!TMMClient.gameComponent.isRole(self, ModRoles.AWESOME_BINGLUS)) {
                return -1;
            }
            if (TMMClient.isPlayerSpectatingOrCreative()) {
                return -1;
            }
            if (targetPlayer.distanceTo(self) <= 5) {
                var awpc = AwesomePlayerComponent.KEY.get(targetPlayer);
                int redDepth = (int) (255
                        * ((float) awpc.nearByDeathTime
                                / (float) AwesomePlayerComponent.nearByDeathTimeRecordTime));
                redDepth = Math.clamp(redDepth, 0, 255);
                return new Color(redDepth, 0, 0).getRGB();
            }
            return -1;
        });
        // 记者
        OnGetInstinctHighlight.EVENT.register((target, hasInstinct) -> {
            if (Minecraft.getInstance() == null)
                return -1;
            var self = Minecraft.getInstance().player;
            if (self == null)
                return -1;
            if (GameFunctions.isPlayerSpectatingOrCreative(self))
                return -1;
            if (TMMClient.gameComponent == null) {
                return -1;
            }
            if (!(target instanceof PlayerBodyEntity)) {
                return -1;
            }
            if (!TMMClient.gameComponent.isRole(self, SERoles.AMNESIAC)) {
                return -1;
            }
            if (TMMClient.isPlayerSpectatingOrCreative()) {
                return -1;
            }
            return SERoles.AMNESIAC.color();
        });

        // 通用逻辑
        OnGetInstinctHighlight.EVENT.register((target, hasInstinct) -> {
            if (Minecraft.getInstance() == null)
                return -1;
            var self = Minecraft.getInstance().player;
            if (self == null)
                return -1;
            if (TMMClient.gameComponent == null) {
                return -1;
            }
            WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(target.level());
            var self_role = TMMClient.gameComponent.getRole(self);
            if (worldModifierComponent != null) {
                if (worldModifierComponent.isModifier(self, SEModifiers.SPLIT_PERSONALITY)) {
                    if (self.isSpectator()) {
                        var splitComponent = SplitPersonalityComponent.KEY.get(self);
                        if (splitComponent != null && !splitComponent.isDeath()) {
                            return -2;
                        }
                    }
                }
            }
            if (target instanceof Player target_player) {
                // 不开直觉，默认有
                // 风精灵
                if (TMMClient.gameComponent.isRole(self, ModRoles.WIND_YAOSE)) {
                    return ModRoles.WIND_YAOSE.getColor();
                }

                // 红尘客
                if (TMMClient.gameComponent.isRole(self, ModRoles.WAYFARER)) {
                    if (GameFunctions.isPlayerAliveAndSurvival(target_player)) {
                        var wayC = WayfarerPlayerComponent.KEY.get(self);
                        if (wayC.phase == 1) {
                            if (wayC.killer != null) {
                                if (target_player.getUUID().equals(wayC.killer)) {
                                    return Color.RED.getRGB();
                                }
                            }
                        }
                        return -2;
                    }

                }
                var target_role = TMMClient.gameComponent.getRole(target_player);
                BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(target_player);
                PlayerPoisonComponent playerPoisonComponent = PlayerPoisonComponent.KEY.get(target_player);
                if (TMMClient.gameComponent.isRole(self, ModRoles.BETTER_VIGILANTE)) {
                    var betterC = BetterVigilantePlayerComponent.KEY.get(self);
                    if (betterC.lastStandActivated) {
                        return (Color.BLUE.getRGB());
                    }
                }
                if (TMMClient.gameComponent.isRole(self, ModRoles.CHEF)) {
                    // LoggerFactory.getLogger("renderer").info("glowTick {}",
                    // bartenderPlayerComponent.glowTicks);
                    if (bartenderPlayerComponent.glowTicks.getOrDefault(1, 0) > 0) {
                        return (Color.GREEN.getRGB());
                    }
                }
                if (TMMClient.gameComponent.isRole(self, ModRoles.BARTENDER)) {
                    // LoggerFactory.getLogger("renderer").info("glowTick {}",
                    // bartenderPlayerComponent.glowTicks);
                    if (bartenderPlayerComponent.getArmor() > 0 && playerPoisonComponent.poisonTicks > 0) {
                        return (new Color(186, 255, 65).getRGB());
                    }
                    if (bartenderPlayerComponent.getArmor() > 0) {
                        return (Color.BLUE.getRGB());
                    }
                    if (bartenderPlayerComponent.glowTicks.getOrDefault(0, 0) > 0) {
                        return (Color.GREEN.getRGB());
                    }

                }
                if ((TMMClient.gameComponent.isRole(self, ModRoles.BARTENDER)
                        || TMMClient.gameComponent.isRole(self, ModRoles.POISONER))
                        && playerPoisonComponent.poisonTicks > 0) {
                    return (Color.RED.getRGB());
                }

                if (TMMClient.gameComponent.isRole(self, ModRoles.EXECUTIONER)) {
                    ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY
                            .get(self);
                    if (executionerPlayerComponent != null && executionerPlayerComponent.target != null) {
                        if (executionerPlayerComponent.target.equals(target.getUUID())) {
                            return (Color.orange.getRGB());
                        }
                    }
                }
                if (TMMClient.gameComponent.isRole(self, ModRoles.MANIPULATOR)) {
                    ManipulatorPlayerComponent manipulatorPlayerComponent = (ManipulatorPlayerComponent) ManipulatorPlayerComponent.KEY
                            .get(self);
                    if (manipulatorPlayerComponent != null && manipulatorPlayerComponent.target != null) {
                        if (manipulatorPlayerComponent.target.equals(target.getUUID())) {
                            return (Color.orange.getRGB());
                        }
                    }
                }
                if (TMMClient.gameComponent.isRole(self, ModRoles.ADMIRER)) {
                    AdmirerPlayerComponent admirerPlayerComponent = (AdmirerPlayerComponent) AdmirerPlayerComponent.KEY
                            .get(self);
                    if (admirerPlayerComponent != null && admirerPlayerComponent.getBoundTarget() != null) {
                        if (admirerPlayerComponent.getBoundTarget().getUUID().equals(target.getUUID())) {
                            // LoggerFactory.getLogger("Instinct").info("PINK");
                            return (Color.PINK.getRGB());
                        }
                    }
                }
                if (TMMClient.gameComponent.isRole(self, ModRoles.MONITOR)) {
                    MonitorPlayerComponent monitorComponent = MonitorPlayerComponent.KEY
                            .get(self);
                    if (monitorComponent != null && monitorComponent.getMarkedTarget() != null) {
                        if (monitorComponent.getMarkedTarget().equals(target.getUUID())) {
                            return (Color.CYAN.getRGB());
                        }
                    }
                }
                // 需要开启直觉
                if (!hasInstinct)
                    return -1;
                if (GameFunctions.isPlayerSpectatingOrCreative(self))
                    return -1; // 旁观默认高亮
                // 直觉看不到旁观
                if ((target_player).isSpectator())
                    return -2;
                // 傀儡师
                PuppeteerPlayerComponent selfPuppeteerComp = ModComponents.PUPPETEER.get(self);
                if (selfPuppeteerComp.isPuppeteerMarked && TMMClient.isPlayerAliveAndInSurvival()
                        && selfPuppeteerComp.phase >= 1) {
                    return -1;
                }
                // 小透明：杀手无法看到高亮（所有，包括爱慕）
                if (TMMClient.gameComponent.isRole(target_player, ModRoles.GHOST) && isKillerTeam(self_role)
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    return -2;
                }
                // 记录员
                if (TMMClient.gameComponent.isRole(self, ModRoles.RECORDER)) {
                    if (target instanceof Player targetPlayer) {
                        if (targetPlayer == self)
                            return -2;

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
                // 爱慕
                if (TMMClient.gameComponent.isRole(self, ModRoles.ADMIRER) && TMMClient.isPlayerAliveAndInSurvival()) {
                    return (Color.PINK.getRGB());
                }
                // 小丑&LOOSE END
                if ((TMMClient.gameComponent.isRole(self, ModRoles.JESTER)
                        || TMMClient.gameComponent.isRole(self, TMMRoles.LOOSE_END))
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    if (TMMClient.gameComponent.isRole(target_player, ModRoles.GHOST)) {
                        return -2;
                    }
                    return (Color.PINK.getRGB());
                }
                // 柜子区
                if (TMMClient.gameComponent.isRole(self, ModRoles.EXECUTIONER)
                        && TMMClient.isPlayerAliveAndInSurvival()) {
                    return (ModRoles.EXECUTIONER.color());
                }

                // 杀手直觉
                if (isKillerTeam(self_role) && TMMClient.isPlayerAliveAndInSurvival()) {
                    // 强盗直觉：只能透视半径10格内的玩家，透视杀手队友无距离限制
                    if (TMMClient.gameComponent.isRole(self, ModRoles.BANDIT)) {
                        // 检查目标是否是杀手队友
                        if (target_role != null && target_role.canUseKiller()) {
                            // 杀手队友无距离限制
                            return getRoleColor(target_role);
                        } else {
                            // 普通玩家只能透视10格内
                            if (target_player.distanceTo(self) <= 10) {
                                return getRoleColor(target_role);
                            } else {
                                return -1;
                            }
                        }
                    }

                    // 魔术师：杀手看魔术师时显示红色边框（像看其他杀手一样）
                    if (TMMClient.gameComponent.isRole(target_player, ModRoles.MAGICIAN)) {
                        target_role = RoleUtils
                                .getRole(MagicianPlayerComponent.KEY.get(target_player).getDisguiseRoleId());
                    }

                    if (RoleUtils.compareRole(target_role, ModRoles.PUPPETEER)) {
                        int entityOffset = target_player.getId() * 7;
                        return (getGradientColor(entityOffset + 10));
                    }
                    if (TMMClient.gameComponent.isRole(self, ModRoles.COMMANDER)) {
                        if (isKillerTeam(target_role)) {
                            return getRoleColor(target_role);
                        }
                        if (target_player.distanceTo(self) <= 5) {
                            var role = TMMClient.gameComponent.getRole(target_player);
                            if (role!=null && role.isVigilanteTeam()) {
                                return new Color(63, 72, 204).getRGB();
                            }
                        }
                    }
                    if (RoleUtils.compareRole(target_role, ModRoles.VULTURE)) {
                        return (ModRoles.VULTURE.color());
                    }
                    if (RoleUtils.compareRole(target_role, ModRoles.ADMIRER)) {
                        return (ModRoles.ADMIRER.color());
                    }
                    if (RoleUtils.compareRole(target_role, ModRoles.EXECUTIONER)) {
                        return (ModRoles.EXECUTIONER.color());
                    }
                    if (RoleUtils.compareRole(target_role, ModRoles.JESTER)) {
                        return (Color.PINK.getRGB());
                    }
                    if (RoleUtils.compareRole(target_role, ModRoles.SLIPPERY_GHOST)) {
                        return -2;
                    }
                    if (RoleUtils.compareRole(target_role, SERoles.AMNESIAC)) {
                        if (StupidExpress.CONFIG.rolesSection.amnesiacSection.amnesiacGlowsDifferently) {
                            return SERoles.AMNESIAC.color();
                        }
                    }

                    // 默认fallback
                    if (target_role == null)
                        return Color.WHITE.getRGB();
                    if (target_role.canUseKiller()) {
                        return Color.RED.getRGB();
                    } else if (target_role.isNeutralForKiller()) {
                        return Color.ORANGE.getRGB();
                    } else {
                        if (TMMClient.gameComponent.isRole(target_player, ModRoles.GAMBLER)) {
                            return -2;
                        }
                        return TMMRoles.CIVILIAN.color();
                    }
                }
            }

            return -1;
        });
    }

    private static int getRoleColor(Role target_role) {
        if (target_role == null)
            return TMMRoles.CIVILIAN.color();
        return target_role.color();
    }

    private static boolean isKillerTeam(Role role) {
        if (role == null)
            return false;
        if (role.canUseKiller())
            return true;
        if (role.canUseInstinct() && role.isNeutralForKiller())
            return true;
        return false;
    }

    private static final int[] GRADIENT_COLORS = {
            new Color(255, 0, 0).getRGB(), // 红色
            new Color(255, 85, 0).getRGB(), // 橙红
            new Color(255, 170, 0).getRGB(), // 橙色
            new Color(255, 255, 0).getRGB(), // 黄色
            new Color(255, 170, 0).getRGB(), // 橙色
            new Color(255, 85, 0).getRGB(), // 橙红
    };

    // 渐变周期（tick）
    private static final int GRADIENT_CYCLE = 60; // 3秒一个周期

    /**
     * 获取渐变颜色
     * 
     * @param tickOffset 每个实体的偏移量，使不同实体颜色略有不同
     * @return 当前渐变颜色
     */
    public static int getGradientColor(int tickOffset) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null)
            return GRADIENT_COLORS[0];

        long worldTime = client.level.getGameTime();
        int cyclePosition = (int) ((worldTime + tickOffset) % GRADIENT_CYCLE);

        // 计算在颜色数组中的位置
        float progress = (float) cyclePosition / GRADIENT_CYCLE * GRADIENT_COLORS.length;
        int colorIndex = (int) progress;
        float blend = progress - colorIndex;

        // 获取当前颜色和下一个颜色
        int currentColor = GRADIENT_COLORS[colorIndex % GRADIENT_COLORS.length];
        int nextColor = GRADIENT_COLORS[(colorIndex + 1) % GRADIENT_COLORS.length];

        // 混合两个颜色
        return blendColors(currentColor, nextColor, blend);
    }

    /**
     * 混合两个颜色
     */
    public static int blendColors(int color1, int color2, float blend) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * blend);
        int g = (int) (g1 + (g2 - g1) * blend);
        int b = (int) (b1 + (b2 - b1) * blend);

        return (r << 16) | (g << 8) | b;
    }
}