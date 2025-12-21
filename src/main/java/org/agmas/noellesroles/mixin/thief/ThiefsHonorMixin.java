package org.agmas.noellesroles.mixin.thief;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.thief.ThiefPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ThiefsHonorMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    void checkThiefsHonorVictory(CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(world);
        
        if (!gameWorldComponent.isRunning()) {
            return;
        }

        for (PlayerEntity player : world.getPlayers()) {
            if (!gameWorldComponent.isRole(player, Noellesroles.THIEF)) {
                continue;
            }
            
            ThiefPlayerComponent thiefComponent = ThiefPlayerComponent.KEY.get(player);

            if (thiefComponent.hasThiefsHonor) {
                // Check if at breathing point
                // TODO: Implement breathing point check
                
                // TODO: Implement victory condition properly - setWinner method doesn't exist
            }
        }
    }
}