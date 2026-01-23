package org.agmas.noellesroles.client.animation;

import net.minecraft.client.gui.components.AbstractWidget;

public abstract class AbstractAnimation {
    protected AbstractAnimation(AbstractWidget widget) {
        this.widget = widget;
    }
    public void renderUpdate(float deltaTime) {
        delayTime += deltaTime;
        if(delayTime >= secondPerTick)
        {
            update();
            delayTime -= secondPerTick;
        }
    }
    public boolean isFinished() {
        return isFinished;
    }
    public abstract void update();

    protected final AbstractWidget widget;
    protected final float secondPerTick = (float)1 / 20;
    protected float delayTime = 0f;
    protected boolean isFinished = false;
}
