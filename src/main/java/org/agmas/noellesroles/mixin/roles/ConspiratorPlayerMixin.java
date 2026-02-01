package org.agmas.noellesroles.mixin.roles;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public class ConspiratorPlayerMixin {
   @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/game/GameConstants;getMoneyPerKill()I",shift = At.Shift.AFTER), cancellable = true)
    private static void killPlayer(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason, CallbackInfo ci) {
       final var gameWorldComponent = GameWorldComponent.KEY.get(killer.level());
       if (gameWorldComponent.isRole(killer, ModRoles.CONSPIRATOR)){
           if ("heart_attack".equals(deathReason.getPath())){
               final var playerShopComponent = PlayerShopComponent.KEY.get(killer);
               playerShopComponent.setBalance(playerShopComponent.balance - GameConstants.getMoneyPerKill());
           }
       }
    }
}
