package org.agmas.noellesroles.mixin.roles.insanekiller;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class PlayerNoBoxMixin {
    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    public void getBoundingBox(CallbackInfoReturnable<AABB> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof ServerPlayer serverPlayer){
            if (GameWorldComponent.KEY.get(serverPlayer.level()).isRole(serverPlayer, ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                final var insaneKillerPlayerComponent = InsaneKillerPlayerComponent.KEY.get(serverPlayer);
                if (insaneKillerPlayerComponent.isActive) {
                    if (InsaneKillerPlayerComponent.skipPD) {
                        InsaneKillerPlayerComponent.skipPD = false;
                    } else {

                        cir.setReturnValue(AABB.ofSize(serverPlayer.position().add(0,0.3,0), 1, 0.3, 1));
                    }
                }
            }
        }
    }
}
