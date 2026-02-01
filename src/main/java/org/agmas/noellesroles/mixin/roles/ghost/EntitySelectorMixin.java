package org.agmas.noellesroles.mixin.roles.ghost;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public class EntitySelectorMixin {
    @Inject(method = "pushableBy", at = @At("HEAD"), cancellable = true)
    private static void pushableBy(Entity entity, CallbackInfoReturnable<Predicate<Entity>> cir) {

    }
}
