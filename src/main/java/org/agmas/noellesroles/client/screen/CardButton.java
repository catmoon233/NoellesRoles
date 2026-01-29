package org.agmas.noellesroles.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class CardButton extends AbstractButton {
    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 100;
    private static final int CARD_CORNER_RADIUS = 8;

    private final String roleName;
    private final String description;
    private final int cardColor;
    private final int highlightColor;
    private final Runnable onClick;

    private boolean isSelected = false;
    private float hoverAnimation = 0f;

    public CardButton(int x, int y, String roleName, String description,
                      int cardColor, Runnable onClick) {
        super(x, y, CARD_WIDTH, CARD_HEIGHT, Component.literal(roleName));
        this.roleName = roleName;
        this.description = description;
        this.cardColor = cardColor;
        this.highlightColor = lightenColor(cardColor, 0.3f);
        this.onClick = onClick;
    }

    private int lightenColor(int color, float factor) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        r = Math.min(255, (int)(r + (255 - r) * factor));
        g = Math.min(255, (int)(g + (255 - g) * factor));
        b = Math.min(255, (int)(b + (255 - b) * factor));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        // 更新悬停动画
        if (isHovered || isSelected) {
            hoverAnimation = Mth.lerp(0.1f, hoverAnimation, 1f);
        } else {
            hoverAnimation = Mth.lerp(0.1f, hoverAnimation, 0f);
        }

        // 计算当前颜色
        int currentColor = isSelected ? highlightColor : cardColor;
        if (hoverAnimation > 0) {
            currentColor = blendColors(currentColor, highlightColor, hoverAnimation * 0.5f);
        }

        // 绘制卡牌背景
        drawCardBackground(guiGraphics, currentColor);

        // 绘制卡牌外发光效果
        if (isSelected) {
            drawGlowEffect(guiGraphics);
        }

        // 绘制卡牌顶部装饰
        drawCardHeader(guiGraphics);

        // 绘制职业图标（这里使用占位符，实际应使用纹理）
        drawRoleIcon(guiGraphics);

        // 绘制职业名称
        drawRoleName(guiGraphics, font);

        // 绘制描述文字
        drawDescription(guiGraphics, font);

        // 绘制选择状态指示器
        if (isSelected) {
            drawSelectionIndicator(guiGraphics);
        }
    }

    private void drawCardBackground(GuiGraphics guiGraphics, int color) {
        // 绘制圆角矩形背景
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        // 主背景
        guiGraphics.fill(x, y, x + width, y + height, color);

        // 顶部渐变色条
        int gradientColor = darkenColor(color, 0.2f);
        guiGraphics.fill(x, y, x + width, y + 5, gradientColor);

        // 边框
        int borderColor = darkenColor(color, 0.3f);
        drawRoundedBorder(guiGraphics, borderColor);
    }

    private void drawRoundedBorder(GuiGraphics guiGraphics, int color) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();
        int radius = CARD_CORNER_RADIUS;

        // 简化版的圆角边框（实际项目中可能需要使用九宫格或特殊渲染）
        guiGraphics.hLine(x + radius, x + width - radius, y, color); // 上边框
        guiGraphics.hLine(x + radius, x + width - radius, y + height - 1, color); // 下边框
        guiGraphics.vLine(x, y + radius, y + height - radius, color); // 左边框
        guiGraphics.vLine(x + width - 1, y + radius, y + height - radius, color); // 右边框

        // 绘制四个角（简化版）
        for (int i = 0; i < radius; i++) {
            // 左上角
            guiGraphics.fill(x + i, y, x + i + 1, y + 1, color);
            guiGraphics.fill(x, y + i, x + 1, y + i + 1, color);
            // 右上角
            guiGraphics.fill(x + width - i - 1, y, x + width - i, y + 1, color);
            guiGraphics.fill(x + width - 1, y + i, x + width, y + i + 1, color);
            // 左下角
            guiGraphics.fill(x + i, y + height - 1, x + i + 1, y + height, color);
            guiGraphics.fill(x, y + height - i - 1, x + 1, y + height - i, color);
            // 右下角
            guiGraphics.fill(x + width - i - 1, y + height - 1, x + width - i, y + height, color);
            guiGraphics.fill(x + width - 1, y + height - i - 1, x + width, y + height - i, color);
        }
    }

    private int darkenColor(int color, float factor) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;

        r = (int)(r * (1 - factor));
        g = (int)(g * (1 - factor));
        b = (int)(b * (1 - factor));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int blendColors(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        int r = (int)(r1 + (r2 - r1) * ratio);
        int g = (int)(g1 + (g2 - g1) * ratio);
        int b = (int)(b1 + (b2 - b1) * ratio);
        int a = (int)(a1 + (a2 - a1) * ratio);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private void drawGlowEffect(GuiGraphics guiGraphics) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        int glowColor = 0x40FFFFFF; // 半透明白色发光

        // 简单的发光效果
        for (int i = 1; i <= 3; i++) {
            int glowAlpha = (int)((1f - i * 0.2f) * 64);
            int currentGlowColor = (glowAlpha << 24) | 0xFFFFFF;

            guiGraphics.fill(
                    x - i, y - i,
                    x + width + i, y + height + i,
                    currentGlowColor
            );
        }
    }

    private void drawCardHeader(GuiGraphics guiGraphics) {
        int x = getX();
        int y = getY();
        int width = getWidth();

        // 绘制装饰性顶部
        int headerColor = 0x80000000; // 半透明黑色
        guiGraphics.fill(x, y, x + width, y + 25, headerColor);

        // 绘制装饰线
        guiGraphics.fill(x + 10, y + 22, x + width - 10, y + 23, 0x80FFFFFF);
    }

    private void drawRoleIcon(GuiGraphics guiGraphics) {
        int x = getX();
        int y = getY();
        int width = getWidth();

        // 图标位置
        int iconX = x + width / 2 - 16;
        int iconY = y + 30;

        // 绘制图标背景（圆形）
        guiGraphics.fill(iconX - 2, iconY - 2, iconX + 34, iconY + 34, 0x80000000);

        // 绘制图标占位符（实际应使用纹理）
        guiGraphics.fill(iconX, iconY, iconX + 32, iconY + 32, 0xFF888888);

        // 图标边框
        guiGraphics.hLine(iconX, iconX + 31, iconY, 0xFFCCCCCC);
        guiGraphics.hLine(iconX, iconX + 31, iconY + 31, 0xFFCCCCCC);
        guiGraphics.vLine(iconX, iconY, iconY + 31, 0xFFCCCCCC);
        guiGraphics.vLine(iconX + 31, iconY, iconY + 31, 0xFFCCCCCC);
    }

    private void drawRoleName(GuiGraphics guiGraphics, Font font) {
        int x = getX();
        int y = getY();
        int width = getWidth();

        // 职业名称
        int textX = x + width / 2;
        int textY = y + 70;

        // 文字阴影
        guiGraphics.drawCenteredString(font, roleName, textX + 1, textY + 1, 0x80000000);

        // 文字主体
        int textColor = isSelected ? 0xFFFFFF00 : 0xFFFFFFFF; // 选中时为黄色
        guiGraphics.drawCenteredString(font, roleName, textX, textY, textColor);
    }

    private void drawDescription(GuiGraphics guiGraphics, Font font) {
        int x = getX();
        int y = getY();
        int width = getWidth();

        // 描述文字
        int descY = y + 85;

        // 如果描述文字太长，可以分割显示
        String[] words = description.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (font.width(line + " " + word) < width - 20) {
                if (!line.isEmpty()) line.append(" ");
                line.append(word);
            } else {
                // 绘制当前行
                guiGraphics.drawCenteredString(font, line.toString(), x + width / 2, descY, 0xFFCCCCCC);
                descY += 10;
                line = new StringBuilder(word);
            }
        }

        if (!line.isEmpty()) {
            guiGraphics.drawCenteredString(font, line.toString(), x + width / 2, descY, 0xFFCCCCCC);
        }
    }

    private void drawSelectionIndicator(GuiGraphics guiGraphics) {
        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        // 选中时的边框指示器
        int indicatorColor = 0xFFFFD700; // 金色

        // 绘制加粗边框
        for (int i = 0; i < 3; i++) {
            guiGraphics.hLine(x + i, x + width - i - 1, y + i, indicatorColor);
            guiGraphics.hLine(x + i, x + width - i - 1, y + height - i - 1, indicatorColor);
            guiGraphics.vLine(x + i, y + i, y + height - i - 1, indicatorColor);
            guiGraphics.vLine(x + width - i - 1, y + i, y + height - i - 1, indicatorColor);
        }

        // 绘制角标
        int cornerSize = 8;
        guiGraphics.fill(x + 2, y + 2, x + cornerSize, y + 6, indicatorColor);
        guiGraphics.fill(x + 2, y + 2, x + 6, y + cornerSize, indicatorColor);

        guiGraphics.fill(x + width - cornerSize, y + 2, x + width - 2, y + 6, indicatorColor);
        guiGraphics.fill(x + width - 6, y + 2, x + width - 2, y + cornerSize, indicatorColor);

        guiGraphics.fill(x + 2, y + height - 6, x + cornerSize, y + height - 2, indicatorColor);
        guiGraphics.fill(x + 2, y + height - cornerSize, x + 6, y + height - 2, indicatorColor);

        guiGraphics.fill(x + width - cornerSize, y + height - 6, x + width - 2, y + height - 2, indicatorColor);
        guiGraphics.fill(x + width - 6, y + height - cornerSize, x + width - 2, y + height - 2, indicatorColor);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public void onPress() {
        if (onClick != null) {
            onClick.run();
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        // 辅助功能支持
        narrationElementOutput.add(NarratedElementType.USAGE, roleName + ": " + description);
    }
}