package org.agmas.noellesroles.executioner;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

/**
 * Executioner角色组件 - 管理目标选择和胜利状态
 *
 * <p>Executioner需要选择一个平民阵营的玩家作为目标，当目标死亡后转变为杀手。
 */
public class ExecutionerPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<ExecutionerPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "executioner"), ExecutionerPlayerComponent.class);
    private final PlayerEntity player;
    public UUID target;
    public boolean won = false;
    public boolean targetSelected = false;
    public boolean shopUnlocked = false;

    /**
     * 重置组件状态
     */
    public void reset() {
        this.target = null;
        this.targetSelected = false;
        this.won = false;
        this.shopUnlocked = false;
        this.sync();
    }

    public ExecutionerPlayerComponent(PlayerEntity player) {
        this.player = player;
        this.target = null;
        this.targetSelected = false;
        this.shopUnlocked = false;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void serverTick() {
        // 如果目标已经死亡且executioner尚未获胜，解锁商店并重置目标
        if (target != null && !won) {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
            if (!gameWorldComponent.isRole(player, Noellesroles.EXECUTIONER)) return;
            
            PlayerEntity targetPlayer = player.getWorld().getPlayerByUuid(target);
            if (targetPlayer == null || GameFunctions.isPlayerEliminated(targetPlayer)) {
                // 目标死亡，解锁商店
                this.shopUnlocked = true;
                this.target = null;
                this.targetSelected = false;
                sync();
            }
        }
    }

    /**
     * 设置目标玩家（仅允许选择平民阵营）
     *
     * @param target 目标玩家的UUID
     */
    public void setTarget(UUID target) {
        this.target = target;
        this.targetSelected = true;
        this.sync();
    }

    /**
     * 解锁商店（当目标死亡时调用）
     */
    public void unlockShop() {
        this.shopUnlocked = true;
        this.sync();
    }

    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (this.target != null) {
            tag.putUuid("target", this.target);
        }
        tag.putBoolean("won", this.won);
        tag.putBoolean("targetSelected", this.targetSelected);
        tag.putBoolean("shopUnlocked", this.shopUnlocked);
    }

    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.target = tag.contains("target") ? tag.getUuid("target") : null;
        this.won = tag.getBoolean("won");
        this.targetSelected = tag.getBoolean("targetSelected");
        this.shopUnlocked = tag.getBoolean("shopUnlocked");
    }

    @Override
    public void clientTick() {

    }
}
