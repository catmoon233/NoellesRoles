package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.entity.player.PlayerEntity.class)
public class SpawnMixin {
    @Inject(at = @At("HEAD"), method = "onDeath")
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        final var player = (PlayerEntity) (Object) this;
        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
            final var gameWorldComponent = GameWorldComponent.KEY.get(player.getWorld());
            if (gameWorldComponent != null) {
                if (gameWorldComponent.isRunning()) {
                    GameFunctions.killPlayer(player, false, player.getLastAttacker() instanceof PlayerEntity killerPlayer ? killerPlayer : null, GameConstants.DeathReasons.FELL_OUT_OF_TRAIN);

                }
            }
        }
    }
}
