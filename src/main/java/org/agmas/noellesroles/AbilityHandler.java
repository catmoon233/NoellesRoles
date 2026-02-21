package org.agmas.noellesroles;

import org.agmas.noellesroles.component.BomberPlayerComponent;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.NianShouPlayerComponent;
import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.entity.WheelchairEntity;
import org.agmas.noellesroles.packet.AbilityC2SPacket;
import org.agmas.noellesroles.packet.AbilityWithTargetC2SPacket;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.commander.CommanderHandler;
import org.agmas.noellesroles.roles.fortuneteller.FortunetellerPlayerComponent;
import org.agmas.noellesroles.roles.noise_maker.NoiseMakerPlayerComponent;
import org.agmas.noellesroles.roles.recaller.RecallerPlayerComponent;
import org.agmas.noellesroles.utils.RoleUtils;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class AbilityHandler {

    public static void handler(AbilityC2SPacket payload, Context context) {
        // 通用技能服务端处理
        NoellesRolesAbilityPlayerComponent abilityPlayerComponent = (NoellesRolesAbilityPlayerComponent) NoellesRolesAbilityPlayerComponent.KEY
                .get(context.player());
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                .get(context.player().level());
        final ServerPlayer player = context.player();
        if (gameWorldComponent.isRole(context.player(), ModRoles.CLEANER)) {
            if (abilityPlayerComponent.cooldown > 0) {
                context.player().displayClientMessage(Component.translatable(
                        "message.noellesroles.cleaner.cooldown", abilityPlayerComponent.cooldown / 20)
                        .withStyle(ChatFormatting.RED), true);
            } else {
                var items = player.level().getEntitiesOfClass(ItemEntity.class,
                        player.getBoundingBox().inflate(5.), (p) -> true);
                for (var it : items) {
                    it.discard();
                }
                player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1f,
                        1f);
                context.player().displayClientMessage(Component.translatable(
                        "message.noellesroles.cleaner.cleanned", items.size())
                        .withStyle(ChatFormatting.GOLD), true);
                abilityPlayerComponent.setCooldown(90);
            }
            return;
        }
        if (gameWorldComponent.isRole(context.player(), ModRoles.GLITCH_ROBOT)) {
            if (!RoleUtils.isPlayerHasFreeSlot(context.player())) {
                context.player().displayClientMessage(
                        Component.translatable("message.hotbar.full").withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!context.player().getSlot(103).get().is(ModItems.NIGHT_VISION_GLASSES)) {
                context.player().displayClientMessage(
                        Component.translatable("info.glitch_robot.noglasses_on_head").withStyle(ChatFormatting.RED),
                        true);
                return;
            }
            RoleUtils.insertStackInFreeSlot(context.player(), context.player().getSlot(103).get().copy());
            // RoleUtils.removeStackItem(context.player(), 103);
            context.player().getInventory().armor.set(3, ItemStack.EMPTY);
            context.player().displayClientMessage(
                    Component.translatable("info.glitch_robot.take_off_glasses.success")
                            .withStyle(ChatFormatting.GREEN),
                    true);
            context.player().removeEffect(MobEffects.NIGHT_VISION);
            return;
        }
        if (gameWorldComponent.isRole(context.player(), ModRoles.COMMANDER)) {
            CommanderHandler.tryActiveAbility(context.player());
            return;
        }
        if (gameWorldComponent.isRole(context.player(), ModRoles.ATTENDANT)) {
            if (abilityPlayerComponent.cooldown > 0) {
                context.player().displayClientMessage(Component.translatable(
                        "message.noellesroles.attendant.cooldown", abilityPlayerComponent.cooldown / 20)
                        .withStyle(ChatFormatting.RED), true);
                return;
            }
            if (!context.player().isCreative())
                abilityPlayerComponent.setCooldown(60 * 20);
            AttendantHandler.openLight(context.player());
            return;
        }
        if (gameWorldComponent.isRole(context.player(), ModRoles.BOMBER)) {
            BomberPlayerComponent bomberPlayerComponent = ModComponents.BOMBER.get(context.player());
            bomberPlayerComponent.buyBomb();
            return;
        }
        if (gameWorldComponent.isRole(context.player(), ModRoles.NOISEMAKER)) {
            NoiseMakerPlayerComponent noiseMakerPlayerComponent = ModComponents.NOISEMAKER.get(context.player());
            noiseMakerPlayerComponent.useAbility();
            return;
        }
        if (gameWorldComponent.isRole(context.player(), ModRoles.GHOST)) {
            org.agmas.noellesroles.roles.ghost.GhostPlayerComponent ghostPlayerComponent = org.agmas.noellesroles.roles.ghost.GhostPlayerComponent.KEY
                    .get(context.player());
            ghostPlayerComponent.useAbility();
            return;
        }

        if (gameWorldComponent.isRole(context.player(), ModRoles.RECALLER)
                && abilityPlayerComponent.cooldown <= 0) {
            RecallerPlayerComponent recallerPlayerComponent = RecallerPlayerComponent.KEY.get(context.player());
            PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());
            if (!recallerPlayerComponent.placed) {
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                        NoellesRolesConfig.HANDLER.instance().recallerMarkCooldown);
                recallerPlayerComponent.setPosition();
            } else if (playerShopComponent.balance >= 100) {
                playerShopComponent.balance -= 100;
                playerShopComponent.sync();
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                        NoellesRolesConfig.HANDLER.instance().recallerTeleportCooldown);
                recallerPlayerComponent.teleport();
            }

        }
        if (gameWorldComponent.isRole(context.player(), ModRoles.OLDMAN)) {
            if (player.getVehicle() != null && player.getVehicle() instanceof WheelchairEntity) {
                player.getVehicle().discard();
                RoleUtils.insertStackInFreeSlot(player, ModItems.WHEELCHAIR.getDefaultInstance());
                player.stopRiding();
                player.displayClientMessage(
                        Component.translatable("message.oldman.get_back").withStyle(ChatFormatting.GOLD), true);
            }
            return;
        }
        if (gameWorldComponent.isRole(context.player(), ModRoles.PHANTOM)) {
            if (abilityPlayerComponent.cooldown <= 0) {
                context.player().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
                        NoellesRolesConfig.HANDLER.instance().phantomInvisibilityDuration * 20, 0, true, false,
                        true));
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                        NoellesRolesConfig.HANDLER.instance().phantomInvisibilityCooldown);
            } else {
                var effectINVISIBILITY = context.player().getEffect(MobEffects.INVISIBILITY);
                if (effectINVISIBILITY != null) {
                    if (effectINVISIBILITY.getDuration() > 0) {
                        context.player().removeEffect(MobEffects.INVISIBILITY);
                        context.player().displayClientMessage(
                                Component.translatable("tip.phantom.exited").withStyle(ChatFormatting.YELLOW),
                                true);
                    }
                }
            }

        }
        if (gameWorldComponent.isRole(context.player(), ModRoles.NIAN_SHOU)) {
            var sender = context.player();

            NianShouPlayerComponent nianShouComponent = NianShouPlayerComponent.KEY.get(sender);

            // 简单实现：检查准星对准的玩家
            Player target = null;
            // 由于raycastPlayer方法不存在，使用简化逻辑
            // 获取准星对准的玩家
            double minDistance = 5.0;
            for (Player otherPlayer : sender.level().players()) {
                if (otherPlayer.isSpectator())
                    continue;
                if (otherPlayer.getUUID().equals(sender.getUUID())) {
                    continue; // 不能给自己发红包
                }
                double distance = sender.distanceTo(otherPlayer);
                if (distance <= minDistance) {
                    // 检查是否在准星方向
                    net.minecraft.world.phys.Vec3 eyePos = sender.getEyePosition();
                    net.minecraft.world.phys.Vec3 lookVec = sender.getLookAngle().normalize();
                    net.minecraft.world.phys.Vec3 toTarget = otherPlayer.position().subtract(eyePos).normalize();
                    double dotProduct = lookVec.dot(toTarget);
                    if (dotProduct > 0.8) { // 准星方向大致对准目标
                        if (target == null || distance < sender.distanceTo(target)) {
                            target = otherPlayer;
                        }
                    }
                }
            }

            if (target == null) {
                sender.displayClientMessage(
                        Component.translatable("message.noellesroles.nianshou.no_target")
                                .withStyle(ChatFormatting.RED),
                        true);
                return;
            }

            if (nianShouComponent.getRedPacketCount() <= 0) {
                sender.displayClientMessage(
                        Component.translatable("message.noellesroles.nianshou.no_red_packet")
                                .withStyle(ChatFormatting.RED),
                        true);
                return;
            }

            // 发放红包
            nianShouComponent.useRedPacket();

            // 添加延迟发放计时器
            if (target instanceof ServerPlayer) {
                ConfigWorldComponent configWorld = ConfigWorldComponent.KEY.get(target.level());
                configWorld.addRedPacketTimer(target.getUUID());

                // 提示年兽
                sender.displayClientMessage(
                        Component.translatable("message.noellesroles.nianshou.red_packet_sent", target.getName())
                                .withStyle(ChatFormatting.GOLD),
                        true);
            }
        }
    }

    public static void handlerWithTarget(AbilityWithTargetC2SPacket payload, Context context) {
        NoellesRolesAbilityPlayerComponent abilityPlayerComponent = (NoellesRolesAbilityPlayerComponent) NoellesRolesAbilityPlayerComponent.KEY
                .get(context.player());

        PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY
                .get(context.player());
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                .get(context.player().level());
        final ServerPlayer player = context.player();
        var targetPlayer = player.level().getPlayerByUUID(payload.target());
        if (gameWorldComponent.isRole(player, ModRoles.FORTUNETELLER)) {
            if (abilityPlayerComponent.cooldown > 0) {
                player.displayClientMessage(Component.translatable("message.noellesroles.ability_cooldown"), true);
                return;
            }
            if (targetPlayer != null) {
                if (playerShopComponent.balance >= 150) {
                    playerShopComponent.addToBalance(-150);
                    FortunetellerPlayerComponent.KEY.get(player).protectPlayer(targetPlayer);
                    abilityPlayerComponent.setCooldown(120 * 20);
                } else {
                    player.displayClientMessage(Component.translatable("message.noellesroles.insufficient_funds"),
                            true);
                    return;
                }
            }
            return;
        }
    }
}
