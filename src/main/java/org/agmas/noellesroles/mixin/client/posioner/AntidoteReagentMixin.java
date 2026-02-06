package org.agmas.noellesroles.mixin.client.posioner;

import dev.doctor4t.trainmurdermystery.client.StaminaRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.agmas.noellesroles.item.AntidoteReagentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StaminaRenderer.class)
public class AntidoteReagentMixin {
    @ModifyVariable(method = "renderHud", at = @At(value = "STORE", ordinal = 0), name = "isChargingWeapon")
    private static boolean modifyStamina(boolean value) {
        final var player = Minecraft.getInstance().player;
        if (player.getMainHandItem().getItem() instanceof AntidoteReagentItem){
            maxStamina = 20.0F;
            int itemUseTime = player.getTicksUsingItem();
            staminaPercent = Math.min((float)itemUseTime / 20.0F, 1.0F);
            isChargingWeapon = true;
            return true;
        }
        return false;
    }
}
