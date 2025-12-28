package org.agmas.noellesroles.mixin.roles.framing;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.MurderGameMode;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MurderGameMode.class)
public abstract class FramingPassiveIncomeMixin {

    @WrapOperation(method = "tickServerGameLoop", at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/cca/GameWorldComponent;canUseKillerFeatures(Lnet/minecraft/entity/player/PlayerEntity;)Z"))
    public boolean passiveMoneyGeneration(GameWorldComponent instance, PlayerEntity player, Operation<Boolean> original) {
        if (instance.isRole(player, ModRoles.NOISEMAKER) || instance.isRole(player, ModRoles.STALKER) || instance.isRole(player, ModRoles.JESTER) || instance.isRole(player, ModRoles.EXECUTIONER)) return true;
        return original.call(instance,player);
    }

}
