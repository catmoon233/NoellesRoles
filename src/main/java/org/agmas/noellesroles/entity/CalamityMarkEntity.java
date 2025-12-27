package org.agmas.noellesroles.entity;


import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.TrapperPlayerComponent;
import  org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 灾厄印记实体
 * 
 * 隐形陷阱，当非设陷者玩家踩中时触发：
 * - 发出巨响暴露位置
 * - 给予发光效果
 * - 囚禁玩家（时间随触发次数递增）
 */
public class CalamityMarkEntity extends Entity {
    
    /** 所有者 UUID */
    private static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(
        CalamityMarkEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID
    );
    
    /** 陷阱触发半径（格） */
    public static final double TRIGGER_RADIUS = 1.0;
    
    /** 陷阱存在时间上限（5分钟 = 6000 tick），防止无限存在 */
    public static final int MAX_LIFETIME = 6000;
    
    /** 存活时间计数器 */
    private int lifetime = 0;
    
    /** 所有者玩家引用（缓存） */
    private PlayerEntity ownerCache = null;
    
    public CalamityMarkEntity(EntityType<?> type, World world) {
        super(type, world);
        this.setInvisible(true); // 对所有人隐形
        this.setNoGravity(true); // 无重力
    }
    
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(OWNER_UUID, Optional.empty());
    }
    
    /**
     * 设置所有者
     */
    public void setOwner(PlayerEntity owner) {
        if (owner != null) {
            this.dataTracker.set(OWNER_UUID, Optional.of(owner.getUuid()));
            this.ownerCache = owner;
        }
    }
    
    /**
     * 获取所有者 UUID
     */
    public Optional<UUID> getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID);
    }
    
    /**
     * 获取所有者玩家
     */
    public PlayerEntity getOwner() {
        if (ownerCache != null && ownerCache.isAlive()) {
            return ownerCache;
        }
        
        Optional<UUID> ownerUuid = getOwnerUuid();
        if (ownerUuid.isPresent()) {
            ownerCache = getWorld().getPlayerByUuid(ownerUuid.get());
            return ownerCache;
        }
        return null;
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (getWorld().isClient()) return;
        
        // 增加存活时间
        lifetime++;
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }
        
        // 检查所有者是否还是设陷者
        PlayerEntity owner = getOwner();
        if (owner == null) {
            this.discard();
            return;
        }
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(getWorld());
        if (!gameWorld.isRole(owner, ModRoles.TRAPPER)) {
            this.discard();
            return;
        }
        
        // 检测触发
        checkTrigger();
    }
    
    /**
     * 检测是否有玩家触发陷阱
     */
    private void checkTrigger() {
        World world = getWorld();
        Vec3d pos = this.getPos();
        
        // 创建检测区域
        Box detectionBox = new Box(
            pos.x - TRIGGER_RADIUS, pos.y - 0.5, pos.z - TRIGGER_RADIUS,
            pos.x + TRIGGER_RADIUS, pos.y + 2.0, pos.z + TRIGGER_RADIUS
        );
        
        // 获取区域内的所有玩家
        List<PlayerEntity> players = world.getEntitiesByClass(
            PlayerEntity.class, detectionBox,
            player -> {
                // 排除所有者
                Optional<UUID> ownerUuid = getOwnerUuid();
                if (ownerUuid.isPresent() && player.getUuid().equals(ownerUuid.get())) {
                    return false;
                }
                
                // 排除死亡或观察者模式的玩家
                if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                    return false;
                }
                
                // 排除其他杀手阵营玩家（同阵营不触发）
                GameWorldComponent gameWorld = GameWorldComponent.KEY.get(world);
                if (gameWorld.canUseKillerFeatures(player)) {
                    return false;
                }
                
                return true;
            }
        );
        
        // 如果有玩家触发
        if (!players.isEmpty()) {
            PlayerEntity victim = players.get(0);
            triggerTrap(victim);
        }
    }
    
    /**
     * 触发陷阱
     */
    private void triggerTrap(PlayerEntity victim) {
        PlayerEntity owner = getOwner();
        if (owner == null) {
            this.discard();
            return;
        }
        
        // 获取设陷者组件并触发效果
        TrapperPlayerComponent trapperComp = ModComponents.TRAPPER.get(owner);
        trapperComp.onTrapTriggered(victim, this.getPos());
        
        // 陷阱触发后消失（一次性）
        this.discard();
    }
    
    /**
     * 该实体是否对指定玩家可见
     * 只有设陷者本人可以看到自己的陷阱（半透明效果在客户端渲染）
     */
    public boolean isVisibleTo(PlayerEntity player) {
        Optional<UUID> ownerUuid = getOwnerUuid();
        return ownerUuid.isPresent() && player.getUuid().equals(ownerUuid.get());
    }
    
    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        // 对所有非所有者玩家隐形
        return !isVisibleTo(player);
    }
    
    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.contains("OwnerUUID")) {
            try {
                UUID uuid = UUID.fromString(nbt.getString("OwnerUUID"));
                this.dataTracker.set(OWNER_UUID, Optional.of(uuid));
            } catch (IllegalArgumentException ignored) {}
        }
        this.lifetime = nbt.contains("Lifetime") ? nbt.getInt("Lifetime") : 0;
    }
    
    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        Optional<UUID> ownerUuid = getOwnerUuid();
        ownerUuid.ifPresent(uuid -> nbt.putString("OwnerUUID", uuid.toString()));
        nbt.putInt("Lifetime", this.lifetime);
    }
    
    @Override
    public boolean canHit() {
        return false; // 不能被点击
    }
    
    @Override
    public boolean isPushable() {
        return false; // 不能被推动
    }
    
    @Override
    public boolean isCollidable() {
        return false; // 无碰撞
    }
}