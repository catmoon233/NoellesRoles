package org.agmas.noellesroles.role;

import org.agmas.noellesroles.component.MaChenXuPlayerComponent;
import org.agmas.noellesroles.component.ModComponents;

import dev.doctor4t.trainmurdermystery.api.NormalRole;
import dev.doctor4t.trainmurdermystery.index.TMMItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 马晨絮角色 - 四段杀手
 * 
 * 特性：
 * - 恐惧机制：好人在附近时持续掉落SAN值
 * - 四段成长：通过累计SAN掉落解锁更强形态
 * - 里世界机制：全图好人掉SAN，添加黑暗效果
 * - 魂噬：可击杀低SAN玩家
 */
public class MaChenXuRole extends NormalRole {

    public MaChenXuRole(ResourceLocation identifier, int color, boolean isInnocent, boolean canUseKiller,
            MoodType moodType, int maxSprintTime, boolean canSeeTime) {
        super(identifier, color, isInnocent, canUseKiller, moodType, maxSprintTime, canSeeTime);
    }

    @Override
    public void onFinishQuest(Player player, String quest) {
        // 当完成任务时，可以给马晨絮一些奖励或触发特殊效果
        MaChenXuPlayerComponent component = ModComponents.MA_CHEN_XU.get(player);
        if (component != null) {
            // 可以在这里添加任务完成的特殊逻辑
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.noellesroles.ma_chen_xu.quest_completed")
                                .withStyle(ChatFormatting.DARK_PURPLE),
                        true);
            }
        }
    }
}