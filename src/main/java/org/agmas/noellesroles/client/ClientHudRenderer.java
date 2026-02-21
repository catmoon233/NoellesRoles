package org.agmas.noellesroles.client;

import java.awt.Color;

import org.agmas.noellesroles.AttendantHandler;
import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.entity.WheelchairEntity;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.commander.CommanderHudRender;

import dev.doctor4t.trainmurdermystery.cca.BartenderPlayerComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientHudRenderer {

    public static void registerRenderersEvent() {
        CommanderHudRender.register();
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            // 渲染老人的提示
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent == null || !TMMClient.gameComponent.isRole(client.player, ModRoles.OLDMAN)) {
                return;
            }
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();
            var font = client.font;
            int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
            int xOffset = screenWidth - 10; // 距离右边缘
            if (client.player.getVehicle() != null && client.player.getVehicle() instanceof WheelchairEntity) {
                var text = Component
                        .translatable("hud.oldman.get_back", NoellesrolesClient.abilityBind.getTranslatedKeyMessage())
                        .withStyle(ChatFormatting.AQUA);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset, Color.WHITE.getRGB());
            }

            return;
        });
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            // 渲染酒保的提示
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent != null && TMMClient.gameComponent.isRole(client.player, ModRoles.BARTENDER)) {
                var comc = BartenderPlayerComponent.KEY.maybeGet(client.player).orElse(null);
                if (comc == null)
                    return;
                if (comc.getArmor() <= 0)
                    return;
                int screenWidth = guiGraphics.guiWidth();
                int screenHeight = guiGraphics.guiHeight();
                var font = client.font;
                int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
                int xOffset = screenWidth - 10; // 距离右边缘
                var text = Component.translatable("hud.bartender.has_armor", comc.getArmor())
                        .withStyle(ChatFormatting.GOLD);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset, Color.WHITE.getRGB());
                return;
            }

            if (TMMClient.gameComponent != null && TMMClient.gameComponent.isRole(client.player, ModRoles.ATTENDANT)) {
                var comc = NoellesRolesAbilityPlayerComponent.KEY.maybeGet(client.player).orElse(null);
                if (comc == null)
                    return;
                int screenWidth = guiGraphics.guiWidth();
                int screenHeight = guiGraphics.guiHeight();
                var font = client.font;
                int yOffset = screenHeight - 10 - font.lineHeight; // 右下角
                int xOffset = screenWidth - 10; // 距离右边缘
                var text = Component.literal("");
                if (comc.cooldown <= 0) {
                    text.append(Component.translatable("hud.noellesroles.attendant.available",
                            Component.keybind("key.noellesroles.ability"), AttendantHandler.area_distance)
                            .withStyle(ChatFormatting.GOLD));
                } else {
                    text.append(Component.translatable("hud.noellesroles.attendant.cooldown", (comc.cooldown / 20))
                            .withStyle(ChatFormatting.RED));
                }

                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset, Color.WHITE.getRGB());
                return;
            }
        });
    }

}
