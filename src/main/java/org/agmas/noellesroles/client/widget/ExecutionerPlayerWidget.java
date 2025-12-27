package org.agmas.noellesroles.client.widget;

import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.packet.ExecutionerSelectTargetC2SPacket;
import org.jetbrains.annotations.NotNull;

/**
 * Executioner选择目标的UI Widget
 * 显示可选择的平民玩家，点击后发送选择请求
 */
public class ExecutionerPlayerWidget extends ButtonWidget {
    public final AbstractClientPlayerEntity targetCandidate;

    public ExecutionerPlayerWidget(int x, int y, @NotNull AbstractClientPlayerEntity targetCandidate, int index) {
        super(x, y, 16, 16, targetCandidate.getName(), (a) -> {
            // 检查是否启用了手动选择目标功能
            if (!NoellesRolesConfig.HANDLER.instance().executionerCanSelectTarget) {
                return; // 如果未启用，则忽略点击事件
            }
            
            ExecutionerPlayerComponent component = ExecutionerPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
            if (!component.targetSelected) {
                ClientPlayNetworking.send(new ExecutionerSelectTargetC2SPacket(targetCandidate.getUuid()));
            }
        }, DEFAULT_NARRATION_SUPPLIER);
        this.targetCandidate = targetCandidate;
    }

    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        ExecutionerPlayerComponent component = ExecutionerPlayerComponent.KEY.get(MinecraftClient.getInstance().player);
        
        // 如果还没有选择目标，显示可选择的玩家
        if (!component.targetSelected) {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerSkinDrawer.draw(context, targetCandidate.getSkinTextures().texture(), this.getX(), this.getY(), 16);
            
            if (this.isHovered()) {
                this.drawShopSlotHighlight(context, this.getX(), this.getY(), 0);
                context.drawTooltip(MinecraftClient.getInstance().textRenderer, targetCandidate.getName(), 
                    this.getX() - 4 - MinecraftClient.getInstance().textRenderer.getWidth(targetCandidate.getName()) / 2, 
                    this.getY() - 9);
            }
        }
        // 如果已经选择了目标，显示灰色
        else {
            super.renderWidget(context, mouseX, mouseY, delta);
            context.setShaderColor(0.25f, 0.25f, 0.25f, 0.5f);
            context.drawGuiTexture(ShopEntry.Type.POISON.getTexture(), this.getX() - 7, this.getY() - 7, 30, 30);
            PlayerSkinDrawer.draw(context, targetCandidate.getSkinTextures().texture(), this.getX(), this.getY(), 16);
            context.setShaderColor(1f, 1f, 1f, 1f);
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