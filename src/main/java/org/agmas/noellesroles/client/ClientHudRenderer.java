package org.agmas.noellesroles.client;

import java.awt.Color;
import org.agmas.noellesroles.role.ModRoles;

import dev.doctor4t.trainmurdermystery.cca.BartenderPlayerComponent;
import dev.doctor4t.trainmurdermystery.client.TMMClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientHudRenderer {

    public static void registerRenderersEvent() {
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
            }
        });
    }

}
