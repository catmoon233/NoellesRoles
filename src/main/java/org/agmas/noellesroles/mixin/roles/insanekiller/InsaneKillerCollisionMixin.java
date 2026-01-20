package org.agmas.noellesroles.mixin.roles.insanekiller;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class InsaneKillerCollisionMixin extends LivingEntity {

    protected InsaneKillerCollisionMixin(net.minecraft.world.entity.EntityType<? extends LivingEntity> entityType,
            net.minecraft.world.level.Level level) {
        super(entityType, level);
    }

    @Inject(method = "canBeCollidedWith", at = @At("HEAD"), cancellable = true)
    private void onCanBeCollidedWith(CallbackInfoReturnable<Boolean> cir) {
        InsaneKillerPlayerComponent component = InsaneKillerPlayerComponent.KEY.get(this);
        if (component.isActive) {
            cir.setReturnValue(false);
        }
    }
}