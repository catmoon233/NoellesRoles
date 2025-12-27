package org.agmas.noellesroles.mixin.client.executioner;

import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedHandledScreen;
import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LimitedInventoryScreen.class)
public abstract class ExecutionerShopScreenMixin extends LimitedHandledScreen<PlayerScreenHandler> {
    @Shadow
    @Final
    public ClientPlayerEntity player;

    public ExecutionerShopScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    // @Inject(method = "init", at = @At("HEAD"))
    // void onInit(CallbackInfo ci) {
    //     GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
    //     
    //     // 检查是否是Executioner角色
    //     if (gameWorldComponent.isRole(player, Noellesroles.EXECUTIONER)) {
    //         ExecutionerPlayerComponent executionerComponent = ExecutionerPlayerComponent.KEY.get(player);
    //         
    //         // 检查商店是否已解锁
    //         if (executionerComponent.shopUnlocked) {
    //         }
    //     }
    // }
}