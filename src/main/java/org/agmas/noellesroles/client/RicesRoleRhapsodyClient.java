package org.agmas.noellesroles.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.ModEntities;
import org.agmas.noellesroles.client.renderer.CalamityMarkEntityRenderer;
import org.agmas.noellesroles.client.renderer.LockEntityRender;
import org.agmas.noellesroles.client.renderer.ManipulatorBodyEntityRenderer;
import org.agmas.noellesroles.client.renderer.PuppeteerBodyEntityRenderer;
import org.agmas.noellesroles.client.screen.*;

import org.agmas.noellesroles.component.*;
import org.agmas.noellesroles.item.ConspiracyPageItem;
import org.agmas.noellesroles.item.WrittenNoteItem;
import org.agmas.noellesroles.packet.*;
import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.screen.ModScreenHandlers;
import org.agmas.noellesroles.utils.RoleUtils;

import static org.agmas.noellesroles.client.NoellesrolesClient.abilityBind;

/**
 * Rice's Role Rhapsody - 客户端初始化
 * 
 * 负责：
 * 1. 注册按键绑定
 * 2. 注册客户端事件
 * 3. 注册渲染器
 * 4. 注册物品提示
 */
public class RicesRoleRhapsodyClient implements ClientModInitializer {

    // ==================== 按键绑定 ====================
    // 技能使用按键（默认 G 键）

    // 跟踪者窥视状态
    @SuppressWarnings("unused")
    private static boolean stalkerGazingLastTick = false;
    // 跟踪者蓄力状态
    private static boolean stalkerChargingLastTick = false;
    // 慕恋者窥视状态
    private static boolean admirerGazingLastTick = false;

    // ==================== 客户端状态 ====================
    // 当前选中的目标玩家（用于需要选择目标的技能）
    public static Player targetPlayer;
    // 管理员自由视角开关
    public static boolean freeCamEnabled = false;

    @Override
    public void onInitializeClient() {

        // 1. 注册按键绑定

        // 2. 注册客户端事件
        registerClientEvents();

        // 注册客户端指令
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("nr_free_cam")
                    .executes(context -> {
                        if (context.getSource().getPlayer().hasPermissions(2)) {
                            freeCamEnabled = !freeCamEnabled;
                            context.getSource().sendFeedback(
                                    Component.literal("自由视角: " + (freeCamEnabled ? "开启" : "关闭")));
                            return 1;
                        }
                        return 0;
                    }));
        });
    }

    /**
     * 注册按键绑定
     */

    /**
     * 注册客户端事件
     */
    public static void registerClientEvents() {
        // 每 tick 检查按键状态
        // ClientTickEvents.END_CLIENT_TICK.register(client -> {
        // if (client.player == null)
        // return;
        //
        // // 检查技能按键是否被按下
        // //while (abilityBind.wasPressed()) {
        // onAbilityKeyPressed(client);
        // //}
        //
        // // 跟踪者持续按键检测（窥视和蓄力）
        // handleStalkerContinuousInput(client);
        //
        // // 慕恋者持续按键检测（窥视）
        // handleAdmirerContinuousInput(client);
        // });

        // 检查书页物品使用 - 通过检测物品使用来打开GUI
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null)
                return;

            // 管理员脱离医生控制逻辑
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player)) {
                // 检查是否有医生
                boolean hasDoctor = false;
                GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.level);
                for (Player p : client.level.players()) {
                    if (gameWorld.isRole(p, ModRoles.DOCTOR)) {
                        hasDoctor = true;
                        break;
                    }
                }

                if (hasDoctor && client.player.hasPermissions(2)) {
                    if (!freeCamEnabled) {
                        client.player.displayClientMessage(
                                Component.translatable("message.noellesroles.admin.free_cam_hint"), true);
                    } else {
                        // 强制脱离控制
                        if (client.getCameraEntity() != client.player) {
                            client.setCameraEntity(client.player);
                        }
                    }
                }
            } else {
                // 复活后重置
                if (freeCamEnabled)
                    freeCamEnabled = false;
            }

            // 检查是否是阴谋家
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.level);
            if (!gameWorld.isRole(client.player, ModRoles.CONSPIRATOR))
                return;

            // 检查是否正在使用书页物品（通过检测使用状态）
            // 书页物品的使用会在服务端验证后触发
        });
    }

    /**
     * 设置物品回调函数
     */
    public static void setupItemCallbacks() {
        // 设置阴谋之书页的GUI打开回调
        ConspiracyPageItem.openScreenCallback = () -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null)
                return;
            client.setScreen(new ConspiratorScreen());
        };
        WrittenNoteItem.openScreenCallback = () -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null)
                return;
            client.setScreen(new RecorderScreen());
        };
    }

    /**
     * 打开阴谋家选择屏幕
     * 由物品使用触发
     */
    public static void openConspiratorScreen() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null)
            return;

        // 验证玩家持有书页
        ItemStack mainHand = client.player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = client.player.getItemInHand(InteractionHand.OFF_HAND);

        if (mainHand.is(ModItems.CONSPIRACY_PAGE) || offHand.is(ModItems.CONSPIRACY_PAGE)) {
            client.setScreen(new ConspiratorScreen());
        }
    }

    /**
     * 技能按键被按下时的处理
     */
    public static void onAbilityKeyPressed(Minecraft client) {
        if (client.player == null)
            return;
        // 获取游戏世界组件
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.level());

        // 获取玩家的技能组件
        @SuppressWarnings("unused")
        AbilityPlayerComponent abilityComponent = AbilityPlayerComponent.KEY.get(client.player);
        if (gameWorld.isRole(client.player, ModRoles.PUPPETEER) ||
                PuppeteerPlayerComponent.KEY.get(client.player).isActivePuppeteer()) {
            // 检查玩家是否存活
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;

            PuppeteerPlayerComponent puppeteerComp = PuppeteerPlayerComponent.KEY.get(client.player);

            // 阶段一：收集者模式，提示玩家需要收集更多尸体
            if (puppeteerComp.phase == 1) {
                // 计算阈值
                int totalPlayers = client.level.players().size();
                int threshold = Math.max(1, totalPlayers / 6);

                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.noellesroles.puppeteer.collect_more",
                                puppeteerComp.collectedBodies, threshold),
                        true);
                return;
            }

            // 阶段二：使用假人技能
            if (puppeteerComp.phase == 2) {
                if (puppeteerComp.canUsePuppetAbility()) {
                    ClientPlayNetworking.send(new PuppeteerC2SPacket(PuppeteerC2SPacket.Action.USE_PUPPET));
                } else if (puppeteerComp.abilityCooldown > 0) {
                    client.player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    "message.noellesroles.puppeteer.ability_cooldown",
                                    String.format("%.0f", puppeteerComp.getAbilityCooldownSeconds())),
                            true);
                } else if (puppeteerComp.getRemainingPuppetUses() <= 0) {
                    client.player.displayClientMessage(
                            net.minecraft.network.chat.Component
                                    .translatable("message.noellesroles.puppeteer.no_puppets"),
                            true);
                }
            }
            return;
        }
        // ==================== 傀儡师：优先检测操控假人状态 ====================
        // 必须放在所有角色之前，因为傀儡师操控假人时角色会临时变成其他杀手
        // 如果不优先检测，假人角色的按键处理会拦截G键
        PuppeteerPlayerComponent puppeteerCompEarly = PuppeteerPlayerComponent.KEY.get(client.player);
        if (puppeteerCompEarly.isControllingPuppet) {
            // 检查玩家是否存活
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;

            // 正在操控假人，按G返回本体
            ClientPlayNetworking.send(new PuppeteerC2SPacket(PuppeteerC2SPacket.Action.RETURN_TO_BODY));
            return;
        }

        // ==================== 拳击手：激活钢筋铁骨技能 ====================
        if (gameWorld.isRole(client.player, ModRoles.BOXER)) {
            // 检查玩家是否存活
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;

            BoxerPlayerComponent boxerComponent = BoxerPlayerComponent.KEY.get(client.player);
            // 检查技能是否可用（客户端显示提示）
            if (boxerComponent.canUseAbility()) {
                // 发送网络包到服务端激活技能
                ClientPlayNetworking.send(new BoxerAbilityC2SPacket());
            } else if (boxerComponent.cooldown > 0) {
                // 显示冷却提示
                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.noellesroles.boxer.on_cooldown",
                                String.format("%.1f", boxerComponent.getCooldownSeconds())),
                        true);
            }
            return;
        }

        // ==================== 失控机器人：眼镜 ====================
        if (gameWorld.isRole(client.player, ModRoles.GLITCH_ROBOT)) {
            // 检查玩家是否存活
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;
            if (!client.player.getSlot(103).get().is(ModItems.NIGHT_VISION_GLASSES)) {
                client.player.displayClientMessage(
                        Component.translatable("info.glitch_robot.noglasses_on_head").withStyle(ChatFormatting.RED),
                        true);
                return;

            }
            if (!RoleUtils.isPlayerHasFreeSlot(client.player)) {
                client.player.displayClientMessage(
                        Component.translatable("message.hotbar.full").withStyle(ChatFormatting.RED), true);
                return;

            }
            // 发送网络包到服务端激活技能
            ClientPlayNetworking.send(new AbilityC2SPacket());
            if (!client.player.getSlot(103).get().is(ModItems.NIGHT_VISION_GLASSES)) {
                client.player.removeEffect(MobEffects.NIGHT_VISION);
            }
            return;
        }

        // ==================== 运动员：激活疾跑技能 ====================
        if (gameWorld.isRole(client.player, ModRoles.ATHLETE)) {
            // 检查玩家是否存活
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;

            AthletePlayerComponent athleteComponent = AthletePlayerComponent.KEY.get(client.player);
            // 检查技能是否可用（客户端显示提示）
            if (athleteComponent.canUseAbility()) {
                // 发送网络包到服务端激活技能
                ClientPlayNetworking.send(new AthleteAbilityC2SPacket());
            } else if (athleteComponent.cooldown > 0) {
                // 显示冷却提示
                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.noellesroles.athlete.on_cooldown",
                                String.format("%.1f", athleteComponent.getCooldownSeconds())),
                        true);
            }
            return;
        }

        // ==================== 慕恋者：窥视积能量 ====================
        if (gameWorld.isRole(client.player, ModRoles.ADMIRER) ||
                AdmirerPlayerComponent.KEY.get(client.player).isActiveAdmirer()) {
            AdmirerPlayerComponent admirerComp = AdmirerPlayerComponent.KEY.get(client.player);

            // 按G开始/停止窥视
            if (!admirerComp.isGazing) {
                ClientPlayNetworking.send(new AdmirerGazeC2SPacket(true));
            } else {
                ClientPlayNetworking.send(new AdmirerGazeC2SPacket(false));
            }
            return;
        }

        // ==================== 跟踪者：窥视和突进 ====================
        if (gameWorld.isRole(client.player, ModRoles.STALKER) ||
                StalkerPlayerComponent.KEY.get(client.player).isActiveStalker()) {
            StalkerPlayerComponent stalkerComp = StalkerPlayerComponent.KEY.get(client.player);

            // 一阶段和二阶段：按G开始/停止窥视
            if (stalkerComp.phase <= 2) {
                if (!stalkerComp.isGazing) {
                    ClientPlayNetworking.send(new StalkerGazeC2SPacket(true));
                } else {
                    ClientPlayNetworking.send(new StalkerGazeC2SPacket(false));
                }
            }
            // 三阶段突进由鼠标右键控制，这里不处理
            return;
        }

        // ==================== 私家侦探：审查玩家物品栏 ====================
        if (gameWorld.isRole(client.player, ModRoles.DETECTIVE)) {
            // 使用准星检测目标玩家
            net.minecraft.world.phys.HitResult hitResult = client.hitResult;
            if (hitResult != null && hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                net.minecraft.world.phys.EntityHitResult entityHit = (net.minecraft.world.phys.EntityHitResult) hitResult;
                if (entityHit.getEntity() instanceof Player targetPlayer) {
                    // 发送审查请求到服务端
                    ClientPlayNetworking
                            .send(new org.agmas.noellesroles.packet.DetectiveC2SPacket(targetPlayer.getUUID()));
                }
            }
            return;
        }

        // ==================== 设陷者：放置灾厄印记陷阱 ====================
        if (gameWorld.isRole(client.player, ModRoles.TRAPPER)) {
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;

            TrapperPlayerComponent trapperComponent = TrapperPlayerComponent.KEY.get(client.player);
            if (trapperComponent.canPlaceTrap()) {
                ClientPlayNetworking.send(new TrapperC2SPacket());
            } else {
                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.noellesroles.trapper.no_charges",
                                String.format("%.1f", trapperComponent.getRechargeSeconds())),
                        true);
            }
            return;
        }

        // ==================== 明星：聚光灯技能 ====================
        if (gameWorld.isRole(client.player, ModRoles.STAR)) {
            // 检查玩家是否存活
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;

            StarPlayerComponent starComponent = StarPlayerComponent.KEY.get(client.player);
            // 检查技能是否可用
            if (starComponent.canUseAbility()) {
                // 发送网络包到服务端激活技能
                ClientPlayNetworking.send(new StarAbilityC2SPacket());
            } else if (starComponent.abilityCooldown > 0) {
                // 显示冷却提示
                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.noellesroles.star.on_cooldown",
                                String.format("%.0f", starComponent.getCooldownSeconds())),
                        true);
            }
            return;
        }

        // // ==================== 歌手：播放音乐技能 已变为物品栏购买 ====================
        // if (gameWorld.isRole(client.player, ModRoles.SINGER)) {
        // // 检查玩家是否存活
        // if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
        // return;

        // SingerPlayerComponent singerComponent =
        // SingerPlayerComponent.KEY.get(client.player);
        // // 检查技能是否可用
        // if (singerComponent.canUseAbility()) {
        // // 发送网络包到服务端激活技能
        // ClientPlayNetworking.send(new SingerAbilityC2SPacket());
        // } else if (singerComponent.abilityCooldown > 0) {
        // // 显示冷却提示
        // client.player.displayClientMessage(
        // net.minecraft.network.chat.Component.translatable("message.noellesroles.singer.on_cooldown",
        // String.format("%.0f", singerComponent.getCooldownSeconds())),
        // true);
        // }
        // return;
        // }

        // ==================== 傀儡师：使用假人技能 ====================
        // 注意：操控假人时的返回本体逻辑已在方法开头优先处理

        // ==================== 心理学家：心理治疗技能 ====================
        if (gameWorld.isRole(client.player, ModRoles.PSYCHOLOGIST)) {
            // 检查玩家是否存活
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;

            PsychologistPlayerComponent psychComponent = PsychologistPlayerComponent.KEY.get(client.player);

            // 如果正在治疗，按G取消
            if (psychComponent.isHealing) {
                // 发送空UUID取消治疗
                psychComponent.stopHealing("message.noellesroles.psychologist.cancelled");
                return;
            }

            // 检查技能是否可用
            if (!psychComponent.canUseAbility()) {
                if (psychComponent.cooldown > 0) {
                    // 显示冷却提示
                    client.player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                    "message.noellesroles.psychologist.on_cooldown",
                                    psychComponent.getCooldownSeconds()),
                            true);
                } else {
                    // san值不足
                    client.player.displayClientMessage(
                            net.minecraft.network.chat.Component
                                    .translatable("message.noellesroles.psychologist.not_full_san"),
                            true);
                }
                return;
            }

            // 使用准星检测目标玩家
            net.minecraft.world.phys.HitResult hitResult = client.hitResult;
            if (hitResult != null && hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                net.minecraft.world.phys.EntityHitResult entityHit = (net.minecraft.world.phys.EntityHitResult) hitResult;
                if (entityHit.getEntity() instanceof Player targetPlayer) {
                    // 发送治疗请求到服务端
                    ClientPlayNetworking.send(new PsychologistC2SPacket(targetPlayer.getUUID()));
                }
            } else {
                // 没有瞄准玩家
                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component
                                .translatable("message.noellesroles.psychologist.no_target"),
                        true);
            }
            return;
        }
        if (gameWorld.isRole(client.player,
                ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;

            InsaneKillerPlayerComponent component = InsaneKillerPlayerComponent.KEY.get(client.player);
            if (component.cooldown <= 0) {
                ClientPlayNetworking.send(new InsaneKillerAbilityC2SPacket());
                if (!component.isActive) {
                    Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
                } else {
                    Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
                }
            } else {
                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.noellesroles.cooldown",
                                component.cooldown / 20),
                        true);
            }
            return;
        }

        // 监察员：标记目标
        if (gameWorld.isRole(client.player, ModRoles.MONITOR)) {
            if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
                return;

            MonitorPlayerComponent monitorComponent = MonitorPlayerComponent.KEY.get(client.player);

            // 检查冷却
            if (!monitorComponent.canUseAbility()) {
                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.noellesroles.monitor.cooldown",
                                String.format("%.1f", monitorComponent.getCooldownSeconds())),
                        true);
                return;
            }

            net.minecraft.world.phys.HitResult hitResult = client.hitResult;
            if (hitResult != null && hitResult.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                net.minecraft.world.phys.EntityHitResult entityHit = (net.minecraft.world.phys.EntityHitResult) hitResult;
                if (entityHit.getEntity() instanceof Player targetPlayer) {
                    ClientPlayNetworking.send(new MonitorMarkC2SPacket(targetPlayer.getUUID()));
                }
            } else {
                client.player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.noellesroles.monitor.no_target"),
                        true);
            }
            return;
        }
        // 赌徒：打开GUI
        if (gameWorld.isRole(client.player, ModRoles.GAMBLER)) {
            client.setScreen(new GamblerScreen(client.player));
            return;
        }

        // 大嗓门：更大的说话
        if (gameWorld.isRole(client.player, ModRoles.NOISEMAKER)) {
            ClientPlayNetworking.send(new AbilityC2SPacket());
            return;
        }

        // if (abilityComponent.cooldown > 0) {
        // return;
        // }

        // ==================== 示例：根据角色执行不同技能 ====================
        //
        // if (gameWorld.isRole(client.player, ModRoles.EXAMPLE_ROLE)) {
        // // 发送技能使用包到服务端
        // ClientPlayNetworking.send(new AbilityC2SPacket());
        // }

        // 默认：发送通用技能包
        // ClientPlayNetworking.send(new AbilityC2SPacket());
    }

    /**
     * 处理跟踪者持续按键输入
     * 用于窥视（一二阶段）和蓄力突进（三阶段）
     */
    public static void handleStalkerContinuousInput(Minecraft client) {
        if (client.player == null)
            return;

        StalkerPlayerComponent stalkerComp = StalkerPlayerComponent.KEY.get(client.player);
        if (!stalkerComp.isActiveStalker())
            return;
        if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
            return;

        // 三阶段：鼠标右键蓄力突进
        if (stalkerComp.phase == 3 && stalkerComp.dashModeActive) {
            boolean isRightMouseDown = client.options.keyUse.isDown();

            // 检查玩家手持刀
            boolean holdingKnife = client.player.getMainHandItem().is(
                    dev.doctor4t.trainmurdermystery.index.TMMItems.KNIFE);

            if (holdingKnife) {
                if (isRightMouseDown && !stalkerChargingLastTick) {
                    // 开始蓄力
                    ClientPlayNetworking.send(new StalkerDashC2SPacket(true));
                } else if (!isRightMouseDown && stalkerChargingLastTick) {
                    // 释放蓄力
                    ClientPlayNetworking.send(new StalkerDashC2SPacket(false));
                }
                stalkerChargingLastTick = isRightMouseDown;
            }
        }
    }

    /**
     * 处理慕恋者持续按键输入
     * 用于窥视积累能量
     */
    public static void handleAdmirerContinuousInput(Minecraft client) {
        if (client.player == null)
            return;

        AdmirerPlayerComponent admirerComp = AdmirerPlayerComponent.KEY.get(client.player);
        if (!admirerComp.isActiveAdmirer())
            return;
        if (!GameFunctions.isPlayerAliveAndSurvival(client.player))
            return;

        // 检查技能键是否按住
        boolean isAbilityKeyDown = abilityBind.isDown();

        if (isAbilityKeyDown && !admirerGazingLastTick) {
            // 开始窥视
            ClientPlayNetworking.send(new AdmirerGazeC2SPacket(true));
        } else if (!isAbilityKeyDown && admirerGazingLastTick) {
            // 停止窥视
            ClientPlayNetworking.send(new AdmirerGazeC2SPacket(false));
        }
        admirerGazingLastTick = isAbilityKeyDown;
    }

    /**
     * 注册物品提示
     */
    @SuppressWarnings("unused")
    private void registerItemTooltips() {
        // 示例：为自定义物品添加提示
        // ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipType,
        // list) -> {
        // if (itemStack.isOf(ModItems.EXAMPLE_ITEM)) {
        // list.add(Text.translatable("item." + Noellesroles.MOD_ID +
        // ".example_item.tooltip")
        // .setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        // }
        // });
    }

    /**
     * 注册实体渲染器
     */
    public static void registerEntityRenderers() {
        // 烟雾弹实体渲染器 - 使用飞行物品渲染器
        EntityRendererRegistry.register(ModEntities.SMOKE_GRENADE, ThrownItemRenderer::new);

        // 灾厄印记实体渲染器 - 使用自定义渲染器（对设陷者半透明可见）
        EntityRendererRegistry.register(ModEntities.CALAMITY_MARK, CalamityMarkEntityRenderer::new);

        // 傀儡本体实体渲染器 - 使用玩家皮肤渲染
        EntityRendererRegistry.register(ModEntities.PUPPETEER_BODY, PuppeteerBodyEntityRenderer::new);

        // 操纵师本体实体渲染器 - 使用玩家皮肤渲染
        EntityRendererRegistry.register(ModEntities.MANIPULATOR_BODY, ManipulatorBodyEntityRenderer::new);

        // 锁实体渲染器 - 使用自定义渲染器
        EntityRendererRegistry.register(ModEntities.LOCK_ENTITY, LockEntityRender::new);
    }

    /**
     * 注册Screen
     */
    public static void registerScreens() {
        // 注册邮差传递界面
        MenuScreens.register(ModScreenHandlers.POSTMAN_SCREEN_HANDLER, PostmanHandledScreen::new);

        // 注册私家侦探审查界面
        MenuScreens.register(ModScreenHandlers.DETECTIVE_INSPECT_SCREEN_HANDLER, DetectiveInspectScreen::new);
    }

    // ==================== 工具方法 ====================

    /**
     * 获取格式化的冷却时间（秒）
     */
    public static String formatCooldown(int ticks) {
        return String.format("%.1f", ticks / 20.0);
    }

    /**
     * 检查当前玩家是否有指定角色
     */
    public static boolean hasRole(dev.doctor4t.trainmurdermystery.api.Role role) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null)
            return false;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(client.player.level());
        return gameWorld.isRole(client.player, role);
    }
}