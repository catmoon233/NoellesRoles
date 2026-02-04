package org.agmas.noellesroles.mixin.roles.bartender;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.packet.BreakArmorPayload;
import org.agmas.noellesroles.roles.bartender.BartenderPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author canyuesama
 */
@Mixin(GameFunctions.class)
public class KillMixin {
    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Ldev/doctor4t/trainmurdermystery/event/AllowPlayerDeath;allowDeath(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)Z"), cancellable = true)

    private static void killPlayer(Player victim, boolean spawnBody, Player killer, ResourceLocation deathReason, CallbackInfo ci) {
        BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(victim);
        if (bartenderPlayerComponent.getArmor() > 0) {

            victim.level().playSound(victim, victim.blockPosition(),
                    TMMSounds.ITEM_PSYCHO_ARMOUR, SoundSource.MASTER, 5.0F, 1.0F);
            bartenderPlayerComponent.removeArmor();
            if (killer instanceof ServerPlayer serverPlayer){
                ServerPlayNetworking.send(serverPlayer,new BreakArmorPayload(victim.getX(), victim.getY(), victim.getZ()

                ));
            }

            ci.cancel();
        }
    }
}
