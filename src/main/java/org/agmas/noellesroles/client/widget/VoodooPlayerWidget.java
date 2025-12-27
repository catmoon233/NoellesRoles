package org.agmas.noellesroles.client.widget;

import dev.doctor4t.trainmurdermystery.client.gui.screen.ingame.LimitedInventoryScreen;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.packet.MorphC2SPacket;
import org.agmas.noellesroles.roles.voodoo.VoodooPlayerComponent;

import java.awt.*;
import java.util.UUID;

public class VoodooPlayerWidget extends ButtonWidget{
    public final LimitedInventoryScreen screen;
    public final UUID targetUUID;
    public final PlayerListEntry targetPlayerEntry;


    public VoodooPlayerWidget(LimitedInventoryScreen screen, int x, int y, UUID targetUUID, PlayerListEntry targetPlayerEntry, World world, int index) {
        super(x, y, 16, 16, Text.literal(""), (a) -> {
            ClientPlayNetworking.send(new MorphC2SPacket(targetUUID));
        }, DEFAULT_NARRATION_SUPPLIER);
        this.screen = screen;
        this.targetPlayerEntry = targetPlayerEntry;
        this.targetUUID = targetUUID;
    }

    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);
        final var player = MinecraftClient.getInstance().player;
        if (player==null)return;
        if (targetPlayerEntry==null)return;

        VoodooPlayerComponent voodooPlayerComponent = (VoodooPlayerComponent) VoodooPlayerComponent.KEY.get(player);

        final var abilityPlayerComponent = AbilityPlayerComponent.KEY.get(player);
        if (abilityPlayerComponent==null)return;
        final var target = voodooPlayerComponent.target;
        if (target==null)return;
        
        // 检查皮肤纹理是否存在，避免空指针异常
        var skinTextures = targetPlayerEntry.getSkinTextures();
        if (skinTextures == null || skinTextures.texture() == null) return;

        final var textRenderer = MinecraftClient.getInstance().textRenderer;
        if (textRenderer == null) return;
        if (abilityPlayerComponent.cooldown == 0) {
            context.drawGuiTexture(ShopEntry.Type.TOOL.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerSkinDrawer.draw(context, skinTextures.texture(), this.getX(), this.getY(), 16);
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                final var displayName = targetPlayerEntry.getProfile().getName();
                if (displayName!=null){
                context.drawTooltip(textRenderer, Text.of(displayName), this.getX() - 4 - 10, this.getY() - 9);
}
            }

            if (target.equals(targetUUID)) {

                context.drawTooltip(textRenderer, Text.literal("选择"), this.getX() - 4 - textRenderer.getWidth("选择") / 2, this.getY() - 9);
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
            }
        }

        if (abilityPlayerComponent.cooldown > 0) {
            context.setShaderColor(0.25f,0.25f,0.25f,0.5f);
            context.drawGuiTexture(ShopEntry.Type.TOOL.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerSkinDrawer.draw(context, skinTextures.texture(), this.getX(), this.getY(), 16);
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
            }

            if (target.equals(targetUUID)) {
                context.drawTooltip(textRenderer, Text.literal("Selected"), this.getX() - 4 - textRenderer.getWidth("Selected") / 2, this.getY() - 9);
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
            }
            context.setShaderColor(1f,1f,1f,1f);
            context.drawText(textRenderer, abilityPlayerComponent.cooldown/20+"",this.getX(),this.getY(), Color.RED.getRGB(),true);

        }

    }

    private void drawShopSlotHighlight(DrawContext context, int x, int y, int z) {
        int color = -1862287543;
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y, x + 16, y + 14, color, color, z);
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y + 14, x + 15, y + 15, color, color, z);
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y + 15, x + 14, y + 16, color, color, z);
    }

    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
    }

}
