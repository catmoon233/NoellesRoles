package org.agmas.noellesroles.roles.commander;

import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.client.TMMClient;
import io.github.mortuusars.exposure.util.color.Color;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class CommanderHudRender {
    public static void register() {
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            var client = Minecraft.getInstance();
            if (client == null)
                return;
            if (client.player == null)
                return;
            if (TMMClient.gameComponent != null && TMMClient.gameComponent.isRole(client.player, ModRoles.COMMANDER)) {
                var comc = NoellesRolesAbilityPlayerComponent.KEY.maybeGet(client.player).orElse(null);
                if (comc == null)
                    return;
                int screenWidth = guiGraphics.guiWidth();
                int screenHeight = guiGraphics.guiHeight();
                var font = client.font;
                int yOffset = screenHeight - 14 - font.lineHeight * 2; // 右下角
                int xOffset = screenWidth - 10; // 距离右边缘
                var channelText = Component.translatable("message.commander.channel.normal")
                        .withStyle(ChatFormatting.GREEN);
                var channelTip = Component.translatable("message.commander.channel.normal.tip")
                        .withStyle(ChatFormatting.WHITE);
                if (comc.status == 1) {
                    channelText = Component.translatable("message.commander.channel.killer")
                            .withStyle(ChatFormatting.RED);
                    channelTip = Component.translatable("message.commander.channel.killer.tip")
                            .withStyle(ChatFormatting.WHITE);
                }
                var text = Component.translatable("message.commander.channel.tip", channelText)
                        .withStyle(ChatFormatting.GOLD);
                guiGraphics.drawString(font, text, xOffset - font.width(text), yOffset, Color.WHITE.getRGB());
                guiGraphics.drawString(font, channelTip, xOffset - font.width(channelTip), yOffset + 2 + font.lineHeight,
                        Color.WHITE.getRGB());
                return;
            }
        });
    }
}
