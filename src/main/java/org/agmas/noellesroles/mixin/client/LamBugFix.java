package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import dev.lambdaurora.lambdynlights.LambDynLights;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LambDynLights.class)
public class LamBugFix {
    @Inject(method = "getLivingEntityLuminanceFromItems", at = @At("HEAD"), cancellable = true)
    private static void getLivingEntityLuminanceFromItems(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof PlayerBodyEntity) {
            cir.setReturnValue(3);
            cir.cancel();
        }
    }

}
