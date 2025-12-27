package org.agmas.noellesroles.client.screen;

import org.agmas.noellesroles.component.TelegrapherPlayerComponent;
import org.agmas.noellesroles.packet.TelegrapherC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 电报员消息编辑屏幕
 * 
 * 功能：
 * - 输入匿名消息文本
 * - 点击确认发送给所有玩家
 * - 显示剩余使用次数
 */
public class TelegrapherScreen extends Screen {
    
    // 文本输入框
    private TextFieldWidget messageField;
    
    // 确认按钮
    private ButtonWidget confirmButton;
    
    // 剩余使用次数
    private int remainingUses = 0;
    
    public TelegrapherScreen() {
        super(Text.translatable("screen.noellesroles.telegrapher.title"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 获取剩余使用次数
        if (client != null && client.player != null) {
            TelegrapherPlayerComponent component = TelegrapherPlayerComponent.KEY.get(client.player);
            remainingUses = component.remainingUses;
        }
        
        // 创建文本输入框
        int fieldWidth = 300;
        int fieldHeight = 20;
        int fieldX = (width - fieldWidth) / 2;
        int fieldY = height / 2 - 10;
        
        messageField = new TextFieldWidget(
            textRenderer,
            fieldX,
            fieldY,
            fieldWidth,
            fieldHeight,
            Text.translatable("screen.noellesroles.telegrapher.message")
        );
        messageField.setMaxLength(200); // 限制最大长度
        messageField.setPlaceholder(Text.translatable("screen.noellesroles.telegrapher.placeholder")
            .formatted(Formatting.GRAY));
        addSelectableChild(messageField);
        setInitialFocus(messageField);
        
        // 创建确认按钮
        int buttonWidth = 100;
        int buttonHeight = 20;
        int buttonX = (width - buttonWidth) / 2;
        int buttonY = fieldY + fieldHeight + 10;
        
        confirmButton = ButtonWidget.builder(
            Text.translatable("screen.noellesroles.telegrapher.confirm"),
            button -> onConfirm()
        )
        .dimensions(buttonX, buttonY, buttonWidth, buttonHeight)
        .build();
        
        addDrawableChild(confirmButton);
    }
    
    /**
     * 确认按钮被点击
     */
    private void onConfirm() {
        if (client == null || client.player == null) return;
        
        String message = messageField.getText().trim();
        
        // 验证消息不为空
        if (message.isEmpty()) {
            client.player.sendMessage(
                Text.translatable("message.noellesroles.telegrapher.empty")
                    .formatted(Formatting.RED),
                false
            );
            return;
        }
        
        // 验证还有剩余次数
        if (remainingUses <= 0) {
            client.player.sendMessage(
                Text.translatable("message.noellesroles.telegrapher.no_uses")
                    .formatted(Formatting.RED),
                false
            );
            close();
            return;
        }
        
        // 发送网络包到服务端
        ClientPlayNetworking.send(new TelegrapherC2SPacket(message));
        
        // 关闭屏幕
        close();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渲染背景
        renderBackground(context, mouseX, mouseY, delta);
        
        // 渲染标题
        Text title = Text.translatable("screen.noellesroles.telegrapher.title")
            .formatted(Formatting.AQUA, Formatting.BOLD);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 40, 0xFFFFFF);
        
        // 渲染剩余次数提示
        Text usesText = Text.translatable("screen.noellesroles.telegrapher.remaining", remainingUses)
            .formatted(remainingUses > 0 ? Formatting.GREEN : Formatting.RED);
        context.drawCenteredTextWithShadow(textRenderer, usesText, width / 2, 60, 0xFFFFFF);
        
        // 渲染说明文本
        Text hint = Text.translatable("screen.noellesroles.telegrapher.hint")
            .formatted(Formatting.GRAY);
        context.drawCenteredTextWithShadow(textRenderer, hint, width / 2, height / 2 - 30, 0x888888);
        
        super.render(context, mouseX, mouseY, delta);
        
        // 在super.render之后渲染文本输入框，确保它在最上层
        messageField.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String text = messageField.getText();
        super.resize(client, width, height);
        messageField.setText(text);
    }
}