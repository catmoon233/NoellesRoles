package org.agmas.noellesroles.mixin.roles.coroner;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class CoronerAddDeathReasonMixin {

    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/entity/PlayerBodyEntity;setYHeadRot(F)V"))
    private static void setDeathReason(Player victim, boolean spawnBody, Player killer, ResourceLocation identifier, CallbackInfo ci, @Local PlayerBodyEntity playerBodyEntity) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
        if (gameWorldComponent.getRole(victim) == null) return;
        final var bodyDeathReasonComponent = BodyDeathReasonComponent.KEY.get(playerBodyEntity);
        bodyDeathReasonComponent.killer = killer.getUUID();
        bodyDeathReasonComponent.deathReason = identifier;
        bodyDeathReasonComponent.playerRole = gameWorldComponent.getRole(victim).identifier();
        bodyDeathReasonComponent.sync();
    }

}
