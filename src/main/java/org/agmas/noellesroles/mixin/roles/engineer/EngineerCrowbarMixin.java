package org.agmas.noellesroles.mixin.roles.engineer;


import dev.doctor4t.trainmurdermystery.block_entity.DoorBlockEntity;
import dev.doctor4t.trainmurdermystery.item.CrowbarItem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.agmas.noellesroles.item.AlarmTrapItem;
import org.agmas.noellesroles.item.ReinforcementItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin：拦截撬棍使用，处理工程师的加固门和警报陷阱
 */
@Mixin(CrowbarItem.class)
public class EngineerCrowbarMixin {
    
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void engineer$interceptCrowbar(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        
        if (player == null) return;
        
        // 获取门的 BlockEntity
        BlockEntity entity = world.getBlockEntity(context.getBlockPos());
        if (!(entity instanceof DoorBlockEntity)) {
            entity = world.getBlockEntity(context.getBlockPos().down());
        }
        
        if (entity instanceof DoorBlockEntity door && !door.isBlasted()) {
            // 首先检查警报陷阱 - 无论是否有加固都会触发
            boolean alarmTriggered = AlarmTrapItem.triggerAlarmTrap(door, world);
            
            // 然后检查加固
            if (ReinforcementItem.consumeReinforcement(door)) {
                // 加固被消耗，门不会被破坏
                if (!world.isClient) {
                    // 播放加固被破坏的声音
                    world.playSound(null, context.getBlockPos(),
                        SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0f, 0.8f);
                    
                    // 给使用撬棍的玩家发送消息
                    player.sendMessage(Text.translatable("message.noellesroles.engineer.reinforcement_broken")
                        .formatted(Formatting.YELLOW), true);
                }
                
                // 仍然消耗冷却时间
                if (!player.isCreative()) {
                    player.getItemCooldownManager().set(context.getStack().getItem(), 6000);
                }
                
                // 取消原版行为（门不会被破坏）
                cir.setReturnValue(ActionResult.SUCCESS);
            }
            // 如果没有加固，但触发了警报，继续执行原版撬棍逻辑（门会被破坏）
        }
    }
}