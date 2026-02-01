
package org.agmas.noellesroles.roles.manipulator;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.agmas.noellesroles.ModEntities;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.entity.ManipulatorBodyEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

/**
 * 操纵师组件
 */
public class ManipulatorPlayerComponent implements RoleComponent, ServerTickingComponent {

    public static final ComponentKey<ManipulatorPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "manipulator"),
            ManipulatorPlayerComponent.class);
    @Override
    public Player getPlayer() {
        return player;
    }
    public static final int CONTROL_DURATION = 30 * 20;

    public static final int CONTROL_COOLDOWN = 60 * 20;

    // ==================== 状态变量 ====================

    private final Player player;

    public UUID target;

    public boolean isControlling;

    public int controlTimer;

    public int cooldown;

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }

    public ManipulatorPlayerComponent(Player player) {
        this.player = player;
        this.target = null;
        this.isControlling = false;
        this.controlTimer = 0;
        this.cooldown = 0;
    }

    public void reset() {
        this.target = null;
        this.isControlling = false;
        this.controlTimer = 0;
        this.cooldown = 0;

        this.sync();
    }

    public void clearAll() {
        this.target = null;
        this.isControlling = false;
        this.controlTimer = 0;
        this.cooldown = 0;

        this.sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public boolean canUseAbility() {
        return  !isControlling && cooldown <= 0;
    }

    /**
     * 
     * @param targetUuid
     */
    public void setTarget(UUID targetUuid) {
        if (!canUseAbility())
            return;
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        Player targetPlayer = player.level().getPlayerByUUID(targetUuid);
        if (targetPlayer == null || !(targetPlayer instanceof ServerPlayer serverTarget))
            return;

        if (targetUuid.equals(player.getUUID()))
            return;

        if (!GameFunctions.isPlayerAliveAndSurvival(targetPlayer))
            return;
        isControlling = true;
        controlTimer = CONTROL_DURATION;
        this.target = targetUuid;
        InControlCCA.KEY.get(targetPlayer).isControlling = true;
        this.sync();
    }


    /**
     * 停止操控
     * 
     * @param timeout
     */
    public void stopControl(boolean timeout) {
        if (!isControlling)
            return;
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        Player targetPlayer = player.level().getPlayerByUUID(target);
        if (targetPlayer != null) {
            InControlCCA.KEY.get(targetPlayer).isControlling = false;
            InControlCCA.KEY.get(targetPlayer).sync();
        }


        isControlling = false;
        controlTimer = 0;
        target = null;

        // 设置冷却
        cooldown = CONTROL_COOLDOWN;

        // 发送消息
        if (timeout) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.manipulator.control_timeout")
                            .withStyle(ChatFormatting.YELLOW),
                    true);
        } else {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.manipulator.control_stopped")
                            .withStyle(ChatFormatting.GREEN),
                    true);
        }

        // 播放音效
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

        this.sync();
    }






    public float getControlSeconds() {
        return controlTimer / 20.0f;
    }

    public float getCooldownSeconds() {
        return cooldown / 20.0f;
    }

    @Override
    public void serverTick() {


        if (!GameFunctions.isPlayerAliveAndSurvival(player))
            return;

        if (cooldown > 0) {
            cooldown--;
            if (cooldown % 20 == 0 ) {
                sync();
            }
        }

        if (isControlling) {

            if (controlTimer > 0) {
                controlTimer--;

                if (controlTimer % 20 == 0) {
                    sync();
                }

                if (controlTimer <= 0) {
                    stopControl(true);
                }
            }
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (this.target != null) {
            tag.putUUID("target", this.target);
        }
        tag.putBoolean("isControlling", this.isControlling);
        tag.putInt("controlTimer", this.controlTimer);
        tag.putInt("cooldown", this.cooldown);




    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.target = tag.contains("target") ? tag.getUUID("target") : null;
        this.isControlling = tag.contains("isControlling") && tag.getBoolean("isControlling");
        this.controlTimer = tag.contains("controlTimer") ? tag.getInt("controlTimer") : 0;
        this.cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;

    }
}