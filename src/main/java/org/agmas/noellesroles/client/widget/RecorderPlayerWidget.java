package org.agmas.noellesroles.client.widget;

import org.agmas.noellesroles.client.screen.RecorderScreen;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.UUID;

public class RecorderPlayerWidget extends Button {

    public final RecorderScreen screen;
    public final UUID playerUuid;
    public final String playerName;
    private final ResourceLocation skinTexture;
    private final int size;
    public boolean highlight = true;

    public RecorderPlayerWidget(RecorderScreen screen, int x, int y, int size,
            UUID playerUuid, String playerName, ResourceLocation skinTexture, int index) {
        super(x, y, size, size, Component.literal(playerName),
                (button) -> screen.onPlayerSelected(playerUuid, playerName),
                DEFAULT_NARRATION);
        this.screen = screen;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.skinTexture = skinTexture;
        this.size = size;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {

        int bgColor = this.isHovered() ? new Color(100, 50, 150, 200).getRGB() : new Color(50, 25, 75, 150).getRGB();
        context.fill(getX() - 2, getY() - 2, getX() + size + 2, getY() + size + 2, bgColor);

        int borderColor = this.isHovered() ? new Color(150, 100, 200).getRGB() : new Color(75, 50, 100).getRGB();
        context.renderOutline(getX() - 2, getY() - 2, size + 4, size + 4, borderColor);

        if (skinTexture != null) {
            if (highlight || this.isHovered())
                PlayerFaceRenderer.draw(context, skinTexture, getX(), getY(), size);
            else {
                context.fill(getX(), getY(), getX() + size, getY() + size, new Color(127, 127, 127).getRGB());
            }
        }

        if (this.isHovered()) {
            drawShopSlotHighlight(context, getX(), getY(), 0);

            Font textRenderer = Minecraft.getInstance().font;
            int textWidth = textRenderer.width(getMessage());
            context.renderTooltip(textRenderer, getMessage(),
                    getX() + size / 2 - textWidth / 2, getY() - 12);
        }
    }

    private void drawShopSlotHighlight(GuiGraphics context, int x, int y, int z) {
        int color = new Color(150, 100, 200, 100).getRGB();
        context.fillGradient(RenderType.guiOverlay(), x, y, x + size, y + size, color, color, z);
    }

    @Override
    public void renderString(GuiGraphics context, Font textRenderer, int color) {
    }
}