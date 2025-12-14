package org.agmas.noellesroles.client.mixin;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.repack.HSRConstants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

import static org.agmas.noellesroles.repack.HSRConstants.BANDIT_SHOP_ENTRIES;
import static org.agmas.noellesroles.repack.HSRConstants.POISONER_SHOP_ENTRIES;

@Mixin(LimitedInventoryScreen.class)
public abstract class SimpleRoleCompat extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow
    @Final
    public ClientPlayerEntity player;

    public SimpleRoleCompat(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

//    @Inject(method = "init", at = @At("HEAD"))
//    void bartenderShopRenderer(CallbackInfo ci) {
//        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
//        if (gameWorldComponent.isRole(player,Noellesroles.POISONER)) {
//            List<ShopEntry> entries = POISONER_SHOP_ENTRIES;
//            //entries.add(new ShopEntry(ModItems.DEFENSE_VIAL.getDefaultStack(), 250, ShopEntry.Type.POISON));
//            int apart = 36;
//            int x = width / 2 - (entries.size()) * apart / 2 + 9;
//            int shouldBeY = (((LimitedInventoryScreen)(Object)this).height - 32) / 2;
//            int y = shouldBeY - 46;
//
//            for(int i = 0; i < entries.size(); ++i) {
//                addDrawableChild(new LimitedInventoryScreen.StoreItemWidget((LimitedInventoryScreen) (Object)this, x + apart * i, y, (ShopEntry)entries.get(i), i));
//            }
//        }
//    }

    @Inject(
            method = "getShopEntries",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true)
    private void redirectGetShopEntries(CallbackInfoReturnable<List<ShopEntry>> cir) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent)GameWorldComponent.KEY.get(this.player.getWorld());

        if (gameWorldComponent.isRole(this.player, Noellesroles.POISONER)) {

            cir.setReturnValue( POISONER_SHOP_ENTRIES);
        } else if (gameWorldComponent.isRole(this.player, Noellesroles.BANDIT)) {
            cir.setReturnValue( BANDIT_SHOP_ENTRIES);
        }

    }
}