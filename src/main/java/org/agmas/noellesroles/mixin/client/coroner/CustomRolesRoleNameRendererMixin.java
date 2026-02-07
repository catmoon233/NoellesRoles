package org.agmas.noellesroles.mixin.client.coroner;

import com.llamalad7.mixinextras.sugar.Local;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import dev.doctor4t.trainmurdermystery.client.gui.RoleNameRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;
import pro.fazeclan.river.stupid_express.modifier.split_personality.cca.SplitPersonalityComponent;
import net.minecraft.network.chat.Component;

import java.awt.Color;

import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.client.HarpymodloaderClient;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.modifiers.Modifier;
import org.agmas.noellesroles.component.ModComponents;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RoleNameRenderer.class)
public abstract class CustomRolesRoleNameRendererMixin {

    @Shadow
    private static float nametagAlpha;

    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I", ordinal = 0))
    private static void b(Font renderer, @NotNull LocalPlayer lp, GuiGraphics context, DeltaTracker tickCounter,
            CallbackInfo ci) {
        if (HarpymodloaderClient.hudRole != null) {
            if (TMMClient.isPlayerSpectatingOrCreative()) {
                MutableComponent name = Harpymodloader.getRoleName(HarpymodloaderClient.hudRole);
                if (HarpymodloaderClient.modifiers != null) {
                    for (Modifier modifier : HarpymodloaderClient.modifiers) {
                        name.append(
                                Component.literal(" [").append(modifier.getName()).append("]")
                                        .withColor(modifier.color));
                    }
                }
                // 死亡惩罚
                if (Minecraft.getInstance() != null || Minecraft.getInstance().player == null)
                    return;
                Player player = Minecraft.getInstance().player;
                int di_color = HarpymodloaderClient.hudRole.color();
                var deathPenalty = ModComponents.DEATH_PENALTY.get(player.level());
                boolean hasPenalty = false;
                if (deathPenalty != null)
                    hasPenalty = deathPenalty.hasPenalty();

                final var worldModifierComponent = WorldModifierComponent.KEY
                        .get(player.level());
                if (worldModifierComponent != null) {
                    if (worldModifierComponent.isModifier(player.getUUID(), SEModifiers.SPLIT_PERSONALITY)) {
                        if (!SplitPersonalityComponent.KEY.get(player).isDeath()) {
                            hasPenalty = true;
                        }
                    }
                }

                if (hasPenalty) {
                    name = Component.translatable("message.noellesroles.penalty.limit.role");
                    di_color = Color.RED.getRGB();
                }
                context.drawString(renderer, name, -renderer.width(name) / 2, 0,
                        di_color | (int) (nametagAlpha * 255.0F) << 24);
            }

        }
    }

    @Inject(method = "renderHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDisplayName()Lnet/minecraft/network/chat/Component;"))
    private static void b(Font renderer, @NotNull LocalPlayer player, GuiGraphics context, DeltaTracker tickCounter,
            CallbackInfo ci, @Local Player target) {
        GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.level());
        WorldModifierComponent worldModifierComponent = WorldModifierComponent.KEY.get(player.level());
        if (gameWorldComponent.getRole(target) != null) {
            HarpymodloaderClient.hudRole = gameWorldComponent.getRole(target);
            HarpymodloaderClient.modifiers = worldModifierComponent.getModifiers(target);
        } else {
            HarpymodloaderClient.hudRole = TMMRoles.CIVILIAN;
            HarpymodloaderClient.modifiers = null;
        }
    }
}
