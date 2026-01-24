package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * 监察员组件
 * 
 * 
 */
public class MonitorPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<MonitorPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "monitor"), MonitorPlayerComponent.class);

    private final Player player;

    public UUID markedTarget;

    public int cooldown = 0;

    /** 冷却时间 */
    public static final int COOLDOWN_TICKS = 60 * 20;

    public MonitorPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }

    /**
     * 标记目标玩家
     * 
     * @param target 目标玩家 UUID
     */
    public void markTarget(UUID target) {
        this.markedTarget = target;
        this.cooldown = COOLDOWN_TICKS;
        this.sync();
    }

    public boolean canUseAbility() {
        return cooldown <= 0;
    }

    public UUID getMarkedTarget() {
        return markedTarget;
    }

    /**
     * 重置组件状态
     */
    public void reset() {
        this.markedTarget = null;
        this.cooldown = 0;
        this.sync();
    }

    public float getCooldownSeconds() {
        return cooldown / 20.0f;
    }

    @Override
    public void serverTick() {
        if (cooldown > 0) {
            cooldown--;
            if (cooldown % 20 == 0 || cooldown == 0) {
                this.sync();
            }
        }

        // 检查目标是否存活，如果死亡则清除标记
        if (markedTarget != null) {
            Player targetPlayer = player.level().getPlayerByUUID(markedTarget);
            if (targetPlayer == null || !GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                // 目标不存在或已死亡，清除标记
                this.markedTarget = null;
                this.sync();
            }
        }
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (this.markedTarget != null) {
            tag.putUUID("markedTarget", this.markedTarget);
        }
        tag.putInt("cooldown", this.cooldown);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.markedTarget = tag.contains("markedTarget") ? tag.getUUID("markedTarget") : null;
        this.cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
    }
}