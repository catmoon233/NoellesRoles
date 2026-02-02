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
import org.agmas.noellesroles.component.NoellesRolesAbilityPlayerComponent;
import org.agmas.noellesroles.packet.ManipulatorC2SPacket;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ManipulatorPlayerWidget extends Button {
    public final LimitedInventoryScreen screen;
    public final AbstractClientPlayer targetPlayer;

    public ManipulatorPlayerWidget(LimitedInventoryScreen screen, int x, int y,
            @NotNull AbstractClientPlayer targetPlayer, int index) {
        super(x, y, 16, 16, targetPlayer.getName(), (button) -> {
            ManipulatorPlayerComponent manipulatorComp = ManipulatorPlayerComponent.KEY
                    .get(Minecraft.getInstance().player);
            NoellesRolesAbilityPlayerComponent abilityComp = NoellesRolesAbilityPlayerComponent.KEY.get(Minecraft.getInstance().player);

            if (abilityComp.cooldown <= 0 && !manipulatorComp.isControlling) {
                ClientPlayNetworking.send(new ManipulatorC2SPacket(targetPlayer.getUUID()));
            }
        }, DEFAULT_NARRATION);
        this.screen = screen;
        this.targetPlayer = targetPlayer;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        ManipulatorPlayerComponent manipulatorComp = ManipulatorPlayerComponent.KEY.get(Minecraft.getInstance().player);
        NoellesRolesAbilityPlayerComponent abilityComp = NoellesRolesAbilityPlayerComponent.KEY.get(Minecraft.getInstance().player);

        boolean canControl = abilityComp.cooldown <= 0 && !manipulatorComp.isControlling;

        if (canControl) {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.blitSprite(ShopEntry.Type.WEAPON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerFaceRenderer.draw(context, targetPlayer.getSkin().texture(), this.getX(), this.getY(), 16);

            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.renderTooltip(Minecraft.getInstance().font, targetPlayer.getName(),
                        this.getX() - 4 - Minecraft.getInstance().font.width(targetPlayer.getName()) / 2,
                        this.getY() - 9);
            }
        } else {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.setColor(0.25f, 0.25f, 0.25f, 0.5f);
            context.blitSprite(ShopEntry.Type.WEAPON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerFaceRenderer.draw(context, targetPlayer.getSkin().texture(), this.getX(), this.getY(), 16);

            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.renderTooltip(Minecraft.getInstance().font, targetPlayer.getName(),
                        this.getX() - 4 - Minecraft.getInstance().font.width(targetPlayer.getName()) / 2,
                        this.getY() - 9);
            }

            context.setColor(1f, 1f, 1f, 1f);

            if (abilityComp.cooldown > 0) {
                int cooldownSeconds = abilityComp.cooldown / 20;
                context.drawString(Minecraft.getInstance().font, cooldownSeconds + "s",
                        this.getX(), this.getY(), Color.RED.getRGB(), true);
            }
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