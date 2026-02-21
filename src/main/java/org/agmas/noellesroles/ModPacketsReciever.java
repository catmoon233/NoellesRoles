package org.agmas.noellesroles;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.roles.voodoo.VoodooPlayerComponent;
import org.agmas.noellesroles.roles.vulture.VulturePlayerComponent;
import org.agmas.noellesroles.utils.RoleUtils;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import java.util.*;
import org.agmas.noellesroles.component.BroadcasterPlayerComponent;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.MonitorPlayerComponent;
import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.component.SwapperPlayerComponent;
import org.agmas.noellesroles.packet.AbilityWithTargetC2SPacket;
import org.agmas.noellesroles.packet.GamblerSelectRoleC2SPacket;
import org.agmas.noellesroles.packet.RecorderC2SPacket;

public class ModPacketsReciever {

    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.MORPH_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(context.player().level());
            NoellesRolesAbilityPlayerComponent abilityPlayerComponent = (NoellesRolesAbilityPlayerComponent) NoellesRolesAbilityPlayerComponent.KEY
                    .get(context.player());

            if (payload.player() == null)
                return;
            if (abilityPlayerComponent.cooldown > 0)
                return;
            if (context.player().level().getPlayerByUUID(payload.player()) == null)
                return;

            if (gameWorldComponent.isRole(context.player(), ModRoles.VOODOO)) {
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                        NoellesRolesConfig.HANDLER.instance().voodooCooldown);
                abilityPlayerComponent.sync();
                VoodooPlayerComponent voodooPlayerComponent = (VoodooPlayerComponent) VoodooPlayerComponent.KEY
                        .get(context.player());
                voodooPlayerComponent.setTarget(payload.player());

            }
            if (gameWorldComponent.isRole(context.player(), ModRoles.MORPHLING)) {
                MorphlingPlayerComponent morphlingPlayerComponent = (MorphlingPlayerComponent) MorphlingPlayerComponent.KEY
                        .get(context.player());
                morphlingPlayerComponent.startMorph(payload.player());
            }
        });

        // 操纵师数据包处理
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.MANIPULATOR_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(context.player().level());
            NoellesRolesAbilityPlayerComponent abilityPlayerComponent = (NoellesRolesAbilityPlayerComponent) NoellesRolesAbilityPlayerComponent.KEY
                    .get(context.player());

            if (payload.player() == null)
                return;
            if (abilityPlayerComponent.cooldown > 0)
                return;
            if (context.player().level().getPlayerByUUID(payload.player()) == null)
                return;

            if (gameWorldComponent.isRole(context.player(), ModRoles.MANIPULATOR)) {
                // 设置操纵师的冷却时间（根据配置）
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                        NoellesRolesConfig.HANDLER.instance().manipulatorCooldown);
                abilityPlayerComponent.sync();

                // 获取操纵师组件并设置目标
                ManipulatorPlayerComponent manipulatorPlayerComponent = (ManipulatorPlayerComponent) ManipulatorPlayerComponent.KEY
                        .get(context.player());
                manipulatorPlayerComponent.setTarget(payload.player());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.VULTURE_PACKET, (payload, context) -> {
            final var player = context.player();
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(player.level());
            NoellesRolesAbilityPlayerComponent abilityPlayerComponent = (NoellesRolesAbilityPlayerComponent) NoellesRolesAbilityPlayerComponent.KEY
                    .get(player);

            if (gameWorldComponent.isRole(player, ModRoles.VULTURE)
                    && GameFunctions.isPlayerAliveAndSurvival(player)) {
                if (abilityPlayerComponent.cooldown > 0)
                    return;
                abilityPlayerComponent.sync();
                List<PlayerBodyEntity> playerBodyEntities = player.level().getEntities(
                        EntityTypeTest.forExactClass(PlayerBodyEntity.class), player.getBoundingBox().inflate(10),
                        (playerBodyEntity -> {
                            return playerBodyEntity.getUUID().equals(payload.playerBody());
                        }));
                if (!playerBodyEntities.isEmpty()) {
                    BodyDeathReasonComponent bodyDeathReasonComponent = BodyDeathReasonComponent.KEY
                            .get(playerBodyEntities.getFirst());
                    if (!bodyDeathReasonComponent.vultured) {
                        abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                                NoellesRolesConfig.HANDLER.instance().vultureEatCooldown);
                        VulturePlayerComponent vulturePlayerComponent = VulturePlayerComponent.KEY
                                .get(player);
                        vulturePlayerComponent.bodiesEaten++;
                        vulturePlayerComponent.sync();
                        player.playSound(SoundEvents.PLAYER_BURP, 1.0F, 0.5F);
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
                        if (vulturePlayerComponent.bodiesEaten >= vulturePlayerComponent.bodiesRequired) {
                            ArrayList<Role> shuffledKillerRoles = new ArrayList<>(Noellesroles.getEnableKillerRoles());
                            shuffledKillerRoles.removeIf(role -> role.identifier().equals(ModRoles.EXECUTIONER_ID)
                                    || role.identifier().equals(ModRoles.POISONER_ID)
                                    || Harpymodloader.VANNILA_ROLES.contains(role) || !role.canUseKiller()
                                    || HarpyModLoaderConfig.HANDLER.instance().disabled
                                            .contains(role.identifier().getPath()));
                            if (shuffledKillerRoles.isEmpty())
                                shuffledKillerRoles.add(TMMRoles.KILLER);
                            Collections.shuffle(shuffledKillerRoles);

                            PlayerShopComponent playerShopComponent = (PlayerShopComponent) PlayerShopComponent.KEY
                                    .get(player);
                            final var first = shuffledKillerRoles.getFirst();
                            // gameWorldComponent.addRole(player, first);
                            // ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player,
                            // first);
                            RoleUtils.changeRole(player, first);
                            playerShopComponent.setBalance(100);

                            RoleUtils.sendWelcomeAnnouncement(player);
                        }

                        bodyDeathReasonComponent.vultured = true;
                        bodyDeathReasonComponent.sync();
                    }
                }

            }
        });
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.SWAP_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(context.player().level());
            if (gameWorldComponent.isRole(context.player(), ModRoles.SWAPPER)) {
                NoellesRolesAbilityPlayerComponent abilityPlayerComponent = NoellesRolesAbilityPlayerComponent.KEY
                        .get(context.player());
                if (!abilityPlayerComponent.canUseAbility())
                    return;

                if (payload.player() != null && payload.player2() != null) {
                    if (context.player().level().getPlayerByUUID(payload.player()) != null &&
                            context.player().level().getPlayerByUUID(payload.player2()) != null) {

                        SwapperPlayerComponent swapperComponent = ModComponents.SWAPPER.get(context.player());
                        if (!swapperComponent.isSwapping) {
                            swapperComponent.startSwap(payload.player(), payload.player2());
                        }
                    }
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.EXECUTIONER_SELECT_TARGET_PACKET,
                (payload, context) -> {
                    // 检查是否启用了手动选择目标功能
                    if (!NoellesRolesConfig.HANDLER.instance().executionerCanSelectTarget) {
                        return; // 如果未启用，则忽略该数据包
                    }

                    GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                            .get(context.player().level());
                    if (gameWorldComponent.isRole(context.player(), ModRoles.EXECUTIONER)) {
                        ExecutionerPlayerComponent executionerPlayerComponent = ExecutionerPlayerComponent.KEY
                                .get(context.player());
                        if (executionerPlayerComponent.targetSelected)
                            return;

                        if (payload.target() != null) {
                            Player targetPlayer = context.player().level().getPlayerByUUID(payload.target());
                            if (targetPlayer != null && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                                if (gameWorldComponent.getRole(targetPlayer).isInnocent()) {
                                    executionerPlayerComponent.setTarget(payload.target());
                                } else {
                                    context.player().displayClientMessage(
                                            Component.translatable("message.error.executioner.invalid_target"), true);
                                }
                            } else {
                                context.player().displayClientMessage(
                                        Component.translatable("message.error.executioner.target_not_found"), true);
                            }
                        }
                    }
                });
        ServerPlayNetworking.registerGlobalReceiver(GamblerSelectRoleC2SPacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                GamblerPlayerComponent component = GamblerPlayerComponent.KEY.get(context.player());
                component.selectRole(payload.roleId());
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(org.agmas.noellesroles.packet.BroadcasterC2SPacket.ID,
                (payload, context) -> {
                    NoellesRolesAbilityPlayerComponent abilityPlayerComponent = NoellesRolesAbilityPlayerComponent.KEY
                            .get(context.player());
                    GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(context.player().level());
                    PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());
                    if (!GameFunctions.isPlayerAliveAndSurvival(context.player())) {
                        context.player().displayClientMessage(
                                Component.translatable("message.noellesroles.fuck_death_send"),
                                true);
                        return;
                    }
                    if (gameWorldComponent.isRole(context.player(), ModRoles.BROADCASTER)) {
                        BroadcasterPlayerComponent comp = BroadcasterPlayerComponent.KEY.get(context.player());
                        String message = payload.message();
                        boolean onlySave = payload.onlySave();
                        if (onlySave) {
                            comp.setStoredStr(message);
                            return;
                        }
                        if (playerShopComponent.balance < 100) {
                            context.player().displayClientMessage(
                                    Component.translatable("message.noellesroles.insufficient_funds"),
                                    true);
                            comp.setStoredStr(message);
                            if (context.player() instanceof ServerPlayer) {
                                ServerPlayer player = (ServerPlayer) context.player();
                                player.connection.send(new ClientboundSoundPacket(
                                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY_FAIL),
                                        SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F,
                                        0.9F + player.getRandom().nextFloat() * 0.2F, player.getRandom().nextLong()));
                            }
                            return;
                        }
                        if (message.length() > 256) {
                            message = message.substring(0, 256);
                        }
                        if (comp != null) {
                            comp.setStoredStr("");
                        }
                        playerShopComponent.balance -= 100;
                        playerShopComponent.sync();

                        for (ServerPlayer player : Objects.requireNonNull(context.player().getServer())
                                .getPlayerList().getPlayers()) {
                            org.agmas.noellesroles.packet.BroadcastMessageS2CPacket packet = new org.agmas.noellesroles.packet.BroadcastMessageS2CPacket(
                                    Component.translatable("message.noellesroles.broadcaster.general",
                                            Component.literal(message).withStyle(ChatFormatting.WHITE))
                                            .withStyle(ChatFormatting.GREEN));
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, packet);
                        }
                        abilityPlayerComponent.cooldown = 0;
                        abilityPlayerComponent.sync();
                    }
                });

        ServerPlayNetworking.registerGlobalReceiver(ModPackets.ABILITY_PACKET, (payload, context) -> {
            AbilityHandler.handler(payload, context);
        });
        ServerPlayNetworking.registerGlobalReceiver(AbilityWithTargetC2SPacket.ID, (payload, context) -> {
            AbilityHandler.handlerWithTarget(payload, context);
        });
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.INSANE_KILLER_ABILITY_PACKET, (payload, context) -> {
            ServerPlayer player = (ServerPlayer) context.player();
            InsaneKillerPlayerComponent component = InsaneKillerPlayerComponent.KEY.get(player);

            // 检查冷却
            if (component.cooldown > 0 && !component.isActive)
                return;

            component.toggleAbility();
            component.sync();
        });
        ServerPlayNetworking.registerGlobalReceiver(RecorderC2SPacket.TYPE, RecorderC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(ModPackets.MONITOR_MARK_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(context.player().level());
            if (gameWorldComponent.isRole(context.player(), ModRoles.MONITOR)) {
                MonitorPlayerComponent monitorComponent = MonitorPlayerComponent.KEY.get(context.player());

                // 检查冷却
                if (monitorComponent.canUseAbility()) {
                    if (payload.target() != null) {
                        Player targetPlayer = context.player().level().getPlayerByUUID(payload.target());
                        if (targetPlayer != null && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                            // 标记目标
                            monitorComponent.markTarget(payload.target());

                            // 发送成功消息
                            context.player().displayClientMessage(
                                    Component
                                            .translatable("message.noellesroles.monitor.marked",
                                                    targetPlayer.getName().getString())
                                            .withStyle(ChatFormatting.AQUA),
                                    true);
                        } else {
                            context.player().displayClientMessage(
                                    Component.translatable("message.noellesroles.monitor.target_not_found"), true);
                        }
                    }
                } else {
                    // 冷却中
                    context.player().displayClientMessage(
                            Component.translatable("message.noellesroles.monitor.cooldown",
                                    String.format("%.1f", monitorComponent.getCooldownSeconds())),
                            true);
                }
            }
        });
    }

}
