package org.agmas.noellesroles.item;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.util.AdventureUsable;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 绳索
 * <p>
 * - 3点耐久
 * - 右键：将前方直线距离10格内的离你最近的玩家拉到自己身前
 * - 使用后进入20秒冷却并消耗1点耐久
 * </p>
 */
public class RopeItem extends Item implements AdventureUsable {
    private static final int MAX_DURABILITY = 3;
    private static final int COOLDOWN = 20 * 20; // 20秒
    private static final int MAX_DISTANCE = 10; // 最大距离10格

    public RopeItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 检查耐久（耐久值为3时已损坏，不能使用）
        if (stack.getDamageValue() >= MAX_DURABILITY) {
            if (!world.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("item.noellesroles.rope.no_durability")
                                .withStyle(ChatFormatting.RED),
                        true);
            }
            return InteractionResultHolder.fail(stack);
        }

        // 检查冷却
        if (player.getCooldowns().isOnCooldown(this)) {
            if (!world.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("item.noellesroles.rope.on_cooldown")
                                .withStyle(ChatFormatting.RED),
                        true);
            }
            return InteractionResultHolder.fail(stack);
        }

        // 查找前方直线距离10格内最近的玩家
        Player target = findClosestPlayerInView(world, player);

        if (target == null) {
            if (!world.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("item.noellesroles.rope.no_target")
                                .withStyle(ChatFormatting.YELLOW),
                        true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (!world.isClientSide) {
            // 记录物品使用
            if (TMM.REPLAY_MANAGER != null) {
                TMM.REPLAY_MANAGER.recordItemUse(player.getUUID(),
                        BuiltInRegistries.ITEM.getKey(this));
            }

            // 添加冷却和消耗耐久
            if (!player.isCreative()) {
                player.getCooldowns().addCooldown(this, COOLDOWN);
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }

            // 将目标玩家拉到玩家身前
            pullPlayer(player, target);

            // 播放声音
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0f, 1.0f);

            player.displayClientMessage(
                    Component.translatable("item.noellesroles.rope.success")
                            .withStyle(ChatFormatting.GREEN),
                    true);
        }

        return InteractionResultHolder.success(stack);
    }

    /**
     * 查找前方直线距离10格内最近的玩家
     */
    private Player findClosestPlayerInView(Level world, Player player) {
        Player closestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        // 获取玩家视线方向
        var viewVector = player.getViewVector(1.0f);

        for (Player target : world.players()) {
            // 跳过自己
            if (target == player) continue;

            // 跳过不在生存模式的玩家（死亡、观察者等）
            if (!GameFunctions.isPlayerAliveAndSurvival(target)) continue;

            // 计算距离
            double distance = player.distanceTo(target);
            if (distance > MAX_DISTANCE) continue;

            // 计算向量到目标的向量
            var toTarget = target.position().subtract(player.position());

            // 计算向量点积，判断是否在前方视野内
            double dotProduct = viewVector.dot(toTarget.normalize());

            // 如果目标在前方（点积 > 0.7，大约45度角内）
            if (dotProduct > 0.7) {
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = target;
                }
            }
        }

        return closestPlayer;
    }

    /**
     * 将目标玩家拉到玩家身前
     */
    private void pullPlayer(Player player, Player target) {
        // 计算拉到的位置（玩家前方1.5格处）
        var viewVector = player.getViewVector(1.0f);
        double pullDistance = 1.5;

        var targetPos = player.position().add(
                viewVector.x * pullDistance,
                0,
                viewVector.z * pullDistance
        );

        // 传送目标玩家
        if (target instanceof ServerPlayer serverTarget) {
            serverTarget.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        } else {
            target.moveTo(targetPos.x, targetPos.y, targetPos.z);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        tooltip.add(Component.translatable("item.noellesroles.rope.tooltip.durability",
                MAX_DURABILITY - stack.getDamageValue(), MAX_DURABILITY)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.noellesroles.rope.tooltip.use")
                .withStyle(ChatFormatting.AQUA));
    }
}
