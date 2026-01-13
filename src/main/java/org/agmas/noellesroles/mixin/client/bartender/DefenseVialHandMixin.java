package org.agmas.noellesroles.mixin.client.bartender;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerRenderer.class)
public class DefenseVialHandMixin {
    @WrapOperation(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack view(AbstractClientPlayer instance, InteractionHand hand, Operation<ItemStack> original) {

        ItemStack ret = original.call(instance, hand);
        if (ret.is(ModItems.DEFENSE_VIAL) || ret.is(ModItems.SMOKE_GRENADE) || ret.is(ModItems.BLANK_CARTRIDGE)) {
            ret = ItemStack.EMPTY;
        }

        return ret;
    }
}
