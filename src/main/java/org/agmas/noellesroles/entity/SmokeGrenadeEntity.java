package org.agmas.noellesroles.entity;


import dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.agmas.noellesroles.ModItems;

import java.util.List;

/**
 * 烟雾弹实体
 * - 碰撞时爆炸形成烟雾区域
 * - 烟雾持续10秒
 * - 进入烟雾的玩家获得失明效果
 * - 直接命中玩家时清空目标的san值
 */
public class SmokeGrenadeEntity extends ThrownItemEntity {
    
    // 烟雾持续时间：10秒 = 200 ticks
    private static final int SMOKE_DURATION_TICKS = 200;
    // 烟雾半径
    private static final double SMOKE_RADIUS = 4.0;
    // 失明效果持续时间（比烟雾略长，确保连续性）
    private static final int BLINDNESS_DURATION = 40; // 2秒
    
    private boolean directHit = false;
    private PlayerEntity directHitTarget = null;
    
    public SmokeGrenadeEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    protected Item getDefaultItem() {
        return ModItems.SMOKE_GRENADE;
    }
    
    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        
        // 检查是否直接命中玩家
        if (entityHitResult.getEntity() instanceof PlayerEntity player) {
            if (GameFunctions.isPlayerAliveAndSurvival(player)) {
                directHit = true;
                directHitTarget = player;
            }
        }
    }
    
    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        
        if (this.getWorld() instanceof ServerWorld world) {
            // 播放烟雾爆炸音效
            world.playSound(null, this.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 
                    SoundCategory.PLAYERS, 1.5f, 0.5f);
            
            // 如果直接命中玩家，清空目标san值
            if (directHit && directHitTarget != null && directHitTarget instanceof ServerPlayerEntity serverTarget) {
                PlayerMoodComponent moodComponent = PlayerMoodComponent.KEY.get(serverTarget);
                // 设置san值为0（疯狂状态）
                moodComponent.setMood(0);
                moodComponent.sync();
                
                // 给予目标额外的失明效果
                serverTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, false, false));
            }
            
            // 生成烟雾区域实体（使用粒子模拟）
            // 创建一个区域效果云或使用定时任务
            SmokeAreaManager.createSmokeArea(world, this.getPos(), SMOKE_RADIUS, SMOKE_DURATION_TICKS);
            
            // 初始烟雾粒子爆发 - 大幅增强效果（10倍粒子）
            for (int i = 0; i < 1500; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * SMOKE_RADIUS * 2;
                double offsetY = this.random.nextDouble() * 3;  // 增加高度范围
                double offsetZ = (this.random.nextDouble() - 0.5) * SMOKE_RADIUS * 2;
                
                // 主要烟雾粒子（增加数量）
                world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                        3, 0.1, 0.1, 0.1, 0.03);
                        
                // 额外添加大量大型烟雾粒子
                if (i % 3 == 0) {
                    world.spawnParticles(ParticleTypes.LARGE_SMOKE,
                            this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                            2, 0.2, 0.2, 0.2, 0.05);
                }
                
                // 添加更多的烟雾效果粒子
                if (i % 5 == 0) {
                    world.spawnParticles(ParticleTypes.SMOKE,
                            this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ,
                            1, 0.15, 0.15, 0.15, 0.04);
                }
            }
            
            // 立即对范围内玩家应用失明
            applyBlindnessToPlayersInRadius(world);
            
            this.discard();
        }
    }
    
    /**
     * 对范围内的玩家应用失明效果
     */
    private void applyBlindnessToPlayersInRadius(ServerWorld world) {
        Box area = new Box(
                this.getX() - SMOKE_RADIUS, this.getY() - 1, this.getZ() - SMOKE_RADIUS,
                this.getX() + SMOKE_RADIUS, this.getY() + 3, this.getZ() + SMOKE_RADIUS
        );
        
        List<ServerPlayerEntity> players = world.getEntitiesByClass(
                ServerPlayerEntity.class, area,
                player -> GameFunctions.isPlayerAliveAndSurvival(player) && player != this.getOwner()
        );
        
        for (ServerPlayerEntity player : players) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, BLINDNESS_DURATION, 0, false, false));
        }
    }
}