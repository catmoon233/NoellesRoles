package org.agmas.noellesroles.mixin.ghost;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.agmas.noellesroles.Noellesroles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class GhostSoundMixin {
    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true)
    private void onPlaySound(PlayerEntity source, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo ci) {
        if (source != null) {
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(source.getWorld());
            if (gameWorldComponent.isRole(source, Noellesroles.GHOST)) {
                // 如果发出声音的玩家是“小透明”，则取消该声音事件，阻止其被其他玩家听到。
                ci.cancel();
            }
        }
    }
}