package org.agmas.noellesroles.mixin.client.morphling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerRenderer.class)
public abstract class MorphlingRendererMixin {


    @Shadow public abstract ResourceLocation getTextureLocation(AbstractClientPlayer abstractClientPlayerEntity);
    

    
    @Unique
    private static final ThreadLocal<Boolean> isInMorphingCall = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getTextureLocation(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    void renderMorphlingSkin(AbstractClientPlayer abstractClientPlayerEntity, CallbackInfoReturnable<ResourceLocation> cir) {
        // 防止递归调用
        if (isInMorphingCall.get()) {
            return;
        }
        
        try {
            isInMorphingCall.set(true);
            
            if (TMMClient.moodComponent != null) {
                if ((ConfigWorldComponent.KEY.get(abstractClientPlayerEntity.level())).insaneSeesMorphs && TMMClient.moodComponent.isLowerThanDepressed() && NoellesrolesClient.SHUFFLED_PLAYER_ENTRIES_CACHE.containsKey(abstractClientPlayerEntity.getUUID())) {
                    final var playerInfo = TMMClient.PLAYER_ENTRIES_CACHE.get(NoellesrolesClient.SHUFFLED_PLAYER_ENTRIES_CACHE.get(abstractClientPlayerEntity.getUUID()));
                    if (playerInfo==null)return;
                    final var skin = playerInfo.getSkin();
                    if (skin==null)return;
                    final var texture = skin.texture();
                    cir.setReturnValue(texture);
                    cir.cancel();
                    return;
                }

            }
            final var morphlingPlayerComponent = MorphlingPlayerComponent.KEY.get(abstractClientPlayerEntity);
            if (morphlingPlayerComponent != null && morphlingPlayerComponent.getMorphTicks() > 0 ) {
                if (abstractClientPlayerEntity.getCommandSenderWorld().getPlayerByUUID((MorphlingPlayerComponent.KEY.get(abstractClientPlayerEntity)).disguise) != null) {
                    AbstractClientPlayer disguisePlayer = (AbstractClientPlayer) abstractClientPlayerEntity.getCommandSenderWorld().getPlayerByUUID((MorphlingPlayerComponent.KEY.get(abstractClientPlayerEntity)).disguise);
                    if (disguisePlayer != null && disguisePlayer != abstractClientPlayerEntity) { // 防止自己伪装成自己导致递归
                        cir.setReturnValue(getTextureLocation(disguisePlayer));
                        cir.cancel();
                        return;
                    }
                } else {
                    Log.info(LogCategory.GENERAL, "Morphling disguise is null!!!");

                }
                if (Minecraft.getInstance().player != null && MorphlingPlayerComponent.KEY.get(abstractClientPlayerEntity).disguise.equals(Minecraft.getInstance().player.getUUID())) {
                    if (Minecraft.getInstance().player != abstractClientPlayerEntity) { // 防止自己伪装成自己导致递归
                        cir.setReturnValue(getTextureLocation(Minecraft.getInstance().player));
                        cir.cancel();
                    }
                }
                        return;
                    }


        } finally {
            isInMorphingCall.set(false);
        }
    }
    
    @WrapOperation(method = "renderHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getSkin()Lnet/minecraft/client/resources/PlayerSkin;"))
    PlayerSkin renderArm(AbstractClientPlayer instance, Operation<PlayerSkin> original) {
        if ((MorphlingPlayerComponent.KEY.get(instance)).getMorphTicks() > 0) {
            if (instance.getCommandSenderWorld().getPlayerByUUID((MorphlingPlayerComponent.KEY.get(instance)).disguise) != null) {
                 AbstractClientPlayer disguisePlayer = (AbstractClientPlayer) instance.getCommandSenderWorld().getPlayerByUUID((MorphlingPlayerComponent.KEY.get(instance)).disguise);
                 if (disguisePlayer != null && disguisePlayer != instance) { // 防止自己伪装成自己导致递归
                     return disguisePlayer.getSkin();
                 }
            } else {
                Log.info(LogCategory.GENERAL, "Morphling disguise is null!!!");
            }
        }
        if (TMMClient.moodComponent != null) {
            if ((ConfigWorldComponent.KEY.get(instance.level())).insaneSeesMorphs && TMMClient.moodComponent.isLowerThanDepressed() && NoellesrolesClient.SHUFFLED_PLAYER_ENTRIES_CACHE.containsKey(instance.getUUID())) {
                return TMMClient.PLAYER_ENTRIES_CACHE.get(NoellesrolesClient.SHUFFLED_PLAYER_ENTRIES_CACHE.get(instance.getUUID())).getSkin();
            }
        }
        return original.call(instance);
    }

}
