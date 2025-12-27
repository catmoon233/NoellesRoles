package org.agmas.noellesroles.item;

import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.block_entity.DoorBlockEntity;
import dev.doctor4t.trainmurdermystery.block_entity.SmallDoorBlockEntity;
import dev.doctor4t.trainmurdermystery.util.AdventureUsable;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * 警报陷阱物品
 * - 工程师商店物品（所有人可使用）
 * - 在商店以150金币购买
 * - 右键门：在门上放置警报陷阱
 * - 当撬棍使用时触发，发出响亮的警报声
 */
public class AlarmTrapItem extends Item implements AdventureUsable {
    
    public AlarmTrapItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        
        if (player == null) return ActionResult.PASS;
        
        // 检查是否为门方块
        if (state.getBlock() instanceof SmallDoorBlock) {
            BlockPos lowerPos = state.get(SmallDoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
            
            if (world.getBlockEntity(lowerPos) instanceof SmallDoorBlockEntity doorEntity) {
                // 检查门是否已被破坏
                if (doorEntity.isBlasted()) {
                    if (!world.isClient) {
                        player.sendMessage(Text.translatable("message.noellesroles.engineer.already_broken")
                            .formatted(Formatting.RED), true);
                    }
                    return ActionResult.FAIL;
                }
                
                // 检查门是否已有警报陷阱
                if (hasDoorAlarmTrap(doorEntity)) {
                    if (!world.isClient) {
                        player.sendMessage(Text.translatable("message.noellesroles.engineer.already_trapped")
                            .formatted(Formatting.YELLOW), true);
                    }
                    return ActionResult.FAIL;
                }
                
                // 放置警报陷阱
                setDoorAlarmTrap(doorEntity, true);
                
                if (!world.isClient) {
                    world.playSound(null, lowerPos.getX() + 0.5, lowerPos.getY() + 1, lowerPos.getZ() + 0.5,
                        SoundEvents.BLOCK_TRIPWIRE_ATTACH, SoundCategory.BLOCKS, 0.7f, 1.2f);
                    player.sendMessage(Text.translatable("message.noellesroles.engineer.trap_placed")
                        .formatted(Formatting.GREEN), true);
                }
                
                // 消耗物品
                if (!player.isCreative()) {
                    context.getStack().decrement(1);
                }
                
                return ActionResult.SUCCESS;
            }
        }
        
        return ActionResult.PASS;
    }
    
    /**
     * 检查门是否有警报陷阱
     * 我们使用 keyName 字段来存储陷阱状态（如果包含 "alarmed:" 则表示有陷阱）
     */
    public static boolean hasDoorAlarmTrap(DoorBlockEntity doorEntity) {
        String keyName = doorEntity.getKeyName();
        return keyName != null && keyName.contains("alarmed:");
    }
    
    /**
     * 设置门的警报陷阱状态
     */
    public static void setDoorAlarmTrap(DoorBlockEntity doorEntity, boolean trapped) {
        String currentKeyName = doorEntity.getKeyName();
        if (currentKeyName == null) currentKeyName = "";
        
        if (trapped) {
            if (!hasDoorAlarmTrap(doorEntity)) {
                doorEntity.setKeyName(currentKeyName + "alarmed:");
            }
        } else {
            if (hasDoorAlarmTrap(doorEntity)) {
                doorEntity.setKeyName(currentKeyName.replace("alarmed:", ""));
            }
        }
    }
    
    /**
     * 触发警报陷阱（被撬棍使用时调用）
     * @return true 如果触发了警报
     */
    public static boolean triggerAlarmTrap(DoorBlockEntity doorEntity, World world) {
        if (hasDoorAlarmTrap(doorEntity)) {
            // 移除陷阱（一次性）
            setDoorAlarmTrap(doorEntity, false);
            
            // 播放响亮的警报声
            if (!world.isClient) {
                BlockPos pos = doorEntity.getPos();
                // 播放多个叠加的声音让警报更响亮
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 3.0f, 0.8f);
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.BLOCKS, 3.0f, 0.5f);
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    SoundEvents.ENTITY_WARDEN_ROAR, SoundCategory.BLOCKS, 1.5f, 2.0f);
            }
            
            return true;
        }
        return false;
    }
    
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.noellesroles.alarm_trap.tooltip")
            .formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("item.noellesroles.alarm_trap.tooltip2")
            .formatted(Formatting.GRAY));
    }
}