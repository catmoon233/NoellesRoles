package org.agmas.noellesroles.mixin.roles.executioner;

import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerShopComponent.class)
public abstract class ExecutionerShopMixin {
    @Shadow public int balance;

    @Shadow @Final private Player player;

    @Shadow public abstract void sync();

//    @Inject(method = "tryBuy", at = @At("HEAD"))
//    void onTryBuy(int index, CallbackInfo ci) {
//        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
//
//        // 检查是否是Executioner角色
////        if (gameWorldComponent.isRole(player, Noellesroles.EXECUTIONER)) {
////            ExecutionerPlayerComponent executionerComponent = ExecutionerPlayerComponent.KEY.get(player);
////
////            // 检查商店是否已解锁
////            if (!executionerComponent.shopUnlocked) {
////                ci.cancel();
////                return;
////            }
////        }
//    }
}