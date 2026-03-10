package org.agmas.noellesroles.roles.hoan_meirin;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;

public class FistPunchHandler {

    /**
     * 记录每个攻击者（UUID）当前的 [目标UUID, 连击次数]
     */
    public static final Map<UUID, PunchRecord> PUNCH_RECORDS = new HashMap<>();

    /**
     * 触发致死所需的连击次数
     */
    private static final int KILL_THRESHOLD = 3;

    public static void register() {
        // 监听实体受到伤害事件（服务端）
        AttackEntityCallback.EVENT.register(FistPunchHandler::onEntityDamaged);
    }

    /**
     * 当一个 LivingEntity 受到伤害时触发。
     * 判断攻击者是否为玩家、是否空手，然后处理连击逻辑。
     *
     * @return true = 允许原版伤害照常处理
     *         false = 取消原版伤害（第3拳时我们自己处理死亡）
     */
    public static InteractionResult onEntityDamaged(Player attacker, Level level, InteractionHand hand, Entity entity,
            EntityHitResult hitResult) {
        // 仅在服务端处理，且攻击者必须是玩家
        var gameWorldComponent = GameWorldComponent.KEY.get(level);
        if (!GameFunctions.isPlayerAliveAndSurvival(attacker))
            return InteractionResult.PASS;
        if (!(entity instanceof Player victim)) {
            return InteractionResult.PASS;
        }
        if (!gameWorldComponent.isRole(attacker, ModRoles.HOAN_MEIRIN))
            return InteractionResult.PASS;

        // 必须空手（主手持空气）
        ItemStack mainHand = attacker.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return InteractionResult.PASS;
        }

        // 仅对 LivingEntity（含玩家、生物）生效，排除自伤
        if (attacker.getUUID().equals(entity.getUUID())) {
            return InteractionResult.PASS;
        }

        UUID attackerUUID = attacker.getUUID();
        UUID victimUUID = victim.getUUID();

        PunchRecord record = PUNCH_RECORDS.computeIfAbsent(attackerUUID, k -> new PunchRecord());

        // 如果目标换人，重置计数
        if (!victimUUID.equals(record.targetUUID)) {
            record.targetUUID = victimUUID;
            record.count = 0;
        }

        record.count++;

        int remaining = KILL_THRESHOLD - record.count;

        if (record.count >= KILL_THRESHOLD) {
            // 第3拳：清除记录并强制击杀目标
            PUNCH_RECORDS.remove(attackerUUID);

            // 取消普通伤害，改用即死逻辑
            // 使用 hurt + setHealth(0) 确保触发死亡事件和战利品
            GameFunctions.killPlayer(victim, true, attacker, Noellesroles.id("hoan_meirin_attack"));
            victim.hurt(
                    victim.damageSources().playerAttack(attacker),
                    victim.getMaxHealth() * 100f // 超大伤害确保击杀
            );

            sendActionBar(attacker,
                    Component.translatable("message.hoan_meirin_attack.3").withStyle(ChatFormatting.RED));

            // 返回 false 取消本次原版伤害（我们已经用新的 hurt 替代）
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        } else {
            // 第1、2拳：允许原版伤害，并提示剩余次数
            ChatFormatting color = remaining == 1 ? ChatFormatting.YELLOW : ChatFormatting.GREEN;
            sendActionBar(attacker,
                    Component.translatable("message.hoan_meirin_attack.general", Component
                            .translatable("[%s/%s]", record.count, KILL_THRESHOLD).withStyle(ChatFormatting.WHITE))
                            .withStyle(color));
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
    }

    /**
     * 向玩家发送 Action Bar 消息（HUD中间底部）
     */
    private static void sendActionBar(Player player, Component message) {
        player.displayClientMessage(
                message,
                true // true = action bar，false = chat
        );
    }

    /**
     * 存储单个攻击者的连击状态
     */
    private static class PunchRecord {
        UUID targetUUID = null;
        int count = 0;
    }
}
