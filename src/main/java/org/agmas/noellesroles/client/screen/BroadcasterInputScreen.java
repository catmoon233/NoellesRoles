package org.agmas.noellesroles.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.agmas.noellesroles.packet.BroadcasterC2SPacket;

/**
 * 广播者输入屏幕 - 允许玩家输入广播消息
 */
public class BroadcasterInputScreen extends Screen {
    private EditBox messageField;
    private Button sendButton;
    private Button cancelButton;
    private final Screen parent;

    public BroadcasterInputScreen(Screen parent) {
        super(Component.translatable("screen.broadcaster.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        // 计算居中位置
        int fieldWidth = 200;
        int fieldHeight = 20;
        int x = (this.width - fieldWidth) / 2;
        int y = this.height / 2 - 10;

        // 创建文本框
        this.messageField = new EditBox(this.font, x, y, fieldWidth, fieldHeight, Component.translatable("screen.broadcaster.hint"));
        this.messageField.setMaxLength(256);
        this.messageField.setCanLoseFocus(false);
        this.messageField.setFocused(true);
        this.messageField.setTextColor(0xFFFFFF); // 白色文本以提高对比度
        this.addWidget(this.messageField);
        this.setInitialFocus(this.messageField);

        // 发送按钮
        this.sendButton = Button.builder(Component.translatable("screen.broadcaster.send"), button -> sendMessage())
                .bounds(x, y + fieldHeight + 10, fieldWidth / 2 - 5, 20)
                .build();
        this.addRenderableWidget(this.sendButton);

        // 取消按钮
        this.cancelButton = Button.builder(Component.translatable("screen.broadcaster.cancel"), button -> onClose())
                .bounds(x + fieldWidth / 2 + 5, y + fieldHeight + 10, fieldWidth / 2 - 5, 20)
                .build();
        this.addRenderableWidget(this.cancelButton);
    }

    private void sendMessage() {
        String message = this.messageField.getValue().trim();
        if (!message.isEmpty()) {
            // 发送包到服务器
            ClientPlayNetworking.send(new BroadcasterC2SPacket(message));
        }
        closeScreen();
    }

    private void closeScreen() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // 绘制自定义背景（70% 不透明以消除模糊）
        context.fill(0, 0, this.width, this.height, 0xB3000000);
        // 渲染标题（无背景矩形，因为背景已足够深）
        int titleY = this.height / 2 - 40;
        context.drawCenteredString(this.font, this.title, this.width / 2, titleY, 0xFFFFFF);
        
        // 绘制文本框的不透明背景以提高可读性
        int fieldWidth = 200;
        int fieldHeight = 20;
        int x = (this.width - fieldWidth) / 2;
        int y = this.height / 2 - 10;
        // 扩大背景区域以完全覆盖文本框的边框
        context.fill(x - 4, y - 4, x + fieldWidth + 4, y + fieldHeight + 4, 0xB3000000);
        
        // 渲染文本框
        this.messageField.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 按Enter发送
        if (keyCode == 257) {
            this.sendMessage();
            return true;
        }
        // 按Esc取消
        if (keyCode == 256) {
            this.closeScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        this.closeScreen();
    }
}