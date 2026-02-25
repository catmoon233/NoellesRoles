package org.agmas.noellesroles.mixin.client.roles.bartender;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.index.TMMParticles;

import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BeveragePlateBlockEntity.class)
public class DefenseVialViewMixin {
    @Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
    private static void view(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity instanceof BeveragePlateBlockEntity tray) {
            if (tray.getPoisoner() != null) {
                Role role = TMMClient.gameComponent.getRole(Minecraft.getInstance().player);
                if (role == null)
                    return;
                if (role.identifier().getPath().equals(ModRoles.BARTENDER.identifier().getPath())
                        || role.identifier().getPath().equals(ModRoles.POISONER.identifier().getPath())) {
                    world.addParticle(TMMParticles.POISON, true, (double) ((float) pos.getX() + 0.5F),
                            (double) pos.getY(), (double) ((float) pos.getZ() + 0.5F), (double) 0.0F, (double) 0.15F,
                            (double) 0.0F);
                    ci.cancel();
                    return;
                }
            }
            if (tray.getArmorer() != null) {
                Role role = TMMClient.gameComponent.getRole(Minecraft.getInstance().player);
                if (role == null)
                    return;
                // LoggerFactory.getLogger("defense").info("Helloworld");
                if (role.identifier().getPath().equals(ModRoles.BARTENDER.identifier().getPath())) {
                    world.addParticle(ParticleTypes.EFFECT, true, (double) ((float) pos.getX() + 0.5F),
                            (double) pos.getY(), (double) ((float) pos.getZ() + 0.5F), (double) 0.0F, (double) 0.15F,
                            (double) 0.0F);
                    ci.cancel();
                    return;
                }
            }
        }
    }
}
