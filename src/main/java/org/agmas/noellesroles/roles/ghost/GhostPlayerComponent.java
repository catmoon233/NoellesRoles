package org.agmas.noellesroles.roles.ghost;

import dev.doctor4t.trainmurdermystery.cca.GameTimeComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class GhostPlayerComponent implements RoleComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<GhostPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "ghost"), GhostPlayerComponent.class);
    private final Player player;
    public boolean isActive = true;
    public int cooldown = 0;
    public int invisibilityTicks = 0;
    public boolean abilityUnlocked = false;
    public boolean unlockNotified = false;
    /** 解锁所需的游戏剩余时间（3分钟 = 180秒 = 3600 tick） */
    public static final int UNLOCK_REMAINING_TICKS = 180 * 20;

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void reset() {
        this.isActive = true;
        this.cooldown = 0;
        this.invisibilityTicks = 0;
        this.abilityUnlocked = false;
        this.unlockNotified = false;
        this.sync();
    }

    @Override
    public void clear() {
        this.reset();
    }

    public GhostPlayerComponent(Player player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void clientTick() {
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }

    public void serverTick() {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, ModRoles.GHOST)) {
            return;
        }
        if (!gameWorld.isRunning()) {
            return;
        }
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
            return;
        }

        // 检查技能解锁（当游戏剩余3分钟时解锁）
        if (!abilityUnlocked) {
            // 获取游戏剩余时间
            GameTimeComponent gameTime = GameTimeComponent.KEY.get(player.level());
            if (gameTime != null) {
                long remainingTicks = gameTime.getTime();
                // 当剩余时间 <= 3分钟时解锁
                if (remainingTicks <= UNLOCK_REMAINING_TICKS) {
                    abilityUnlocked = true;
                    sync();
                }
            }
        }

        // 发送解锁提示
        if (abilityUnlocked && !unlockNotified) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.noellesroles.ghost.ability_unlocked")
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                        true);
                unlockNotified = true;
            }
        }

        if (cooldown > 0) {
            cooldown--;
        }
        if (invisibilityTicks > 0) {
            invisibilityTicks--;
        }
        if (cooldown % 20 == 0) {
            sync();
        }
    }

    public void useAbility() {
        if (!abilityUnlocked) {
            player.displayClientMessage(
                    Component.translatable("message.noellesroles.ghost.not_unlocked")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        if (cooldown > 0) {
            player.displayClientMessage(
                    Component.translatable("message.noellesroles.ability_cooldown", (cooldown + 19) / 20), true);
            return;
        }

        PlayerShopComponent shopComponent = PlayerShopComponent.KEY.get(player);
        if (shopComponent.balance < 150) {
            player.displayClientMessage(Component.translatable("message.noellesroles.insufficient_funds"), true);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.level().playSound(null, serverPlayer.blockPosition(), TMMSounds.UI_SHOP_BUY_FAIL,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return;
        }


        shopComponent.balance -= 150;
        shopComponent.sync();

        cooldown = 400;
        invisibilityTicks = 160;
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0, false, false, true));

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), TMMSounds.UI_SHOP_BUY,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        sync();
    }

    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putBoolean("isActive", this.isActive);
        tag.putInt("cooldown", this.cooldown);
        tag.putInt("invisibilityTicks", this.invisibilityTicks);
        tag.putBoolean("abilityUnlocked", this.abilityUnlocked);
        tag.putBoolean("unlockNotified", this.unlockNotified);
    }

    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.isActive = !tag.contains("isActive") || tag.getBoolean("isActive");
        this.cooldown = tag.getInt("cooldown");
        this.invisibilityTicks = tag.getInt("invisibilityTicks");
        this.abilityUnlocked = tag.getBoolean("abilityUnlocked");
        this.unlockNotified = tag.getBoolean("unlockNotified");
    }
}