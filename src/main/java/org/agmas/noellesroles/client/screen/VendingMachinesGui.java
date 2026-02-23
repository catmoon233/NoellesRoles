package org.agmas.noellesroles.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class VendingMachinesGui extends AbstractPixelScreen {
    private static final int MAX_PANEL_WIDTH = 300;
    private static final int MAX_PANEL_HEIGHT = 208;
    private static final int GRID_COLUMNS = 4;
    private static final int DEFAULT_VISIBLE_ROWS = 3;
    private static final int SLOT_GAP = 6;
    private static final int MIN_SLOT_SIZE = 16;
    private static final int MAX_SLOT_SIZE = 28;
    private static final int KNOB_ANIMATION_TICKS = 10;
    private static final int DROP_FALL_ANIMATION_TICKS = 24;

    // Reserved texture layers (safe to replace with real textures later).
    private static final ResourceLocation LAYER_BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/vending_machine/layer_bg.png");
    private static final ResourceLocation LAYER_MACHINE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/vending_machine/layer_machine.png");
    private static final ResourceLocation LAYER_FOREGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/vending_machine/layer_foreground.png");
    private static final ResourceLocation LAYER_KNOB_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/vending_machine/layer_knob.png");
    private static final ResourceLocation LAYER_DROP_SLOT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/vending_machine/layer_drop_slot.png");

    private final List<VendingGoods> goods = new ArrayList<>();
    private final DroppedItem droppedItem = new DroppedItem();

    private BiPredicate<ItemStack, Integer> purchaseCheck = (stack, price) -> {
        return (PlayerShopComponent.KEY.get(Minecraft.getInstance().player).balance >= price);


    };
    private BiConsumer<ItemStack, Integer> onPurchaseTriggered = (stack, price) -> {
    };
    private Consumer<ItemStack> onCollectDroppedItem = stack -> {
    };

    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    private int gridLeft;
    private int gridTop;
    private int gridWidth;
    private int gridHeight;
    private int visibleRows = DEFAULT_VISIBLE_ROWS;
    private int slotSize = 22;

    private int controlLeft;
    private int controlWidth = 98;
    private int previewX;
    private int previewY;
    private int previewSize;

    private int knobCenterX;
    private int knobCenterY;
    private int knobRadius = 16;
    private int knobAnimationTick = KNOB_ANIMATION_TICKS;

    private int dropSlotX;
    private int dropSlotY;
    private int dropSlotSize;

    private int selectedIndex = -1;
    private int scrollRows = 0;

    private boolean hasBgLayerTexture;
    private boolean hasMachineLayerTexture;
    private boolean hasForegroundLayerTexture;
    private boolean hasKnobLayerTexture;
    private boolean hasDropSlotLayerTexture;
    
    // 悬停状态跟踪
    private boolean isKnobHovered = false;
    private boolean isDropSlotHovered = false;
    private int hoveredGoodsIndex = -1;

    public VendingMachinesGui(Map<ItemStack, Integer> vendingItems) {
        this(Component.literal("Vending Machine"), vendingItems);
    }

    public VendingMachinesGui(Component title, Map<ItemStack, Integer> vendingItems) {
        super(title == null ? Component.empty() : title);
        setGoods(vendingItems);
    }

    public VendingMachinesGui(Component title, Map<ItemStack, Integer> vendingItems,
                              BiPredicate<ItemStack, Integer> purchaseCheck,
                              BiConsumer<ItemStack, Integer> onPurchaseTriggered,
                              Consumer<ItemStack> onCollectDroppedItem) {
        this(title, vendingItems);
        setPurchaseCheck(purchaseCheck);
        setOnPurchaseTriggered(onPurchaseTriggered);
        setOnCollectDroppedItem(onCollectDroppedItem);
    }

    public final void setGoods(Map<ItemStack, Integer> vendingItems) {
        this.goods.clear();
        if (vendingItems != null) {
            for (Map.Entry<ItemStack, Integer> entry : vendingItems.entrySet()) {
                ItemStack stack = entry.getKey();
                if (stack == null || stack.isEmpty()) {
                    continue;
                }
                Integer price = entry.getValue();
                this.goods.add(new VendingGoods(stack.copy(), Math.max(0, price == null ? 0 : price)));
            }
        }
        this.selectedIndex = this.goods.isEmpty() ? -1 : 0;
        this.scrollRows = 0;
        clampScrollRows();
    }

    public void setPurchaseCheck(BiPredicate<ItemStack, Integer> purchaseCheck) {
        if (purchaseCheck != null) {
            this.purchaseCheck = purchaseCheck;
        }
    }

    public void setOnPurchaseTriggered(BiConsumer<ItemStack, Integer> onPurchaseTriggered) {
        if (onPurchaseTriggered != null) {
            this.onPurchaseTriggered = onPurchaseTriggered;
        }
    }

    public void setOnCollectDroppedItem(Consumer<ItemStack> onCollectDroppedItem) {
        if (onCollectDroppedItem != null) {
            this.onCollectDroppedItem = onCollectDroppedItem;
        }
    }

    @Override
    protected void init() {
        super.init();
        rebuildLayout();
        this.hasBgLayerTexture = hasTexture(LAYER_BG_TEXTURE);
        this.hasMachineLayerTexture = hasTexture(LAYER_MACHINE_TEXTURE);
        this.hasForegroundLayerTexture = hasTexture(LAYER_FOREGROUND_TEXTURE);
        this.hasKnobLayerTexture = hasTexture(LAYER_KNOB_TEXTURE);
        this.hasDropSlotLayerTexture = hasTexture(LAYER_DROP_SLOT_TEXTURE);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.knobAnimationTick < KNOB_ANIMATION_TICKS) {
            this.knobAnimationTick++;
        }
        updateDropAnimation();
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderLayerBackground(guiGraphics);
        renderLayerMachineBase(guiGraphics);
        renderLayerGoodsSlots(guiGraphics, mouseX, mouseY);
        renderLayerFrontOverlay(guiGraphics);
        renderLayerControl(guiGraphics, delta);
        renderLayerDropZone(guiGraphics, delta);
        renderLayerText(guiGraphics, mouseX, mouseY);
        
        // 渲染tooltip
        renderTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isInsideDropSlot(mouseX, mouseY) && this.droppedItem.phase == DropPhase.READY_TO_COLLECT) {
                collectDroppedItem();
                return true;
            }

            if (isInsideKnob(mouseX, mouseY)) {
                onKnobPressed();
                return true;
            }

            int goodsIndex = getGoodsIndexAt(mouseX, mouseY);
            if (goodsIndex >= 0) {
                this.selectedIndex = goodsIndex;
                playClickSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        // 更新悬停状态
        isKnobHovered = isInsideKnob(mouseX, mouseY);
        isDropSlotHovered = isInsideDropSlot(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = getMaxScrollRows();
        if (maxScroll <= 0) {
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        if (isInsideGoodsArea(mouseX, mouseY)) {
            if (verticalAmount > 0) {
                this.scrollRows--;
            } else if (verticalAmount < 0) {
                this.scrollRows++;
            }
            clampScrollRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void rebuildLayout() {
        this.panelWidth = Math.min(MAX_PANEL_WIDTH, this.width - 16);
        this.panelHeight = Math.min(MAX_PANEL_HEIGHT, this.height - 16);
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        this.gridLeft = this.panelLeft + 14;
        this.gridTop = this.panelTop + 18;
        this.gridWidth = this.panelWidth - this.controlWidth - 30;

        int reservedBottomSpace = 56;
        int availableGridHeight = this.panelHeight - 18 - reservedBottomSpace;

        int widthLimitedSize = (this.gridWidth - SLOT_GAP * (GRID_COLUMNS - 1)) / GRID_COLUMNS;
        int heightLimitedSize = (availableGridHeight - SLOT_GAP * (DEFAULT_VISIBLE_ROWS - 1)) / DEFAULT_VISIBLE_ROWS;
        this.slotSize = clampInt(Math.min(widthLimitedSize, heightLimitedSize), MIN_SLOT_SIZE, MAX_SLOT_SIZE);

        this.visibleRows = DEFAULT_VISIBLE_ROWS;
        if ((this.slotSize <= MIN_SLOT_SIZE) && heightLimitedSize < MIN_SLOT_SIZE) {
            this.visibleRows = 2;
        }

        this.gridHeight = this.slotSize * this.visibleRows + SLOT_GAP * (this.visibleRows - 1);

        this.controlLeft = this.panelLeft + this.panelWidth - this.controlWidth;
        this.previewSize = Math.min(42, this.slotSize + 14);
        this.previewX = this.controlLeft + (this.controlWidth - this.previewSize) / 2;
        this.previewY = this.panelTop + 26;

        this.knobRadius = 16;
        this.knobCenterX = this.controlLeft + this.controlWidth / 2;
        this.knobCenterY = this.previewY + this.previewSize + 44;

        this.dropSlotSize = this.slotSize + 10;
        this.dropSlotX = this.gridLeft + this.gridWidth / 2 - this.dropSlotSize / 2;
        this.dropSlotY = this.gridTop + this.gridHeight + 10;

        clampScrollRows();
    }

    private void renderLayerBackground(GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xD0101010);
        if (this.hasBgLayerTexture) {
            blitLayer(guiGraphics, LAYER_BG_TEXTURE, this.panelLeft, this.panelTop, this.panelWidth, this.panelHeight);
        }
    }

    private void renderLayerMachineBase(GuiGraphics guiGraphics) {
        guiGraphics.fill(this.panelLeft, this.panelTop, this.panelLeft + this.panelWidth, this.panelTop + this.panelHeight, 0xFF1F2328);
        guiGraphics.fill(this.panelLeft + 2, this.panelTop + 2, this.panelLeft + this.panelWidth - 2,
                this.panelTop + this.panelHeight - 2, 0xFF2A2F36);

        int splitX = this.controlLeft - 8;
        guiGraphics.fill(splitX, this.panelTop + 8, splitX + 2, this.panelTop + this.panelHeight - 8, 0x80202020);

        if (this.hasMachineLayerTexture) {
            blitLayer(guiGraphics, LAYER_MACHINE_TEXTURE, this.panelLeft, this.panelTop, this.panelWidth, this.panelHeight);
        }
    }

    private void renderLayerGoodsSlots(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startIndex = this.scrollRows * GRID_COLUMNS;
        int endExclusive = Math.min(this.goods.size(), startIndex + this.visibleRows * GRID_COLUMNS);
        
        // 更新悬停的商品索引
        hoveredGoodsIndex = -1;

        for (int index = startIndex; index < endExclusive; index++) {
            int displayIndex = index - startIndex;
            int row = displayIndex / GRID_COLUMNS;
            int col = displayIndex % GRID_COLUMNS;

            int slotX = this.gridLeft + col * (this.slotSize + SLOT_GAP);
            int slotY = this.gridTop + row * (this.slotSize + SLOT_GAP);

            boolean hovered = isInsideRect(mouseX, mouseY, slotX, slotY, this.slotSize, this.slotSize);
            boolean selected = index == this.selectedIndex;
            
            // 记录悬停的商品索引
            if (hovered) {
                hoveredGoodsIndex = index;
            }

            // 悬停效果颜色
            int slotColor = selected ? 0xFF5A7090 : hovered ? 0xFF697587 : 0xFF343D4A;
            int innerColor = hovered ? 0xFF2D333B : 0xFF1D232B;
            
            guiGraphics.fill(slotX, slotY, slotX + this.slotSize, slotY + this.slotSize, slotColor);
            guiGraphics.fill(slotX + 1, slotY + 1, slotX + this.slotSize - 1, slotY + this.slotSize - 1, innerColor);

            VendingGoods goods = this.goods.get(index);
            int itemX = slotX + (this.slotSize - 16) / 2;
            int itemY = slotY + (this.slotSize - 16) / 2;
            guiGraphics.renderItem(goods.stack, itemX, itemY);

            String priceText = String.valueOf(goods.price);
            guiGraphics.drawString(this.font, priceText,
                    slotX + 2,
                    slotY + this.slotSize - this.font.lineHeight - 2,
                    0xFFE6C878,
                    false);
        }

        if (getMaxScrollRows() > 0) {
            int trackX = this.gridLeft + this.gridWidth + 2;
            int trackTop = this.gridTop;
            int trackBottom = this.gridTop + this.gridHeight;
            guiGraphics.fill(trackX, trackTop, trackX + 4, trackBottom, 0xFF222831);

            int thumbHeight = Math.max(12, this.gridHeight / (getMaxScrollRows() + this.visibleRows));
            int range = Math.max(1, this.gridHeight - thumbHeight);
            int thumbY = trackTop + range * this.scrollRows / Math.max(1, getMaxScrollRows());
            guiGraphics.fill(trackX, thumbY, trackX + 4, thumbY + thumbHeight, 0xFF8FA7C8);
        }
    }

    private void renderLayerFrontOverlay(GuiGraphics guiGraphics) {
        guiGraphics.fill(this.gridLeft - 4, this.gridTop - 4,
                this.gridLeft + this.gridWidth + 6, this.gridTop + this.gridHeight + 4, 0x22000000);
        if (this.hasForegroundLayerTexture) {
            blitLayer(guiGraphics, LAYER_FOREGROUND_TEXTURE, this.panelLeft, this.panelTop, this.panelWidth, this.panelHeight);
        }
    }

    private void renderLayerControl(GuiGraphics guiGraphics, float delta) {
        guiGraphics.fill(this.controlLeft, this.panelTop + 10, this.panelLeft + this.panelWidth - 10,
                this.panelTop + this.panelHeight - 10, 0x44303030);

        guiGraphics.fill(this.previewX, this.previewY, this.previewX + this.previewSize, this.previewY + this.previewSize, 0xFF38404C);
        guiGraphics.fill(this.previewX + 1, this.previewY + 1, this.previewX + this.previewSize - 1,
                this.previewY + this.previewSize - 1, 0xFF1E242C);

        if (isSelectedIndexValid()) {
            VendingGoods selected = this.goods.get(this.selectedIndex);
            guiGraphics.renderItem(selected.stack,
                    this.previewX + (this.previewSize - 16) / 2,
                    this.previewY + (this.previewSize - 16) / 2);
        }

        float knobAngle = getKnobAngle(delta);
        renderKnob(guiGraphics, knobAngle);
        
        // 渲染悬停效果
        renderHoverEffects(guiGraphics);
    }

    private void renderKnob(GuiGraphics guiGraphics, float angle) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(this.knobCenterX, this.knobCenterY, 0.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-this.knobRadius, -this.knobRadius, 0.0f);

        if (this.hasKnobLayerTexture) {
            guiGraphics.blit(
                    LAYER_KNOB_TEXTURE,
                    0,
                    0,
                    this.knobRadius * 2,
                    this.knobRadius * 2,
                    0,
                    0,
                    this.knobRadius * 2,
                    this.knobRadius * 2,
                    this.knobRadius * 2,
                    this.knobRadius * 2
            );
        } else {
            // 基础旋钮颜色
            int knobColor = isKnobHovered ? 0xFF6A6A6A : 0xFF4A4A4A;
            int innerColor = isKnobHovered ? 0xFF3A3A3A : 0xFF2A2A2A;
            int highlightColor = isKnobHovered ? 0xFFFFFFFF : 0xFFE5E5E5;
            
            guiGraphics.fill(0, 0, this.knobRadius * 2, this.knobRadius * 2, knobColor);
            guiGraphics.fill(3, 3, this.knobRadius * 2 - 3, this.knobRadius * 2 - 3, innerColor);
            guiGraphics.fill(this.knobRadius - 1, 5, this.knobRadius + 1, this.knobRadius + 2, highlightColor);
        }

        poseStack.popPose();
    }

    private void renderLayerDropZone(GuiGraphics guiGraphics, float delta) {
        // 背景色根据悬停状态变化
        int bgColor = isDropSlotHovered ? 0xFF4F5762 : 0xFF2F3742;
        int innerColor = isDropSlotHovered ? 0xFF242830 : 0xFF141820;
        
        guiGraphics.fill(this.dropSlotX, this.dropSlotY, this.dropSlotX + this.dropSlotSize, this.dropSlotY + this.dropSlotSize, bgColor);
        guiGraphics.fill(this.dropSlotX + 1, this.dropSlotY + 1, this.dropSlotX + this.dropSlotSize - 1,
                this.dropSlotY + this.dropSlotSize - 1, innerColor);

        if (this.hasDropSlotLayerTexture) {
            guiGraphics.blit(
                    LAYER_DROP_SLOT_TEXTURE,
                    this.dropSlotX,
                    this.dropSlotY,
                    this.dropSlotSize,
                    this.dropSlotSize,
                    0,
                    0,
                    this.dropSlotSize,
                    this.dropSlotSize,
                    this.dropSlotSize,
                    this.dropSlotSize
            );
        }

        renderDroppedItem3D(guiGraphics, delta);
    }

    private void renderDroppedItem3D(GuiGraphics guiGraphics, float delta) {
        if (this.droppedItem.phase == DropPhase.NONE || this.droppedItem.stack.isEmpty() || this.minecraft == null) {
            return;
        }

        float renderX = this.droppedItem.x;
        float renderY = this.droppedItem.y;
        float renderScale = this.droppedItem.scale;
        float spin = this.droppedItem.spinY;

        if (this.droppedItem.phase == DropPhase.READY_TO_COLLECT) {
            float time = this.droppedItem.tick + delta;
            renderY += (float) Math.sin(time * 0.2f) * 1.5f;
            spin = time * 20.0f;
            renderScale = this.droppedItem.scale * 0.95f;
        }

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(renderX, renderY, 160.0f);
        pose.scale(renderScale, renderScale, renderScale);
        pose.mulPose(Axis.XP.rotationDegrees(24.0f));
        pose.mulPose(Axis.YP.rotationDegrees(spin));

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(this.droppedItem.stack, null, null, 0);

        RenderSystem.enableDepthTest();
        itemRenderer.render(
                this.droppedItem.stack,
                ItemDisplayContext.FIXED,
                false,
                pose,
                guiGraphics.bufferSource(),
                0xF000F0,
                OverlayTexture.NO_OVERLAY,
                model
        );
        pose.popPose();
    }

    private void renderLayerText(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component title = Component.literal("VENDING MACHINE");
        guiGraphics.drawString(this.font, title,
                this.panelLeft + 10,
                this.panelTop + 8,
                0xFFEAEAEA,
                false);

        Component buyLabel = Component.literal("BUY");
        guiGraphics.drawString(this.font, buyLabel,
                this.knobCenterX - this.font.width(buyLabel) / 2,
                this.knobCenterY + this.knobRadius + 6,
                0xFFE9E9E9,
                false);

        Component dropLabel = Component.literal("Collect");
        guiGraphics.drawString(this.font, dropLabel,
                this.dropSlotX + this.dropSlotSize / 2 - this.font.width(dropLabel) / 2,
                this.dropSlotY + this.dropSlotSize + 4,
                0xFFB8C6DC,
                false);

        VendingGoods infoGoods = null;
        int hoveredIndex = getGoodsIndexAt(mouseX, mouseY);
        if (hoveredIndex >= 0 && hoveredIndex < this.goods.size()) {
            infoGoods = this.goods.get(hoveredIndex);
        } else if (isSelectedIndexValid()) {
            infoGoods = this.goods.get(this.selectedIndex);
        }

        if (infoGoods != null) {
            String name = infoGoods.stack.getHoverName().getString();
            String text = name + "  $" + infoGoods.price;
            guiGraphics.drawString(this.font, text,
                    this.panelLeft + 10,
                    this.panelTop + this.panelHeight - this.font.lineHeight - 8,
                    0xFFD9E6F5,
                    false);
        }

        if (this.droppedItem.phase == DropPhase.READY_TO_COLLECT) {
            Component hint = Component.literal("Click slot to get item");
            guiGraphics.drawString(this.font, hint,
                    this.panelLeft + this.panelWidth - this.font.width(hint) - 10,
                    this.panelTop + this.panelHeight - this.font.lineHeight - 8,
                    0xFF95F28A,
                    false);
        }
    }

    private void updateDropAnimation() {
        if (this.droppedItem.phase == DropPhase.FALLING) {
            this.droppedItem.tick++;
            float progress = clampFloat((float) this.droppedItem.tick / DROP_FALL_ANIMATION_TICKS, 0.0f, 1.0f);
            float easedX = easeOutCubic(progress);

            this.droppedItem.x = lerp(this.droppedItem.startX, this.droppedItem.endX, easedX);
            this.droppedItem.y = this.droppedItem.startY + (this.droppedItem.endY - this.droppedItem.startY) * progress * progress;
            this.droppedItem.scale = lerp(8.0f, 15.0f + this.slotSize * 0.4f, clampFloat(progress * 2.2f, 0.0f, 1.0f));
            this.droppedItem.spinY = 540.0f * progress;

            if (progress >= 1.0f) {
                this.droppedItem.phase = DropPhase.READY_TO_COLLECT;
                this.droppedItem.tick = 0;
                playDropReadySound();
            }
        } else if (this.droppedItem.phase == DropPhase.READY_TO_COLLECT) {
            this.droppedItem.tick++;
        }
    }

    private void onKnobPressed() {
        if (!isSelectedIndexValid()) {
            playClickSound();
            return;
        }
        if (this.droppedItem.phase != DropPhase.NONE) {
            playClickSound();
            return;
        }

        this.knobAnimationTick = 0;
        playClickSound();

        VendingGoods selected = this.goods.get(this.selectedIndex);
        ItemStack purchaseStack = selected.stack.copy();
        if (!this.purchaseCheck.test(purchaseStack, selected.price)) {
            return;
        }
        this.onPurchaseTriggered.accept(purchaseStack.copy(), selected.price);
        startDropAnimationForSelection(selected);
    }

    private void startDropAnimationForSelection(VendingGoods selected) {
        float startX = getSlotCenterX(this.selectedIndex);
        float startY = getSlotCenterY(this.selectedIndex);

        ItemStack droppedStack = selected.stack.copy();
        droppedStack.setCount(1);

        this.droppedItem.stack = droppedStack;
        this.droppedItem.startX = startX;
        this.droppedItem.startY = startY;
        this.droppedItem.endX = this.dropSlotX + this.dropSlotSize / 2.0f;
        this.droppedItem.endY = this.dropSlotY + this.dropSlotSize / 2.0f - 2.0f;
        this.droppedItem.x = startX;
        this.droppedItem.y = startY;
        this.droppedItem.scale = 8.0f;
        this.droppedItem.spinY = 0.0f;
        this.droppedItem.tick = 0;
        this.droppedItem.phase = DropPhase.FALLING;
    }

    private void collectDroppedItem() {
        if (this.droppedItem.phase != DropPhase.READY_TO_COLLECT || this.droppedItem.stack.isEmpty()) {
            return;
        }

        this.onCollectDroppedItem.accept(this.droppedItem.stack.copy());
        this.droppedItem.clear();
        playCollectSound();
    }

    private int getGoodsIndexAt(double mouseX, double mouseY) {
        if (!isInsideGoodsArea(mouseX, mouseY)) {
            return -1;
        }

        for (int row = 0; row < this.visibleRows; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                int slotX = this.gridLeft + col * (this.slotSize + SLOT_GAP);
                int slotY = this.gridTop + row * (this.slotSize + SLOT_GAP);
                if (!isInsideRect(mouseX, mouseY, slotX, slotY, this.slotSize, this.slotSize)) {
                    continue;
                }

                int index = (this.scrollRows + row) * GRID_COLUMNS + col;
                if (index >= 0 && index < this.goods.size()) {
                    return index;
                }
                return -1;
            }
        }

        return -1;
    }

    private float getSlotCenterX(int index) {
        int visibleRow = index / GRID_COLUMNS - this.scrollRows;
        int col = index % GRID_COLUMNS;
        if (visibleRow < 0 || visibleRow >= this.visibleRows) {
            return this.previewX + this.previewSize / 2.0f;
        }
        return this.gridLeft + col * (this.slotSize + SLOT_GAP) + this.slotSize / 2.0f;
    }

    private float getSlotCenterY(int index) {
        int visibleRow = index / GRID_COLUMNS - this.scrollRows;
        if (visibleRow < 0 || visibleRow >= this.visibleRows) {
            return this.previewY + this.previewSize / 2.0f;
        }
        return this.gridTop + visibleRow * (this.slotSize + SLOT_GAP) + this.slotSize / 2.0f;
    }

    private float getKnobAngle(float partialTick) {
        float progress = clampFloat((this.knobAnimationTick + partialTick) / KNOB_ANIMATION_TICKS, 0.0f, 1.0f);
        return -110.0f * (float) Math.sin(progress * Math.PI);
    }

    private boolean isInsideGoodsArea(double mouseX, double mouseY) {
        return isInsideRect(mouseX, mouseY, this.gridLeft, this.gridTop, this.gridWidth, this.gridHeight);
    }

    private boolean isInsideDropSlot(double mouseX, double mouseY) {
        return isInsideRect(mouseX, mouseY, this.dropSlotX, this.dropSlotY, this.dropSlotSize, this.dropSlotSize);
    }

    private boolean isInsideKnob(double mouseX, double mouseY) {
        double dx = mouseX - this.knobCenterX;
        double dy = mouseY - this.knobCenterY;
        return dx * dx + dy * dy <= (double) this.knobRadius * this.knobRadius;
    }

    private boolean isSelectedIndexValid() {
        return this.selectedIndex >= 0 && this.selectedIndex < this.goods.size();
    }

    private int getTotalRows() {
        if (this.goods.isEmpty()) {
            return 0;
        }
        return (this.goods.size() + GRID_COLUMNS - 1) / GRID_COLUMNS;
    }

    private int getMaxScrollRows() {
        return Math.max(0, getTotalRows() - this.visibleRows);
    }

    private void clampScrollRows() {
        this.scrollRows = clampInt(this.scrollRows, 0, getMaxScrollRows());
    }

    private boolean hasTexture(ResourceLocation location) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return false;
        }
        return client.getResourceManager().getResource(location).isPresent();
    }

    private void blitLayer(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
        guiGraphics.blit(texture, x, y, width, height, 0, 0, width, height, width, height);
    }

    private void playClickSound() {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    }

    private void playDropReadySound() {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F));
        }
    }

    private void playCollectSound() {
        Minecraft client = Minecraft.getInstance();
        if (client != null) {
            client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 1.0F));
        }
    }

    private static int clampInt(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.min(max, Math.max(min, value));
    }

    private static boolean isInsideRect(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static float lerp(float from, float to, float progress) {
        return from + (to - from) * progress;
    }

    private static float easeOutCubic(float progress) {
        float inverse = 1.0f - progress;
        return 1.0f - inverse * inverse * inverse;
    }
    
    /**
     * 渲染悬停效果
     */
    private void renderHoverEffects(GuiGraphics guiGraphics) {
        // 可以在这里添加额外的悬停视觉效果
        if (isKnobHovered) {
            // 旋钮悬停时的发光效果
            guiGraphics.fill(this.knobCenterX - this.knobRadius - 2, this.knobCenterY - this.knobRadius - 2,
                    this.knobCenterX + this.knobRadius + 2, this.knobCenterY + this.knobRadius + 2, 0x40FFFFFF);
        }
        
        if (isDropSlotHovered) {
            // 收集槽悬停时的边框效果
            guiGraphics.renderOutline(this.dropSlotX - 1, this.dropSlotY - 1,
                    this.dropSlotSize + 2, this.dropSlotSize + 2, 0xFFAAAAAA);
        }
    }
    
    /**
     * 渲染tooltip
     */
    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 渲染旋钮tooltip
        if (isKnobHovered) {
            Component tooltip = Component.translatable("gui.vendingmachine.knob.tooltip", "点击购买选中商品");
            renderTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }
        
        // 渲染收集槽tooltip
        if (isDropSlotHovered && this.droppedItem.phase == DropPhase.READY_TO_COLLECT) {
            Component tooltip = Component.translatable("gui.vendingmachine.collect.tooltip", "点击收集商品");
            renderTooltip(guiGraphics, tooltip, mouseX, mouseY);
        }
        
        // 渲染商品tooltip
        if (hoveredGoodsIndex >= 0 && hoveredGoodsIndex < this.goods.size()) {
            VendingGoods goods = this.goods.get(hoveredGoodsIndex);
            if (goods != null && !goods.stack.isEmpty()) {
                // 使用Minecraft原生的物品tooltip渲染
                guiGraphics.renderTooltip(this.font, goods.stack, mouseX, mouseY);
            }
        }
    }
    
    /**
     * 渲染自定义tooltip
     */
    private void renderTooltip(GuiGraphics guiGraphics, Component text, int x, int y) {
        int tooltipWidth = this.font.width(text);
        int tooltipHeight = this.font.lineHeight + 4;
        
        // 背景
        guiGraphics.fill(x + 8, y - 12, x + tooltipWidth + 16, y - 12 + tooltipHeight, 0xF0100010);
        guiGraphics.fill(x + 9, y - 11, x + tooltipWidth + 15, y - 11 + tooltipHeight - 2, 0xF0100010);
        
        // 边框
        guiGraphics.fill(x + 8, y - 12, x + tooltipWidth + 16, y - 11, 0xFF505050);
        guiGraphics.fill(x + 8, y - 12 + tooltipHeight - 1, x + tooltipWidth + 16, y - 12 + tooltipHeight, 0xFF505050);
        guiGraphics.fill(x + 8, y - 12, x + 9, y - 12 + tooltipHeight, 0xFF505050);
        guiGraphics.fill(x + tooltipWidth + 15, y - 12, x + tooltipWidth + 16, y - 12 + tooltipHeight, 0xFF505050);
        
        // 文本
        guiGraphics.drawString(this.font, text, x + 12, y - 10, 0xFFFFFF);
    }

    private static final class VendingGoods {
        private final ItemStack stack;
        private final int price;

        private VendingGoods(ItemStack stack, int price) {
            this.stack = stack;
            this.price = price;
        }
    }

    private enum DropPhase {
        NONE,
        FALLING,
        READY_TO_COLLECT
    }

    private static final class DroppedItem {
        private ItemStack stack = ItemStack.EMPTY;
        private float startX;
        private float startY;
        private float endX;
        private float endY;
        private float x;
        private float y;
        private float scale;
        private float spinY;
        private int tick;
        private DropPhase phase = DropPhase.NONE;

        private void clear() {
            this.stack = ItemStack.EMPTY;
            this.phase = DropPhase.NONE;
            this.tick = 0;
            this.spinY = 0.0f;
        }
    }
}
