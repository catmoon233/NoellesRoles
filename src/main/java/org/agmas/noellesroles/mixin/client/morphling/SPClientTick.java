package org.agmas.noellesroles.mixin.client.morphling;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;

@Mixin(SplitPersonalityComponent.class)
public class SPClientTick {
    @Shadow @Final private Player player;

    @Inject(method = "clientDo", at = @At("HEAD"), cancellable = true)
    private void clientDo(CallbackInfo ci) {
        SplitPersonalityComponent comp = (SplitPersonalityComponent) (Object) this;
        if (!comp.isDeath()){
            if (!comp.isCurrentlyActive()){
                player.level().players().forEach(p -> {
                    if (p.getUUID().equals(comp.getCurrentActivePerson())){
                        Minecraft.getInstance().setCameraEntity( p);
                    }
                });
            }
        }
    }
}
