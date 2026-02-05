package org.agmas.noellesroles.client.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;

/**
 * 匀速动画
 * - 只需要输入目标值和持续时间即可
 */
public class ConstantSpeedAnimation extends AbstractAnimation{
    public interface Callback<T> {
        void onExecute(T param);
    }
    public ConstantSpeedAnimation(AbstractWidget widget, Vec2 end, int duration, Callback<Vec2> callback) {
        super(widget);
        this.end = end;
        this.durationTicks = duration;
        this.callback = callback;
    }
    public ConstantSpeedAnimation(AbstractWidget widget, Vec2 end, int duration) {
        this(widget, end, duration,
                // 默认实现位置偏移动画
                (Vec2 pos) ->
                {
                    widget.setPosition((int)pos.x + widget.getX(), (int)pos.y + widget.getY());
                });
    }
    // TODO : 将update、误差修正进行抽象
    private Vec2 calculateDeltaPosition(float t) {
        // start x = 0, start y = 0
        float x = end.x * t;
        float y = end.y * t;
        float ansX = x - lastX;
        float ansY = y - lastY;
//        if (Minecraft.getInstance().player != null) {
//            Minecraft.getInstance().player.sendSystemMessage(Component.literal(
//                    "X : " + x +
//                            " deltaX: " + ansX));
//        }
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
//        if (Minecraft.getInstance().player != null) {
//            Minecraft.getInstance().player.sendSystemMessage(Component.literal(
//                    "process : " + progress +
//                            " 实时速度播报: " + durationTicks
//            ));
//        }
    }
    public void setCallback(Callback<Vec2> callback) {
        this.callback = callback;
    }
    public void openIntErrorFix() {
        isIntErrorFixOpen = true;
    }
    public void closeIntErrorFix() {
        isIntErrorFixOpen = false;
    }

    private final Vec2 end;
    private Callback<Vec2> callback;
    // 是否打开整数误差修正：用于回调修改为整数时的误差修正
    private boolean isIntErrorFixOpen = true;
    private float lastX = 0;
    private float lastY = 0;
    // 误差：每次移动控件时被舍去的小数
    private float deltaErrorX = 0;
    private float deltaErrorY = 0;
}
