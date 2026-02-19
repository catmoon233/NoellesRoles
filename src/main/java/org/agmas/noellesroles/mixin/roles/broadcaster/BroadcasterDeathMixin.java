package org.agmas.noellesroles.mixin.roles.broadcaster;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import org.agmas.noellesroles.component.BroadcasterPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameFunctions.class)
public class BroadcasterDeathMixin {
    @Inject(method = "killPlayer(Lnet/minecraft/world/entity/player/Player;ZLnet/minecraft/world/entity/player/Player;Lnet/minecraft/resources/ResourceLocation;)V", at = @At("HEAD"), cancellable = true)
    private static void onBroadcasterDeath(Player victim, boolean spawnBody, Player killer, ResourceLocation identifier,
            CallbackInfo ci) {
        // if (identifier.getPath().equals("fell_out_of_train"))
        // return;
        // if (identifier.getPath().equals("disconnected"))
        // return;

        final var world = victim.level();
        if (world.isClientSide)
            return;

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(world);
        if (gameWorldComponent.isRole(victim, ModRoles.BROADCASTER)) {
            String last_message = null;

            BroadcasterPlayerComponent comp = BroadcasterPlayerComponent.KEY.get(victim);
            if (comp != null) {
                LoggerFactory.getLogger("debug").error(comp.getStoredStr());
                last_message = comp.getStoredStr();
            }
            Component msg;
            if (last_message != null && !last_message.trim().isEmpty()) {
                msg = Component
                        .translatable("message.noellesroles.broadcaster.death_with_msg",
                                Component.literal(last_message).withStyle(ChatFormatting.GOLD))
                        .withStyle(ChatFormatting.RED);
            } else {
                msg = Component.translatable("message.noellesroles.broadcaster.death")
                        .withStyle(ChatFormatting.RED);
            }
            world.players().forEach(
                    player -> {
                        if (player instanceof ServerPlayer sp) {
                            player.playNotifySound(SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.5F, 1.3F);

                            org.agmas.noellesroles.packet.BroadcastMessageS2CPacket packet = new org.agmas.noellesroles.packet.BroadcastMessageS2CPacket(
                                    msg);
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(sp, packet);
                        }
                    });
        }
    }
}