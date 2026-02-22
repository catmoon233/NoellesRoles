package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;

import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.utils.RoleUtils;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;

import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

/**
 * 红尘客组件
 *
 * 管理红尘客的阶段机制：
 * - 一阶段
 * - 二阶段
 * - 三阶段
 */
public class WayfarerPlayerComponent implements RoleComponent, ServerTickingComponent, ClientTickingComponent {
    @Override
    public Player getPlayer() {
        return player;
    }

    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<WayfarerPlayerComponent> KEY = ModComponents.WAYFARER;

    // ==================== 状态变量 ====================

    private final Player player;

    /** 当前阶段（1、2、3） */
    public int phase = 0;

    /** 凶手 */
    public UUID killer;

    /**
     * 死亡原因
     * 存储只存Path
     */
    public String deathReason;

    public WayfarerPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return this.player == player;
    }

    /**
     * 重置组件状态
     * 在游戏开始时或角色分配时调用
     */
    @Override
    public void reset() {
        this.phase = 1;
        this.killer = null;
        this.deathReason = null;
        this.sync();
    }

    @Override
    public void clear() {
        this.phase = 0;
        this.killer = null;
    }

    /**
     * 同步到客户端
     */
    public void sync() {
        ModComponents.STALKER.sync(this.player);
    }

    // ==================== Tick 处理 ====================

    @Override
    public void serverTick() {
        // 检查玩家是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(player))
            return;
        var level = this.player.level();
        if (!GameWorldComponent.KEY.get(level).isRole(this.player, ModRoles.WAYFARER))
            return;
        if (this.phase == 1) {
            if (level.getGameTime() % 20 == 0) {
                if (this.killer != null) {
                    if (level.getPlayerByUUID(this.killer) == null) {

                    }
                }

            }
        }
    }

    // ==================== NBT 序列化 ====================

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("phase", this.phase);
        tag.putString("death_reason", this.deathReason);
        tag.putUUID("killer", this.killer);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.phase = tag.contains("phase") ? tag.getInt("phase") : 0;
        this.deathReason = tag.contains("death_reason") ? tag.getString("death_reason") : null;
        if (tag.contains("killer")) {
            this.killer = tag.hasUUID("killer") ? tag.getUUID("killer") : null;
        } else {
            this.killer = null;
        }
    }

    @Override
    public void clientTick() {
    }

    public static void registerEvents() {

    }

    public void startFindKiller(PlayerBodyEntity be, Player targetVictim, Player targetKiller) {
        boolean hasKey = false;
        if (targetVictim != null) {
            for (var item : targetVictim.getInventory().items) {
                if (item.is(TMMItems.KEY)) {
                    hasKey = true;
                    RoleUtils.insertStackInFreeSlot(player, item.copy());
                    break;
                }
            }
        }
        if (!hasKey) {
            int roomNumber = GameFunctions.roomToPlayer.getOrDefault(be.getPlayerUuid(), 0);
            String roomName = "Room " + roomNumber;
            var keyItem = TMMItems.KEY.getDefaultInstance();
            ItemStack itemStack = new ItemStack(TMMItems.KEY);
            var keyLore = new ItemLore(Component.literal(roomName)
                    .toFlatList(
                            net.minecraft.network.chat.Style.EMPTY.withItalic(false).withColor(16747520)));
            itemStack.set(DataComponents.LORE, keyLore);
            RoleUtils.insertStackInFreeSlot(player, keyItem);
        }
        var item = TMMItems.INIT_ITEMS.LETTER.getDefaultInstance();
        if (player instanceof ServerPlayer sp) {
            if (targetVictim != null) {
                if (targetVictim instanceof ServerPlayer targetServerVictim)
                    TMMItems.INIT_ITEMS.LETTER_UpdateItemFunc.accept(item, targetServerVictim);
            } else {
                TMMItems.INIT_ITEMS.LETTER_UpdateItemFunc.accept(item, sp);
            }
        }
        RoleUtils.insertStackInFreeSlot(player, item);
        this.phase = 2;
        this.killer = targetKiller.getUUID();
        this.player.displayClientMessage(Component.translatable(""), true);
        this.sync();
    }
}