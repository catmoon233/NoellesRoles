package org.agmas.noellesroles.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.agmas.noellesroles.component.DefibrillatorComponent;
import org.agmas.noellesroles.component.ModComponents;

public class DefibrillatorItem extends Item {
    public DefibrillatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int timeCharged) {
        if (!level.isClientSide && user instanceof Player player) {
            if (this.getUseDuration(stack, user) - timeCharged >= 10) {
                // 查找目标
                // 这里简化处理，假设玩家对着目标使用。实际上可能需要射线检测。
                // 参考 AntidoteItem 的 getAntidoteTarget
                net.minecraft.world.phys.HitResult hitResult = org.agmas.noellesroles.repack.items.AntidoteItem
                        .getAntidoteTarget(player);

                if (hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHitResult) {
                    if (entityHitResult.getEntity() instanceof Player target) {
                        DefibrillatorComponent component = ModComponents.DEFIBRILLATOR.get(target);
                        component.setProtection(90 * 20); // 90秒

                        player.displayClientMessage(
                                Component.translatable("message.noellesroles.defibrillator.used", target.getName()),
                                true);
                        target.displayClientMessage(
                                Component.translatable("message.noellesroles.defibrillator.protected"), true);

                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}