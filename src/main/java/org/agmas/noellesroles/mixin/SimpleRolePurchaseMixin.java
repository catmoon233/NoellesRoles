package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import org.spongepowered.asm.mixin.Mixin;
@Mixin({PlayerShopComponent.class})
public abstract class SimpleRolePurchaseMixin implements RoleComponent, ServerTickingComponent, ClientTickingComponent {
//    @Shadow
//    @Final
//    private PlayerEntity player;
//
//    @Inject(
//            method = {"getShopEntries"},
//            at = @At(
//                    value = "RETURN"
//            ),
//            cancellable = true)
//    private void redirectPurchase(CallbackInfoReturnable<List<ShopEntry>> cir) {
//        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(this.player.getWorld());
//        if (gameWorldComponent.isRole(this.player, Noellesroles.POISONER)) {
//            cir.setReturnValue(HSRConstants.POISONER_SHOP_ENTRIES);
//        } else if (gameWorldComponent.isRole(this.player, Noellesroles.BANDIT)) {
//            cir.setReturnValue(HSRConstants.BANDIT_SHOP_ENTRIES);
//        }
//    }
}
