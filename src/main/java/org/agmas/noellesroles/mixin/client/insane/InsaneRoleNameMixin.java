package org.agmas.noellesroles.mixin.client.insane;

import com.llamalad7.mixinextras.sugar.Local;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;

import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.component.InsaneKillerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class InsaneRoleNameMixin {

    @Shadow
    private static float nametagAlpha;

    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDisplayName()Lnet/minecraft/network/chat/Component;"), cancellable = true)
    private static void b(Font renderer, @NotNull LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter,
            CallbackInfo ci, @Local Player target) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.level());
        if (gameWorldComponent.getRole(target) != null) {
            var role = gameWorldComponent.getRole(target);
            if (role.identifier().getPath()
                    .equals(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES
                            .identifier().getPath())) {
                var insaneComponent = InsaneKillerPlayerComponent.KEY.get(target);
                if(insaneComponent!=null){
                    if(insaneComponent.isActive || insaneComponent.inNearDeath()){
                        ci.cancel();
                        return;
                    }
                }
            }
        }
    }
}
