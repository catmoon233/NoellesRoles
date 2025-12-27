package org.agmas.noellesroles.item;

import org.agmas.noellesroles.ModItems;
import  org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.block_entity.DoorBlockEntity;
import dev.doctor4t.trainmurdermystery.block_entity.SmallDoorBlockEntity;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
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
 * 加固门道具
 * - 工程师商店物品（所有人可使用）
 * - 在商店以75金币购买
 * - 右键门：使门能够防御一次撬棍攻击
 * - 蹲下右键被卡住的门：解除卡住状态
 * - 蹲下右键已加固/有警报的门（工程师专属）：取下对应道具
 */
public class ReinforcementItem extends Item implements AdventureUsable {
    
    public ReinforcementItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        
        if (player == null) return ActionResult.PASS;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(world);
        boolean isEngineer = gameWorld.isRole(player, ModRoles.ENGINEER);
        
        // 检查是否为门方块
        if (state.getBlock() instanceof SmallDoorBlock) {
            BlockPos lowerPos = state.get(SmallDoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.down();
            
            if (world.getBlockEntity(lowerPos) instanceof SmallDoorBlockEntity doorEntity) {
                // 蹲下右键：工程师专属功能 - 解除卡住状态或取下道具
                if (player.isSneaking()) {
                    // 优先检查是否要解除卡住状态
                    if (doorEntity.isJammed()) {
                        // 解除卡住
                        doorEntity.setJammed(0);
                        
                        if (!world.isClient) {
                            world.playSound(null, lowerPos.getX() + 0.5, lowerPos.getY() + 1, lowerPos.getZ() + 0.5,
                                SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.2f);
                            player.sendMessage(Text.translatable("message.noellesroles.engineer.unjammed")
                                .formatted(Formatting.GREEN), true);
                        }
                        
                        // 不消耗物品
                        return ActionResult.SUCCESS;
                    }
                    
                    // 工程师专属：取下门上的道具
                    if (isEngineer) {
                        // 检查是否有加固
                        if (isDoorReinforced(doorEntity)) {
                            setDoorReinforced(doorEntity, false);
                            
                            if (!world.isClient) {
                                world.playSound(null, lowerPos.getX() + 0.5, lowerPos.getY() + 1, lowerPos.getZ() + 0.5,
                                    SoundEvents.BLOCK_ANVIL_HIT, SoundCategory.BLOCKS, 0.5f, 1.2f);
                                player.sendMessage(Text.translatable("message.noellesroles.engineer.removed_reinforcement")
                                    .formatted(Formatting.GREEN), true);
                                // 返还加固物品
                                player.giveItemStack(new ItemStack(ModItems.REINFORCEMENT));
                            }
                            return ActionResult.SUCCESS;
                        }
                        
                        // 检查是否有警报陷阱
                        if (AlarmTrapItem.hasDoorAlarmTrap(doorEntity)) {
                            AlarmTrapItem.setDoorAlarmTrap(doorEntity, false);
                            
                            if (!world.isClient) {
                                world.playSound(null, lowerPos.getX() + 0.5, lowerPos.getY() + 1, lowerPos.getZ() + 0.5,
                                    SoundEvents.BLOCK_TRIPWIRE_DETACH, SoundCategory.BLOCKS, 0.7f, 1.2f);
                                player.sendMessage(Text.translatable("message.noellesroles.engineer.removed_alarm")
                                    .formatted(Formatting.GREEN), true);
                                // 返还警报陷阱物品
                                player.giveItemStack(new ItemStack(ModItems.ALARM_TRAP));
                            }
                            return ActionResult.SUCCESS;
                        }
                        
                        // 没有可取下的道具
                        if (!world.isClient) {
                            player.sendMessage(Text.translatable("message.noellesroles.engineer.nothing_to_remove")
                                .formatted(Formatting.YELLOW), true);
                        }
                        return ActionResult.FAIL;
                    } else {
                        // 非工程师蹲下右键时
                        if (!world.isClient) {
                            player.sendMessage(Text.translatable("message.noellesroles.engineer.not_jammed")
                                .formatted(Formatting.YELLOW), true);
                        }
                        return ActionResult.FAIL;
                    }
                }
                
                // 普通右键：加固门
                // 门已被撬棍破坏，无法加固
                if (doorEntity.isBlasted()) {
                    if (!world.isClient) {
                        player.sendMessage(Text.translatable("message.noellesroles.engineer.already_broken")
                            .formatted(Formatting.RED), true);
                    }
                    return ActionResult.FAIL;
                }
                
                // 检查门是否已被加固
                if (isDoorReinforced(doorEntity)) {
                    if (!world.isClient) {
                        player.sendMessage(Text.translatable("message.noellesroles.engineer.already_reinforced")
                            .formatted(Formatting.YELLOW), true);
                    }
                    return ActionResult.FAIL;
                }
                
                // 加固门
                setDoorReinforced(doorEntity, true);
                
                if (!world.isClient) {
                    world.playSound(null, lowerPos.getX() + 0.5, lowerPos.getY() + 1, lowerPos.getZ() + 0.5,
                        SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.5f, 1.5f);
                    player.sendMessage(Text.translatable("message.noellesroles.engineer.reinforced")
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
     * 检查门是否已被加固
     * 我们使用 keyName 字段来存储加固状态（如果以 "reinforced:" 开头则表示已加固）
     */
    public static boolean isDoorReinforced(DoorBlockEntity doorEntity) {
        String keyName = doorEntity.getKeyName();
        return keyName != null && keyName.startsWith("reinforced:");
    }
    
    /**
     * 设置门的加固状态
     */
    public static void setDoorReinforced(DoorBlockEntity doorEntity, boolean reinforced) {
        String currentKeyName = doorEntity.getKeyName();
        if (reinforced) {
            if (!isDoorReinforced(doorEntity)) {
                doorEntity.setKeyName("reinforced:" + (currentKeyName != null ? currentKeyName : ""));
            }
        } else {
            if (isDoorReinforced(doorEntity) && currentKeyName != null) {
                doorEntity.setKeyName(currentKeyName.substring(11)); // 移除 "reinforced:" 前缀
            }
        }
    }
    
    /**
     * 消耗一次加固（被撬棍使用时调用）
     * @return true 如果成功消耗了加固（门不会被破坏），false 如果没有加固
     */
    public static boolean consumeReinforcement(DoorBlockEntity doorEntity) {
        if (isDoorReinforced(doorEntity)) {
            setDoorReinforced(doorEntity, false);
            return true;
        }
        return false;
    }
    
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.noellesroles.reinforcement.tooltip")
            .formatted(Formatting.GRAY));
    }
}