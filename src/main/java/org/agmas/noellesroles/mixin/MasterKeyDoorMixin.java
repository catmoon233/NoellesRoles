package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.index.TMMItems;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.repack.HSRItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({SmallDoorBlock.class})
public abstract class MasterKeyDoorMixin {
    @Redirect(
            method = {"onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z",
                    ordinal = 0
            )
    )
    private boolean attendant(ItemStack instance, Item item, BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (instance.isOf(ModItems.MASTER_KEY_P)) {
            if (!player.isCreative()) {
                instance.damage(1, player, player.getPreferredEquipmentSlot(instance));
            }

            return true;
        } else {
            return instance.isOf(TMMItems.LOCKPICK);
        }
    }
}
