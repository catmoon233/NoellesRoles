package org.agmas.noellesroles.mixin.roles.voodoo;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.voodoo.VoodooPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public abstract class VoodooVooMixin {

    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"))
    private static void voodoovoo(Player victim, boolean spawnBody, Player killer, ResourceLocation identifier, CallbackInfo ci) {
        if (NoellesRolesConfig.HANDLER.instance().voodooNonKillerDeaths || killer != null) {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(victim.level());
            if (gameWorldComponent.isRole(victim, ModRoles.VOODOO)) {
                VoodooPlayerComponent voodooPlayerComponent = (VoodooPlayerComponent) VoodooPlayerComponent.KEY.get(victim);
                if (voodooPlayerComponent.target != null) {
                    Player voodooed = victim.level().getPlayerByUUID(voodooPlayerComponent.target);
                    if (voodooed != null) {
                        if (GameFunctions.isPlayerAliveAndSurvival(voodooed) && voodooed != victim) {
                            GameFunctions.killPlayer(voodooed, true, null, ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "voodoo"));
                        }
                    }
                }
            }
        }
    }

}
