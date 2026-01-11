package org.agmas.noellesroles.mixin.roles.bartender;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.bartender.BartenderPlayerComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerPoisonComponent.class)
public abstract class PoisonToHealsMixin {

    @Shadow @Final private Player player;

    @Inject(method = "setPoisonTicks", at = @At("HEAD"), cancellable = true)
    private void defenseVialApply(int ticks, UUID poisoner, CallbackInfo ci) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        if (gameWorldComponent.isRole(poisoner, ModRoles.BARTENDER)) {
            if (player.level().getPlayerByUUID(poisoner) == null) return;
            BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(player);
            bartenderPlayerComponent.giveArmor();
            ci.cancel();
        }
    }
}
