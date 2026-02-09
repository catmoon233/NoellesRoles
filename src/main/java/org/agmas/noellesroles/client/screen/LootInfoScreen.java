package org.agmas.noellesroles.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.agmas.noellesroles.packet.LootRequestC2SPacket;

/**
 * 抽奖信息页
 * - 用于显示卡池信息，以及启动抽奖
 */
public class LootInfoScreen extends AbstractPixelScreen {
    public LootInfoScreen() {
        super(Component.empty());
    }
    @Override
    protected void init(){
        super.init();

        startBtnPosX = centerX - startBtnSizeX / 2;
        startBtnPosY = centerY - startBtnSizeY / 2;

         startBtn = Button.builder(
                        Component.translatable("screen.noellesroles.loot.lootBtn"), // 按钮文本
                        (buttonWidget) -> {
                            // 发送抽奖请求
                            ClientPlayNetworking.send(new LootRequestC2SPacket());
                            this.onClose();
                        }
                )
                .pos(startBtnPosX, startBtnPosY)
                .size(startBtnSizeX, startBtnSizeY) // 大小
                .build();
        addRenderableWidget(startBtn);
    }
    private Button startBtn = null;
    private int startBtnPosX = 0;
    private int startBtnPosY = 0;
    private int startBtnSizeX = 6 * 8;
    private int startBtnSizeY = 2 * 8;
}
