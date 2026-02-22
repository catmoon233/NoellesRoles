package org.agmas.noellesroles.client.screen;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.agmas.noellesroles.client.widget.TextureWidget;
import org.agmas.noellesroles.utils.Pair;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class CookingGameScreen extends AbstractPixelScreen {
    public static class GameItem {
        public static boolean isOverlap(GameItem item1, GameItem item2) {
            return item1.imgWidget.getY() < item2.imgWidget.getBottom() && item1.imgWidget.getBottom() > item2.imgWidget.getY() &&
                    item1.imgWidget.getX() < item2.imgWidget.getRight() && item1.imgWidget.getRight() > item2.imgWidget.getX();
        }
        public GameItem(int i, int j, int k, int l, int textureWidth, int textureHeight, ResourceLocation texture) {
            imgWidget = new TextureWidget(i, j, k, l, textureWidth, textureHeight, texture);
        }
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            imgWidget.render(guiGraphics, mouseX, mouseY, delta);
        }
        public void tick() {
        }
        protected TextureWidget imgWidget;
    }
    public static class FoodCard extends GameItem{
        public FoodCard(int i, int j, int k, int l, int buffID) {
            super(i, j, k, l, BASE_FOOD_SIZE, BASE_FOOD_SIZE,
                    ResourceLocation.fromNamespaceAndPath(
                            "noellesroles", "textures/gui/cooking/" + buffID + ".png"
                    ));
        }
        @Override
        public void tick() {
            velocity += gravity;
            imgWidget.setY((int)(imgWidget.getY() + velocity));
        }
        private static final int BASE_FOOD_SIZE = 16;
        /** 屏幕重力可变，使用static重力需要由屏幕修改 */
        private static float gravity = 0f;
        private float velocity = 0;
    }
    public static class Pan extends GameItem{
        public Pan(int boundMinX, int boundMaxX, int y, int imgW, int imgH, int velocity) {
            super(boundMinX, y, imgW, imgH, panWidth, panHeight,
                    ResourceLocation.fromNamespaceAndPath(
                            "noellesroles", "textures/gui/cooking/pan.png"
                    ));
            this.minX = boundMinX;
            this.maxX = boundMaxX;
            this.velocity = velocity;
        }
        @Override
        public void tick() {
            int deltaX = 0;
            if (isLeft)
                deltaX -= velocity * (isSpeedUp ? 5 : 1);
            if (isRight)
                deltaX += velocity * (isSpeedUp ? 5 : 1);
            if ((deltaX < 0 && imgWidget.getX() > minX) || (deltaX > 0 && imgWidget.getX() + panWidth < maxX)) {
                imgWidget.setX(imgWidget.getX() + deltaX);
            }
        }
        private static final int panWidth = 23;
        private static final int panHeight = 7;
        private final int minX, maxX;
        private boolean isLeft = false;
        private boolean isRight = false;
        private boolean isSpeedUp = false;
        private int velocity = 0;
    }
    public static class InfoCard {
        public InfoCard(int i, int j, int k, int l, int textureWidth, int textureHeight, ResourceLocation texture, Font font) {
            infoImg = new TextureWidget(i, j, l, l, textureWidth, textureHeight, texture);
            infoText = new StringWidget(i + l, j, k - l, l, Component.literal(" 0 Second"), font);
        }
        public int getBuffSecond() {
            return buffSecond;
        }
        public void setBuffSecond(int buffSecond) {
            this.buffSecond = buffSecond;
            infoText.setMessage(Component.literal(" " + buffSecond + " Second"));
        }
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            infoImg.render(guiGraphics, mouseX, mouseY, delta);
            infoText.render(guiGraphics, mouseX, mouseY, delta);
        }
        private TextureWidget infoImg;
        private StringWidget infoText;
        private int buffSecond = 0;
    }
    protected CookingGameScreen() {
        super(Component.empty());
        game_introuction = new EditBox(
                font,
                width / 2 - 100,
                height / 2 - 50,
                200,
                20,
                Component.translatable("screen.noellesroles.cooking.introduction")
        );
        randomSource = RandomSource.create();
    }
    @Override
    protected void init() {
        super.init();
        foods.clear();
        isInitialized = false;
        gravity = (float) (2 * height) / (FALL_TICKS * FALL_TICKS);
        FoodCard.gravity = this.gravity;
        nextTick = 0;
        int gameStartX = (int)(width * (1f - GAME_BOUND));
        while (FoodCard.BASE_FOOD_SIZE * ROW_FOOD_NUM * (pixelSize + 1) < (width - gameStartX))
            ++pixelSize;
        pan = new Pan(
                gameStartX,
                width,
                height - (FoodCard.BASE_FOOD_SIZE * pixelSize) / 2,
                Pan.panWidth * pixelSize,
                Pan.panHeight * pixelSize,
                (int)(height * GAME_BOUND) / 10
        );
    }
    @Override
    public void tick() {
        super.tick();
        if (isInitialized == false)
            return;
        // 到时则检查并随机生成食材
        if (foods.size() < MAX_FOOD_COUNT && nextTick <= 0) {
            // 最小初速度，最大速度
            float v_min = .15f * height / FALL_TICKS;
            float v_max = 1.5f * height / FALL_TICKS;
            // 使用正太分布进行速度生成
            float v = v_min + Math.min((float) abs(randomSource.nextGaussian()), 3);
            nextTick = randomSource.nextInt(FALL_TICKS);
        }
        foods.forEach(FoodCard::tick);
        pan.tick();
    }
    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        int gameStartX = (int)(width * (1f - GAME_BOUND));
        guiGraphics.fill(0, 0, gameStartX, height, INFO_BG_COLOR.first.getRGB());
        guiGraphics.renderOutline(0, 0, gameStartX, height, INFO_BG_COLOR.second.getRGB());
        pan.render(guiGraphics, i, j, f);
        foods.forEach(food -> food.render(guiGraphics, i, j, f));
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_A -> {
                pan.isLeft = true;
                yield true;
            }
            case GLFW.GLFW_KEY_D -> {
                pan.isRight = true;
                yield true;
            }
            default -> super.keyPressed(keyCode, scanCode, modifiers);
        };
    }
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return switch (keyCode) {
            case GLFW.GLFW_KEY_A -> {
                pan.isLeft = false;
                yield true;
            }
            case GLFW.GLFW_KEY_D -> {
                pan.isRight = false;
                yield true;
            }
            default -> super.keyReleased(keyCode, scanCode, modifiers);
        };
    }
    /**
     * buffID 边界:
     * <p>
     *     - 负值绝对值代表 debuff ID; 正值代表 buff ID（nextInt右不取因此要+1)
     *     - 返回时仅返回id值用于发包，服务器进行解析自行给予buff
     *     - 根据buffID进行图像读取，只需要以buff + "ID"命名即可
     * </p>
     */
    private static final Pair<Integer, Integer> BUFF_BOUNDS = new Pair<>(-3, 7);
    /** 屏幕占比(x,y) */
    private static final Pair<Float, Float> INTRODUCTION_SIZE = new Pair<>(0.5f, 0.7f);
    /** 信息框背景颜色和线框颜色 */
    private static final Pair<Color, Color> INFO_BG_COLOR =
            new Pair<>(new Color(0x1C222222, true), new Color(0xFF77CCFF, true));
    /** 游戏部分屏幕占比(x) */
    private static final float GAME_BOUND = 0.8f;
    /** 游戏区域内可以排列的食材数量：影响pixelSize */
    private static final int ROW_FOOD_NUM = 15;
    private static final int MAX_FOOD_COUNT = 5;
    /** 自由落体时食物掉落的时间：由于屏幕分辨率不同，以最终时间控制重力保持掉落时间统一 */
    private static final int FALL_TICKS = 40;
    private final List<FoodCard> foods = new ArrayList<>();
    private final RandomSource randomSource;
    private final EditBox game_introuction;
    private Pan pan;
    private boolean isInitialized = false;
    private float gravity = 0f;
    private int nextTick = 0;
}
