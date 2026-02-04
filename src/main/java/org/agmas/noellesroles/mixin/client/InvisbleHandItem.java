package org.agmas.noellesroles.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(PlayerRenderer.class)
public class InvisbleHandItem {
    

    private static final Set<Item> HIDDEN_ITEMS = Set.of(
            ModItems.DEFENSE_VIAL,
            ModItems.SMOKE_GRENADE,
            ModItems.BLANK_CARTRIDGE,
            ModItems.ALARM_TRAP,
            ModItems.HALLUCINATION_BOTTLE,
            ModItems.REINFORCEMENT,
            ModItems.CONSPIRACY_PAGE
    );
    
    @WrapOperation(method = "getArmPose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack view(AbstractClientPlayer instance, InteractionHand hand, Operation<ItemStack> original) {
        ItemStack itemStack = original.call(instance, hand);
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(instance.level());
        
        if (gameWorld.isRole(instance, ModRoles.VETERAN) && itemStack.is(TMMItems.KNIFE)) {
            return ModItems.SP_KNIFE.getDefaultInstance();
        }
        
        // 隐藏指定的物品
        if (HIDDEN_ITEMS.contains(itemStack.getItem())) {
            return ItemStack.EMPTY;
        }
        
        return itemStack;
    }
}
