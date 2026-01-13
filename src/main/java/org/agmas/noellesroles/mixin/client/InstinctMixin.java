package org.agmas.noellesroles.mixin.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPoisonComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import org.agmas.noellesroles.component.AdmirerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@Mixin(TMMClient.class)
public abstract class InstinctMixin {


    @Shadow public static KeyMapping instinctKeybind;

    @Inject(method = "isInstinctEnabled", at = @At("HEAD"), cancellable = true)
    private static void b(CallbackInfoReturnable<Boolean> cir) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(Minecraft.getInstance().player.level());
        if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.JESTER)) {
            if (instinctKeybind.isDown()) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    @Inject(method = "getInstinctHighlight", at = @At("HEAD"), cancellable = true)
    private static void getInstinctHighlightColor(Entity target, CallbackInfoReturnable<Integer> cir) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(Minecraft.getInstance().player.level());
        if (target instanceof Player) {
            if (!((Player)target).isSpectator()) {
                BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get((Player) target);
                PlayerPoisonComponent playerPoisonComponent =  PlayerPoisonComponent.KEY.get((Player) target);
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BARTENDER) && bartenderPlayerComponent.glowTicks > 0) {
                    cir.setReturnValue(Color.GREEN.getRGB());
                }
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BARTENDER) && bartenderPlayerComponent.armor > 0) {
                    cir.setReturnValue(Color.BLUE.getRGB());
                    cir.cancel();
                }
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.BARTENDER) && playerPoisonComponent.poisonTicks > 0) {
                    cir.setReturnValue(Color.RED.getRGB());
                }
            }
        }
        if (target instanceof Player) {
            if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.EXECUTIONER)) {
                ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY.get((Player) Minecraft.getInstance().player);
                if (executionerPlayerComponent!=null&&executionerPlayerComponent.target!=null) {
                    if (executionerPlayerComponent.target.equals(target.getUUID())) {
                        cir.setReturnValue(Color.orange.getRGB());
                        cir.cancel();
                    }
                }
            }
            if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.ADMIRER)) {
                AdmirerPlayerComponent admirerPlayerComponent = (AdmirerPlayerComponent) AdmirerPlayerComponent.KEY.get((Player) Minecraft.getInstance().player);
                if (admirerPlayerComponent!=null&&admirerPlayerComponent.getBoundTarget()!=null) {
                    if (admirerPlayerComponent.getBoundTarget().getUUID().equals(target.getUUID())) {
                        cir.setReturnValue(Color.PINK.getRGB());
                        cir.cancel();
                    }
                }
            }
            if (!((Player)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((Player) target, ModRoles.VULTURE) && TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.VULTURE.color());
                    cir.cancel();
                }
            }
            if (!((Player)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((Player) target, ModRoles.EXECUTIONER) && TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.EXECUTIONER.color());
                    cir.cancel();
                }
            }
            if (!((Player)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((Player) target, ModRoles.JESTER) && TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(Color.PINK.getRGB());
                    cir.cancel();
                }
            }
            // 小透明：杀手无法看到高亮
            if (!((Player)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole((Player) target, ModRoles.GHOST) && TMMClient.isKiller() && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(-1);
                    cir.cancel();
                }
            }
            if (!((Player)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.JESTER) && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(Color.PINK.getRGB());
                    cir.cancel();
                }
            }
            if (!((Player)target).isSpectator() && TMMClient.isInstinctEnabled()) {
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.EXECUTIONER) && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.EXECUTIONER.color());
                    cir.cancel();
                }
                if (gameWorldComponent.isRole(Minecraft.getInstance().player, ModRoles.POSTMAN) && TMMClient.isPlayerAliveAndInSurvival()) {
                    cir.setReturnValue(ModRoles.POSTMAN.color());
                    cir.cancel();
                }

            }
        }
    }
}
