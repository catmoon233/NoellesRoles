package org.agmas.noellesroles.client.widget;

import org.agmas.noellesroles.client.screen.RecorderScreen;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;

public class RecorderPlayerWidget extends Button {

    public final RecorderScreen screen;
    public final AbstractClientPlayer player;
    private final int size;

    public RecorderPlayerWidget(RecorderScreen screen, int x, int y, int size,
            @NotNull AbstractClientPlayer player, int index) {
        super(x, y, size, size, player.getName(),
                (button) -> screen.onPlayerSelected(player.getUUID(), player.getName().getString()),
                DEFAULT_NARRATION);
        this.screen = screen;
        this.player = player;
        this.size = size;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        int bgColor = this.isHovered() ? new Color(100, 50, 150, 200).getRGB() : new Color(50, 25, 75, 150).getRGB();
        context.fill(getX() - 2, getY() - 2, getX() + size + 2, getY() + size + 2, bgColor);

        int borderColor = this.isHovered() ? new Color(150, 100, 200).getRGB() : new Color(75, 50, 100).getRGB();
        context.renderOutline(getX() - 2, getY() - 2, size + 4, size + 4, borderColor);

        PlayerFaceRenderer.draw(context, player.getSkin().texture(), getX(), getY(), size);

        if (this.isHovered()) {
            drawShopSlotHighlight(context, getX(), getY(), 0);

            Font textRenderer = Minecraft.getInstance().font;
            int textWidth = textRenderer.width(player.getName());
            context.renderTooltip(textRenderer, player.getName(),
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