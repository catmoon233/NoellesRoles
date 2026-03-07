package org.agmas.noellesroles.mixin.roles.jojo;

import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.utils.MCItemsUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.index.tag.TMMItemTags;
import dev.doctor4t.trainmurdermystery.network.tmm.GunShootPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

@Mixin(GunShootPayload.Receiver.class)
public class GunCooldownMixin {
    // JOJO 两倍枪冷却
    @Inject(method = "receive", at = @At("HEAD"), cancellable = true)
    public void receive(@NotNull GunShootPayload payload, ServerPlayNetworking.@NotNull Context context,
            CallbackInfo ci) {
        ServerPlayer player = context.player();
        var gameWorldComponent = GameWorldComponent.KEY.get(player.level());
        ItemStack mainHandStack = player.getMainHandItem();
        if (mainHandStack.is(TMMItemTags.GUNS)) {
            if (gameWorldComponent.isRole(player, ModRoles.JOJO)) {
                player.getCooldowns().addCooldown(mainHandStack.getItem(),
                        (Integer) GameConstants.ITEM_COOLDOWNS.getOrDefault(mainHandStack.getItem(), 0) * 2);
            }
        }
    }

    // 枪标签都冷却
    @Inject(method = "receive", at = @At("TAIL"), cancellable = true)
    public void receive2(@NotNull GunShootPayload payload, ServerPlayNetworking.@NotNull Context context,
            CallbackInfo ci) {
        ServerPlayer player = context.player();
        if (!player.isCreative()) {
            var cooldowns = player.getCooldowns();

            MCItemsUtils.getItemsByTag(player.level(), TMMItemTags.GUNS).forEach((item) -> {
                if (!cooldowns.isOnCooldown(item))
                    cooldowns.addCooldown(item,
                            (Integer) GameConstants.ITEM_COOLDOWNS.getOrDefault(item, 0));
            });
        }
    }
}
