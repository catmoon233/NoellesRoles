package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InsaneKillerPlayerComponent
        implements RoleComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<InsaneKillerPlayerComponent> KEY = ModComponents.INSANE_KILLER;
    private final Player player;

    public boolean isActive = false;
    public int cooldown = 200;
    // public UUID target = null;
    public static Map<UUID, PlayerBodyEntity> playerBodyEntities = new HashMap<>();
    public static Map<UUID, Boolean> isPlayerBodyEntity = new HashMap<>();

    public InsaneKillerPlayerComponent(Player player) {
        this.player = player;
        this.isActive = false;
    }

    public void reset() {
        isActive = false;
        cooldown = 200;
    }

    // @Override
    // public boolean shouldSyncWith(ServerPlayer player) {
    // return player == this.player;
    // }

    public void toggleAbility() {
        if (isActive) {
            isActive = false;
            cooldown = 45 * 20;
            // 发送取消激活的消息提示
            player.displayClientMessage(net.minecraft.network.chat.Component
                    .translatable("message.noellesroles.insane_killer.ability_deactivated")
                    .withStyle(net.minecraft.ChatFormatting.RED), true);
            // 播放取消激活的音效
            // player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            // SoundEvents.ALLAY_DEATH, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F,
            // 0.8F);
        } else {
            isActive = true;
            // 发送激活的消息提示
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a能力已激活！"), true);
            // 播放激活的音效
            // player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            // SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS,
            // 0.7F, 1.2F);
        }

        // if (cooldown > 0 && !isActive)
        // return;
        //
        // isActive = !isActive;
        // if (!isActive) {
        // cooldown = 30 * 20;
        // }
        sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void serverTick() {
        if (!GameWorldComponent.KEY.get(player.level()).isRole(player,
                ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES))
            return;
        if (cooldown > 0) {
            cooldown--;
            // if (cooldown == 0){
            //
            // }
            if (cooldown % 20 == 0)
                sync();
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putBoolean("isActive", isActive);
        tag.putInt("cooldown", cooldown);
        // tag.putUUID("target", target);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        isActive = tag.contains("isActive") && tag.getBoolean("isActive");
        cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
        // target = tag.contains("target") ? tag.getUUID("target") : null;
    }

    @Override
    public void clientTick() {

    }

    // @Override
    // public void clientTick() {
    // final var player1 = Minecraft.getInstance().player;
    // if (!GameWorldComponent.KEY.get(player1.level()).isRole(player1,
    // ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES))
    // {
    // return;
    // }
    // if (isActive){
    // Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
    // }else {
    // Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
    // }
    //
    // }
}