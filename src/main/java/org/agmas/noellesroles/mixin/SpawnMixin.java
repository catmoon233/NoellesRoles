package org.agmas.noellesroles.mixin;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.entity.player.Player.class)
public class SpawnMixin {
    @Inject(at = @At("HEAD"), method = "die", cancellable = true)
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        final var player = (Player) (Object) this;
        if (GameFunctions.isPlayerAliveAndSurvival(player)) {
            final var gameWorldComponent = GameWorldComponent.KEY.get(player.level());
            if (gameWorldComponent != null) {
                if (gameWorldComponent.isRunning()) {
                    ci.cancel();
                    player.setHealth(20.0F);
                    GameFunctions.killPlayer(player, false, player.getLastAttacker() instanceof Player killerPlayer ? killerPlayer : null, GameConstants.DeathReasons.FELL_OUT_OF_TRAIN);

                }
            }
        }
    }
}
