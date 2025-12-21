package org.agmas.noellesroles.mixin.thief;

import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerShopComponent.class)
public abstract class DelayedCoinDisplayMixin {
    
    @Shadow public int balance;
    private int displayedBalance; 
    private int actualBalance; 
    private boolean hasNegativeCoins; 
    
    public int getActualBalance() {
        return actualBalance;
    }
    
    public int getDisplayedBalance() {
        return displayedBalance;
    }
    
    @Inject(method = "setBalance", at = @At("HEAD"))
    void setBalance(int balance, CallbackInfo ci) {

        actualBalance = balance;
        
        if (hasNegativeCoins && actualBalance >= 0) {
            displayedBalance = actualBalance;
            hasNegativeCoins = false;
        } 
        else if (balance > displayedBalance) {
            displayedBalance = balance;
            hasNegativeCoins = false;
        } 
        else if (balance < displayedBalance) {

            hasNegativeCoins = true;
        }
    }
}