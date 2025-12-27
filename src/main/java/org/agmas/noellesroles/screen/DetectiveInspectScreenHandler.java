package org.agmas.noellesroles.screen;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.agmas.noellesroles.component.DetectivePlayerComponent;
import org.agmas.noellesroles.component.ModComponents;

import java.util.UUID;

/**
 * 私家侦探审查界面的 ScreenHandler
 *
 * 这是一个只读的界面，玩家无法拿取或移动任何物品。
 * 当目标玩家移动时，界面会自动关闭。
 */
public class DetectiveInspectScreenHandler extends ScreenHandler {
    
    private final Inventory displayInventory;
    private final PlayerEntity viewer;
    private final UUID targetPlayerUuid;
    
    // 显示4行（36个槽位，对应玩家的主物品栏+快捷栏）
    public static final int ROWS = 4;
    public static final int COLUMNS = 9;
    public static final int SLOT_COUNT = ROWS * COLUMNS;
    
    /**
     * 服务端构造函数
     *
     * @param syncId 同步ID
     * @param playerInventory 查看者的物品栏
     * @param targetPlayer 目标玩家
     */
    public DetectiveInspectScreenHandler(int syncId, PlayerInventory playerInventory, ServerPlayerEntity targetPlayer) {
        super(ModScreenHandlers.DETECTIVE_INSPECT_SCREEN_HANDLER, syncId);
        
        this.viewer = playerInventory.player;
        this.targetPlayerUuid = targetPlayer.getUuid();
        this.displayInventory = new SimpleInventory(SLOT_COUNT);
        
        // 复制目标玩家的物品栏内容到显示物品栏
        // 主物品栏 (slots 9-35) + 快捷栏 (slots 0-8)
        for (int i = 0; i < Math.min(SLOT_COUNT, targetPlayer.getInventory().size()); i++) {
            this.displayInventory.setStack(i, targetPlayer.getInventory().getStack(i).copy());
        }
        
        // 添加只读槽位 - 4行9列
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int slotIndex = row * COLUMNS + col;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new ReadOnlySlot(this.displayInventory, slotIndex, x, y));
            }
        }
    }
    
    /**
     * 客户端构造函数（用于屏幕初始化）
     * 使用 UUID 作为扩展数据
     */
    public DetectiveInspectScreenHandler(int syncId, PlayerInventory playerInventory, UUID targetUuid) {
        super(ModScreenHandlers.DETECTIVE_INSPECT_SCREEN_HANDLER, syncId);
        
        this.viewer = playerInventory.player;
        this.targetPlayerUuid = targetUuid;
        this.displayInventory = new SimpleInventory(SLOT_COUNT);
        
        // 客户端的物品会通过槽位同步自动填充
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.displayInventory.setStack(i, ItemStack.EMPTY);
        }
        
        // 添加只读槽位 - 4行9列
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int slotIndex = row * COLUMNS + col;
                int x = 8 + col * 18;
                int y = 18 + row * 18;
                this.addSlot(new ReadOnlySlot(this.displayInventory, slotIndex, x, y));
            }
        }
    }
    
    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        // 禁止快速移动
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean canUse(PlayerEntity player) {
        // 检查查看者是否还在审查状态
        DetectivePlayerComponent component = ModComponents.DETECTIVE.get(player);
        return component.isInspecting();
    }
    
    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        
        // 清除审查状态
        DetectivePlayerComponent component = ModComponents.DETECTIVE.get(player);
        component.stopInspecting();
    }
    
    /**
     * 获取目标玩家 UUID
     */
    public UUID getTargetPlayerUuid() {
        return this.targetPlayerUuid;
    }
    
    /**
     * 获取显示物品栏
     */
    public Inventory getDisplayInventory() {
        return this.displayInventory;
    }
    
    /**
     * 只读槽位 - 禁止所有交互
     */
    private static class ReadOnlySlot extends Slot {
        
        public ReadOnlySlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }
        
        @Override
        public boolean canInsert(ItemStack stack) {
            // 禁止插入物品
            return false;
        }
        
        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            // 禁止拿取物品
            return false;
        }
        
        @Override
        public ItemStack takeStack(int amount) {
            // 禁止拿取物品
            return ItemStack.EMPTY;
        }
        
        @Override
        public void setStack(ItemStack stack) {
            // 只允许在初始化时设置物品，不允许后续修改
            // 这个方法用于同步，必须保留
            super.setStack(stack);
        }
        
        @Override
        public void setStackNoCallbacks(ItemStack stack) {
            // 用于同步
            super.setStackNoCallbacks(stack);
        }
    }
}