package org.agmas.noellesroles.component;


import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.event.AllowPlayerDeath;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.effects.TimeStopEffect;
import org.agmas.noellesroles.init.ModEffects;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 迪奥组件
 *
 * 管理迪奥的特殊技能：
 * - The World: 时间停止 10 秒，仅自己可以移动
 * - 最后的狂欢：被动技能，免疫死亡并获得临时生命
 * - 吸食：右键尸体吸食，获得金钱和速度加成
 */
public class DIOPlayerComponent implements RoleComponent, ServerTickingComponent {
    @Override
    public Player getPlayer() {
        return player;
    }


    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<DIOPlayerComponent> KEY = ModComponents.DIO;
    
    // ==================== 常量定义 ====================
    
    /** 时间停止持续时间（10 秒 = 200 tick） */
    public static final int TIME_STOP_DURATION = 200;
    
    /** 时间停止冷却时间（15 秒 = 300 tick） */
    public static final int TIME_STOP_COOLDOWN = 300;
    
    /** 解锁时间停止所需的吸食次数 */
    public static final int[] TIME_STOP_UNLOCK_THRESHOLDS = {1, 4, 7,10,13,16,19,22,25,28,31,34,37,40,43,46,49,52,55,58,61,64,67,70,73,76,79,82,85,88,91,94,97,100,103,106,109,112,115,118,121,124,127,130,133,136,139,142,145,148,151,154,157};
    
    /** 解锁"最后的狂欢"所需的吸食次数 */
    public static final int FINAL_CARNIVAL_THRESHOLD = 8;
    
    /** 临时生命基础持续时间（30 秒 = 600 tick） */
    public static final int TEMP_LIFE_BASE_DURATION = 600;
    
    /** 伤害免疫概率（50%） */
    public static final float DAMAGE_IMMUNITY_CHANCE = 0.5f;
    
    /** 速度加成比例（30%） */
    public static final float SPEED_BONUS_FINAL = 0.3f;
    
    /** 吸食后速度加成比例（40%） */
    public static final float SPEED_BONUS_FEED = 0.4f;
    
    /** 吸食后速度加成持续时间（5 秒 = 100 tick） */
    public static final int SPEED_BONUS_FEED_DURATION = 100;
    
    /** 每次吸食获得的金钱 */
    public static final int MONEY_PER_FEED = 30;
    
    /** 最大储存时间停止使用次数 */
    public static final int MAX_TIME_STOP_CHARGES = 5;
    
    // ==================== 状态变量 ====================
    
    private final Player player;
    
    /** 累计吸食尸体次数 */
    public int totalFeedCount = 0;
    
    /** 当前可用的时间停止使用次数 */
    public int timeStopCharges = 0;

    
    /** 时间停止冷却计时器（tick） */
    public int timeStopCooldown = 0;
    

    
    /** "最后的狂欢"是否已解锁 */
    public boolean isFinalCarnivalUnlocked = false;
    
    /** "最后的狂欢"是否激活中 */
    public boolean isFinalCarnivalActive = false;
    
    /** 临时生命剩余时间（tick） */
    public int tempLifeRemaining = 0;
    
    /** 是否正在吸食尸体 */
    public boolean isFeeding = false;
    
    /** 吸食动作剩余时间（tick） */
    public int feedingRemaining = 0;
    

    
    /**
     * 构造函数
     */
    public DIOPlayerComponent(Player player) {
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
        this.totalFeedCount = 0;
        this.timeStopCharges = 0;

        this.timeStopCooldown = 0;

        this.isFinalCarnivalUnlocked = false;
        this.isFinalCarnivalActive = false;
        this.tempLifeRemaining = 0;
        this.isFeeding = false;
        this.feedingRemaining = 0;

        this.sync();
    }
    
    @Override
    public void clear() {
        this.totalFeedCount = 0;
        this.timeStopCharges = 0;

        this.timeStopCooldown = 0;

        this.isFinalCarnivalUnlocked = false;
        this.isFinalCarnivalActive = false;
        this.tempLifeRemaining = 0;
        this.isFeeding = false;
        this.feedingRemaining = 0;

        this.sync();
    }
    
    /**
     * 完全清除组件状态（游戏结束时调用）
     */
    public void clearAll() {
        clear();
    }
    
    /**
     * 检查是否可以放置陷阱
     */
    public boolean canUseTimeStop() {
        return timeStopCharges > 0 && timeStopCooldown <= 0;
    }
    
    /**
     * 获取当前时间停止使用次数
     */
    public int getTimeStopCharges() {
        return timeStopCharges;
    }
    
    /**
     * 获取总吸食次数
     */
    public int getTotalFeedCount() {
        return totalFeedCount;
    }
    
    /**
     * 检查"最后的狂欢"是否已解锁
     */
    public boolean hasFinalCarnival() {
        return isFinalCarnivalUnlocked;
    }

    static {
        AllowPlayerDeath.EVENT.register((player, resourceLocation) -> {
                    if (GameWorldComponent.KEY.get(player.level()).isRole(player, ModRoles.DIO)) {
                        DIOPlayerComponent dio = KEY.getNullable(player);
                        if (dio == null) return true;

                        // 如果"最后的狂欢"已激活，尝试免疫伤害
                        if (dio.isFinalCarnivalActive) {
                            // 50% 概率免疫伤害
                            if (dio.tryImmuneDamage(999.0f)) {
                                return false; // 阻止死亡
                            }
                            
                            // 如果临时生命时间足够，消耗时间来避免死亡
                            if (dio.tempLifeRemaining > 500) { // 至少需要 25 秒
                                dio.tempLifeRemaining -= 500; // 消耗 25 秒
                                return false; // 阻止死亡
                            }
                        }
                        
                        // 如果未激活但已解锁，且受到致命伤害，激活"最后的狂欢"
                        if (!dio.isFinalCarnivalActive && dio.hasFinalCarnival()) {
                            dio.activateFinalCarnival();
                            return false; // 阻止死亡
                        }
                    }
                    return true;
                }
        );
    }
    /**
     * 尝试使用时间停止
     * @return 是否成功使用
     */
    public boolean tryActivateTimeStop() {
        if (player.hasEffect(ModEffects.TIME_STOP))return false;

        if (!canUseTimeStop()) return false;
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        
        // 消耗一次使用次数
        this.timeStopCharges--;

        this.timeStopCooldown = TIME_STOP_COOLDOWN;

        TimeStopEffect.triggerStart(serverPlayer, TIME_STOP_DURATION,Component.translatable("message.noellesroles.dio.time_stop_start"));
        
        // 播放音效
        Level world = player.level();
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        serverPlayer.displayClientMessage(
            Component.translatable("message.noellesroles.dio.time_stop_activate")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
            true
        );
        
        this.sync();
        return true;
    }
    
    /**
     * 吸食尸体（单独方法供外部调用）
     * @param corpse 尸体实体
     * @return 是否成功吸食
     */
    public boolean feedOnCorpse(Entity corpse) {
        if (corpse == null || !(corpse instanceof PlayerBodyEntity livingCorpse)) return false;
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        
        // 检查距离
        double dist = player.distanceTo(corpse);
        if (dist > 3.0) return false;
        BodyDeathReasonComponent bodyDeathReasonComponent = BodyDeathReasonComponent.KEY.get(corpse);
        if (bodyDeathReasonComponent.vultured)return false;
        bodyDeathReasonComponent.vultured = true;
        bodyDeathReasonComponent.sync();
        // 开始吸食
        this.isFeeding = true;
        this.feedingRemaining = 60; // 1 秒（假设）
        
        Level world = player.level();
        
        // 播放音效
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 1.0f, 0.8f);
        
        serverPlayer.displayClientMessage(
            Component.translatable("message.noellesroles.dio.feeding_start")
                .withStyle(ChatFormatting.RED),
            true
        );
        
        this.sync();
        return true;
    }
    
    /**
     * 完成吸食动作
     */
    private void completeFeeding() {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        // 增加吸食计数
        this.totalFeedCount++;
        
        // 检查是否解锁时间停止
        for (int threshold : TIME_STOP_UNLOCK_THRESHOLDS) {
            if (totalFeedCount == threshold && timeStopCharges < MAX_TIME_STOP_CHARGES) {
                this.timeStopCharges++;
                serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.dio.time_stop_unlocked", threshold)
                        .withStyle(ChatFormatting.GOLD),
                    true
                );
            }
        }
        
        // 检查是否解锁"最后的狂欢"
        if (totalFeedCount == FINAL_CARNIVAL_THRESHOLD) {
            this.isFinalCarnivalUnlocked = true;
            serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.dio.final_carnival_unlocked")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                true
            );
        }

        PlayerShopComponent.KEY.get( player).addToBalance(MONEY_PER_FEED);

        
        // 给予速度加成
        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SPEED, SPEED_BONUS_FEED_DURATION, 1, false, false, true
        ));
        
        // 播放音效
        Level world = player.level();
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        serverPlayer.displayClientMessage(
            Component.translatable("message.noellesroles.dio.feed_complete", MONEY_PER_FEED)
                .withStyle(ChatFormatting.GREEN),
            true
        );
        
        this.isFeeding = false;
        this.feedingRemaining = 0;
        this.sync();
    }
    
    /**
     * 激活"最后的狂欢"
     */
    public void activateFinalCarnival() {
        if (!isFinalCarnivalUnlocked || isFinalCarnivalActive) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        
        this.isFinalCarnivalActive = true;
        this.tempLifeRemaining = TEMP_LIFE_BASE_DURATION;
        
        // 给予生命恢复和速度加成
        player.addEffect(new MobEffectInstance(
            MobEffects.REGENERATION, TEMP_LIFE_BASE_DURATION, 1, false, false, true
        ));
        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SPEED, TEMP_LIFE_BASE_DURATION, 1, false, false, true
        ));
        
        serverPlayer.displayClientMessage(
            Component.translatable("message.noellesroles.dio.final_carnival_activate")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
            true
        );
        
        this.sync();
    }
    
    /**
     * 延长临时生命时间（当吸食更多尸体时）
     */
    public void extendTempLife() {
        if (!isFinalCarnivalActive) return;
        
        this.tempLifeRemaining += TEMP_LIFE_BASE_DURATION;
        
        // 刷新效果
        player.addEffect(new MobEffectInstance(
            MobEffects.REGENERATION, tempLifeRemaining, 1, false, false, true
        ));
        player.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SPEED, tempLifeRemaining, 2, false, false, true
        ));
        
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.dio.temp_life_extended")
                    .withStyle(ChatFormatting.GREEN),
                true
            );
        }
        
        this.sync();
    }
    
    /**
     * 取消"最后的狂欢"（死亡时调用）
     */
    public void cancelFinalCarnival() {
        if (!isFinalCarnivalActive) return;
        
        this.isFinalCarnivalActive = false;
        this.tempLifeRemaining = 0;
        
        // 移除相关效果
        player.removeEffect(MobEffects.REGENERATION);
        player.removeEffect(MobEffects.MOVEMENT_SPEED);
        
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.dio.final_carnival_ended")
                    .withStyle(ChatFormatting.RED),
                true
            );
        }
        GameFunctions.killPlayer(player, true, null, Noellesroles.id("dio_final_carnival_cancel"));
        
        this.sync();
    }
    
    /**
     * 尝试免疫伤害（50% 概率）
     * @param amount 原始伤害值
     * @return 是否成功免疫
     */
    public boolean tryImmuneDamage(float amount) {
        if (!isFinalCarnivalActive) return false;
        
        if (player.level().random.nextFloat() < DAMAGE_IMMUNITY_CHANCE) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.dio.damage_immuned")
                        .withStyle(ChatFormatting.GOLD),
                    true
                );
            }
            return true;
        }
        return false;
    }
    
    /**
     * 同步到客户端
     */
    public void sync() {
        ModComponents.DIO.sync(this.player);
    }
    
    // ==================== Tick 处理 ====================
    
    private static int tickCounter = 0;
    
    @Override
    public void serverTick() {
        tickCounter++;
        if (tickCounter % 20 == 0) {
            sync();
        }

        
        if (timeStopCooldown > 0) {
            timeStopCooldown--;
        }
        
        // 处理吸食动作
        if (isFeeding) {
            if (feedingRemaining > 0) {
                player.setSwimming(true);
                feedingRemaining--;
                
                // 吸食期间无法移动（通过效果实现）
                if (feedingRemaining % 10 == 0) {
                    player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 15, 255, false, false, false
                    ));
                }
                
                if (feedingRemaining == 0) {
                    completeFeeding();
                }
            } else {
                isFeeding = false;
                player.setSwimming(false);
            }
        }
        
        // 处理"最后的狂欢"
        if (isFinalCarnivalActive) {
            if (tempLifeRemaining > 0) {
                tempLifeRemaining--;
                
                if (tempLifeRemaining == 0) {
                    cancelFinalCarnival();
                }
            }
        }
    }
    

    
    // ==================== NBT 序列化 ====================
    
    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("totalFeedCount", this.totalFeedCount);
        tag.putInt("timeStopCharges", this.timeStopCharges);

        tag.putInt("timeStopCooldown", this.timeStopCooldown);

        tag.putBoolean("isFinalCarnivalUnlocked", this.isFinalCarnivalUnlocked);
        tag.putBoolean("isFinalCarnivalActive", this.isFinalCarnivalActive);
        tag.putInt("tempLifeRemaining", this.tempLifeRemaining);
        tag.putBoolean("isFeeding", this.isFeeding);
        tag.putInt("feedingRemaining", this.feedingRemaining);

}
    
    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.totalFeedCount = tag.contains("totalFeedCount") ? tag.getInt("totalFeedCount") : 0;
        this.timeStopCharges = tag.contains("timeStopCharges") ? tag.getInt("timeStopCharges") : 0;
        this.timeStopCooldown = tag.contains("timeStopCooldown") ? tag.getInt("timeStopCooldown") : 0;
        this.isFinalCarnivalUnlocked = tag.contains("isFinalCarnivalUnlocked") && tag.getBoolean("isFinalCarnivalUnlocked");
        this.isFinalCarnivalActive = tag.contains("isFinalCarnivalActive") && tag.getBoolean("isFinalCarnivalActive");
        this.tempLifeRemaining = tag.contains("tempLifeRemaining") ? tag.getInt("tempLifeRemaining") : 0;
        this.isFeeding = tag.contains("isFeeding") && tag.getBoolean("isFeeding");
        this.feedingRemaining = tag.contains("feedingRemaining") ? tag.getInt("feedingRemaining") : 0;
        

        

    }
}
