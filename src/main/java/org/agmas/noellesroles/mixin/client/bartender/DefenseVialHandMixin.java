package org.agmas.noellesroles.mixin.client.bartender;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerRenderer.class)
public class DefenseVialHandMixin {
    @WrapOperation(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack view(AbstractClientPlayer instance, InteractionHand hand, Operation<ItemStack> original) {

        ItemStack ret = original.call(instance, hand);
        if (GameWorldComponent.KEY.get(instance.level()).isRole(instance, ModRoles.VETERAN)) {
            if (ret.is(TMMItems.KNIFE)){
                return ModItems.SP_KNIFE.getDefaultInstance();
            }
        }
        if (ret.is(ModItems.DEFENSE_VIAL) || ret.is(ModItems.SMOKE_GRENADE) || ret.is(ModItems.BLANK_CARTRIDGE) || ret.is(ModItems.ALARM_TRAP) || ret.is(ModItems.HALLUCINATION_BOTTLE ) || ret.is(ModItems.REINFORCEMENT) || ret.is(ModItems.CONSPIRACY_PAGE )) {
            ret = ItemStack.EMPTY;
        }

        return ret;
    }
}
