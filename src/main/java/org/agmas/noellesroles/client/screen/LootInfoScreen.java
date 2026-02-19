package org.agmas.noellesroles.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import org.agmas.noellesroles.client.animation.AbstractAnimation;
import org.agmas.noellesroles.client.animation.AnimationTimeLineManager;
import org.agmas.noellesroles.client.animation.BezierAnimation;
import org.agmas.noellesroles.client.widget.TextureWidget;
import org.agmas.noellesroles.utils.Pair;
import org.agmas.noellesroles.utils.lottery.LotteryManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽奖信息页
 * - 用于显示卡池信息，以及启动抽奖
 * TODO : 点击立绘预览卡池-打开预览screen
 */
public class LootInfoScreen extends AbstractPixelScreen {
    public static class PoolButton extends AbstractButton {
        public interface OnRelease {
            void onRelease(PoolButton button);
        }
        public static final ResourceLocation[] poolBtnTextures = {
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/poolBtnIdle.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/poolBtnHover.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/poolBtnPressed.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/poolBtnIdleSelected.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/poolBtnHoverSelected.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/poolBtnPressedSelected.png"),
        };
        private static final int poolBtnWidth = 32;
        private static final int poolBtnHeight = 18;
        /**
         * 卡池按钮的贴图
         * <p>
         *     0: idle
         *     1: hover
         *     2: pressed
         * </p>
         */
        private final List<TextureWidget> poolBtnTextureWidgets;
        private final int poolID;
        private OnRelease onRelease;
        private boolean isPressed = false;
        private int curTexIdx = 0;

        public PoolButton(int poolId, int i, int j, int k, int l, Component component, OnRelease onRelease) {
            super(i, j, k, l, component);
            this.poolID = poolId;
            poolBtnTextureWidgets = new ArrayList<>();
            poolBtnTextureWidgets.add(new TextureWidget(
                    i, j, k, l,
                    poolBtnWidth, poolBtnHeight,
                    poolBtnTextures[0]
            ));
            poolBtnTextureWidgets.add(new TextureWidget(
                    i, j, k, l,
                    poolBtnWidth, poolBtnHeight,
                    poolBtnTextures[1]
            ));
            poolBtnTextureWidgets.add(new TextureWidget(
                    i, j, k, l,
                    poolBtnWidth, poolBtnHeight,
                    poolBtnTextures[2]
            ));
            for (TextureWidget textureWidget : poolBtnTextureWidgets)
                textureWidget.visible = false;
            this.onRelease = onRelease;
        }
        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float f) {
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            // 根据按钮状态选择贴图
            poolBtnTextureWidgets.get(curTexIdx).visible = false;
            if (isPressed)
                curTexIdx = 2;
            else if (isHovered)
                curTexIdx = 1;
            else
                curTexIdx = 0;
            poolBtnTextureWidgets.get(curTexIdx).visible = true;
            poolBtnTextureWidgets.get(curTexIdx).render(guiGraphics, mouseX, mouseY, f);

            // 渲染黑色文本
            Minecraft minecraft = Minecraft.getInstance();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
            this.renderString(guiGraphics, minecraft.font, (int)(this.alpha * 255) << 24);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        @Override
        public void onPress() {
            isPressed = true;
        }
        @Override
        public void onRelease(double d, double e) {
            isPressed = false;
            if (isHovered) {
                onRelease.onRelease(this);
            }
        }
        @Override
        public void setX(int x) {
            super.setX(x);
            for(TextureWidget textureWidget : poolBtnTextureWidgets)
                textureWidget.setX(x);
        }
        @Override
        public void setY(int y) {
            super.setY(y);
            for(TextureWidget textureWidget : poolBtnTextureWidgets)
                textureWidget.setY(y);
        }
        @Override
        public void setAlpha(float alpha) {
            super.setAlpha(alpha);
            for(TextureWidget textureWidget : poolBtnTextureWidgets)
                textureWidget.setAlpha(alpha);
        }
        public float getAlpha() {
            return this.alpha;
        }
        public boolean isOnButton(int mouseX, int mouseY) {
            return mouseX >= getX() && mouseX < getX() + getWidth() && mouseY >= getY() && mouseY < getY() + getHeight();
        }
    }
    protected static class AnimationController extends AbstractWidget {
        public AnimationController() {
            super(0, 0, 0, 0, Component.empty());
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {

        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
        private float curBgProcess = 0f;
    }
    public LootInfoScreen() {
        super(Component.empty());
        animationTimeLineManager = AnimationTimeLineManager.builder()
                .build();
    }
    @Override
    protected void init(){
        super.init();
        List<Pair<Float, AbstractAnimation>> animations = new ArrayList<>();
        int sketchX = centerX - totalWidth / 2 + poolBtnWidth + poolBtnEdgeWidth + sketchEdge / 2;
        int sketchY = centerY - totalHeight / 2 + sketchEdge;
        // 无卡池信息时的处理
        try{
            curPool = LotteryManager.getInstance().getLotteryPools().getFirst();
            poolSketch = new TextureWidget(
                    sketchX,
                    sketchY,
                    sketchWidth, sketchHeight,
                    sketchWidth, sketchHeight,
                    ResourceLocation.fromNamespaceAndPath(
                            "noellesroles", "textures/gui/poolBg" +
                                    curPool.getPoolID()
                                    + ".png"
                    )
            );
            addRenderableWidget(poolSketch);
            poolSketch.setAlpha(0f);
            // 立绘动画：仅alpha和Y有移动，但int值需要误差计算不能用x值的位置来控制alpha移动
            animations.add(new Pair<>(initBgTime, BezierAnimation.builder(
                            poolSketch,
                            new Vec2(0f, (float) -sketchEdge / 2),
                            (int)(initWidgetTime / AbstractAnimation.secondPerTick))
                    .build()
            ));
            animations.add(new Pair<>(initBgTime, BezierAnimation.builder(
                            poolSketch,
                            new Vec2(1f, 0f),
                            (int)(initWidgetTime / AbstractAnimation.secondPerTick))
                    .setCallback((vec2)->{
                        poolSketch.setAlpha(poolSketch.getAlpha() + vec2.x);
                    })
                    .setIntErrorFix(false)
                    .build()
            ));
        }
        catch (Exception e){
            curPool = null;
            poolSketch = null;
        }

        int poolBtnX = centerX - totalWidth / 2 + ((poolBtnWidth + poolBtnEdgeWidth) - poolBtnWidth) / 2;
        int poolBtnY = centerY - totalHeight / 2;
        // 为每个卡池添加卡池按钮
        List<LotteryManager.LotteryPool> lotteryPools = LotteryManager.getInstance().getLotteryPools();
        for (int i = 0; i < lotteryPools.size(); ++i)
        {
            LotteryManager.LotteryPool curBtnPool = lotteryPools.get(i);
            PoolButton poolBtn = new PoolButton(
                    curBtnPool.getPoolID(),
                    poolBtnX,
                    poolBtnY,
                    poolBtnWidth, poolBtnHeight,
                    Component.literal(curBtnPool.getName()),
                    (buttonWidget) -> {
                        switchToPool(curBtnPool.getPoolID());
                    }
            );
            poolBtn.setAlpha(0f);
            // 按钮排列动画
            animations.add(new Pair<>(initBgTime, BezierAnimation.builder(
                    poolBtn,
                    new Vec2(0f, i * ((float) poolBtnInterval / 2 + poolBtnHeight) + (float) poolBtnInterval / 2),
                    (int)(initWidgetTime / AbstractAnimation.secondPerTick))
                    .build()
            ));
            // 透明度动画
            animations.add(new Pair<>(initBgTime, BezierAnimation.builder(
                    poolBtn,
                    new Vec2(1f, 0f),
                    (int)(initWidgetTime / AbstractAnimation.secondPerTick))
                    .setCallback((vec2)->{
                        poolBtn.setAlpha(poolBtn.getAlpha() + vec2.x);
                    })
                    .setIntErrorFix(false)
                    .build()
            ));
            poolButtons.add(poolBtn);
            poolBtn.active = false;
            addRenderableWidget(poolBtn);
        }
        if (curPool != null)
        {
            PoolButton curPoolBtn = poolButtons.get(curPool.getPoolID() - 1);
            for (int i = 0; i < 3 && curPoolBtn != null; ++i)
                curPoolBtn.poolBtnTextureWidgets.get(i).setTEXTURE(PoolButton.poolBtnTextures[i + 3]);
        }
        // 发送抽奖请求
//        ClientPlayNetworking.send(new LootRequestC2SPacket(1));

        animationTimeLineManager = AnimationTimeLineManager.builder()
                .addAnimation(0f, BezierAnimation.builder(
                        animationController,
                        new Vec2(1.0f, 0),
                        (int)(initBgTime / AbstractAnimation.secondPerTick))
                        .setCallback((vec2)->{
                            animationController.curBgProcess += vec2.x;
                        })
                        .setIntErrorFix(false)
                        .build()
                )
                .addAnimations(animations)
                .build();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if (!initialized)
        {
            if (animationTimeLineManager.isFinished())
            {
                initialized = true;
                for (PoolButton poolButton : poolButtons)
                    poolButton.active = true;
            }
            else
                animationTimeLineManager.renderUpdate(delta);
        }
        animationStack.forEach(animation->animation.renderUpdate(delta));
        animationStack.removeIf(AbstractAnimation::isFinished);
        // 绘制左侧按钮背景
        guiGraphics.fill(
                // 适配动画缩放
                (int) (centerX - (float) totalWidth / 2 + (1.0f - animationController.curBgProcess) * poolBtnWidth),
                centerY - totalHeight / 2,
                centerX - totalWidth / 2 + (poolBtnWidth + poolBtnEdgeWidth),
                centerY + totalHeight / 2,
                poolBtnBgColor.getRGB()
        );
        // 绘制立绘部分背景
        guiGraphics.fill(
                centerX - totalWidth / 2 + (poolBtnWidth + poolBtnEdgeWidth),
                centerY - totalHeight / 2,
                // 适配动画缩放
                (int) (centerX + (float) totalWidth / 2 - (1.0f - animationController.curBgProcess) * sketchWidth),
                centerY + totalHeight / 2,
                sketchBgColor.getRGB()
        );
        poolSketch.render(guiGraphics, mouseX, mouseY, delta);
        for (PoolButton poolBtn : poolButtons) {
            poolBtn.render(guiGraphics, mouseX, mouseY, delta);
        }
    }
    public void switchToPool(int poolD) {
        if (curPool != null && poolD == curPool.getPoolID())
            return;
        LotteryManager.LotteryPool nextPool = LotteryManager.getInstance().getLotteryPool(poolD);
        if (nextPool == null)
            return;
        poolSketch.setTEXTURE(
                ResourceLocation.fromNamespaceAndPath(
                        "noellesroles", "textures/gui/poolBg" + nextPool.getPoolID() + ".png"
                ));
        // 添加位移和透明度动画
        poolSketch.setY(centerY - totalHeight / 2 + sketchEdge);
        animationStack.add(
                BezierAnimation.builder(
                                poolSketch,
                                new Vec2(0f, (float) -sketchEdge / 2),
                                (int)(initWidgetTime / AbstractAnimation.secondPerTick))
                        .build()
        );
        poolSketch.setAlpha(0f);
        animationStack.add(
                BezierAnimation.builder(
                                poolSketch,
                                new Vec2(1f, 0f),
                                (int)(initWidgetTime / AbstractAnimation.secondPerTick))
                        .setCallback((vec2)->{
                            poolSketch.setAlpha(poolSketch.getAlpha() + vec2.x);
                        })
                        .setIntErrorFix(false)
                        .build()
        );
        switchToPoolBtn(poolD);
        curPool = nextPool;
    }
    public void switchToPoolBtn(int poolD) {
        if (curPool == null || poolD == curPool.getPoolID())
            return;
        PoolButton curPoolBtn = null;
        PoolButton nextPoolBtn = null;
        for (PoolButton poolButton : poolButtons)
            if (curPool != null && poolButton.poolID == curPool.getPoolID())
                curPoolBtn = poolButton;
            else if (poolButton.poolID == poolD)
                nextPoolBtn = poolButton;
        for (int i = 0; i < 3; ++i)
        {
            // 构造函数中必定构造了三个
            if (curPoolBtn != null)
                curPoolBtn.poolBtnTextureWidgets.get(i).setTEXTURE(PoolButton.poolBtnTextures[i]);
            if (nextPoolBtn != null)
                nextPoolBtn.poolBtnTextureWidgets.get(i).setTEXTURE(PoolButton.poolBtnTextures[i + 3]);
        }
    }

    private static final Color poolBtnBgColor = new Color(0xFF555555, true);
    private static final Color sketchBgColor = new Color(0xFFEEEEEE, true);
    private static final int sketchWidth = 320;
    private static final int sketchHeight = 180;
    private static final int poolBtnWidth = (int) (32 * 1.5f);
    private static final int poolBtnHeight = (int) (18 * 1.5f);
    /** 立绘边距：用于确定背景大小 */
    private static final int sketchEdge = 36;
    private static final int poolBtnEdgeWidth = 8;
    private static final int poolBtnInterval = 6;
    private static final int totalWidth = sketchEdge + sketchWidth + poolBtnWidth + poolBtnEdgeWidth;
    private static final int totalHeight = sketchEdge + sketchHeight;
    /** 背景初始化时间 */
    private static final float initBgTime = 0.5f;
    private static final float initWidgetTime = 1.0f;
    private final List<PoolButton> poolButtons = new ArrayList<>();
    private final List<AbstractAnimation> animationStack = new ArrayList<>();
    private final AnimationController animationController = new AnimationController();
    private AnimationTimeLineManager animationTimeLineManager;
    private LotteryManager.LotteryPool curPool;
    private TextureWidget poolSketch;
    private boolean initialized = false;
}
