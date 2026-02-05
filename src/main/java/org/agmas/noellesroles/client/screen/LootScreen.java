package org.agmas.noellesroles.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec2;
import org.agmas.noellesroles.client.animation.AbstractAnimation;
import org.agmas.noellesroles.client.animation.BezierAnimation;
import org.agmas.noellesroles.client.animation.ConstantSpeedAnimation;
import org.agmas.noellesroles.client.widget.TextureWidget;
import org.agmas.noellesroles.client.widget.TimerWidget;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import com.mojang.math.Axis;
//import org.joml.Matrix4f;

// TODO : 渲染3D方块动画
public class LootScreen extends Screen {
//    // 方块动画所需变量
//    private float rotationX = 30;
//    private float rotationY = 45;
//    private float lidAngle = 0;

    // TODO: 作为皮肤查询使用，当皮肤系统建成后移动到皮肤管理器中
    public static final ResourceLocation[] skinList = {
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/lock.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/bomb.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/note.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/sp_knife.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/master_key.png"),
    };
    // 皮肤品质映射
    public static final int[] skinQualityList = {
            4,
            3,
            0,
            2,
            1,
    };
    public static final ResourceLocation[] qualityList = {
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/common_skin.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/uncommon_skin.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/rare_skin.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/epic_skin.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/legendary_skin.png"),
    };

    private static int pixelSize = 4;// 最大（默认）像素缩放的大小
    // 随机的过卡次数：目标卡片位置会在最大和最小中roll一个随机值
    private final int minCardNum = 30;
    private final int maxCardNum = 80;
    private final int lastCardNum = 3;
    private final int accelerationCardNum = 15;// 加速经过的卡片数
    private final int slowDownCardNum = 40;// 减速经过的卡片数
    private final int cardSize = 18;// 卡片大小
    private final int cardInterval = 2;
    private final int ansID;// 目标物品 id
    private final int baseDuration = 30;// 卡片从右到左的时间
    private final ArrayList<Card> cards = new ArrayList<>();
    private final ArrayList<AbstractAnimation> animations = new ArrayList<>();
    private LootSpeedController speedController;// 速度控制器：用于使用动画控制所有卡片移动
    private RandomSource randomSource;// 随机数源
    private TimerWidget timerWidget;// 定时器：用于抽卡结束之后缩放
    private Button startBtn;
    private boolean isStart = false;// 是否启动抽卡
    private boolean isLooting = false;// 是否正在抽卡
    private float deltaScale = .3f;// 最终缩放增量比例
    private int endCardIdx = 20;// 最终卡片所在索引
    private int totalPixels;// 显示的总像素数
    private int curEndIdx = 0;// 当前移除卡片所在索引
    private int curStartIdx = 0;// 当前显示卡片所在索引
    private int accelerationTime = 0;// 加速时间
    private int slowDownTime = 0;// 减速时间

    public static class Card extends AbstractWidget
    {
        // skin: 16*16 ; bg: 18*18
        protected TextureWidget skinBG;
        protected TextureWidget skin;
        protected boolean isSelected = false;// 是否被选中过：首次选中播放音效
        public Card(int x, int y, int id) {
            this(x, y, 16, 16, id);
        }
        public Card(int x, int y, int w, int h, int id) {
            super(x, y, w, h, Component.empty());
            skinBG = new TextureWidget(x, y, w, h, w, h, qualityList[skinQualityList[id]]);
            skin = new TextureWidget(x + pixelSize, y + pixelSize,
                    w - 2 * pixelSize, h - 2 * pixelSize, w - 2 * pixelSize, h - 2 * pixelSize, skinList[id]);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            if(skinBG != null)
                skinBG.render(guiGraphics, i, j, f);
            if(skin != null)
                skin.render(guiGraphics, i, j, f);
        }
        @Override
        public void setPosition(int x, int y) {
            int deltaX = skinBG.getWidth() - skin.getWidth();
            int deltaY = skinBG.getHeight() - skin.getHeight();
            skinBG.setPosition(x, y);
            skin.setPosition(x + deltaX / 2, y + deltaY / 2);
            super.setPosition(x, y);
        }
        @Override
        public void setSize(int i, int j) {
            float scaleX = (float) i / (float) this.width;
            float scaleY = (float) j / (float) this.height;
            skinBG.setSize(i, j);
            skin.setSize((int) (skin.getWidth() * scaleX), (int) (skin.getHeight() * scaleY));
            super.setSize(i, j);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }
    public static class LootSpeedController extends AbstractWidget
    {

        public LootSpeedController() {
            super(0, 0, 0, 0, Component.empty());
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            deltaTime += f;
            while (deltaTime >= .5f)
            {
                cards.forEach(card -> {
                    card.setPosition(card.getX() + speed, card.getY());
                    if(!card.isSelected && card.getX() + card.getWidth() / 2 < width / 2)
                    {
                        card.isSelected = true;
                        // 播放默认按钮音效
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(
                                        SoundEvents.UI_BUTTON_CLICK, // 按钮点击音效
                                        1.0F // 音量
                                )
                        );

                    }
                });
                deltaTime -= .5f;
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
        protected ArrayList<Card> cards = new ArrayList<>();
        protected int speed = 0;
        protected float deltaTime = 0;
    }

    public LootScreen(int ansID) {
        super(Component.empty());
        this.ansID = ansID;
    }
    @Override
    protected void init()
    {
        super.init();

        int centerX = width / 2;
        int centerY = height / 2;
        // 根据屏幕大小重新规划像素缩放 : 使显示的卡片范围略大于屏幕
        while (width > cardSize * (lastCardNum * 2 + 1) * pixelSize)
            ++pixelSize;

        speedController = new LootSpeedController();
        // 用于计算屏幕中心
        speedController.setWidth(width);
        timerWidget = new TimerWidget(1, true, null);
        randomSource = RandomSource.create();
        endCardIdx = randomSource.nextInt(minCardNum, maxCardNum);
        startBtn = Button.builder(
                    Component.translatable("screen.noellesroles.loot.lootBtn"), // 按钮文本
                    (buttonWidget) -> {
                        isStart = true;
                        isLooting = true;
                        startBtn.visible = false;
                    }
            )
            .pos(centerX - cardSize * pixelSize / 2, centerY - cardSize * pixelSize / 6)
            .size(cardSize * pixelSize, cardSize / 3 * pixelSize) // 大小
            .build();
        addRenderableWidget(startBtn);

        totalPixels = cardSize * (lastCardNum * 2 + 1) + cardInterval * lastCardNum * 2;

        int accelerationLen = 0;
        int slowDownLen = 0;
        // 提前备好所需卡片
        for(int i = 0; i <= endCardIdx + lastCardNum; ++i)
        {
            Card card = new Card(centerX + totalPixels / 2 * pixelSize, centerY - cardSize * pixelSize / 2,
                    cardSize * pixelSize, cardSize * pixelSize,
                    i != endCardIdx ? randomSource.nextInt(skinList.length) : ansID);
            card.visible = false;
            cards.add(card);
            int curLen = i * (cardSize + cardInterval) + totalPixels;
            if(i == accelerationCardNum)
                accelerationLen = curLen;
            if(i == slowDownCardNum)
                slowDownLen = curLen;
            addRenderableWidget(card);
        }
        cards.getFirst().visible = true;
        // 将第一张卡片插入位移队列：后续卡片的出现根据前一张卡片的位置
        speedController.cards.add(cards.getFirst());

        /*
         * 使用匀加速直线运动调整卡片速度
         * 卡片终速度为v,加速时间为t,加速度为a
         * 路程为s已知为accelerationIdx路程accelerationLen长，终速度v已知为totalPixels / baseDuration, 求a t
         * {s = 1/2 a * t ^ 2;
         * {sv = a * t
         * => a = v ^ 2 / 2 * s
         * => t = 2 * s / v
         * 计算加速减速时间，用于插入速度管理动画
         */
        accelerationTime = (2 * accelerationLen / (totalPixels / baseDuration));
        slowDownTime = (2 * slowDownLen / (totalPixels / baseDuration));
        ConstantSpeedAnimation speedAnimation =
                // 设置加速阶段为匀加速直线运动
                new ConstantSpeedAnimation(
                        speedController,
                        new Vec2((float) (totalPixels * pixelSize) / baseDuration, 0),
                        accelerationTime
                );
        speedAnimation.setCallback(
                (Vec2 ans) -> {
                    speedController.speed -= (int)ans.x;
                    });
        animations.add(speedAnimation);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        super.render(guiGraphics, mouseX, mouseY, delta);
        if(!isStart)
            return;

        animations.forEach(animation -> animation.renderUpdate(delta));
        animations.removeIf(AbstractAnimation::isFinished);
        int centerX = width / 2;
        int startX = centerX + totalPixels / 2 * pixelSize;// 卡片出现点
        // 当目标卡片移动到中心时停止处理卡片位移
        if(cards.get(endCardIdx).getX() > centerX - cardSize * pixelSize / 2)
        {
            speedController.render(guiGraphics, mouseX, mouseY, delta);

            // 当新出现的卡片移动 1 间距 + 1 卡片 长时显示下一张卡片
            if (curStartIdx < cards.size() - 1 && cards.get(curStartIdx).getX() <= startX - (cardSize + cardInterval) * pixelSize)
            {
                ++curStartIdx;
                if(curStartIdx < cards.size())
                {
                    cards.get(curStartIdx).visible = true;
                    speedController.cards.add(cards.get(curStartIdx));
                }
            }

            // 将超出屏幕的卡片隐藏
            if (cards.get(curEndIdx) != null &&
                    cards.get(curEndIdx).getX() <= centerX - totalPixels * pixelSize / 2 - cardSize * pixelSize)
            {
                cards.get(curEndIdx).visible = false;
                ++curEndIdx;

                // 当剩余卡片数量达到减速需求时进行减速
                if(curEndIdx == endCardIdx - slowDownCardNum)
                {
                    float speed = (float) (totalPixels * pixelSize) / baseDuration;
                    // 减速使用贝塞尔曲线，使后期速度较慢
                    BezierAnimation speedAnimation =
                            new BezierAnimation(
                                speedController,
                                new Vec2(speed * 0.7f, 0),
                                new Vec2(speed * 0.9f, 0),
                                new Vec2(speed, 0),
                                slowDownTime,
                                (Vec2 ans) -> {
                                    speedController.speed += (int) ans.x;
                                }
                            );
                    animations.add(speedAnimation);
                }
            }
        }
        // 抽卡结束后延迟一秒，缩放抽卡结果，并显示相关信息
        else if (isLooting) {
            isLooting = false;
            // 播放升级音效
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(
                            SoundEvents.PLAYER_LEVELUP,  // 升级音效
                            1.0F,  // 音量
                            1.0F   // 音高
                    )
            );
            animations.clear();
            Card endCard = cards.get(endCardIdx);
            // 卡片缩放：贝塞尔曲线，先放大再回弹到目标大小
            BezierAnimation scaleAnimation =
                    new BezierAnimation(
                            endCard,
                            new Vec2(endCard.getWidth() * (deltaScale - .2f), -endCard.getHeight() * (deltaScale - .2f)),
                            new Vec2(endCard.getWidth() * (deltaScale + .2f), endCard.getHeight() * (deltaScale + .2f)),
                            new Vec2(endCard.getWidth() * deltaScale, endCard.getHeight() * 0.3f),
                            20,
                            (Vec2 pos)->{
                                endCard.setSize((int)  pos.x + endCard.getWidth(),(int) pos.y + endCard.getHeight());
                            }
                    );
            // 设置1s的停顿
            timerWidget.reSet();
            timerWidget.setEndTime(1f);
            timerWidget.setOnCompleteCallback(
                timerWidget ->{
                    animations.add(scaleAnimation);
                    cards.forEach(card->{
                    if(card != endCard)
                        card.visible = false;
                    });
                }
            );
        }
        // 锁定目标卡片的位置防止缩放误差影响
        else
        {
            Card endCard = cards.get(endCardIdx);
            endCard.setPosition(centerX - endCard.getWidth() / 2, endCard.getY());
            // 抽卡结束显示
            // TODO : 需要用id查询目标文本并翻译
            guiGraphics.drawString(this.font, Component.translatable("请输入文本"),
                    centerX - font.width(Component.translatable("请输入文本")) / 2,
                    height / 2 + (int)(endCard.getHeight() * (deltaScale + 1f)) / 2 + cardInterval * pixelSize,
                    0xFFFFFFFF);
        }
        timerWidget.onRenderUpdate(delta);

        if(isLooting)
        {
            // 绘制当前卡片指针
            int rectWidth = 4;
            int rectHeight = 100;
            int centerY = height / 2 - rectHeight / 2;
            // 绘制填充矩形（RGBA颜色）
            guiGraphics.fill(
                    centerX, centerY - cardSize * pixelSize / 2,
                    centerX + rectWidth, centerY + rectHeight - cardSize * pixelSize / 2,
                    0x80FF0000  // 半透明红色 (ARGB: 80=50%透明度)
            );
        }
    }

//    /**
//     * 渲染3D箱子
//     * <p>
//     * @param guiGraphics
//     * @param delta
//     * </p>
//     * TODO : 目前3D方块渲染暂未实现
//     */
//    protected void renderChest(GuiGraphics guiGraphics, float delta)
//    {
//        PoseStack poseStack = guiGraphics.pose();
//        poseStack.pushPose();
//
//        // 定位到屏幕中心
//        poseStack.translate((float) width / 2, (float) height / 2, 200);
//
//        // 缩放
//        float renderScale = 40;
//        poseStack.scale(renderScale, -renderScale, renderScale);
//
//        // 旋转
//        poseStack.mulPose(Axis.YP.rotationDegrees(rotationY));
//        poseStack.mulPose(Axis.XP.rotationDegrees(rotationX));
//
//        // 获取渲染资源
//        MultiBufferSource bufferSource = guiGraphics.bufferSource();
//
//        // 根据箱子类型选择纹理
//        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("minecraft","textures/entity/chest/normal.png");
//
//        // 渲染箱子
//        VertexConsumer vertexConsumer = bufferSource.getBuffer(
//                RenderType.entityTranslucentEmissive(texture)
//        );
//
//        // 渲染箱子模型
//        renderChestModel(guiGraphics, vertexConsumer, lidAngle);
//
//        // 渲染箱子内部（当打开时）
//        if (lidAngle > 10f) {
//            // TODO : 渲染箱子内部
//        }
//
//        poseStack.popPose();
//    }
//
//    private void renderChestModel(GuiGraphics guiGraphics, VertexConsumer vertexConsumer, float lidAngle) {
//        PoseStack poseStack = guiGraphics.pose();
//        final float normaSize = 16;
//        final float chestSize = 14;
//        final float chestBottomHeight = 10;
//        final float chestLidHeight = 4;
//        // 箱子模型顶点数据
//        float size = chestSize / normaSize;
//        float height = chestBottomHeight / normaSize;
//        float lidHeight = chestLidHeight / normaSize;
//
//        // 计算盖子旋转
//        float lidRotation = lidAngle * 1.5708f; // 0到90度
//
//        // 渲染箱子底部
//        renderCuboid(poseStack, vertexConsumer,
//                -size, 0, -size,
//                size, height, size,
//                0xFFFFFFFF, 14 + 14 + 4, 0,
//                14,14, 10);
//
////        // 渲染盖子
////        poseStack.pushPose();
////        // 移动到旋转轴
////        poseStack.translate(0, height, size);
////        poseStack.mulPose(Axis.XP.rotation(-lidRotation));
////        poseStack.translate(0, -height, -size);
////
////        renderCuboid(poseStack, vertexConsumer,
////                -size, height, size - 0.0625f,
////                size, height + lidHeight, size,
////                0xFFFFFFFF, 0,
////                14, 14, 14 * 2 + 4);
////
////        poseStack.popPose();
//    }
//
//    private float uvPercentage(float length)
//    {
//        return length / 64.f;
//    }
//
//    /**
//     * 绘制立方体
//     * <p>
//     * @param poseStack
//     * @param consumer
//     * @param x1
//     * @param y1
//     * @param z1
//     * @param x2
//     * @param y2
//     * @param z2
//     * @param color
//     * @param uvOffsetX
//     * @param uvOffsetY
//     * @param cubeLong
//     * @param cubeWidth
//     * @param cubeHeight
//     * </p>
//     * 该函数是用来渲染3D方块的
//     * TODO: 由于比较急没时间搞方块动画，该函数功能并没有实现完成
//     */
//    private void renderCuboid(PoseStack poseStack, VertexConsumer consumer,
//                              float x1, float y1, float z1,
//                              float x2, float y2, float z2,
//                              int color, float uvOffsetX, float uvOffsetY,
//                              float cubeLong, float cubeWidth, float cubeHeight) {
//        // 渲染立方体的6个面
//        Matrix4f matrix = poseStack.last().pose();
//        PoseStack.Pose normal = poseStack.last();
//
//        float r = ((color >> 16) & 0xFF) / 255f;
//        float g = ((color >> 8) & 0xFF) / 255f;
//        float b = (color & 0xFF) / 255f;
//        float a = ((color >> 24) & 0xFF) / 255f;
//
//        float[] cubeSize = new float[]{cubeLong, cubeWidth, cubeHeight};
//        float[] pos = new float[]{x1,y1,z1,x2,y2,z2};
//        // 遍历每个面
//        for(int i = 0; i < 6; ++i)
//        {
//            float[] curVertex = new float[3];
//            // 调用绘制面时使用的 xyz 坐标
//            final int curFaceIdx = i % 3;
//            // 确定面
//            curVertex[curFaceIdx] = pos[i];
//            // 遍历剩余4点的排列组合->产生4个顶点
//            for(int j = 0; j < 6; ++j)
//            {
//                final int curSideIdx = j % 3;
//                if(curSideIdx == curFaceIdx)
//                    continue;
//                // 确定边1 : 不为面所在轴
//                curVertex[curSideIdx] = pos[j];
//                for(int k = j + 1; k < 6; ++k)
//                {
//                    final int curOtherSideIdx = k % 3;
//                    if(curOtherSideIdx == curFaceIdx || curOtherSideIdx == curSideIdx)
//                        continue;
//                    // 确定边2 : 不为面和另一边所在轴
//                    curVertex[curOtherSideIdx] = pos[k];
//                    consumer.addVertex(matrix, curVertex[0], curVertex[1], curVertex[2])
//                            /*
//                             * 如何确定 uv 位置：
//                             * 首先确定其所在区块，利用uvOffset 确定其所在区块的左上角
//                             * 再根据 face,sizeIdx 确定所属面（上下前后左右等）
//                             * 具体处理（默认自动%3)：
//                             * 当face为0时：为左右面：
//                             *
//                             * 当face为1时：为上下面：
//                             * 当face为2时：为前后面：
//                             * TODO : 需要完成对应面的uv计算 以替换底下24个函数调用
//                             */
//                            .setUv((cubeHeight * 2 + cubeWidth * 2 - (float) (i / 3) * cubeSize[curFaceIdx]), 0);
//
//                }
//            }
//        }
//
//        // 前面
//        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a)
//                .setUv(uvPercentage(cubeLong * 2 + cubeWidth * 2), uvPercentage(uvOffsetX))
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 0, 1);
//        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a)
//                .setUv(uvPercentage(cubeLong + cubeWidth * 2), uvPercentage(uvOffsetX))
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 0, 1);
//        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a)
//                .setUv(uvPercentage(cubeLong + cubeWidth * 2), uvPercentage(cubeHeight + uvOffsetX))
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 0, 1);
//        consumer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a)
//                .setUv(uvPercentage(cubeLong * 2 + cubeWidth * 2), uvPercentage(cubeHeight + uvOffsetX))
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 0, 1);
//
//        // 后面
//        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 0, -1);
//        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setUv(0, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 0, -1);
//        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setUv(1, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 0, -1);
//        consumer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setUv(1, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 0, -1);
//
//        // 上面
//        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setUv(0, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 1, 0);
//        consumer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setUv(0, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 1, 0);
//        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 1, 0);
//        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setUv(1, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, 1, 0);
//
//        // 下面
//        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, -1, 0);
//        consumer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setUv(1, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, -1, 0);
//        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setUv(1, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, -1, 0);
//        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setUv(0, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 0, -1, 0);
//
//        // 左面
//        consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a).setUv(0, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, -1, 0, 0);
//        consumer.addVertex(matrix, x1, y1, z2).setColor(r, g, b, a).setUv(1, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, -1, 0, 0);
//        consumer.addVertex(matrix, x1, y2, z2).setColor(r, g, b, a).setUv(1, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, -1, 0, 0);
//        consumer.addVertex(matrix, x1, y2, z1).setColor(r, g, b, a).setUv(0, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, -1, 0, 0);
//
//        // 右面
//        consumer.addVertex(matrix, x2, y1, z1).setColor(r, g, b, a).setUv(0, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 1, 0, 0);
//        consumer.addVertex(matrix, x2, y2, z1).setColor(r, g, b, a).setUv(0, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 1, 0, 0);
//        consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a).setUv(1, 1)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 1, 0, 0);
//        consumer.addVertex(matrix, x2, y1, z2).setColor(r, g, b, a).setUv(1, 0)
//                .setUv1(0, 0).setUv2(240, 240)
//                .setNormal(normal, 1, 0, 0);
//    }
}
