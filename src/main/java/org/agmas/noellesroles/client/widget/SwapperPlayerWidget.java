package org.agmas.noellesroles.client.widget;

import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.packet.SwapperC2SPacket;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

public class SwapperPlayerWidget extends Button{
    public final LimitedInventoryScreen screen;
    public final PlayerInfo disguiseTarget;

    public static UUID playerChoiceOne = null;

    public SwapperPlayerWidget(LimitedInventoryScreen screen, int x, int y, @NotNull PlayerInfo disguiseTarget, int index) {
        super(x, y, 16, 16, Component.nullToEmpty(disguiseTarget.getProfile().getName()), (a) -> {
            if ((NoellesRolesAbilityPlayerComponent.KEY.get(Minecraft.getInstance().player)).cooldown == 0) {
                if (Minecraft.getInstance().player.level().getPlayerByUUID(disguiseTarget.getProfile().getId()) == null) return;
                if (Minecraft.getInstance().player.level().getPlayerByUUID(disguiseTarget.getProfile().getId()).isPassenger()) return;
                if (playerChoiceOne != null) {
                    ClientPlayNetworking.send(new SwapperC2SPacket(playerChoiceOne, disguiseTarget.getProfile().getId()));
                    playerChoiceOne = null;
                } else {
                    playerChoiceOne = disguiseTarget.getProfile().getId();
                }
            }
        }, DEFAULT_NARRATION);
        this.screen = screen;
        this.disguiseTarget = disguiseTarget;
    }

    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        if ((NoellesRolesAbilityPlayerComponent.KEY.get(Minecraft.getInstance().player)).cooldown == 0) {
            context.blitSprite(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerFaceRenderer.draw(context, disguiseTarget.getSkin().texture(), this.getX(), this.getY(), 16);
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.renderTooltip(Minecraft.getInstance().font, Component.nullToEmpty(disguiseTarget.getProfile().getName()), this.getX() - 4 - Minecraft.getInstance().font.width(disguiseTarget.getProfile().getName()) / 2, this.getY() - 9);
            }

        }

        if ((NoellesRolesAbilityPlayerComponent.KEY.get(Minecraft.getInstance().player)).cooldown > 0) {
            context.setColor(0.25f,0.25f,0.25f,0.5f);
            context.blitSprite(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerFaceRenderer.draw(context, disguiseTarget.getSkin().texture(), this.getX(), this.getY(), 16);
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.renderTooltip(Minecraft.getInstance().font, Component.nullToEmpty(disguiseTarget.getProfile().getName()), this.getX() - 4 - Minecraft.getInstance().font.width(disguiseTarget.getProfile().getName()) / 2, this.getY() - 9);
            }


            context.setColor(1f,1f,1f,1f);
            context.drawString(Minecraft.getInstance().font, NoellesRolesAbilityPlayerComponent.KEY.get(Minecraft.getInstance().player).cooldown/20+"",this.getX(),this.getY(), Color.RED.getRGB(),true);

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
