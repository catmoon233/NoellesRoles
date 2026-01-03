package org.agmas.noellesroles.mixin.client.bartender;

import dev.doctor4t.trainmurdermystery.block_entity.BeveragePlateBlockEntity;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.event.CanSeePoison;
import org.agmas.noellesroles.role.ModRoles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BeveragePlateBlockEntity.class)
public class DefenseVialViewMixin {
    @Inject(method = "clientTick", at = @At("HEAD"), order = 1001, cancellable = true)
    private static void view(Level world, BlockPos pos, BlockState state, BlockEntity blockEntity, CallbackInfo ci) {

        if (blockEntity instanceof BeveragePlateBlockEntity tray) {
            if (tray.getPoisoner() != null) {
                if (((GameWorldComponent) GameWorldComponent.KEY.get(world)).isRole(UUID.fromString(tray.getPoisoner()), ModRoles.BARTENDER) && CanSeePoison.EVENT.invoker().visible(Minecraft.getInstance().player)) {
                    world.addParticle(ParticleTypes.HAPPY_VILLAGER, (double) ((float) pos.getX() + 0.5F), (double) pos.getY(), (double) ((float) pos.getZ() + 0.5F), (double) 0.0F, (double) 0.15F, (double) 0.0F);
                    ci.cancel();
                }
            }
        }
    }
}
