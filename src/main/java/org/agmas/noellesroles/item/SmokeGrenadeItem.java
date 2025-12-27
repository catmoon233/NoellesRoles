package org.agmas.noellesroles.item;


import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.agmas.noellesroles.ModEntities;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.SlipperyGhostPlayerComponent;
import org.agmas.noellesroles.entity.SmokeGrenadeEntity;
import org.jetbrains.annotations.NotNull;

/**
 * 烟雾弹物品
 * - 滑头鬼专属道具
 * - 右键丢掷，形成烟雾区域
 * - 进入烟雾的玩家获得失明效果
 * - 如果直接砸中玩家，清空目标的san值（精神值）
 * - 烟雾持续10秒
 */
public class SmokeGrenadeItem extends Item {
    
    public SmokeGrenadeItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        
        // 检查冷却
        if (!world.isClient) {
            SlipperyGhostPlayerComponent ghostComp = ModComponents.SLIPPERY_GHOST.get(user);
            if (ghostComp.isSmokeGrenadeOnCooldown()) {
                user.sendMessage(Text.literal("烟雾弹冷却中！剩余 " + ghostComp.getSmokeGrenadeCooldownSeconds() + " 秒").formatted(Formatting.RED), true);
                return TypedActionResult.fail(itemStack);
            }
        }
        
        // 播放投掷音效
        world.playSound(null, user.getX(), user.getY(), user.getZ(), 
                TMMSounds.ITEM_GRENADE_THROW, SoundCategory.NEUTRAL, 
                0.5F, 1F + (world.random.nextFloat() - .5f) / 10f);
        
        if (!world.isClient) {
            // 创建烟雾弹实体
            SmokeGrenadeEntity smokeGrenade = new SmokeGrenadeEntity(ModEntities.SMOKE_GRENADE, world);
            smokeGrenade.setOwner(user);
            smokeGrenade.setPos(user.getX(), user.getEyeY() - 0.1, user.getZ());
            smokeGrenade.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 0.5F, 1.0F);
            world.spawnEntity(smokeGrenade);
            
            // 设置冷却
            SlipperyGhostPlayerComponent ghostComp = ModComponents.SLIPPERY_GHOST.get(user);
            ghostComp.setSmokeGrenadeCooldown();
        }
        
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        itemStack.decrementUnlessCreative(1, user);
        
        return TypedActionResult.success(itemStack, world.isClient());
    }
}