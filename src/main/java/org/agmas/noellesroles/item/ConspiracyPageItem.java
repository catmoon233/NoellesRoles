package org.agmas.noellesroles.item;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * 阴谋之书页物品
 *
 * 功能：
 * - 阴谋家专属物品
 * - 右键打开玩家/角色选择 GUI
 * - 使用后消耗
 */
public class ConspiracyPageItem extends Item {
    
    // 静态回调，由客户端设置用于打开GUI
    public static Runnable openScreenCallback = null;
    
    public ConspiracyPageItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        
        // 验证：玩家必须存活
        if (!GameFunctions.isPlayerAliveAndSurvival(user)) {
            return TypedActionResult.fail(stack);
        }
        
        // 客户端：打开GUI
        if (world.isClient()) {
            if (openScreenCallback != null) {
                openScreenCallback.run();
            }
        }
        
        // 返回 success 但不消耗物品，等猜测完成后再消耗
        return TypedActionResult.success(stack, world.isClient());
    }
}