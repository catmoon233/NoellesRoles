package org.agmas.noellesroles.item;


import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.SlipperyGhostPlayerComponent;

/**
 * 空包弹物品
 * - 滑头鬼专属道具
 * - 右键对目标玩家使用
 * - 使目标手中的枪进入30秒冷却
 * - 使用后消耗
 */
public class BlankCartridgeItem extends Item {
    
    // 冷却时间: 30秒 = 600 ticks
    private static final int GUN_COOLDOWN_TICKS = 600;
    
    public BlankCartridgeItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }
        
        // 检查目标是否为玩家
        if (!(entity instanceof ServerPlayerEntity target)) {
            return ActionResult.PASS;
        }
        
        // 检查冷却
        SlipperyGhostPlayerComponent ghostComp = ModComponents.SLIPPERY_GHOST.get(user);
        if (ghostComp.isBlankCartridgeOnCooldown()) {
            user.sendMessage(Text.literal("空包弹冷却中！剩余 " + ghostComp.getBlankCartridgeCooldownSeconds() + " 秒").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        
        // 检查目标是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(target)) {
            return ActionResult.FAIL;
        }
        
        // 获取目标手中的物品，检查是否为枪械
        ItemStack targetMainHand = target.getStackInHand(Hand.MAIN_HAND);
        ItemStack targetOffHand = target.getStackInHand(Hand.OFF_HAND);
        
        boolean appliedCooldown = false;
        
        // 检查主手是否为枪械
        if (isGun(targetMainHand)) {
            target.getItemCooldownManager().set(targetMainHand.getItem(), GUN_COOLDOWN_TICKS);
            appliedCooldown = true;
        }
        
        // 检查副手是否为枪械
        if (isGun(targetOffHand)) {
            target.getItemCooldownManager().set(targetOffHand.getItem(), GUN_COOLDOWN_TICKS);
            appliedCooldown = true;
        }
        
        if (appliedCooldown) {
            // 播放音效
            user.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 0.5F);
            
            // 通知目标
            target.sendMessage(Text.literal("你的枪械被空包弹干扰，进入冷却状态！").formatted(Formatting.RED), true);
            
            // 通知使用者
            user.sendMessage(Text.literal("成功使 " + target.getName().getString() + " 的枪械进入冷却！").formatted(Formatting.GREEN), true);
            
            // 消耗物品
            stack.decrementUnlessCreative(1, user);
            
            // 设置冷却
            ghostComp.setBlankCartridgeCooldown();
            
            return ActionResult.SUCCESS;
        } else {
            // 目标没有枪械
            user.sendMessage(Text.literal("目标没有持有枪械！").formatted(Formatting.YELLOW), true);
            return ActionResult.FAIL;
        }
    }
    
    /**
     * 检查物品是否为枪械
     */
    private boolean isGun(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        // 检查是否为原版枪械
        Item item = stack.getItem();
        return item == TMMItems.REVOLVER || item == TMMItems.DERRINGER;
    }
}