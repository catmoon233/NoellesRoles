package org.agmas.noellesroles.mixin.roles.photographer;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Inventory.class)
public class PhotographerMixin {
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

}
