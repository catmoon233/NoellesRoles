package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LimitedInventoryScreen.class)
public abstract class SimpleRoleCompat extends LimitedHandledScreen<InventoryMenu> {
    @Shadow
    @Final
    public LocalPlayer player;

    public SimpleRoleCompat(InventoryMenu handler, Inventory inventory, Component title) {
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

//    @Inject(
//            method = "getShopEntries",
//            at = @At(
//                    value = "RETURN"
//            ),
//            cancellable = true)
//    private void redirectGetShopEntries(CallbackInfoReturnable<List<ShopEntry>> cir) {
//        GameWorldComponent gameWorldComponent = (GameWorldComponent)GameWorldComponent.KEY.get(this.player.getWorld());
//
//        if (gameWorldComponent.isRole(this.player, Noellesroles.POISONER)) {
//
//            cir.setReturnValue( new ArrayList<>());
//        } else if (gameWorldComponent.isRole(this.player, Noellesroles.BANDIT)) {
//            cir.setReturnValue( new ArrayList<>());
//        }
//
//    }
}