package org.agmas.noellesroles.client.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 纹理控件
 * 用于将纹理显示到屏幕中
 * NOTE :由于目前的作用只是用来渲染撬锁游戏，所以没有作图集相关功能适配，可以通过修改uv参数来更精确控制显示区域
 */
public class TextureWidget extends AbstractWidget {
    public TextureWidget(int i, int j, int k, int l,
                         int renderWidth, int renderHeight,
                         int textureWidth, int textureHeight,
                         int textureU, int textureV,
                         ResourceLocation texture){
        super(i, j, k, l, Component.empty());
        TEXTURE = texture;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.textureU = textureU;
        this.textureV = textureV;
    }
    // 可以只绘制一部分纹理
    public TextureWidget(int i, int j, int k, int l, int renderWidth, int renderHeight, int textureWidth, int textureHeight,
                         ResourceLocation texture){
        this(i, j, k, l, renderWidth, renderHeight, textureWidth, textureHeight, 0, 0, texture);
    }
    public TextureWidget(int i, int j, int k, int l, int textureWidth, int textureHeight, ResourceLocation texture) {
        this(i, j, k, l, textureWidth, textureHeight, textureWidth, textureHeight, texture);
    }

    // 拷贝构造
    public TextureWidget(TextureWidget lockPickBody) {
        this(lockPickBody.getX(), lockPickBody.getY(), lockPickBody.getWidth(), lockPickBody.getHeight(),
                lockPickBody.renderWidth, lockPickBody.renderHeight,
                lockPickBody.textureWidth, lockPickBody.textureHeight,
                lockPickBody.textureU, lockPickBody.textureV,
                lockPickBody.TEXTURE);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
        guiGraphics.blit(TEXTURE,
                this.getX(), this.getY(),
                this.width, this.height,
                textureU, textureV,
                this.renderWidth, this.renderHeight,
                this.textureWidth, this.textureHeight
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void setRenderSize(int renderWidth, int renderHeight){
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
    }
    public void setTextureUV(int textureU, int textureV){
        this.textureU = textureU;
        this.textureV = textureV;
    }
    protected final ResourceLocation TEXTURE;
    protected final int textureWidth;
    protected final int textureHeight;
    protected int renderWidth;
    protected int renderHeight;
    // 纹理起始顶点
    protected int textureU;
    protected int textureV;
}
