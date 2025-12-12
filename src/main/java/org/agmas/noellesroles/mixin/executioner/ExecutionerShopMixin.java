package org.agmas.noellesroles.mixin.executioner;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerShopComponent.class)
public abstract class ExecutionerShopMixin {
    @Shadow public int balance;

    @Shadow @Final private PlayerEntity player;

    @Shadow public abstract void sync();

    @Inject(method = "tryBuy", at = @At("HEAD"), cancellable = true)
    void onTryBuy(int index, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
        
        // 检查是否是Executioner角色
        if (gameWorldComponent.isRole(player, Noellesroles.EXECUTIONER)) {
            ExecutionerPlayerComponent executionerComponent = ExecutionerPlayerComponent.KEY.get(player);
            
            // 检查商店是否已解锁
            if (!executionerComponent.shopUnlocked) {
                ci.cancel();
                return;
            }
        }
    }
}