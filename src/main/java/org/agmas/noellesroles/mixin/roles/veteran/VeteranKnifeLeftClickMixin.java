package org.agmas.noellesroles.mixin.roles.veteran;


import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class VeteranKnifeLeftClickMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onVeteranKnifeAttack(Entity target, CallbackInfo ci) {
        ServerPlayer attacker = (ServerPlayer) (Object) this;
        
        if (!(target instanceof Player targetPlayer)) return;
        
        if (!GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) return;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(attacker.level());
        if (!gameWorld.isRole(attacker, ModRoles.VETERAN)) return;
        
        ItemStack mainHand = attacker.getItemInHand(InteractionHand.MAIN_HAND);
        if (!mainHand.is(TMMItems.KNIFE)) return;
        
        ci.cancel();
    }
}