package org.agmas.noellesroles.item;

import org.agmas.noellesroles.init.ModItems;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HandCuffsItem extends Item {
    public HandCuffsItem(Item.Properties settings) {
        super(settings.durability(10));
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean bl) {
        if (itemStack.is(ModItems.HANDCUFFS)) {
            if (entity instanceof Player player) {
                if (player.getOffhandItem().is(ModItems.HANDCUFFS)) {
                    if (!level.isClientSide && player.isShiftKeyDown() && level.getGameTime() % 20 == 0) {
                        // Noellesroles.LOGGER.info("jumping");
                        itemStack.hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
                    }
                }
            }
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity,
            InteractionHand hand) {
        super.interactLivingEntity(stack, user, entity, hand);
        if (entity instanceof Player target) {
            if (target.getOffhandItem() != null) {
            }
            stack.shrink(1);
        } else {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }
}
