package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(TMMClient.class)
public abstract class InstinctMixin {


    @Shadow public static KeyBinding instinctKeybind;

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void b(CallbackInfoReturnable<Boolean> cir) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.JESTER)) {
            if (instinctKeybind.isPressed()) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(MinecraftClient.getInstance().player.getWorld());
        if (target instanceof PlayerEntity) {
            if (!((PlayerEntity)target).isSpectator()) {
                BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get((PlayerEntity) target);
                PlayerPoisonComponent playerPoisonComponent =  PlayerPoisonComponent.KEY.get((PlayerEntity) target);
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.BARTENDER) && bartenderPlayerComponent.glowTicks > 0) {
                    cir.setReturnValue(Color.GREEN.getRGB());
                }
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.BARTENDER) && bartenderPlayerComponent.armor > 0) {
                    cir.setReturnValue(Color.BLUE.getRGB());
                    cir.cancel();
                }
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.BARTENDER) && playerPoisonComponent.poisonTicks > 0) {
                    cir.setReturnValue(Color.RED.getRGB());
                }
            }
        }
        if (target instanceof PlayerEntity) {
            if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.EXECUTIONER)) {
                ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY.get((PlayerEntity) MinecraftClient.getInstance().player);
                if (executionerPlayerComponent!=null&&executionerPlayerComponent.target!=null) {
                    if (executionerPlayerComponent.target.equals(target.getUuid())) {
                        cir.setReturnValue(Color.orange.getRGB());
                        cir.cancel();
                    }
                }
            }
            if (!((PlayerEntity)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((PlayerEntity) target, ModRoles.VULTURE) && TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.VULTURE.color());
                    cir.cancel();
                }
            }
            if (!((PlayerEntity)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((PlayerEntity) target, ModRoles.EXECUTIONER) && TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.EXECUTIONER.color());
                    cir.cancel();
                }
            }
            if (!((PlayerEntity)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((PlayerEntity) target, ModRoles.JESTER) && TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(Color.PINK.getRGB());
                    cir.cancel();
                }
            }
            // 小透明：杀手无法看到高亮
            if (!((PlayerEntity)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((PlayerEntity) target, ModRoles.GHOST) && TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(-1);
                    cir.cancel();
                }
            }
            if (!((PlayerEntity)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.JESTER) && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(Color.PINK.getRGB());
                    cir.cancel();
                }
            }
            if (!((PlayerEntity)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.EXECUTIONER) && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.EXECUTIONER.color());
                    cir.cancel();
                }
                if (gameWorldComponent.isRole(MinecraftClient.getInstance().player, ModRoles.POSTMAN) && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.POSTMAN.color());
                    cir.cancel();
                }

            }
        }
    }
}
