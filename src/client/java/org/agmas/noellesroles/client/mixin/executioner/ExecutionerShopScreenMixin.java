package org.agmas.noellesroles.client.mixin.executioner;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import  dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(LimitedInventoryScreen.class)
public abstract class ExecutionerShopScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow
    @Final
    public ClientPlayerEntity player;

    public ExecutionerShopScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    void onInit(CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        
        // 检查是否是Executioner角色
        if (gameWorldComponent.isRole(player, Noellesroles.EXECUTIONER)) {
            ExecutionerPlayerComponent executionerComponent = ExecutionerPlayerComponent.KEY.get(player);
            
            // 检查商店是否已解锁
            if (executionerComponent.shopUnlocked) {
                List<ShopEntry> entries = new ArrayList<>();
                entries.add(new ShopEntry(TMMItems.KNIFE.getDefaultStack(), 250, ShopEntry.Type.WEAPON));
                int apart = 36;
                int x = width / 2 - (entries.size()) * apart / 2 + 9;
                int shouldBeY = (((LimitedInventoryScreen)(Object)this).height - 32) / 2;
                int y = shouldBeY + 80;

                for(int i = 0; i < entries.size(); ++i) {
                    addDrawableChild(new LimitedInventoryScreen.StoreItemWidget((LimitedInventoryScreen) (Object)this, x + apart * i, y, (ShopEntry)entries.get(i), i));
                }
            }
        }
    }
}