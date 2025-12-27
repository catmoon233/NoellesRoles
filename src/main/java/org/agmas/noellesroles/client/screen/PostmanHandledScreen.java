package org.agmas.noellesroles.client.screen;

import org.agmas.noellesroles.component.PostmanPlayerComponent;
import org.agmas.noellesroles.packet.PostmanC2SPacket;
import org.agmas.noellesroles.screen.PostmanScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * 邮差传递界面 - 基于 HandledScreen
 *
 * 布局：
 * - 顶部：文字说明
 * - 中间：一个槽位（用于放入物品）
 * - 底部：快捷栏 + 确认按钮
 */
public class PostmanHandledScreen extends HandledScreen<PostmanScreenHandler> {
    
    // 使用漏斗界面纹理作为基础
    private static final Identifier TEXTURE = Identifier.of("minecraft", "textures/gui/container/hopper.png");
    
    private PostmanPlayerComponent postmanComponent;
    private ButtonWidget confirmButton;
    
    public PostmanHandledScreen(PostmanScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 133;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }
    
    @Override
    protected void init() {
        super.init();
        
        this.postmanComponent = PostmanPlayerComponent.KEY.get(client.player);
        
        // 确认交换按钮 - 放在槽位右侧
        int buttonWidth = 70;
        int buttonX = this.x + 106;  // 槽位右侧
        int buttonY = this.y + 32;   // 与槽位对齐
        
        this.confirmButton = ButtonWidget.builder(
            Text.translatable("screen.noellesroles.postman.confirm"),
            button -> onConfirm()
        )
        .dimensions(buttonX, buttonY, buttonWidth, 20)
        .build();
        
        this.addDrawableChild(confirmButton);
        
        updateButtonState();
    }
    
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        
        // 绘制简洁的背景
        // 上半部分：信息和交换区域
        context.fill(x, y, x + this.backgroundWidth, y + 90, 0xC0101010);
        
        // 下半部分：快捷栏区域
        context.fill(x, y + 90, x + this.backgroundWidth, y + this.backgroundHeight, 0xC0202020);
        
        // 绘制分隔线
        context.fill(x, y + 89, x + this.backgroundWidth, y + 91, 0xFF8B8B8B);
        
        // 绘制中央槽位的边框（高亮显示）
        int slotX = x + 79;
        int slotY = y + 34;
        // 绿色边框
        context.fill(slotX - 1, slotY - 1, slotX + 18, slotY, 0xFF55FF55);
        context.fill(slotX - 1, slotY + 17, slotX + 18, slotY + 18, 0xFF55FF55);
        context.fill(slotX - 1, slotY, slotX, slotY + 17, 0xFF55FF55);
        context.fill(slotX + 17, slotY, slotX + 18, slotY + 17, 0xFF55FF55);
        // 槽位背景
        context.fill(slotX, slotY, slotX + 17, slotY + 17, 0xFF8B8B8B);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // 每次渲染时重新获取组件，确保读取最新数据
        this.postmanComponent = PostmanPlayerComponent.KEY.get(client.player);
        
        if (postmanComponent == null || !postmanComponent.isDeliveryActive()) {
            this.close();
            return;
        }
        
        // 绘制标题
        Text title = Text.translatable("screen.noellesroles.postman.title")
            .formatted(Formatting.GOLD, Formatting.BOLD);
        context.drawText(textRenderer, title,
            this.x + this.backgroundWidth / 2 - textRenderer.getWidth(title) / 2,
            this.y + 6, 0xFFFFFF, true);
        
        // 绘制目标玩家名称
        String targetName = postmanComponent.targetName;
        Text targetText = Text.literal("正在与 ").formatted(Formatting.GRAY)
            .append(Text.literal(targetName).formatted(Formatting.YELLOW))
            .append(Text.literal(" 交易").formatted(Formatting.GRAY));
        context.drawText(textRenderer, targetText,
            this.x + this.backgroundWidth / 2 - textRenderer.getWidth(targetText) / 2,
            this.y + 20, 0xFFFFFF, true);
        
        // 绘制槽位上方的文字说明
        Text slotLabel = Text.literal("放入物品").formatted(Formatting.WHITE);
        context.drawText(textRenderer, slotLabel,
            this.x + 80 + 8 - textRenderer.getWidth(slotLabel) / 2,
            this.y + 55, 0xAAAAAA, true);
        
        // 绘制确认状态 - 直接读取组件中的最新值
        boolean isReceiver = postmanComponent.isReceiver;
        boolean myConfirmed = isReceiver ? postmanComponent.targetConfirmed : postmanComponent.postmanConfirmed;
        boolean otherConfirmed = isReceiver ? postmanComponent.postmanConfirmed : postmanComponent.targetConfirmed;
        
        // 显示双方确认状态
        Text myStatus = myConfirmed ?
            Text.literal("你: ✓ 已确认").formatted(Formatting.GREEN) :
            Text.literal("你: ✗ 未确认").formatted(Formatting.RED);
        Text otherStatus = otherConfirmed ?
            Text.literal(targetName + ": ✓ 已确认").formatted(Formatting.GREEN) :
            Text.literal(targetName + ": ✗ 未确认").formatted(Formatting.RED);
        
        context.drawText(textRenderer, myStatus,
            this.x + 10, this.y + 65, 0xFFFFFF, true);
        context.drawText(textRenderer, otherStatus,
            this.x + 10, this.y + 77, 0xFFFFFF, true);
        
        // 绘制提示信息 - 在快捷栏下方
        Text hint;
        if (postmanComponent.isBothConfirmed()) {
            hint = Text.translatable("screen.noellesroles.postman.exchanging")
                .formatted(Formatting.GREEN);
        } else {
            hint = Text.translatable("screen.noellesroles.postman.hint")
                .formatted(Formatting.GRAY);
        }
        context.drawText(textRenderer, hint,
            this.x + this.backgroundWidth / 2 - textRenderer.getWidth(hint) / 2,
            this.y + this.backgroundHeight + 5, 0xFFFFFF, true);
        
        // 更新按钮状态
        updateButtonState();
        
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonState() {
        if (postmanComponent == null || confirmButton == null) return;
        
        boolean isReceiver = postmanComponent.isReceiver;
        boolean myConfirmed = isReceiver ? postmanComponent.targetConfirmed : postmanComponent.postmanConfirmed;
        boolean bothConfirmed = postmanComponent.isBothConfirmed();
        
        // 如果已经确认或双方都确认，禁用确认按钮
        confirmButton.active = !myConfirmed && !bothConfirmed;
        
        // 更新按钮文字
        if (myConfirmed) {
            confirmButton.setMessage(Text.literal("已确认").formatted(Formatting.GRAY));
        } else {
            confirmButton.setMessage(Text.translatable("screen.noellesroles.postman.confirm"));
        }
    }
    
    /**
     * 确认交换
     */
    private void onConfirm() {
        // 使用组件中的目标玩家 UUID（更可靠）
        if (postmanComponent != null && postmanComponent.deliveryTarget != null) {
            ClientPlayNetworking.send(new PostmanC2SPacket(
                PostmanC2SPacket.Action.CONFIRM,
                postmanComponent.deliveryTarget
            ));
        } else if (handler.getTargetPlayerUuid() != null) {
            // 回退使用 handler 中的 UUID
            ClientPlayNetworking.send(new PostmanC2SPacket(
                PostmanC2SPacket.Action.CONFIRM,
                handler.getTargetPlayerUuid()
            ));
        }
        updateButtonState();
    }
    
    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        
        // 每次 tick 重新获取组件，确保读取最新同步数据
        this.postmanComponent = PostmanPlayerComponent.KEY.get(client.player);
        
        updateButtonState();
        
        // 检查传递是否仍然激活
        if (postmanComponent != null && !postmanComponent.isDeliveryActive()) {
            this.close();
        }
        
        // 如果双方都确认，服务端会处理交换逻辑并关闭界面
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}