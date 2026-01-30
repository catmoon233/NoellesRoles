package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.role.ModRoles;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

public class SwapperPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    private final Player player;
    public boolean isSwapping = false;
    public int swapTimer = 0;
    public UUID target1 = null;
    public UUID target2 = null;

    public SwapperPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public void serverTick() {
        if (isSwapping) {
            // 冻结移动
            if (player instanceof ServerPlayer serverPlayer) {
                // 通过不断传送回当前位置来模拟冻结，或者设置速度为0
                // 这里简单地设置速度为0，但可能不够强力。
                // 参考 InsaneKillerPlayerComponent，可能需要更强的控制。
                // 但考虑到只有1秒，简单的速度限制可能足够，或者直接在客户端禁止输入（需要额外的数据包）。
                // 服务端强制传送回上一刻的位置是比较通用的做法。
                // 但为了平滑，我们只在最后执行交换。
                
                // 简单的减速效果
                player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
                player.hurtMarked = true; 
            }

            swapTimer--;
            if (swapTimer <= 0) {
                performSwap();
                isSwapping = false;
                target1 = null;
                target2 = null;
                ModComponents.SWAPPER.sync(player);
            }
        }
    }

    public void startSwap(UUID t1, UUID t2) {
        this.target1 = t1;
        this.target2 = t2;
        this.isSwapping = true;
        this.swapTimer = 20; // 1秒 = 20 ticks
        ModComponents.SWAPPER.sync(player);
    }

    private void performSwap() {
        if (player.level().isClientSide) return;
        
        Player player1 = player.level().getPlayerByUUID(target1);
        Player player2 = player.level().getPlayerByUUID(target2);

        if (player1 != null && player2 != null) {
            Vec3 pos1 = player1.position();
            Vec3 pos2 = player2.position();

            // 检查碰撞（可选，根据原逻辑）
            if (!player.level().noCollision(player1) || !player.level().noCollision(player2)) {
                // 如果需要碰撞检查，可以在这里处理
            }

            player1.teleportTo(pos2.x, pos2.y, pos2.z);
            player2.teleportTo(pos1.x, pos1.y, pos1.z);

            // 发送提示
            player1.displayClientMessage(Component.translatable("message.noellesroles.swapper.swapped"), true);
            player2.displayClientMessage(Component.translatable("message.noellesroles.swapper.swapped"), true);
            
            // 设置冷却
            AbilityPlayerComponent abilityPlayerComponent = AbilityPlayerComponent.KEY.get(player);
            abilityPlayerComponent.cooldown = GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().swapperSwapCooldown);
            abilityPlayerComponent.sync();
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        isSwapping = tag.getBoolean("isSwapping");
        swapTimer = tag.getInt("swapTimer");
        if (tag.hasUUID("target1")) target1 = tag.getUUID("target1");
        if (tag.hasUUID("target2")) target2 = tag.getUUID("target2");
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putBoolean("isSwapping", isSwapping);
        tag.putInt("swapTimer", swapTimer);
        if (target1 != null) tag.putUUID("target1", target1);
        if (target2 != null) tag.putUUID("target2", target2);
    }
}