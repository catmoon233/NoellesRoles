package org.agmas.noellesroles.client.animation;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.phys.Vec2;

/**
 * 三阶贝塞尔曲线动画
 * start为(0,0)作相对运动：以控件自身为原点
 */
public class BezierAnimation extends AbstractByAnimation {
    public BezierAnimation(AbstractWidget widget, Vec2 control1, Vec2 control2, Vec2 end, int duration, Callback<Vec2> callback) {
        super(widget, end, duration, callback);
        this.control1 = control1;
        this.control2 = control2;
    }
    public BezierAnimation(AbstractWidget widget, Vec2 control1, Vec2 control2, Vec2 end, int duration) {
        super(widget, end, duration);
        this.control1 = control1;
        this.control2 = control2;
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

    /** 三阶贝塞尔曲线计算*/
    @Override
    protected Vec2 calculateAns(float t) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        float x = uuu * 0/*start x*/ +
                3 * uu * t * control1.x +
                3 * u * tt * control2.x +
                ttt * end.x;
        float y = uuu * 0/*start y*/ +
                3 * uu * t * control1.y +
                3 * u * tt * control2.y +
                ttt * end.y;
        return new Vec2(x, y);
    }

    private final Vec2 control1, control2;
}
