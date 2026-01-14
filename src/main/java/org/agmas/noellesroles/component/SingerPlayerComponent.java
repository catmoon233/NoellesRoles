package org.agmas.noellesroles.component;

import org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * 歌手组件
 *
 * 主动技能：随机播放原版唱片音乐（60秒冷却）
 * 
 * 歌手为好人阵营（乘客阵营）
 */
public class SingerPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }
    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<SingerPlayerComponent> KEY = ModComponents.SINGER;

    // ==================== 常量定义 ====================

    /** 主动技能冷却时间（4800 tick） */
    public static final int ABILITY_COOLDOWN = 4800;

    /** 音乐播放范围（格） */
    public static final double MUSIC_RANGE = 24.0;

    /** 音乐持续时间（唱片最短约60秒，设为65秒 = 1300 tick） */
    public static final int MUSIC_DURATION = 1300;

    /** 速度效果范围（格） */
    public static final double SPEED_EFFECT_RANGE = 16.0;

    // ==================== 原版唱片音乐列表 ====================
    private static final SoundEvent[] MUSIC_DISCS = {
            SoundEvents.MUSIC_DISC_13.value(),
            SoundEvents.MUSIC_DISC_CAT.value(),
            SoundEvents.MUSIC_DISC_BLOCKS.value(),
            SoundEvents.MUSIC_DISC_CHIRP.value(),
            SoundEvents.MUSIC_DISC_FAR.value(),
            SoundEvents.MUSIC_DISC_MALL.value(),
            SoundEvents.MUSIC_DISC_MELLOHI.value(),
            SoundEvents.MUSIC_DISC_STAL.value(),
            SoundEvents.MUSIC_DISC_STRAD.value(),
            SoundEvents.MUSIC_DISC_WARD.value(),
            SoundEvents.MUSIC_DISC_11.value(),
            SoundEvents.MUSIC_DISC_WAIT.value(),
            SoundEvents.MUSIC_DISC_PIGSTEP.value(),
            SoundEvents.MUSIC_DISC_OTHERSIDE.value(),
            SoundEvents.MUSIC_DISC_5.value(),
            SoundEvents.MUSIC_DISC_RELIC.value()
    };

    // ==================== 状态变量 ====================

    private final Player player;
    private final Random random = new Random();

    /** 主动技能冷却时间（tick） */
    public int abilityCooldown = 0;

    /** 是否已激活（角色分配后） */
    public boolean isActive = false;

    /** 当前正在播放的音乐索引（-1表示没有播放） */
    public int currentMusicIndex = -1;

    /** 音乐剩余时间（tick） - 用于追踪音乐播放状态和给予速度效果 */
    public int musicRemainingTicks = 0;

    /**
     * 构造函数
     */
    public SingerPlayerComponent(Player player) {
        this.player = player;
    }

    /**
     * 重置组件状态
     * 在游戏开始时或角色分配时调用
     */
    public void reset() {
        this.abilityCooldown = 0;
        this.isActive = true;
        this.currentMusicIndex = -1;
        this.musicRemainingTicks = 0;
        this.sync();
    }

    /**
     * 清除所有状态
     */
    public void clearAll() {
        this.abilityCooldown = 0;
        this.isActive = false;
        this.currentMusicIndex = -1;
        this.musicRemainingTicks = 0;
        this.sync();
    }

    /**
     * 检查是否为激活的歌手角色
     */
    public boolean isActiveSinger() {
        if (!isActive || player == null || player.level().isClientSide())
            return false;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        return gameWorld.isRole(player, ModRoles.SINGER);
    }

    /**
     * 检查主动技能是否可用
     * 注意：此方法在客户端和服务端都可以调用
     * 客户端只检查冷却和激活状态，服务端安全检查在网络包处理器中进行
     */
    public boolean canUseAbility() {
        return abilityCooldown <= 0 && isActive;
    }

    /**
     * 使用主动技能 - 随机播放原版唱片音乐
     * 
     * @return 是否成功使用
     */
    public boolean useAbility() {
        if (!canUseAbility()) {
            return false;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        ServerLevel world = serverPlayer.serverLevel();

        // 随机选择一首音乐
        int musicIndex = random.nextInt(MUSIC_DISCS.length);
        SoundEvent music = MUSIC_DISCS[musicIndex];
        this.currentMusicIndex = musicIndex;

        // 播放音乐（给所有在范围内的玩家听到）
        world.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                music,
                SoundSource.RECORDS,
                4.0F, // 音量
                1.0F // 音调
        );

        // 获取音乐名称用于显示
        String musicName = getMusicName(musicIndex);

        // 设置冷却
        this.abilityCooldown = ABILITY_COOLDOWN;

        // 设置音乐持续时间（用于持续给予速度效果）
        this.musicRemainingTicks = MUSIC_DURATION;

        // 发送消息给歌手玩家
        serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.singer.music_played", musicName)
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                true);

        this.sync();
        return true;
    }

    /**
     * 获取音乐名称
     */
    private String getMusicName(int index) {
        String[] musicNames = {
                "13", "Cat", "Blocks", "Chirp", "Far", "Mall", "Mellohi", "Stal",
                "Strad", "Ward", "11", "Wait", "Pigstep", "Otherside", "5", "Relic"
        };
        if (index >= 0 && index < musicNames.length) {
            return musicNames[index];
        }
        return "Unknown";
    }

    /**
     * 获取冷却时间（秒）
     */
    public float getCooldownSeconds() {
        return abilityCooldown / 20.0f;
    }

    /**
     * 同步到客户端
     */
    public void sync() {
        if (player != null && !player.level().isClientSide()) {
            ModComponents.SINGER.sync(this.player);
        }
    }

    // ==================== Tick 处理 ====================

    @Override
    public void serverTick() {
        if (!isActiveSinger())
            return;

        // 减少主动技能冷却时间
        if (this.abilityCooldown > 0) {
            this.abilityCooldown--;
            // 每秒同步一次，减少网络压力
            if (this.abilityCooldown % 20 == 0 || this.abilityCooldown == 0) {
                this.sync();
            }
        }

        // 音乐播放期间给周围玩家速度效果
        if (this.musicRemainingTicks > 0) {
            this.musicRemainingTicks--;

            // 每秒给一次速度效果（持续2秒，确保连续覆盖）
            if (this.musicRemainingTicks % 20 == 0) {
                applySpeedEffectToNearbyPlayers();
            }

            // 音乐结束时重置状态
            if (this.musicRemainingTicks == 0) {
                this.currentMusicIndex = -1;
                this.sync();
            }
        }
    }

    /**
     * 给周围玩家速度效果
     */
    private void applySpeedEffectToNearbyPlayers() {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        ServerLevel world = serverPlayer.serverLevel();

        for (Player target : world.players()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(target))
                continue;

            double distance = target.distanceToSqr(player);
            if (distance > SPEED_EFFECT_RANGE * SPEED_EFFECT_RANGE)
                continue;

            // 给予速度 I 效果（持续2.5秒 = 50 tick，确保连续覆盖）
            target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    50, // 持续时间（tick）
                    0, // 等级（0 = 速度 I）
                    false, // ambient（环境效果，如信标）
                    true, // showParticles（显示粒子）
                    true // showIcon（显示图标）
            ));
        }
    }

    /**
     * 检查是否正在播放音乐
     */
    public boolean isPlayingMusic() {
        return musicRemainingTicks > 0;
    }

    // ==================== NBT 序列化 ====================

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("abilityCooldown", this.abilityCooldown);
        tag.putBoolean("isActive", this.isActive);
        tag.putInt("currentMusicIndex", this.currentMusicIndex);
        tag.putInt("musicRemainingTicks", this.musicRemainingTicks);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.abilityCooldown = tag.contains("abilityCooldown") ? tag.getInt("abilityCooldown") : 0;
        this.isActive = tag.contains("isActive") && tag.getBoolean("isActive");
        this.currentMusicIndex = tag.contains("currentMusicIndex") ? tag.getInt("currentMusicIndex") : -1;
        this.musicRemainingTicks = tag.contains("musicRemainingTicks") ? tag.getInt("musicRemainingTicks") : 0;
    }
}