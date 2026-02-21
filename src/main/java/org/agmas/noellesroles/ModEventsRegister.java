package org.agmas.noellesroles;

import java.util.ArrayList;
import java.util.List;

import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.commands.BroadcastCommand;
import org.agmas.noellesroles.component.AwesomePlayerComponent;
import org.agmas.noellesroles.component.BetterVigilantePlayerComponent;
import org.agmas.noellesroles.component.BoxerPlayerComponent;
import org.agmas.noellesroles.component.DeathPenaltyComponent;
import org.agmas.noellesroles.component.DefibrillatorComponent;
import org.agmas.noellesroles.component.GlitchRobotPlayerComponent;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.MonitorPlayerComponent;
import org.agmas.noellesroles.component.NianShouPlayerComponent;
import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.component.PuppeteerPlayerComponent;
import org.agmas.noellesroles.component.RecorderPlayerComponent;
import org.agmas.noellesroles.component.StalkerPlayerComponent;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.entity.HallucinationAreaManager;
import org.agmas.noellesroles.entity.PuppeteerBodyEntity;
import org.agmas.noellesroles.entity.SmokeAreaManager;
import org.agmas.noellesroles.packet.BloodConfigS2CPacket;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.commander.CommanderHandler;
import org.agmas.noellesroles.roles.conspirator.ConspiratorKilledPlayer;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.roles.ghost.GhostPlayerComponent;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import org.agmas.noellesroles.roles.vulture.VulturePlayerComponent;
import org.agmas.noellesroles.utils.EntityClearUtils;
import org.agmas.noellesroles.utils.MapScanner;
import org.agmas.noellesroles.utils.RoleUtils;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPsychoComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.compat.TrainVoicePlugin;
import dev.doctor4t.trainmurdermystery.entity.NoteEntity;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.event.AllowPlayerDeath;
import dev.doctor4t.trainmurdermystery.event.CanSeePoison;
import dev.doctor4t.trainmurdermystery.event.OnGameTrueStarted;
import dev.doctor4t.trainmurdermystery.event.OnPlayerDeath;
import dev.doctor4t.trainmurdermystery.event.OnPlayerKilledPlayer;
import dev.doctor4t.trainmurdermystery.event.OnTeammateKilledTeammate;
import dev.doctor4t.trainmurdermystery.event.ShouldDropOnDeath;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import pro.fazeclan.river.stupid_express.constants.SEItems;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import pro.fazeclan.river.stupid_express.modifier.refugee.cca.RefugeeComponent;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMGameModes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class ModEventsRegister {
    private static AttributeModifier noJumpingAttribute = new AttributeModifier(
            Noellesroles.id("no_jumping"), -1.0f, AttributeModifier.Operation.ADD_VALUE);

    /**
     * 处理拳击手无敌反制
     * 钢筋铁骨期间可以反弹任何死亡
     *
     * @param victim      受害者
     * @param deathReason 死亡原因
     * @return true 表示成功反制，应阻止死亡
     */
    private static boolean handleBoxerInvulnerability(Player victim, ResourceLocation deathReason) {
        if (victim == null || victim.level().isClientSide())
            return false;

        // 检查受害者是否是拳击手
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
        if (!gameWorld.isRole(victim, ModRoles.BOXER))
            return false;

        // 获取拳击手组件
        BoxerPlayerComponent boxerComponent = ModComponents.BOXER.get(victim);

        // 检查是否处于无敌状态
        if (!boxerComponent.isInvulnerable)
            return false;

        // 钢筋铁骨可以反弹任何死亡 - 不再限制死亡原因

        // 尝试找到攻击者（如果是刀或棍棒攻击）
        boolean isKnife = deathReason.equals(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.KNIFE);
        boolean isBat = deathReason.equals(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.BAT);

        if (isKnife || isBat) {
            // 需要找到攻击者 - 遍历附近玩家找到持有对应武器的
            Player attacker = RicesRoleRhapsody.findAttackerWithWeapon(victim, isKnife);

            if (attacker != null) {
                // 获取攻击者的武器
                ItemStack weapon = attacker.getMainHandItem();

                // 执行反制（对刀和棍棒有额外效果）
                boxerComponent.handleCounterAttack(attacker, weapon);
            }
        }

        // 执行通用反制（反弹任何死亡）
        boxerComponent.handleAnyDeathCounter(deathReason);

        // 无敌状态下阻止任何死亡
        return true;
    }

    /**
     * 处理跟踪者免疫
     * 盾牌只在一阶段有效，进入二阶段后消失
     *
     * @param victim      受害者
     * @param deathReason 死亡原因
     * @return true 表示成功免疫，应阻止死亡
     */
    private static boolean handleStalkerImmunity(Player victim, ResourceLocation deathReason) {
        if (victim == null || victim.level().isClientSide())
            return false;

        // 获取跟踪者组件
        StalkerPlayerComponent stalkerComp = ModComponents.STALKER.get(victim);

        // 检查是否是活跃的跟踪者且处于一阶段（盾牌只在一阶段有效）
        if (!stalkerComp.isActiveStalker())
            return false;
        if (stalkerComp.phase != 1)
            return false;

        // 检查免疫是否已使用
        if (stalkerComp.immunityUsed)
            return false;

        // 消耗免疫
        stalkerComp.immunityUsed = true;
        stalkerComp.sync();

        // 播放音效
        victim.level().playSound(null, victim.blockPosition(),
                dev.doctor4t.trainmurdermystery.index.TMMSounds.ITEM_PSYCHO_ARMOUR,
                SoundSource.MASTER, 5.0F, 1.0F);

        // 发送消息
        if (victim instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.stalker.immunity_triggered")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                    true);
        }

        return true;
    }

    /**
     * 处理傀儡师死亡
     * 假人死亡时返回本体，本体死亡时真正死亡
     *
     * @param victim      受害者
     * @param deathReason 死亡原因
     * @return true 表示假人死亡（阻止真正死亡），false 表示正常处理
     */
    private static boolean handlePuppeteerDeath(Player victim, ResourceLocation deathReason) {
        if (victim == null || victim.level().isClientSide())
            return false;

        // 获取傀儡师组件
        PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(victim);

        // 检查是否是活跃的傀儡师
        if (!puppeteerComp.isActivePuppeteer())
            return false;

        // 检查是否正在操控假人
        if (!puppeteerComp.isControllingPuppet)
            return false;

        // 假人死亡，返回本体
        puppeteerComp.onPuppetDeath();

        return true; // 阻止真正死亡
    }

    private static boolean handleDefibrillator(Player victim) {
        DefibrillatorComponent component = ModComponents.DEFIBRILLATOR.get(victim);
        if (component.hasProtection()) {
            component.triggerDeath(30 * 20, null, victim.position());
            return true;
        }
        return false;
    }

    private static void handleGlitchRobotDeath(Player victim) {
        if (victim == null || victim.level().isClientSide())
            return;

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
        if (!gameWorldComponent.isRole(victim, ModRoles.GLITCH_ROBOT))
            return;

        GlitchRobotPlayerComponent.onKnockOut(victim);

    }

    private static void handleDeathPenalty(Player victim) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
        boolean doctorAlive = false;
        boolean looseEndAlive = false;
        boolean INSANE_alive = false;
        boolean CONSPIRATOR_alive = false;
        boolean limitView = false;
        var refugeeComponent = RefugeeComponent.KEY.get(victim.level());
        if (gameWorldComponent.getGameMode().identifier.equals(TMMGameModes.LOOSE_ENDS_ID))
            return;
        if (refugeeComponent.isAnyRevivals) {
            looseEndAlive = true;
        }
        for (Player player : victim.level().players()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                continue;
            }
            if (gameWorldComponent.isRole(player, ModRoles.DOCTOR)) {
                doctorAlive = true;
            } else if (gameWorldComponent.isRole(player, ModRoles.CONSPIRATOR)) {
                CONSPIRATOR_alive = true;
            } else if (gameWorldComponent.isRole(player,
                    ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                INSANE_alive = true;
            }
            if (doctorAlive && INSANE_alive && CONSPIRATOR_alive) {
                break;
            }
        }
        if (INSANE_alive && CONSPIRATOR_alive) {
            limitView = true;
        }
        if (looseEndAlive) {
            DeathPenaltyComponent component = ModComponents.DEATH_PENALTY.get(victim);
            ServerPlayer refugeePlayer = null;
            component.limitCameraUUID = null;
            if (victim instanceof ServerPlayer sp) {
                for (var p : sp.getServer().getPlayerList().getPlayers()) {
                    if (GameFunctions.isPlayerAliveAndSurvival(p)) {
                        if (gameWorldComponent.isRole(p, TMMRoles.LOOSE_END)) {
                            refugeePlayer = p;
                            break;
                        }
                    }
                }
            }
            if (refugeePlayer != null)
                component.limitCameraUUID = refugeePlayer.getUUID();
            if (component.limitCameraUUID != null) {
                if (victim instanceof ServerPlayer sp) {
                    sp.setCamera(refugeePlayer);
                }
                component.setPenalty(-1);
                victim.sendSystemMessage(
                        Component.translatable("message.noellesroles.penalty.limit.loose_end")
                                .withStyle(ChatFormatting.RED));
                victim.displayClientMessage(
                        Component.translatable("message.noellesroles.penalty.limit.loose_end")
                                .withStyle(ChatFormatting.RED),
                        true);

                if (victim.hasPermissions(2)) {
                    victim.sendSystemMessage(Component.translatable("message.noellesroles.admin.free_cam_hint")
                            .withStyle(ChatFormatting.YELLOW));
                }
            }

        } else if (limitView) {
            DeathPenaltyComponent component = ModComponents.DEATH_PENALTY.get(victim);
            component.setPenalty(-1);
            victim.sendSystemMessage(
                    Component.translatable("message.noellesroles.penalty.limit.god_job_couple")
                            .withStyle(ChatFormatting.RED));
            victim.displayClientMessage(
                    Component.translatable("message.noellesroles.penalty.limit.god_job_couple")
                            .withStyle(ChatFormatting.RED),
                    true);

            if (victim.hasPermissions(2)) {
                victim.sendSystemMessage(Component.translatable("message.noellesroles.admin.free_cam_hint")
                        .withStyle(ChatFormatting.YELLOW));
            }
        } else if (doctorAlive) {
            DeathPenaltyComponent component = ModComponents.DEATH_PENALTY.get(victim);
            component.setPenalty(45 * 20);
            victim.displayClientMessage(
                    Component.translatable("message.noellesroles.doctor.penalty").withStyle(ChatFormatting.RED), true);

            victim.sendSystemMessage(
                    Component.translatable("message.noellesroles.doctor.penalty").withStyle(ChatFormatting.RED));
            if (victim.hasPermissions(2)) {
                victim.sendSystemMessage(Component.translatable("message.noellesroles.admin.free_cam_hint")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    /**
     * 处理医生死亡 - 将针管传递给另一名存活的平民
     */
    private static void handleDoctorDeath(Player victim) {
        if (victim == null || victim.level().isClientSide())
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
        if (!gameWorld.isRole(victim, ModRoles.DOCTOR))
            return;

        // 查找医生背包中的针管
        ItemStack antidote = null;
        for (int i = 0; i < victim.getInventory().getContainerSize(); i++) {
            ItemStack stack = victim.getInventory().getItem(i);
            if (stack.getItem() == org.agmas.noellesroles.repack.HSRItems.ANTIDOTE) {
                antidote = stack.copy();
                victim.getInventory().setItem(i, ItemStack.EMPTY);
                break;
            }
        }

        if (antidote == null || antidote.isEmpty())
            return;

        // 查找另一名存活的平民
        Player targetPlayer = null;
        for (Player player : victim.level().players()) {
            if (player == victim)
                continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(player))
                continue;

            Role role = gameWorld.getRole(player);
            if (role != null && role.isInnocent()) {
                targetPlayer = player;
                break;
            }
        }

        // 如果找到存活的平民，传递针管
        if (targetPlayer != null) {
            targetPlayer.addItem(antidote);
            if (targetPlayer instanceof ServerPlayer serverTarget) {
                serverTarget.displayClientMessage(
                        Component.translatable("message.noellesroles.doctor.antidote_inherited",
                                victim.getName().getString())
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                        true);
            }
        }
    }

    public static void registerEvents() {
        CommanderHandler.registerChatEvent();
        InsaneKillerPlayerComponent.registerEvent();
        ConspiratorKilledPlayer.registerEvents();
        EntityClearUtils.registerResetEvent();
        TMM.cantSendReplay.add(player -> {
            DeathPenaltyComponent component = ModComponents.DEATH_PENALTY.get(player);
            if (component != null) {
                if (component.hasPenalty())
                    return true;
            }
            return false;
        });
        TMM.canStickArmor.add((deathInfo -> {
            String deathReasonPath = deathInfo.deathReason().getPath();
            if (deathReasonPath.equals("ignited")) {
                // 纵火犯
                return true;
            }
            if (deathReasonPath.equals("voodoo")) {
                // 巫毒
                return true;
            }
            if (deathReasonPath.equals("shot_innocent")) {
                // 误杀平民
                return true;
            }
            return false;
        }));
        MapScanner.registerMapScanEvent();
        CustomWinnerClass.registerCustomWinners();
        OnTeammateKilledTeammate.EVENT.register((victim, killer, isInnocent, deathReason) -> {
            if (GameFunctions.isPlayerAliveAndSurvival(killer)) {
                if (isInnocent) {
                    GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
                    if (gameWorldComponent.isRole(victim, ModRoles.VOODOO)) {
                        return;
                    }
                    if (NoellesRolesConfig.HANDLER.instance().accidentalKillPunishment) {
                        if (deathReason.getPath().equals("revolver_shot")
                                || deathReason.getPath().equals("bat_hit")
                                || deathReason.getPath().equals("gun_shot")
                                || deathReason.getPath().equals("arrow")
                                || deathReason.getPath().equals("knife_stab")
                                || deathReason.getPath().equals("fell_out_of_train")) {
                            GameFunctions.killPlayer(killer, true, null, Noellesroles.id("shot_innocent"));
                        }
                    }
                }
            }
        });
        OnPlayerKilledPlayer.EVENT.register((victim, killer, deathReason) -> {
            if (deathReason.equals(OnPlayerKilledPlayer.DeathReason.KNIFE)) {
                killer.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED, // ID
                        1, // 持续时间（tick）
                        1, // 等级（0 = 速度 I）
                        false, // ambient（环境效果，如信标）
                        false, // showParticles（显示粒子）
                        false // showIcon（显示图标）
                ));
            }
        });
        ShouldDropOnDeath.EVENT.register(((itemStack) -> {
            final var key = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            if ("exposure:album".equals(key) || "exposure:photograph".equals(key)
                    || "exposure:stacked_photographs".equals(key) || itemStack.is(ModItems.PATROLLER_REVOLVER)) {
                return true;
            }

            return false;
        }));
        OnPlayerDeath.EVENT.register((playerEntity, reason) -> {
            RoleUtils.RemoveAllEffects(playerEntity);
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(playerEntity.level());
            if (gameWorldComponent.isRole(playerEntity,
                    ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                final var insaneKillerPlayerComponent = InsaneKillerPlayerComponent.KEY.get(playerEntity);
                insaneKillerPlayerComponent.reset();
            }
            if (gameWorldComponent.isRole(playerEntity, ModRoles.BETTER_VIGILANTE)) {
                final var betterVigilantePlayerComponent = BetterVigilantePlayerComponent.KEY.get(playerEntity);
                betterVigilantePlayerComponent.reset();
            }
        });
        AllowPlayerDeath.EVENT.register(((playerEntity, identifier) -> {
            if (identifier == GameConstants.DeathReasons.FELL_OUT_OF_TRAIN)
                return true;
            if (identifier.getPath().equals("disconnected"))
                return true;
            if (identifier.getPath().equals("ignited"))
                return true;
            if (identifier.getPath().equals("failed_ignite"))
                return true;
            if (identifier.getPath().equals("heart_attack"))
                return true;
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(playerEntity.level());
            if (gameWorldComponent.isRole(playerEntity, ModRoles.JESTER)) {
                PlayerPsychoComponent component = PlayerPsychoComponent.KEY.get(playerEntity);
                if (component.getPsychoTicks() > GameConstants.getInTicks(0, 44)) {
                    return false;
                }
            }
            return true;
        }));
        CanSeePoison.EVENT.register((player) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.level());
            if (gameWorldComponent.isRole((Player) player, ModRoles.BARTENDER)) {
                return true;
            }
            if (gameWorldComponent.isRole((Player) player, ModRoles.POISONER)) {
                return true;
            }
            return false;
        });
        AwesomePlayerComponent.registerEvents();
        TrueKillerFinder.registerEvents();

        ModdedRoleAssigned.EVENT.register((player, role) -> {
            if (role.identifier().equals(TMMRoles.KILLER.identifier())) {
                player.addItem(TMMItems.KNIFE.getDefaultInstance().copy());
                return;
            }
            if (role.identifier().equals(TMMRoles.VIGILANTE.identifier())) {
                player.addItem(TMMItems.REVOLVER.getDefaultInstance().copy());
                return;
            }
            if (role.identifier().equals(ModRoles.ATTENDANT.identifier())) {
                if (player instanceof ServerPlayer sp)
                    TMM.SendRoomInfoToPlayer(sp);
                return;
            }
            if (role.identifier().equals(ModRoles.OLDMAN.identifier())){
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, // ID
                        -1, // 持续时间（tick）
                        1, // 等级（0 = 速度 I）
                        false, // ambient（环境效果，如信标）
                        false, // showParticles（显示粒子）
                        false // showIcon（显示图标）
                ));
                return;
            }
            NoellesRolesAbilityPlayerComponent abilityPlayerComponent = (NoellesRolesAbilityPlayerComponent) NoellesRolesAbilityPlayerComponent.KEY
                    .get(player);
            abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;

            if (role.equals(ModRoles.BROADCASTER)) {
                abilityPlayerComponent.cooldown = 0;
                PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(player);
                playerShopComponent.setBalance(200);
                playerShopComponent.sync();
            } else {
                abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;
            }
            if (role.equals(ModRoles.EXECUTIONER)) {
                ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY
                        .get(player);
                executionerPlayerComponent.won = false;
                PlayerShopComponent playerShopComponent = (PlayerShopComponent) PlayerShopComponent.KEY.get(player);
                executionerPlayerComponent.reset();
                playerShopComponent.setBalance(100);
                executionerPlayerComponent.sync();
            }
            if (role.equals(ModRoles.VULTURE)) {
                if (VulturePlayerComponent.KEY.isProvidedBy(player)) {
                    VulturePlayerComponent vulturePlayerComponent = VulturePlayerComponent.KEY.get(player);
                    vulturePlayerComponent.reset();
                    vulturePlayerComponent.bodiesRequired = Math.max(1, (int) ((player.level().players().size() / 3f)
                            - Math.floor(player.level().players().size() / 6f)));
                    vulturePlayerComponent.sync();
                }
            }
            if (role.equals(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                final var insaneKillerPlayerComponent = InsaneKillerPlayerComponent.KEY.get(player);
                insaneKillerPlayerComponent.reset();
                insaneKillerPlayerComponent.sync();
            }
            if (role.equals(ModRoles.RECORDER)) {
                final var recorderPlayerComponent = RecorderPlayerComponent.KEY.get(player);
                recorderPlayerComponent.initializeRoles();
            }

            // 更新所有记录员的可用角色列表
            for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                if (GameWorldComponent.KEY.get(p.level()).isRole(p, ModRoles.RECORDER)) {
                    RecorderPlayerComponent.KEY.get(p).updateAvailableRoles();
                }
            }
            if (role.equals(ModRoles.RECORDER)) {
                final var recorderPlayerComponent = RecorderPlayerComponent.KEY.get(player);
                recorderPlayerComponent.reset();
                recorderPlayerComponent.sync();
            }
            // 使用映射表添加初始物品
            RoleInitialItems.addInitialItemsForRole(player, role);

            if (role.equals(ModRoles.GAMBLER)) {
                org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent gamblerPlayerComponent = org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent.KEY
                        .get(player);
                gamblerPlayerComponent.reset();
                gamblerPlayerComponent.sync();
            }

            if (role.equals(ModRoles.NOISEMAKER)) {
                org.agmas.noellesroles.roles.noise_maker.NoiseMakerPlayerComponent noiseMakerPlayerComponent = org.agmas.noellesroles.roles.noise_maker.NoiseMakerPlayerComponent.KEY
                        .get(player);
                noiseMakerPlayerComponent.reset();
                noiseMakerPlayerComponent.sync();
            }
            if (role.equals(ModRoles.GHOST)) {
                org.agmas.noellesroles.roles.ghost.GhostPlayerComponent ghostPlayerComponent = org.agmas.noellesroles.roles.ghost.GhostPlayerComponent.KEY
                        .get(player);
                ghostPlayerComponent.reset();
                ghostPlayerComponent.sync();
            }
            // 操纵师角色初始化
            if (role.equals(ModRoles.MANIPULATOR)) {
                ManipulatorPlayerComponent manipulatorPlayerComponent = ManipulatorPlayerComponent.KEY.get(player);
                manipulatorPlayerComponent.reset();
                manipulatorPlayerComponent.sync();
            }
            if (role.equals(ModRoles.BOMBER)) {
                if (role.equals(ModRoles.MONITOR)) {
                    MonitorPlayerComponent monitorComponent = MonitorPlayerComponent.KEY.get(player);
                    monitorComponent.reset();
                    monitorComponent.sync();
                }
                // bomberPlayerComponent.reset(); // 如果有 reset 方法
                ModComponents.BOMBER.sync(player);
            }
            // if (role.equals(SHERIFF)) {
            // player.giveItemStack(TMMItems.REVOLVER.getDefaultStack());
            // org.agmas.noellesroles.roles.sheriff.SheriffPlayerComponent
            // sheriffPlayerComponent =
            // org.agmas.noellesroles.roles.sheriff.SheriffPlayerComponent.KEY.get(player);
            // sheriffPlayerComponent.reset();
            // sheriffPlayerComponent.sync();
            // }
            // 在角色分配时清除之前的跟踪者状态（如果有）
            // 但是如果跟踪者正在进化（切换角色），不清除状态
            StalkerPlayerComponent stalkerComp = ModComponents.STALKER.get(player);
            if (!stalkerComp.isActiveStalker()) {
                stalkerComp.clearAll();
            }

            // // 在角色分配时清除之前的傀儡师状态（如果有）
            // // 但是如果傀儡师正在操控假人（临时切换角色），不清除状态
            // PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(player);
            // if (!puppeteerComp.isPuppeteerMarked) {
            // puppeteerComp.clearAll();
            // }
            RicesRoleRhapsody.onRoleAssigned(player, role);
            if (role.identifier().equals(ModRoles.ELF.identifier())) {
                PlayerShopComponent shopComponent = PlayerShopComponent.KEY.get(player);
                shopComponent.setBalance(45);
                return;
            }

            // 魔术师角色初始化
            if (role.equals(ModRoles.MAGICIAN)) {
                var magicianComponent = ModComponents.MAGICIAN.get(player);
                if (magicianComponent != null) {
                    magicianComponent.stopFakePsycho();
                    // 随机分配一个杀手身份给魔术师（原版杀手和毒师除外）
                    List<ResourceLocation> killerRoles = new ArrayList<>();
                    for (var entry : dev.doctor4t.trainmurdermystery.api.TMMRoles.ROLES.entrySet()) {
                        Role r = entry.getValue();
                        if (r.canUseKiller() && !r.identifier().equals(dev.doctor4t.trainmurdermystery.api.TMMRoles.KILLER.identifier())
                                && !r.identifier().equals(ModRoles.POISONER_ID)) {
                            killerRoles.add(r.identifier());
                        }
                    }
                    if (!killerRoles.isEmpty()) {
                        ResourceLocation disguiseRole = killerRoles.get(player.getRandom().nextInt(killerRoles.size()));
                        magicianComponent.setDisguiseRoleId(disguiseRole);
                        player.sendSystemMessage(Component.translatable("message.magician.you_are_playing_as")
                                .append(Component.translatable("announcement.role." + disguiseRole.getPath()))
                                .withStyle(ChatFormatting.GOLD));
                    }
                }
                // 检查是否有指挥官，如果有则加入指挥官频道
                boolean hasCommander = player.getServer().getPlayerList().getPlayers().stream()
                        .anyMatch(p -> {
                            GameWorldComponent gw = GameWorldComponent.KEY.get(p.level());
                            return gw.getRole(p).identifier().equals(ModRoles.COMMANDER_ID);
                        });
                if (hasCommander) {
                    // 魔术师加入指挥官频道
                    player.sendSystemMessage(Component.translatable("message.magician.commander_present_joined_channel").withStyle(ChatFormatting.GOLD));
                }
            }

            // 纵火犯物品初始化
            if (role.equals(SERoles.ARSONIST)) {
                player.addItem(SEItems.JERRY_CAN.getDefaultInstance().copy());
                player.addItem(SEItems.LIGHTER.getDefaultInstance().copy());
            }
            if (role.equals(ModRoles.NIAN_SHOU)) {
                var comc = NianShouPlayerComponent.KEY.maybeGet(player).orElse(null);
                if (comc != null) {
                    comc.reset();
                }
            }
            if (role.equals(ModRoles.PUPPETEER)) {
                var comc = PuppeteerPlayerComponent.KEY.maybeGet(player).orElse(null);
                if (comc != null) {
                    if (!comc.isActivePuppeteer())
                        comc.reset();
                }
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            // 更新烟雾区域和迷幻区域
            SmokeAreaManager.tick();
            HallucinationAreaManager.tick();

            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(server.overworld());
            if (gameWorldComponent.isRunning()) {
                var all_players = server.getPlayerList().getPlayers();
                for (ServerPlayer player : all_players) {
                    if (gameWorldComponent.isRole(player, ModRoles.ELF)) {
                        if (server.overworld().getGameTime() % 200 == 0) {
                            PlayerShopComponent plsc = PlayerShopComponent.KEY.get(player);
                            if (plsc != null) {
                                plsc.addToBalance(5);
                            }
                        }
                    }
                }
                if (!Noellesroles.gunsCooled) {
                    int gunCooldownTicks = 30 * 20;
                    for (ServerPlayer player : all_players) {
                        ItemCooldowns itemCooldownManager = player.getCooldowns();
                        itemCooldownManager.addCooldown(TMMItems.REVOLVER, gunCooldownTicks);
                        itemCooldownManager.addCooldown(TMMItems.KNIFE, gunCooldownTicks);
                        itemCooldownManager.addCooldown(ModItems.FAKE_REVOLVER, gunCooldownTicks);
                    }
                    Noellesroles.gunsCooled = true;
                }
            } else {
                Noellesroles.gunsCooled = false;
            }
        }));
        if (!NoellesRolesConfig.HANDLER.instance().shitpostRoles) {
            HarpyModLoaderConfig.HANDLER.load();

            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(ModRoles.BETTER_VIGILANTE_ID.getPath())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(ModRoles.BETTER_VIGILANTE_ID.getPath());
            }

            HarpyModLoaderConfig.HANDLER.save();
        }
        // // 监听角色分配事件 - 这是最重要的事件！
        // // 当玩家被分配角色时触发，可以在这里给予初始物品、设置初始状态等
        // ModdedRoleAssigned.EVENT.register((player, role) -> {
        //
        // });
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, serverPlayer, bound) -> {
            var spc = SplitPersonalityComponent.KEY.get(serverPlayer);
            if (!spc.isDeath()) {
                ServerPlayer mainP = serverPlayer.server.getPlayerList().getPlayer(spc.getMainPersonality());
                ServerPlayer secondP = serverPlayer.server.getPlayerList().getPlayer(spc.getSecondPersonality());
                if (mainP == null || secondP == null)
                    return true;
                var broadcastMessage = Component
                        .translatable("message.split_personality.broadcast_prefix",
                                Component.literal("").append(serverPlayer.getDisplayName())
                                        .withStyle(ChatFormatting.AQUA),
                                Component.literal(message.signedContent()).withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.GOLD);
                if (serverPlayer.isSpectator()) {
                    BroadcastCommand
                            .BroadcastMessage(mainP,
                                    broadcastMessage);
                    BroadcastCommand.BroadcastMessage(secondP, broadcastMessage);
                } else {
                    BroadcastCommand
                            .BroadcastMessage(mainP,
                                    broadcastMessage);
                    BroadcastCommand.BroadcastMessage(secondP, broadcastMessage);
                }
            }
            return true;
        });

        OnGameTrueStarted.EVENT.register((serverLevel) -> {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(serverLevel);
            serverLevel.players().forEach(player -> {
                if (!gameWorldComponent.isJumpAvailable() && GameFunctions.isPlayerAliveAndSurvival(player)) {
                    // NO JUMPING! For everyone who hasn't permissions
                    if (!player.hasPermissions(2)) {
                        player.getAttribute(Attributes.JUMP_STRENGTH).addOrReplacePermanentModifier(noJumpingAttribute);
                    }
                }
                if (gameWorldComponent.isRole(player, ModRoles.ATTENDANT)) {
                    TMM.SendRoomInfoToPlayer(player);
                    // 发送房间信息
                }
            });

            // 年兽除岁效果：给所有玩家分发4个鞭炮
            boolean hasNianShou = false;
            if (gameWorldComponent != null) {
                for (var player : serverLevel.players()) {
                    if (gameWorldComponent.isRole(player, ModRoles.NIAN_SHOU)) {
                        hasNianShou = true;
                        break;
                    }
                }
            }

            if (hasNianShou) {
                for (ServerPlayer player : serverLevel.players()) {
                    // 给每个玩家4个鞭炮
                    ItemStack firecrackerStack = new ItemStack(TMMItems.FIRECRACKER);
                    firecrackerStack.set(DataComponents.MAX_STACK_SIZE, 4);
                    firecrackerStack.setCount(4);
                    player.getInventory().add(firecrackerStack);

                    // 发送提示消息
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component
                                    .translatable("message.noellesroles.nianshou.firecrackers_distributed")
                                    .withStyle(net.minecraft.ChatFormatting.GOLD),
                            true);
                }
            }
        });
        // 监听玩家死亡事件 - 用于激活复仇者能力、拳击手反制、跟踪者免疫和操纵师死亡判定
        AllowPlayerDeath.EVENT.register((victim, deathReason) -> {
            // 检查拳击手无敌反制
            if (handleBoxerInvulnerability(victim, deathReason)) {
                return false; // 阻止死亡
            }

            // 检查跟踪者免疫
            if (handleStalkerImmunity(victim, deathReason)) {
                return false; // 阻止死亡
            }

            // 检查傀儡师假人状态
            if (handlePuppeteerDeath(victim, deathReason)) {
                return false; // 阻止死亡（假人死亡）
            }

            // 检查起搏器
            if (handleDefibrillator(victim)) {
                // 允许死亡，但已标记复活
            }

            // onPlayerDeath(victim, deathReason);
            return true; // 允许死亡
        });
        OnPlayerDeath.EVENT.register((victim, deathReason) -> {
            // 检查医生死亡 - 传递针管
            handleDoctorDeath(victim);

            // 检查死亡惩罚
            handleDeathPenalty(victim);

            // 检查故障机器人 - 死亡时生成缓慢效果云
            handleGlitchRobotDeath(victim);
        });

        // 服务器Tick事件 - 处理复活
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                DefibrillatorComponent component = ModComponents.DEFIBRILLATOR.get(player);
                if (component.isDead && player.isSpectator()
                        && player.level().getGameTime() >= component.resurrectionTime) {
                    // 复活逻辑
                    if (component.deathPos != null) {
                        player.teleportTo(component.deathPos.x, component.deathPos.y, component.deathPos.z);
                    }
                    player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);

                    player.setHealth(player.getMaxHealth());

                    // 移除尸体
                    if (player.level() instanceof ServerLevel slevel) {
                        var entities = slevel.getAllEntities();
                        for (var bentity : entities) {
                            if (bentity instanceof PlayerBodyEntity body) {
                                if (body.getPlayerUuid().equals(player.getUUID())) {
                                    body.discard();
                                    break;
                                }
                            }
                        }
                    }
                    TrainVoicePlugin.resetPlayer(player.getUUID());
                    component.reset();

                    player.displayClientMessage(Component.translatable("message.noellesroles.defibrillator.revived"),
                            true);
                }

                // 检查死亡惩罚过期
                DeathPenaltyComponent penaltyComponent = ModComponents.DEATH_PENALTY.get(player);
                penaltyComponent.check();
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            sender.sendPacket(new BloodConfigS2CPacket(NoellesRolesConfig.HANDLER.instance().enableClientBlood));
        });

        // 示例：监听是否能看到毒药
        // CanSeePoison.EVENT.register((player) -> {
        // GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        // if (gameWorld.isRole(player, ModRoles.YOUR_ROLE)) {
        // return true;
        // }
        // return false;
        // });
    }

    public static void registerPredicate() {

        // 设置谓词
        TMM.canUseChatHud.add((role -> role.getIdentifier()
                .equals(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID)));
        TMM.canUseOtherPerson.add((role -> role.getIdentifier()
                .equals(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID)));
        TMM.canUseOtherPerson.add((role -> role.getIdentifier()
                .equals(ModRoles.MANIPULATOR_ID)));
        TMM.canCollide.add(a -> {
            final var gameWorldComponent = GameWorldComponent.KEY.get(a.level());
            if (gameWorldComponent.isRole(a,
                    ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                if (InsaneKillerPlayerComponent.KEY.get(a).isActive) {
                    return true;
                }
            }
            return false;
        });
        TMM.canCollide.add(a -> {
            if (a.hasEffect(MobEffects.INVISIBILITY)) {
                return true;
            }
            return false;
        });
        TMM.cantPushableBy.add(entity -> {
            if (entity instanceof PuppeteerBodyEntity) {
                return true;
            }
            return false;
        });
        TMM.cantPushableBy.add(entity -> {
            if (entity instanceof Player serverPlayer) {
                if (serverPlayer.hasEffect(MobEffects.INVISIBILITY)) {
                    return true;
                } else {
                    var modifiers = WorldModifierComponent.KEY.get(serverPlayer.level());
                    if (modifiers.isModifier(serverPlayer.getUUID(), SEModifiers.FEATHER)) {
                        return true;
                    }
                    var gameComp = GameWorldComponent.KEY.get(serverPlayer.level());
                    if (gameComp != null) {
                        if (gameComp.isRole(serverPlayer,
                                ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                            InsaneKillerPlayerComponent insaneKillerPlayerComponent = InsaneKillerPlayerComponent.KEY
                                    .get(serverPlayer);
                            if (insaneKillerPlayerComponent.isActive) {
                                return true;
                            }
                        }
                    }

                }
            }
            return false;
        });
        TMM.cantPushableBy.add(
                entity -> {
                    if (entity instanceof ServerPlayer serverPlayer) {
                        var gameComp = GameWorldComponent.KEY.get(serverPlayer.level());
                        if (gameComp != null) {
                            if (gameComp.isRole(serverPlayer, ModRoles.GHOST)) {
                                GhostPlayerComponent ghostPlayerComponent = GhostPlayerComponent.KEY.get(serverPlayer);
                                return ghostPlayerComponent.isActive;
                            }
                        }

                    }
                    return false;
                });
        TMM.canCollideEntity.add(entity -> {
            return entity instanceof PuppeteerBodyEntity;
        });
        TMM.cantPushableBy.add(entity -> {
            return (entity instanceof NoteEntity);
        });
        TMM.canDropItem.addAll(List.of(
                "exposure:stacked_photographs",
                "exposure:album",
                "exposure:photograph",
                "noellesroles:mint_candies"));

    }

}
