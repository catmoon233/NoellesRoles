package org.agmas.noellesroles.client.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

// TODO：抽象动画类，实现To动画和By动画以支持更多功能
public abstract class AbstractAnimation {
    protected AbstractAnimation(AbstractWidget widget) {
        this.widget = widget;
    }
    public void renderUpdate(float deltaTime) {
        if(isFinished)
            return;
        // deltaTime的单位为0.10秒
        delayTime += deltaTime / 10;
        while (delayTime >= secondPerTick)
        {
            update();
            delayTime -= secondPerTick;
        }
//        if (Minecraft.getInstance().player != null) {
//                        Minecraft.getInstance().player.sendSystemMessage(Component.literal(
//                                "process: " + progress
//                        ));
//                    }
    }
    public void reStrt()
    {
        isFinished = false;
        reSet();
    }
    public void reSet()
    {
        delayTime = 0f;
        progress = 0f;
    }
    public boolean isFinished() {
        return isFinished;
    }
    public abstract void update();
    public void setDurationTicks(int durationTicks)
    {
        this.durationTicks = durationTicks;
//        reSet();
    }
    public int getDurationTicks() {
        return durationTicks;
    }
    public AbstractWidget getWidget() {
        return widget;
    }

    protected final AbstractWidget widget;
    protected final float secondPerTick = (float)1 / 20;
    protected float delayTime = 0f;
    protected float progress = 0f;
    protected boolean isFinished = false;
    protected int durationTicks; // 动画持续时间（刻）
}
