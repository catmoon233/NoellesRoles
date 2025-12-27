package org.agmas.noellesroles.client.widget;

import org.agmas.noellesroles.client.screen.ConspiratorScreen;
import dev.doctor4t.trainmurdermystery.api.Role;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * 阴谋家角色选择 Widget
 * 
 * 显示角色名称和颜色，点击选择该角色进行猜测
 */
public class ConspiratorRoleWidget extends ButtonWidget {
    
    public final ConspiratorScreen screen;
    public final Role role;
    private final int buttonWidth;
    private final int buttonHeight;
    
    public ConspiratorRoleWidget(ConspiratorScreen screen, int x, int y, int width, int height,
                                 @NotNull Role role, int index) {
        super(x, y, width, height, getRoleName(role),
            (button) -> screen.onRoleSelected(role),
            DEFAULT_NARRATION_SUPPLIER);
        this.screen = screen;
        this.role = role;
        this.buttonWidth = width;
        this.buttonHeight = height;
    }
    
    /**
     * 获取角色的显示名称
     */
    private static Text getRoleName(Role role) {
        // 尝试获取翻译后的角色名称
        String translationKey = "announcement.role." + role.identifier().getPath();
        return Text.translatable(translationKey);
    }
    
    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        
        // 获取角色颜色
        Color roleColor = new Color(role.color());
        
        // 背景颜色 - 使用角色颜色的暗化版本
        int bgAlpha = this.isHovered() ? 200 : 150;
        Color bgColor = new Color(
            Math.max(0, roleColor.getRed() / 3),
            Math.max(0, roleColor.getGreen() / 3),
            Math.max(0, roleColor.getBlue() / 3),
            bgAlpha
        );
        
        // 绘制背景
        context.fill(getX(), getY(), getX() + buttonWidth, getY() + buttonHeight, bgColor.getRGB());
        
        // 边框颜色 - 使用角色颜色
        Color borderColor = this.isHovered() 
            ? new Color(
                Math.min(255, roleColor.getRed() + 50),
                Math.min(255, roleColor.getGreen() + 50),
                Math.min(255, roleColor.getBlue() + 50)
            )
            : roleColor;
        
        // 绘制边框
        context.drawBorder(getX(), getY(), buttonWidth, buttonHeight, borderColor.getRGB());
        
        // 绘制角色名称
        Text roleName = getRoleName(role);
        int textWidth = textRenderer.getWidth(roleName);
        int textX = getX() + (buttonWidth - textWidth) / 2;
        int textY = getY() + (buttonHeight - 8) / 2;
        
        // 使用角色颜色绘制文字
        context.drawTextWithShadow(textRenderer, roleName, textX, textY, role.color());
        
        // 高亮效果
        if (this.isHovered()) {
            drawSlotHighlight(context, getX(), getY());
        }
    }
    
    private void drawSlotHighlight(DrawContext context, int x, int y) {
        Color roleColor = new Color(role.color());
        int color = new Color(roleColor.getRed(), roleColor.getGreen(), roleColor.getBlue(), 80).getRGB();
        context.fillGradient(RenderLayer.getGuiOverlay(), x, y, x + buttonWidth, y + buttonHeight, color, color, 0);
    }
    
    @Override
    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        // 不使用默认消息绘制
    }
}