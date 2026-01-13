package org.agmas.noellesroles.client.widget;

import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import org.agmas.noellesroles.packet.MorphC2SPacket;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class MorphlingPlayerWidget extends Button{
    public final LimitedInventoryScreen screen;
    public final AbstractClientPlayer disguiseTarget;

    public MorphlingPlayerWidget(LimitedInventoryScreen screen, int x, int y, @NotNull AbstractClientPlayer disguiseTarget, int index) {
        super(x, y, 16, 16, disguiseTarget.getName(), (a) -> {if ((MorphlingPlayerComponent.KEY.get(Minecraft.getInstance().player)).getMorphTicks() == 0) {ClientPlayNetworking.send(new MorphC2SPacket(disguiseTarget.getUUID()));}}, DEFAULT_NARRATION);
        this.screen = screen;
        this.disguiseTarget = disguiseTarget;
    }

    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if ((MorphlingPlayerComponent.KEY.get(Minecraft.getInstance().player)).getMorphTicks() == 0) {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.blitSprite(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerFaceRenderer.draw(context, disguiseTarget.getSkin().texture(), this.getX(), this.getY(), 16);
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.renderTooltip(Minecraft.getInstance().font, disguiseTarget.getName(), this.getX() - 4 - Minecraft.getInstance().font.width(disguiseTarget.getName()) / 2, this.getY() - 9);
            }

        }

        if ((MorphlingPlayerComponent.KEY.get(Minecraft.getInstance().player)).getMorphTicks() < 0) {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.setColor(0.25f,0.25f,0.25f,0.5f);
            context.blitSprite(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerFaceRenderer.draw(context, disguiseTarget.getSkin().texture(), this.getX(), this.getY(), 16);
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.renderTooltip(Minecraft.getInstance().font, disguiseTarget.getName(), this.getX() - 4 - Minecraft.getInstance().font.width(disguiseTarget.getName()) / 2, this.getY() - 9);
            }


            context.setColor(1f,1f,1f,1f);
            context.drawString(Minecraft.getInstance().font, -MorphlingPlayerComponent.KEY.get(Minecraft.getInstance().player).getMorphTicks()/20+"",this.getX(),this.getY(), Color.RED.getRGB(),true);

        }

    }

    private void drawShopSlotHighlight(GuiGraphics context, int x, int y, int z) {
        int color = -1862287543;
        context.fillGradient(RenderType.guiOverlay(), x, y, x + 16, y + 14, color, color, z);
        context.fillGradient(RenderType.guiOverlay(), x, y + 14, x + 15, y + 15, color, color, z);
        context.fillGradient(RenderType.guiOverlay(), x, y + 15, x + 14, y + 16, color, color, z);
    }

    public void renderString(GuiGraphics context, Font textRenderer, int color) {
    }

}
