package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;

import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.repack.HSRConstants;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin({PlayerShopComponent.class})
public abstract class SimpleRolePurchaseMixin implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    @Shadow
    @Final
    private PlayerEntity player;

    @Inject(
            method = {"getShopEntries"},
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true)
    private void redirectPurchase(CallbackInfoReturnable<List<ShopEntry>> cir) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(this.player.getWorld());
        if (gameWorldComponent.isRole(this.player, Noellesroles.POISONER)) {
            cir.setReturnValue(HSRConstants.POISONER_SHOP_ENTRIES);
        } else if (gameWorldComponent.isRole(this.player, Noellesroles.BANDIT)) {
            cir.setReturnValue(HSRConstants.BANDIT_SHOP_ENTRIES);
        }
    }
}
