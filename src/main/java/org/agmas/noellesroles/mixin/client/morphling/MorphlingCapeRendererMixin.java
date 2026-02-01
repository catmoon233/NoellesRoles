package org.agmas.noellesroles.mixin.client.morphling;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.client.NoellesrolesClient;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(CapeLayer.class)
public abstract class MorphlingCapeRendererMixin {


    @Shadow public abstract void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float j, float k, float l);

    private static AbstractClientPlayer abstractClientPlayerEntity;
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at = @At("HEAD")
    )
    void renderMorphlingSkin(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        abstractClientPlayerEntity = abstractClientPlayer;
    }
    
    @Unique
    private static final ThreadLocal<Boolean> isInMorphingCall = ThreadLocal.withInitial(() -> false);

    @Redirect(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/PlayerSkin;capeTexture()Lnet/minecraft/resources/ResourceLocation;"))
    ResourceLocation renderMorphlingSkin(PlayerSkin instance) {
        // 防止递归调用
        if (isInMorphingCall.get()) {
            return instance.capeTexture();
        }
        
        try {
            isInMorphingCall.set(true);
            
            if (TMMClient.moodComponent != null) {
                if ((ConfigWorldComponent.KEY.get(abstractClientPlayerEntity.level())).insaneSeesMorphs && TMMClient.moodComponent.isLowerThanDepressed() && NoellesrolesClient.SHUFFLED_PLAYER_ENTRIES_CACHE.containsKey(abstractClientPlayerEntity.getUUID())) {
                    final var playerInfo = TMMClient.PLAYER_ENTRIES_CACHE.get(NoellesrolesClient.SHUFFLED_PLAYER_ENTRIES_CACHE.get(abstractClientPlayerEntity.getUUID()));
                    if (playerInfo==null) return instance.capeTexture();
                    final var skin = playerInfo.getSkin();
                    if (skin==null) return instance.capeTexture();
                    final var texture = skin.capeTexture();
                    return texture;
                }

            }
            final var morphlingPlayerComponent = MorphlingPlayerComponent.KEY.get(abstractClientPlayerEntity);
            if (morphlingPlayerComponent != null && morphlingPlayerComponent.getMorphTicks() > 0 ) {
                if (abstractClientPlayerEntity.getCommandSenderWorld().getPlayerByUUID((MorphlingPlayerComponent.KEY.get(abstractClientPlayerEntity)).disguise) != null) {
                    AbstractClientPlayer disguisePlayer = (AbstractClientPlayer) abstractClientPlayerEntity.getCommandSenderWorld().getPlayerByUUID((MorphlingPlayerComponent.KEY.get(abstractClientPlayerEntity)).disguise);
                    if (disguisePlayer != null && disguisePlayer != abstractClientPlayerEntity) { // 防止自己伪装成自己导致递归
                        final var playerInfo = TMMClient.PLAYER_ENTRIES_CACHE.get(NoellesrolesClient.SHUFFLED_PLAYER_ENTRIES_CACHE.get(disguisePlayer.getUUID()));
                        if (playerInfo==null) return instance.capeTexture();
                        return playerInfo.getSkin().capeTexture();
                    }
                } else {
                    Log.info(LogCategory.GENERAL, "Morphling disguise is null!!!");

                }
                if (Minecraft.getInstance().player != null && MorphlingPlayerComponent.KEY.get(abstractClientPlayerEntity).disguise.equals(Minecraft.getInstance().player.getUUID())) {
                    if (Minecraft.getInstance().player != abstractClientPlayerEntity) { // 防止自己伪装成自己导致递归
                        return Minecraft.getInstance().player.getSkin().capeTexture();
                    }
                }
                return null;
                    }


        } finally {
            isInMorphingCall.set(false);
        }
        return instance.capeTexture();
    }
    


}
