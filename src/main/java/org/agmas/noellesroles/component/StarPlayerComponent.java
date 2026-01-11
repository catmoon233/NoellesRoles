package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

/**
 * 明星组件
 *
 * 被动技能：每20秒自动发光2秒
 * 主动技能：使用技能让10格范围内的玩家视野都看向自己（30秒冷却）
 * 
 * 明星为好人阵营（乘客阵营）
 */
public class StarPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    
    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<StarPlayerComponent> KEY = ModComponents.STAR;
    
    // ==================== 常量定义 ====================
    
    /** 发光周期（20秒 = 400 tick） */
    public static final int GLOW_CYCLE = 400;
    
    /** 发光持续时间（2秒 = 40 tick） */
    public static final int GLOW_DURATION = 40;
    
    /** 主动技能冷却时间（30秒 = 600 tick） */
    public static final int ABILITY_COOLDOWN = 600;
    
    /** 技能范围（10格） */
    public static final double ABILITY_RANGE = 10.0;
    
    // ==================== 状态变量 ====================
    
    private final Player player;
    
    /** 发光周期计时器（tick） */
    public int glowTimer = 0;
    
    /** 是否正在发光 */
    public boolean isGlowing = false;
    
    /** 发光剩余时间（tick） */
    public int glowTicksRemaining = 0;
    
    /** 主动技能冷却时间（tick） */
    public int abilityCooldown = 0;
    
    /** 是否已激活（角色分配后） */
    public boolean isActive = false;
    
    /**
     * 构造函数
     */
    public StarPlayerComponent(Player player) {
        this.player = player;
    }
    
    /**
     * 重置组件状态
     * 在游戏开始时或角色分配时调用
     */
    public void reset() {
        this.glowTimer = 0;
        this.isGlowing = false;
        this.glowTicksRemaining = 0;
        this.abilityCooldown = 0;
        this.isActive = true;
        this.sync();
    }
    
    /**
     * 清除所有状态
     */
    public void clearAll() {
        this.glowTimer = 0;
        this.isGlowing = false;
        this.glowTicksRemaining = 0;
        this.abilityCooldown = 0;
        this.isActive = false;
        // 移除发光效果
        if (player != null) {
            player.removeEffect(MobEffects.GLOWING);
        }
        this.sync();
    }
    
    /**
     * 检查是否为激活的明星角色
     */
    public boolean isActiveStar() {
        if (!isActive || player == null || player.level().isClientSide()) return false;
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        return gameWorld.isRole(player, ModRoles.STAR);
    }
    
    /**
     * 检查主动技能是否可用
     * 注意：此方法在客户端和服务端都可以调用
     */
    public boolean canUseAbility() {
        // 客户端只检查冷却和激活状态，服务端安全检查在网络包处理器中进行
        return abilityCooldown <= 0 && isActive;
    }
    
    /**
     * 使用主动技能 - 让10格范围内的玩家视野都看向自己
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
        int affectedCount = 0;
        
        // 遍历范围内的所有玩家
        for (Player target : world.players()) {
            if (target.equals(player)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(target)) continue;
            
            double distance = target.distanceToSqr(player);
            if (distance > ABILITY_RANGE * ABILITY_RANGE) continue;
            
            // 让目标玩家看向明星
            if (target instanceof ServerPlayer serverTarget) {
                // 计算目标应该看向的方向
                double dx = player.getX() - target.getX();
                double dy = (player.getY() + player.getEyeHeight(player.getPose())) - (target.getY() + target.getEyeHeight(target.getPose()));
                double dz = player.getZ() - target.getZ();
                
                double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
                float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));
                
                // 设置玩家视角
                serverTarget.connection.teleport(
                    target.getX(), target.getY(), target.getZ(),
                    yaw, pitch
                );
                
                // 给目标玩家发送提示
                serverTarget.displayClientMessage(
                    Component.translatable("message.noellesroles.star.attracted")
                        .withStyle(ChatFormatting.GOLD),
                    true
                );
                
                affectedCount++;
            }
        }
        
        // 设置冷却
        this.abilityCooldown = ABILITY_COOLDOWN;
        
        // 播放音效
        world.playSound(null, player.blockPosition(),
            SoundEvents.PLAYER_LEVELUP,
            SoundSource.PLAYERS, 1.0F, 1.2F);
        
        // 给明星发光2秒以突出效果
        startGlowing();
        
        // 发送消息给明星玩家
        serverPlayer.displayClientMessage(
            Component.translatable("message.noellesroles.star.ability_used", affectedCount)
                .withStyle(ChatFormatting.GOLD),
            true
        );
        
        this.sync();
        return true;
    }
    
    /**
     * 开始发光
     */
    private void startGlowing() {
        if (player == null) return;
        
        this.isGlowing = true;
        this.glowTicksRemaining = GLOW_DURATION;
        
        // 添加发光效果
        player.addEffect(new MobEffectInstance(
            MobEffects.GLOWING,
            GLOW_DURATION + 5, // 稍微多一点以确保效果
            0,
            false,  // ambient
            false,  // showParticles
            true    // showIcon
        ));
        
        this.sync();
    }
    
    /**
     * 停止发光
     */
    private void stopGlowing() {
        this.isGlowing = false;
        this.glowTicksRemaining = 0;
        
        if (player != null) {
            player.removeEffect(MobEffects.GLOWING);
        }
        
        this.sync();
    }
    
    /**
     * 获取冷却时间（秒）
     */
    public float getCooldownSeconds() {
        return abilityCooldown / 20.0f;
    }
    
    /**
     * 获取距离下次发光的时间（秒）
     */
    public float getNextGlowSeconds() {
        if (isGlowing) return 0;
        int remaining = GLOW_CYCLE - glowTimer;
        return remaining / 20.0f;
    }
    
    /**
     * 同步到客户端
     */
    public void sync() {
        if (player != null && !player.level().isClientSide()) {
            ModComponents.STAR.sync(this.player);
        }
    }
    
    // ==================== Tick 处理 ====================
    
    @Override
    public void serverTick() {
        if (!isActiveStar()) return;
        
        // 减少主动技能冷却时间
        if (this.abilityCooldown > 0) {
            this.abilityCooldown--;
            // 每秒同步一次，减少网络压力
            if (this.abilityCooldown % 20 == 0 || this.abilityCooldown == 0) {
                this.sync();
            }
        }
        
        // 处理发光状态
        if (this.isGlowing) {
            this.glowTicksRemaining--;
            if (this.glowTicksRemaining <= 0) {
                stopGlowing();
                this.glowTimer = 0; // 重置周期计时器
            }
        } else {
            // 增加周期计时器
            this.glowTimer++;
            
            // 达到周期时开始发光
            if (this.glowTimer >= GLOW_CYCLE) {
                startGlowing();
            }
            
            // 每秒同步一次发光倒计时
            if (this.glowTimer % 20 == 0) {
                this.sync();
            }
        }
    }
    
    // ==================== NBT 序列化 ====================
    
    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("glowTimer", this.glowTimer);
        tag.putBoolean("isGlowing", this.isGlowing);
        tag.putInt("glowTicksRemaining", this.glowTicksRemaining);
        tag.putInt("abilityCooldown", this.abilityCooldown);
        tag.putBoolean("isActive", this.isActive);
    }
    
    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.glowTimer = tag.contains("glowTimer") ? tag.getInt("glowTimer") : 0;
        this.isGlowing = tag.contains("isGlowing") && tag.getBoolean("isGlowing");
        this.glowTicksRemaining = tag.contains("glowTicksRemaining") ? tag.getInt("glowTicksRemaining") : 0;
        this.abilityCooldown = tag.contains("abilityCooldown") ? tag.getInt("abilityCooldown") : 0;
        this.isActive = tag.contains("isActive") && tag.getBoolean("isActive");
    }
}