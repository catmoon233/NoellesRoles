package org.agmas.noellesroles.mixin.client.coroner;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TMMClient.class, priority = 500)
public class CoronerInstinctGlowMixin {

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void coronerHighlightBodies(Entity target, CallbackInfoReturnable<Integer> cir) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(player.level());

        if (!gameWorldComponent.isRole(player, ModRoles.CORONER)) {
            return;
        }

        long time = player.level().getGameTime();
        if (time % 400 >= 100) {
            return;
        }

        if (target instanceof PlayerBodyEntity) {
            cir.setReturnValue(ModRoles.CORONER.color());
            return;
        }

        if (target instanceof Player targetPlayer) {
            InsaneKillerPlayerComponent component = InsaneKillerPlayerComponent.KEY.get(targetPlayer);
            if (component.isActive) {
                cir.setReturnValue(ModRoles.CORONER.color());
                return;
            }
        }
    }
}