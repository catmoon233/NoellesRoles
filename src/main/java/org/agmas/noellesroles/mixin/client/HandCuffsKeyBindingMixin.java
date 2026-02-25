package org.agmas.noellesroles.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.agmas.noellesroles.init.ModItems;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyMapping.class)
public abstract class HandCuffsKeyBindingMixin {
    @Shadow
    public abstract boolean same(KeyMapping other);

    @Unique
    private boolean shouldSuppressKey() {
        if (TMM.isLobby)
            return false;
        if (Minecraft.getInstance() == null)
            return false;
        if (Minecraft.getInstance().player == null)
            return false;
        if (TMMClient.gameComponent != null && TMMClient.gameComponent.isRunning()
                && TMMClient.isPlayerAliveAndInSurvival()) {
            final var player = (Minecraft.getInstance().player);
            if (player.getOffhandItem().is(ModItems.HANDCUFFS)) {

                if (this.same(Minecraft.getInstance().options.keySwapOffhand) ||
                        this.same(Minecraft.getInstance().options.keyJump) ||
                        this.same(Minecraft.getInstance().options.keyTogglePerspective) ||
                        this.same(Minecraft.getInstance().options.keyDrop) ||
                        this.same(Minecraft.getInstance().options.keyAttack) ||
                        this.same(Minecraft.getInstance().options.keyUse) ||
                        this.same(Minecraft.getInstance().options.keyDrop) ||
                        this.same(Minecraft.getInstance().options.keyAdvancements))
                    return true;
                return false;
            }
        }
        return false;
    }

    @ModifyReturnValue(method = "consumeClick", at = @At("RETURN"))
    private boolean noe$restrainWasPressedKeys(boolean original) {
        if (this.shouldSuppressKey())
            return false;
        else
            return original;
    }

    @ModifyReturnValue(method = "isDown", at = @At("RETURN"))
    private boolean noe$restrainIsPressedKeys(boolean original) {
        if (this.shouldSuppressKey())
            return false;
        else
            return original;
    }

    @ModifyReturnValue(method = "matches", at = @At("RETURN"))
    private boolean noe$restrainMatchesKey(boolean original) {
        if (this.shouldSuppressKey())
            return false;
        else
            return original;
    }
}
