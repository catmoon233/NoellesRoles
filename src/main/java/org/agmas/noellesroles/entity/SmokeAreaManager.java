package org.agmas.noellesroles.entity;

import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 烟雾区域管理器
 * 用于管理烟雾弹产生的持续烟雾效果区域
 */
public class SmokeAreaManager {
    
    // 所有活跃的烟雾区域
    private static final List<SmokeArea> activeAreas = new ArrayList<>();
    
    // 失明效果持续时间
    private static final int BLINDNESS_DURATION = 40; // 2秒
    
    /**
     * 创建一个新的烟雾区域
     */
    public static void createSmokeArea(ServerWorld world, Vec3d position, double radius, int durationTicks) {
        activeAreas.add(new SmokeArea(world, position, radius, durationTicks));
    }
    
    /**
     * 每tick更新所有烟雾区域
     * 应该在游戏循环中调用
     */
    public static void tick() {
        Iterator<SmokeArea> iterator = activeAreas.iterator();
        while (iterator.hasNext()) {
            SmokeArea area = iterator.next();
            if (area.tick()) {
                // 区域已过期，移除
                iterator.remove();
            }
        }
    }
    
    /**
     * 清除所有烟雾区域（游戏结束时调用）
     */
    public static void clearAll() {
        activeAreas.clear();
    }
    
    /**
     * 烟雾区域数据类
     */
    private static class SmokeArea {
        private final ServerWorld world;
        private final Vec3d center;
        private final double radius;
        private int remainingTicks;
        private int tickCounter = 0;
        
        public SmokeArea(ServerWorld world, Vec3d center, double radius, int durationTicks) {
            this.world = world;
            this.center = center;
            this.radius = radius;
            this.remainingTicks = durationTicks;
        }
        
        /**
         * 每tick更新
         * @return true 如果区域已过期
         */
        public boolean tick() {
            remainingTicks--;
            tickCounter++;
            
            if (remainingTicks <= 0) {
                return true;
            }
            
            // 每3tick生成粒子效果（更频繁）
            if (tickCounter % 3 == 0) {
                spawnSmokeParticles();
            }
            
            // 每10tick检查玩家并应用失明
            if (tickCounter % 10 == 0) {
                applyBlindnessToPlayers();
            }
            
            return false;
        }
        
        /**
         * 生成烟雾粒子
         */
        private void spawnSmokeParticles() {
            // 大幅增加粒子数量以获得超浓密的烟雾效果（从25增加到250，10倍）
            for (int i = 0; i < 250; i++) {
                double offsetX = (world.random.nextDouble() - 0.5) * radius * 2;
                double offsetY = world.random.nextDouble() * 3.5;  // 增加高度范围
                double offsetZ = (world.random.nextDouble() - 0.5) * radius * 2;
                
                // 主要烟雾粒子（增加数量）
                world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                        2, 0.1, 0.1, 0.1, 0.03);
                        
                // 大量添加大型烟雾粒子
                if (i % 3 == 0) {
                    world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                            center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                            2, 0.15, 0.15, 0.15, 0.04);
                }
                
                // 添加普通烟雾粒子
                if (i % 5 == 0) {
                    world.spawnParticles(ParticleTypes.SMOKE,
                            center.x + offsetX, center.y + offsetY, center.z + offsetZ,
                            1, 0.12, 0.12, 0.12, 0.035);
                }
            }
        }
        
        /**
         * 对范围内玩家应用失明效果
         */
        private void applyBlindnessToPlayers() {
            Box area = new Box(
                    center.x - radius, center.y - 1, center.z - radius,
                    center.x + radius, center.y + 4, center.z + radius
            );
            
            List<ServerPlayerEntity> players = world.getEntitiesByClass(
                    ServerPlayerEntity.class, area,
                    GameFunctions::isPlayerAliveAndSurvival
            );
            
            for (ServerPlayerEntity player : players) {
                // 检查玩家是否真的在球形范围内
                double distance = player.getPos().distanceTo(center);
                if (distance <= radius) {
                    player.addStatusEffect(new StatusEffectInstance(
                            StatusEffects.BLINDNESS, BLINDNESS_DURATION, 0, false, false
                    ));
                }
            }
        }
    }
}