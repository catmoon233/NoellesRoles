package org.agmas.noellesroles.client.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.agmas.noellesroles.packet.ProblemSetEventC2SPacket;
import org.agmas.noellesroles.utils.MathProblemsManager;
import org.agmas.noellesroles.utils.MathProblemsManager.MathProblem;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MathSolverScreen extends Screen {
    private final List<MathProblem> MathProblems = new ArrayList<>();
    private final int totalPages = 10;
    private int maxTime = 20 * 20; // 20s

    private int currentIndex = 0;

    private final int BUTTON_WIDTH = 100;
    private final int BUTTON_HEIGHT = 20;

    private boolean hasStarted = false;
    private long startTime = 0;
    private boolean failed = false;

    public MathSolverScreen() {
        super(Component.translatable("screen.math_solver.title"));
        this.MathProblems.clear();
        MathProblemsManager manager = new MathProblemsManager();
        int maxT = 4;
        for (int i = 0; i < totalPages; i++) {
            var newP = manager.generateProblem();
            switch (newP.getType()) {
                case 1:
                    maxT += 2;
                    break;
                case 2:
                    maxT += 5;
                    break;
                default:
                    maxT += 2;
                    break;
            }
            this.MathProblems.add(newP);
        }
        this.maxTime = maxT * 20;
        hasStarted = false;
        currentIndex = -1;
        startTime = 0;
        failed = false;
    }

    public void startMathSolving() {
        this.hasStarted = true;
        startTime = this.minecraft.level.getGameTime();
        this.currentIndex = -1;
        nextProblem();
    }

    public void solveFailed() {
        if (failed)
            return;
        failed = true;
        this.currentIndex = -2;
        this.initFinished();
        ClientPlayNetworking.send(new ProblemSetEventC2SPacket(false));
    }

    @Override
    public void tick() {
        if (this.currentIndex >= 0 && this.hasStarted && !this.failed && this.currentIndex < this.totalPages
                && this.startTime + this.maxTime <= this.minecraft.level.getGameTime()) {
            solveFailed();
        }
    }

    public void solveSuccess() {
        initFinished();

        ClientPlayNetworking.send(new ProblemSetEventC2SPacket(true));
    }

    public void nextProblem() {
        this.currentIndex++;
        if (this.currentIndex >= this.totalPages) {
            this.solveSuccess();
            return;
        }
        initDuring();
    }

    @Override
    public void onClose() {
        if (hasStarted && this.currentIndex >= 0 && this.currentIndex < this.totalPages) {
            solveFailed();
        }
        super.onClose();
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        if (this.currentIndex == -1) {
            initStart();
        } else if (this.currentIndex == -2 && this.failed) {
            initFailed();
        } else if (this.currentIndex < this.totalPages) {
            initDuring();
        } else if (this.currentIndex >= this.totalPages) {
            initFinished();
        }
    }

    private void initFailed() {
        this.clearWidgets();

        int maxWidth = this.width;
        int maxHeight = this.height;
        int buttonX = maxWidth / 2 - BUTTON_WIDTH / 2;
        int buttonY = maxHeight / 2;
        Button btn = Button.builder(Component.translatable("screen.noellesroles.close"), (bbtn) -> {
            this.onClose();
        }).bounds(buttonX, buttonY - 30, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(btn);
    }

    private void initDuring() {
        this.clearWidgets();

        if (currentIndex < 0 || currentIndex >= this.MathProblems.size()) {
            return;
        }

        int maxWidth = this.width;
        int maxHeight = this.height;
        int buttonX = maxWidth / 2;
        int buttonY = maxHeight / 2 - 20;

        var opts = this.MathProblems.get(currentIndex);
        int i = 0;
        for (var opt : opts.getOptions()) {
            i++;
            final int nowSelectionID = i;
            Button btn = Button.builder(Component.translatable(opt), (bbtn) -> {
                this.selectedSelection(nowSelectionID);
            }).bounds(buttonX + (i % 2 == 1 ? (-BUTTON_WIDTH - 10) : (10)),
                    buttonY + (BUTTON_HEIGHT + 5) * (((i - 1) / 2)),
                    BUTTON_WIDTH,
                    BUTTON_HEIGHT).build();
            btn.setTooltip(
                    Tooltip.create(Component.translatable("screen.math_solver.option_btn.tooltip", nowSelectionID)));
            this.addRenderableWidget(btn);
        }
    }

    private void initFinished() {
        this.clearWidgets();
        int maxWidth = this.width;
        int maxHeight = this.height;
        int buttonX = maxWidth / 2 - BUTTON_WIDTH / 2;
        int buttonY = maxHeight / 2;
        Button btn = Button.builder(Component.translatable("screen.math_solver.close"), (bbtn) -> {
            this.onClose();
        }).bounds(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        this.addRenderableWidget(btn);
    }

    private void initStart() {
        this.clearWidgets();

        int maxWidth = this.width;
        int maxHeight = this.height;
        int buttonX = maxWidth / 2 - BUTTON_WIDTH / 2;
        int buttonY = maxHeight / 2;
        var startHint = Component.translatable("screen.math_solver.start_hint");
        Button btn = Button.builder(Component.translatable("screen.math_solver.start"), (bbtn) -> {
            this.startMathSolving();
        }).bounds(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT).build();
        btn.setTooltip(Tooltip.create(startHint));
        this.addRenderableWidget(btn);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染渐变背景
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制标题背景
        int titleBgY = 20;
        int titleBgHeight = 40;
        guiGraphics.fillGradient(0, titleBgY, width, titleBgY + titleBgHeight,
                0x80000000, 0x00000000);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 绘制标题
        guiGraphics.drawCenteredString(font, this.title, width / 2, 30, 0xFFFFFF);

        // 绘制页码信息
        if (totalPages > 0 && currentIndex >= 0 && currentIndex < this.totalPages) {
            Component pageInfo = Component.translatable("screen.math_solver.page_info",
                    currentIndex + 1, totalPages)
                    .withStyle(ChatFormatting.YELLOW);
            guiGraphics.drawCenteredString(font, pageInfo, width / 2, 40, 0xFFFFFF);
        }
        // 绘制题目信息
        if (totalPages > 0 && currentIndex >= 0 && currentIndex < this.totalPages
                && currentIndex < this.MathProblems.size()) {

            Component mathInfo = Component.translatable(this.MathProblems.get(currentIndex).getQuestion())
                    .withStyle(ChatFormatting.WHITE);
            Component mathInfoTitle = Component.translatable("screen.math_solver.please_solve")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            guiGraphics.drawCenteredString(font, mathInfoTitle, width / 2, height / 2 - 50, 0xFFFFFF);
            guiGraphics.drawCenteredString(font, mathInfo, width / 2, height / 2 - 40, 0xFFFFFF);
        }
        if (startTime > 0 && this.hasStarted && !this.failed && this.currentIndex >= 0
                && this.currentIndex < this.totalPages) {
            long spendTime = (this.minecraft.level.getGameTime() - this.startTime);
            long leftTime = (maxTime - spendTime) / 20;
            Component timeInfo = Component.translatable("screen.math_solver.time_info",
                    Component.literal("" + leftTime).withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.AQUA);
            guiGraphics.drawCenteredString(font, timeInfo, width / 2, 50, 0xFFFFFF);
        }

        if (this.currentIndex >= this.totalPages) {
            Component startHint = Component.translatable("screen.math_solver.success")
                    .withStyle(ChatFormatting.GREEN);
            guiGraphics.drawCenteredString(font, startHint, width / 2, height / 2 - 30, 0x888888);
        }
        if (this.failed && this.currentIndex == -2) {
            Component startHint = Component.translatable("screen.math_solver.failed")
                    .withStyle(ChatFormatting.RED);
            guiGraphics.drawCenteredString(font, startHint, width / 2, height / 2 - 30, 0x888888);
        }
        if (this.hasStarted == false && this.currentIndex == -1) {
            Component startHint = Component.translatable("screen.math_solver.start_hint")
                    .withStyle(ChatFormatting.YELLOW);
            guiGraphics.drawCenteredString(font, startHint, width / 2, height / 2 + 30, 0x888888);
        }

        // 绘制提示
        Component hint = Component.translatable("screen.math_solver.hint")
                .withStyle(ChatFormatting.GRAY);
        guiGraphics.drawCenteredString(font, hint, width / 2, height - 30, 0x888888);

    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 创建更现代化的渐变背景
        int topColor = 0xFF1A1A2E; // 深蓝色
        int bottomColor = 0xFF16213E; // 更深的蓝色
        guiGraphics.fillGradient(0, 0, width, height, topColor, bottomColor);

        // 添加一些装饰性粒子效果（可选）
        if (minecraft.level != null) {
            long time = minecraft.level.getGameTime();
            for (int i = 0; i < 20; i++) {
                float x = (float) ((time * 0.5 + i * 50) % width);
                float y = (float) ((Math.sin(time * 0.02 + i) * 20 + height / 2 + i * 10) % height);
                float size = 2 + (float) Math.sin(time * 0.1 + i) * 1;
                int alpha = (int) (100 + 155 * Math.sin(time * 0.05 + i));
                int starColor = (alpha << 24) | 0xFFFFFF;
                guiGraphics.fill((int) x, (int) y, (int) (x + size), (int) (y + size), starColor);
            }
        }
    }

    public void selectedSelection(int selectionIndex) {
        if (this.currentIndex < 0) {
            return;
        }
        if (this.currentIndex > this.MathProblems.size()) {
            return;
        }
        var currentMath = this.MathProblems.get(this.currentIndex);
        if (currentMath.getCorrectIndex() + 1 == selectionIndex) {
            this.nextProblem();
        } else {
            this.solveFailed();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 支持键盘导航
        if (keyCode == 256) { // ESC
            this.onClose();
        } else if (keyCode == GLFW.GLFW_KEY_1) { // A
            selectedSelection(1);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_2) { // B
            selectedSelection(2);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_3) { // C
            selectedSelection(3);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_4) { // D
            selectedSelection(4);
            return true;
        }
        // else if (keyCode == GLFW.GLFW_KEY_5) { // D
        // selectedSelection(5);
        // return true;
        // } else if (keyCode == GLFW.GLFW_KEY_6) { // D
        // selectedSelection(6);
        // return true;
        // } else if (keyCode == GLFW.GLFW_KEY_7) { // D
        // selectedSelection(7);
        // return true;
        // } else if (keyCode == GLFW.GLFW_KEY_8) { // D
        // selectedSelection(8);
        // return true;
        // } else if (keyCode == GLFW.GLFW_KEY_9) { // D
        // selectedSelection(9);
        // return true;
        // }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}