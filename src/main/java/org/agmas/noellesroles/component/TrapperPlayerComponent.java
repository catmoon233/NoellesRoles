package org.agmas.noellesroles.component;


import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.agmas.noellesroles.ModEntities;
import org.agmas.noellesroles.entity.CalamityMarkEntity;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 设陷者组件
 *
 * 管理设陷者的陷阱机制：
 * - 技能对准地面设置隐形灾厄印记陷阱
 * - 其他玩家踩中触发，发出巨响暴露位置并发光
 * - 施加"标记"，被标记者被囚禁
 * - 囚禁时间递增：3秒 -> 10秒 -> 25秒
 */
public class TrapperPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    
    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<TrapperPlayerComponent> KEY = ModComponents.TRAPPER;
    
    // ==================== 常量定义 ====================
    
    /** 设置陷阱的冷却时间（30秒 = 600 tick） */
    public static final int TRAP_COOLDOWN = 600;
    
    /** 最大陷阱放置距离（格） */
    public static final double MAX_PLACE_DISTANCE = 8.0;
    
    /** 第一次触发囚禁时间（3秒 = 60 tick） */
    public static final int PRISON_TIME_1 = 60;
    
    /** 第二次触发囚禁时间（10秒 = 200 tick） */
    public static final int PRISON_TIME_2 = 200;
    
    /** 第三次及以上触发囚禁时间（25秒 = 500 tick） */
    public static final int PRISON_TIME_3 = 500;
    
    /** 发光效果持续时间（5秒 = 100 tick） */
    public static final int GLOWING_DURATION = 100;
    
    // ==================== 状态变量 ====================
    
    private final PlayerEntity player;
    
    /** 技能冷却计时器（tick） */
    public int cooldown = 0;
    
    /** 是否已标记为设陷者 */
    public boolean isTrapperMarked = false;
    
    /** 记录每个玩家被触发陷阱的次数（用于增加囚禁时间） */
    private Map<UUID, Integer> triggerCounts = new HashMap<>();
    
    /** 当前被囚禁玩家的剩余时间 */
    private Map<UUID, Integer> prisonTimers = new HashMap<>();
    
    /** 被囚禁玩家的位置（用于锁定） */
    private Map<UUID, Vec3d> prisonPositions = new HashMap<>();
    
    /**
     * 构造函数
     */
    public TrapperPlayerComponent(PlayerEntity player) {
        this.player = player;
    }
    
    /**
     * 重置组件状态
     * 在游戏开始时或角色分配时调用
     */
    public void reset() {
        this.cooldown = 0;
        this.isTrapperMarked = true;
        this.triggerCounts.clear();
        this.prisonTimers.clear();
        this.prisonPositions.clear();
        this.sync();
    }
    
    /**
     * 完全清除组件状态（游戏结束时调用）
     */
    public void clearAll() {
        this.cooldown = 0;
        this.isTrapperMarked = false;
        this.triggerCounts.clear();
        this.prisonTimers.clear();
        this.prisonPositions.clear();
        this.sync();
    }
    
    /**
     * 检查是否可以放置陷阱
     */
    public boolean canPlaceTrap() {
        return cooldown <= 0;
    }
    
    /**
     * 获取冷却秒数
     */
    public float getCooldownSeconds() {
        return cooldown / 20.0f;
    }
    
    /**
     * 尝试放置陷阱
     * @return 是否成功放置
     */
    public boolean tryPlaceTrap() {
        if (!canPlaceTrap()) return false;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return false;
        
        World world = player.getWorld();
        
        // 射线检测找到地面
        Vec3d eyePos = player.getEyePos();
        Vec3d lookDir = player.getRotationVec(1.0f);
        Vec3d endPos = eyePos.add(lookDir.multiply(MAX_PLACE_DISTANCE));
        
        RaycastContext context = new RaycastContext(
            eyePos, endPos,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            player
        );
        BlockHitResult hit = world.raycast(context);
        
        if (hit.getType() == HitResult.Type.MISS) {
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.trapper.no_ground")
                    .formatted(Formatting.RED),
                true
            );
            return false;
        }
        
        // 获取放置位置（方块上方）
        BlockPos hitPos = hit.getBlockPos();
        Vec3d spawnPos = new Vec3d(hitPos.getX() + 0.5, hitPos.getY() + 1.0, hitPos.getZ() + 0.5);
        
        // 创建灾厄印记实体
        CalamityMarkEntity mark = new CalamityMarkEntity(ModEntities.CALAMITY_MARK, world);
        mark.setPosition(spawnPos);
        mark.setOwner(player);
        world.spawnEntity(mark);
        
        // 设置冷却
        this.cooldown = TRAP_COOLDOWN;
        
        // 播放放置音效（只有设陷者能听到）
        serverPlayer.playSound(SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, 0.5f, 1.5f);
        
        serverPlayer.sendMessage(
            Text.translatable("message.noellesroles.trapper.trap_placed")
                .formatted(Formatting.GREEN),
            true
        );
        
        this.sync();
        return true;
    }
    
    /**
     * 当陷阱被触发时调用
     * @param victim 触发陷阱的玩家
     * @param trapPos 陷阱位置
     */
    public void onTrapTriggered(PlayerEntity victim, Vec3d trapPos) {
        if (victim == null || victim.getWorld().isClient()) return;
        
        // 获取该玩家的触发次数
        UUID victimUuid = victim.getUuid();
        int count = triggerCounts.getOrDefault(victimUuid, 0) + 1;
        triggerCounts.put(victimUuid, count);
        
        // 计算囚禁时间
        int prisonTime;
        if (count == 1) {
            prisonTime = PRISON_TIME_1;
        } else if (count == 2) {
            prisonTime = PRISON_TIME_2;
        } else {
            prisonTime = PRISON_TIME_3;
        }
        
        // 设置囚禁状态
        prisonTimers.put(victimUuid, prisonTime);
        prisonPositions.put(victimUuid, victim.getPos());
        
        // 播放巨响音效（所有人都能听到）
        World world = victim.getWorld();
        world.playSound(null, trapPos.x, trapPos.y, trapPos.z,
            SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.HOSTILE, 2.0f, 0.5f);
        world.playSound(null, trapPos.x, trapPos.y, trapPos.z,
            SoundEvents.BLOCK_BELL_USE, SoundCategory.HOSTILE, 3.0f, 0.3f);
        
        // 给受害者发光效果
        victim.addStatusEffect(new StatusEffectInstance(
            StatusEffects.GLOWING, GLOWING_DURATION, 0, false, false, true
        ));
        
        // 给受害者缓慢和挖掘疲劳（防止移动和破坏方块）
        victim.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SLOWNESS, prisonTime, 255, false, false, false
        ));
        victim.addStatusEffect(new StatusEffectInstance(
            StatusEffects.MINING_FATIGUE, prisonTime, 255, false, false, false
        ));
        victim.addStatusEffect(new StatusEffectInstance(
            StatusEffects.JUMP_BOOST, prisonTime, 128, false, false, false
        )); // 负面跳跃（防止跳跃）
        
        // 发送消息给受害者
        if (victim instanceof ServerPlayerEntity serverVictim) {
            String timeStr = String.format("%.1f", prisonTime / 20.0f);
            serverVictim.sendMessage(
                Text.translatable("message.noellesroles.trapper.trap_triggered", timeStr)
                    .formatted(Formatting.RED, Formatting.BOLD),
                false
            );
            
            if (count > 1) {
                serverVictim.sendMessage(
                    Text.translatable("message.noellesroles.trapper.mark_count", count)
                        .formatted(Formatting.DARK_RED),
                    false
                );
            }
        }
        
        // 发送消息给设陷者
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.trapper.trap_triggered_notify", victim.getName())
                    .formatted(Formatting.GREEN),
                true
            );
        }
        
        this.sync();
    }
    
    /**
     * 检查是否是活跃的设陷者
     */
    public boolean isActiveTrapper() {
        return isTrapperMarked;
    }
    
    /**
     * 同步到客户端
     */
    public void sync() {
        ModComponents.TRAPPER.sync(this.player);
    }
    
    // ==================== Tick 处理 ====================
    
    @Override
    public void serverTick() {
        // 只在设陷者角色时处理
        if (!isActiveTrapper()) return;
        
        // 处理冷却倒计时
        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) {
                sync();
            }
        }
        
        // 处理所有被囚禁玩家
        World world = player.getWorld();
        for (Map.Entry<UUID, Integer> entry : new HashMap<>(prisonTimers).entrySet()) {
            UUID victimUuid = entry.getKey();
            int remaining = entry.getValue();
            
            if (remaining > 0) {
                // 找到受害者
                PlayerEntity victim = world.getPlayerByUuid(victimUuid);
                if (victim != null && GameFunctions.isPlayerAliveAndSurvival(victim)) {
                    // 锁定位置
                    Vec3d prisonPos = prisonPositions.get(victimUuid);
                    if (prisonPos != null) {
                        // 如果玩家移动了，拉回原位
                        double dist = victim.getPos().squaredDistanceTo(prisonPos);
                        if (dist > 0.1) {
                            if (victim instanceof ServerPlayerEntity serverVictim) {
                                serverVictim.teleport(
                                    serverVictim.getServerWorld(),
                                    prisonPos.x, prisonPos.y, prisonPos.z,
                                    victim.getYaw(), victim.getPitch()
                                );
                            }
                        }
                    }
                }
                
                // 减少剩余时间
                prisonTimers.put(victimUuid, remaining - 1);
            } else {
                // 囚禁结束
                prisonTimers.remove(victimUuid);
                prisonPositions.remove(victimUuid);
                
                PlayerEntity victim = world.getPlayerByUuid(victimUuid);
                if (victim instanceof ServerPlayerEntity serverVictim) {
                    serverVictim.sendMessage(
                        Text.translatable("message.noellesroles.trapper.prison_ended")
                            .formatted(Formatting.GREEN),
                        true
                    );
                }
            }
        }
    }
    
    // ==================== NBT 序列化 ====================
    
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("cooldown", this.cooldown);
        tag.putBoolean("isTrapperMarked", this.isTrapperMarked);
        
        // 保存触发次数
        NbtCompound countsTag = new NbtCompound();
        for (Map.Entry<UUID, Integer> entry : triggerCounts.entrySet()) {
            countsTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("triggerCounts", countsTag);
        
        // 保存囚禁计时器
        NbtCompound timersTag = new NbtCompound();
        for (Map.Entry<UUID, Integer> entry : prisonTimers.entrySet()) {
            timersTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("prisonTimers", timersTag);
        
        // 保存囚禁位置
        NbtCompound positionsTag = new NbtCompound();
        for (Map.Entry<UUID, Vec3d> entry : prisonPositions.entrySet()) {
            NbtCompound posTag = new NbtCompound();
            posTag.putDouble("x", entry.getValue().x);
            posTag.putDouble("y", entry.getValue().y);
            posTag.putDouble("z", entry.getValue().z);
            positionsTag.put(entry.getKey().toString(), posTag);
        }
        tag.put("prisonPositions", positionsTag);
    }
    
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
        this.isTrapperMarked = tag.contains("isTrapperMarked") && tag.getBoolean("isTrapperMarked");
        
        // 读取触发次数
        this.triggerCounts.clear();
        if (tag.contains("triggerCounts")) {
            NbtCompound countsTag = tag.getCompound("triggerCounts");
            for (String key : countsTag.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    this.triggerCounts.put(uuid, countsTag.getInt(key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        // 读取囚禁计时器
        this.prisonTimers.clear();
        if (tag.contains("prisonTimers")) {
            NbtCompound timersTag = tag.getCompound("prisonTimers");
            for (String key : timersTag.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    this.prisonTimers.put(uuid, timersTag.getInt(key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        // 读取囚禁位置
        this.prisonPositions.clear();
        if (tag.contains("prisonPositions")) {
            NbtCompound positionsTag = tag.getCompound("prisonPositions");
            for (String key : positionsTag.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    NbtCompound posTag = positionsTag.getCompound(key);
                    Vec3d pos = new Vec3d(
                        posTag.getDouble("x"),
                        posTag.getDouble("y"),
                        posTag.getDouble("z")
                    );
                    this.prisonPositions.put(uuid, pos);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
}