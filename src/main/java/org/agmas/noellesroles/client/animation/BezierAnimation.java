package org.agmas.noellesroles.client.animation;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.phys.Vec2;

/**
 * 三阶贝塞尔曲线动画
 * start为(0,0)作相对运动：以控件自身为原点
 */
public class BezierAnimation extends AbstractAnimation {
    public interface Callback<T> {
        void onExecute(T param);
    }
    public BezierAnimation(AbstractWidget widget, Vec2 control1, Vec2 control2, Vec2 end, int duration, Callback<Vec2> callback) {
        super(widget);
        this.control1 = control1;
        this.control2 = control2;
        this.end = end;
        this.durationTicks = duration;
        this.callback = callback;
    }
    public BezierAnimation(AbstractWidget widget, Vec2 control1, Vec2 control2, Vec2 end, int duration) {
        this(widget, control1, control2, end, duration,
            // 默认实现位置偏移动画
            (Vec2 pos) ->
            {
                widget.setPosition((int)pos.x + widget.getX(), (int)pos.y + widget.getY());
            });
    }
    // 使用默认控制点
    public BezierAnimation(AbstractWidget widget, Vec2 end, int duration) {
        // 控制点靠近起点/终点，产生平滑开始/结束 10%/90%
        this(widget,
                new Vec2(end.x * 0.1f,end.y * 0.1f),new Vec2(end.x * 0.9f, end.y * 0.9f),
                end, duration);
    }
    public BezierAnimation(AbstractWidget widget, Vec2 end, int duration, Callback<Vec2> callback) {
        this(widget,
                new Vec2(end.x * 0.1f,end.y * 0.1f),new Vec2(end.x * 0.9f, end.y * 0.9f),
                end, duration, callback);
    }

    // 三阶贝塞尔曲线计算
    private Vec2 calculateDeltaPosition(float t) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        // start x = 0, start y = 0
        float x = uuu * 0/*start x*/ +
                3 * uu * t * control1.x +
                3 * u * tt * control2.x +
                ttt * end.x;
        float y = uuu * 0/*start y*/ +
                3 * uu * t * control1.y +
                3 * u * tt * control2.y +
                ttt * end.y;
        float ansX = x - lastX;
        float ansY = y - lastY;
        lastX = x;
        lastY = y;

        // 整数误差修正
        if(isIntErrorFixOpen) {
            // 保存误差
            deltaErrorX += ansX - (int) ansX;
            deltaErrorY += ansY - (int) ansY;
            // 误差修正
            if (deltaErrorX > 1) {
                --deltaErrorX;
                ++ansX;
            } else if (deltaErrorX < -1) {
                ++deltaErrorX;
                --ansX;
            }
            if (deltaErrorY > 1) {
                --deltaErrorY;
                ++ansY;
            } else if (deltaErrorY < -1) {
                ++deltaErrorY;
                --ansY;
            }
        }
        return new Vec2(ansX, ansY);
    }

    @Override
    public void update() {
        if(!isFinished)
        {
            progress += 1f / (float) durationTicks;
            progress = Math.min(progress, 1f);
            callback.onExecute(calculateDeltaPosition(progress));
            if(progress >= 1f)
            {
                // 将剩余的误差进行修正
                if(isIntErrorFixOpen)
                    callback.onExecute(new Vec2( deltaErrorX > 0.5f ? 1 : deltaErrorX < -0.5f ? -1 : deltaErrorX,
                        deltaErrorY > 0.5f ? 1 : deltaErrorY < -0.5f ? -1 : deltaErrorY));
                isFinished = true;
            }
        }
    }

    public void openIntErrorFix() {
        isIntErrorFixOpen = true;
    }
    public void closeIntErrorFix() {
        isIntErrorFixOpen = false;
    }

    private final Vec2 control1, control2, end;
    private float progress = 0f;
    private final int durationTicks; // 动画持续时间（刻）
    private final Callback<Vec2> callback;
    // 是否打开整数误差修正：用于回调修改为整数时的误差修正
    private boolean isIntErrorFixOpen = true;
    private float lastX = 0;
    private float lastY = 0;
    // 误差：每次移动控件时被舍去的小数
    private float deltaErrorX = 0;
    private float deltaErrorY = 0;
}
