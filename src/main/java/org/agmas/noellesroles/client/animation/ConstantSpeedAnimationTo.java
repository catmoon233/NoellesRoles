package org.agmas.noellesroles.client.animation;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.phys.Vec2;

public class ConstantSpeedAnimationTo extends AbstractAnimation{
    public interface Callback<T> {
        void onExecute(T param);
    }
    public ConstantSpeedAnimationTo(AbstractWidget widget, Vec2 end, int duration, ConstantSpeedAnimation.Callback<Vec2> callback) {
        super(widget);
        this.end = end;
        this.durationTicks = duration;
        this.callback = callback;
    }
    public ConstantSpeedAnimationTo(AbstractWidget widget, Vec2 end, int duration) {
        this(widget, end, duration,
                // 默认实现位置偏移动画
                (Vec2 pos) ->
                {
                    widget.setPosition((int)pos.x, (int)pos.y);
                });
    }
    private Vec2 calculateDeltaPosition(float t) {
        // start x = 0, start y = 0
        float x = end.x * t;
        float y = end.y * t;
        return new Vec2(x, y);
    }
    @Override
    public void update() {
        progress += 1f / (float) durationTicks;
        progress = Math.min(progress, 1f);
        callback.onExecute(calculateDeltaPosition(progress));
        if(progress >= 1f)
            isFinished = true;
    }
    public void setCallback(ConstantSpeedAnimation.Callback<Vec2> callback) {
        this.callback = callback;
    }
    private final Vec2 end;
    private ConstantSpeedAnimation.Callback<Vec2> callback;
}
