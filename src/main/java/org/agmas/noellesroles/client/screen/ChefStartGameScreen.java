package org.agmas.noellesroles.client.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 阴谋家选择屏幕
 * 
 * 两阶段选择：
 * 1. 选择目标玩家（显示所有玩家头像）
 * 2. 选择角色（显示所有可用角色）
 */
public class ChefStartGameScreen extends Screen {

    public ChefStartGameScreen() {
        super(Component.translatable("screen.noellesroles.conspirator.title"));
    }

    final int BUTTON_WIDTH = 100;
    final int BUTTON_HEIGHT = 20;
    Button btn;

    @Override
    protected void init() {
        super.init();
        int maxWidth = this.width;
        int maxHeight = this.height;
        int buttonX = maxWidth / 2 - BUTTON_WIDTH / 2;
        int buttonY = maxHeight / 2 - BUTTON_HEIGHT / 2;
        btn = Button.builder(Component.translatable("screen.noellesroles.chef.start"), (bbtn) -> {
            this.minecraft.setScreen(new CookingGameScreen());
        }).bounds(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(btn);
    }
}