package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 慕恋者组件
 *
 * 管理慕恋者的机制：
 * - 群体窥视积累能量，满300能量后变为随机杀手角色
 */
public class AdmirerPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    
    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<AdmirerPlayerComponent> KEY = ModComponents.ADMIRER;
    
    // ==================== 常量定义 ====================
    
    /** 进阶所需能量 */
    public static final int MAX_ENERGY = 300;
    
    /** 窥视视野角度（度数） */
    public static final double GAZE_ANGLE = 45.0;
    
    /** 窥视最大距离（格） */
    public static final double GAZE_DISTANCE = 32.0;
    
    // ==================== 状态变量 ====================
    
    private final PlayerEntity player;
    
    /** 当前能量值 */
    public int energy = 0;
    
    /** 是否正在窥视 */
    public boolean isGazing = false;
    
    /** 当前窥视目标数量 */
    public int gazingTargetCount = 0;
    
    /** 是否已标记为慕恋者（用于在角色转换后仍能识别） */
    public boolean isAdmirerMarked = false;
    
    /** 是否已转化 */
    public boolean hasTransformed = false;
    
    /** 能量获取计时器（每秒获取一次） */
    private int energyTickCounter = 0;
    
    /**
     * 构造函数
     */
    public AdmirerPlayerComponent(PlayerEntity player) {
        this.player = player;
    }
    
    /**
     * 重置组件状态
     * 在游戏开始时或角色分配时调用
     */
    public void reset() {
        this.energy = 0;
        this.isGazing = false;
        this.gazingTargetCount = 0;
        this.isAdmirerMarked = true;
        this.hasTransformed = false;
        this.energyTickCounter = 0;
        this.sync();
    }
    
    /**
     * 完全清除组件状态（游戏结束时调用）
     */
    public void clearAll() {
        this.energy = 0;
        this.isGazing = false;
        this.gazingTargetCount = 0;
        this.isAdmirerMarked = false;
        this.hasTransformed = false;
        this.energyTickCounter = 0;
        this.sync();
    }
    
    /**
     * 添加能量
     */
    public void addEnergy(int amount) {
        this.energy += amount;
        if (this.energy >= MAX_ENERGY && !hasTransformed) {
            transform();
        }
        this.sync();
    }
    
    /**
     * 转化为随机杀手角色
     */
    private void transform() {
        if (hasTransformed) return;
        hasTransformed = true;
        
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        
        // 获取可用的杀手角色列表
        List<Role> killerRoles = getAvailableKillerRoles();
        
        if (killerRoles.isEmpty()) {
            // 如果没有可用的杀手角色，使用原版杀手
            killerRoles.add(TMMRoles.KILLER);
        }
        
        // 随机选择一个杀手角色
        Random random = new Random();
        Role selectedRole = killerRoles.get(random.nextInt(killerRoles.size()));
        
        // 清除慕恋者标记
        this.isAdmirerMarked = false;
        
        // 转换角色
        gameWorld.addRole(player, selectedRole);
        
        // 触发角色分配事件 - 这会调用 assignModdedRole 来初始化角色
        // 包括给予初始金币、初始物品、重置组件等
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, selectedRole);
        
        // 原版杀手需要额外给刀（因为 onRoleAssigned 中没有处理原版杀手）
        if (selectedRole.equals(TMMRoles.KILLER)) {
            player.giveItemStack(TMMItems.KNIFE.getDefaultStack());
            // 给予杀手初始金币
            dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent shopComponent =
                dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent.KEY.get(player);
            shopComponent.addToBalance(200);
        }
        
        // 发送转化消息
        serverPlayer.sendMessage(
            Text.translatable("message.noellesroles.admirer.transform",
                Text.translatable("announcement.role." + selectedRole.identifier().getPath()))
                .formatted(Formatting.RED, Formatting.BOLD),
            false
        );
        
        // 播放音效
        player.getWorld().playSound(null, player.getBlockPos(),
            SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 1.0F, 1.5F);
        
        this.sync();
    }
    
    /**
     * 获取可用的杀手角色列表
     * 动态获取所有注册的杀手阵营角色（canUseKiller = true）
     */
    private List<Role> getAvailableKillerRoles() {
        List<Role> killerRoles = new ArrayList<>();
        
        // 遍历所有注册的角色，筛选出杀手阵营角色
        for (Role role : TMMRoles.ROLES) {
            // 杀手阵营：canUseKiller = true
            if (role.canUseKiller()) {
                killerRoles.add(role);
            }
        }
        
        return killerRoles;
    }
    
    /**
     * 开始窥视
     */
    public void startGazing() {
        this.isGazing = true;
        this.sync();
    }
    
    /**
     * 停止窥视
     */
    public void stopGazing() {
        this.isGazing = false;
        this.gazingTargetCount = 0;
        this.sync();
    }
    
    /**
     * 获取可见的玩家列表（用于窥视技能）
     */
    public List<PlayerEntity> getVisiblePlayers() {
        List<PlayerEntity> visible = new ArrayList<>();
        World world = player.getWorld();
        Vec3d eyePos = player.getEyePos();
        Vec3d lookDir = player.getRotationVec(1.0f);
        
        for (PlayerEntity target : world.getPlayers()) {
            if (target.equals(player)) continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(target)) continue;
            
            Vec3d targetPos = target.getEyePos();
            double distance = eyePos.distanceTo(targetPos);
            if (distance > GAZE_DISTANCE) continue;
            
            // 视野角度检查（90度扇形，半角45度）
            Vec3d toTarget = targetPos.subtract(eyePos).normalize();
            double dot = lookDir.dotProduct(toTarget);
            if (dot < Math.cos(Math.toRadians(GAZE_ANGLE))) continue;
            
            // 射线检测
            RaycastContext context = new RaycastContext(
                eyePos, targetPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                player
            );
            BlockHitResult hit = world.raycast(context);
            if (hit.getType() == HitResult.Type.MISS ||
                hit.getPos().distanceTo(targetPos) < 1.0) {
                visible.add(target);
            }
        }
        return visible;
    }
    
    /**
     * 更新窥视状态
     */
    private void updateGazing() {
        List<PlayerEntity> visible = getVisiblePlayers();
        gazingTargetCount = visible.size();
        
        // 每秒获取能量
        energyTickCounter++;
        if (energyTickCounter >= 20) {
            energyTickCounter = 0;
            if (gazingTargetCount > 0) {
                addEnergy(gazingTargetCount);
            }
        }
    }
    
    /**
     * 检查是否是活跃的慕恋者
     */
    public boolean isActiveAdmirer() {
        return isAdmirerMarked && !hasTransformed;
    }
    
    /**
     * 获取能量百分比
     */
    public float getEnergyPercent() {
        return (float) energy / MAX_ENERGY;
    }
    
    /**
     * 同步到客户端
     */
    public void sync() {
        ModComponents.ADMIRER.sync(this.player);
    }
    
    // ==================== Tick 处理 ====================
    
    @Override
    public void serverTick() {
        // 只在慕恋者角色时处理
        if (!isActiveAdmirer()) return;
        
        // 检查玩家是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
        
        // 窥视技能处理
        if (isGazing) {
            updateGazing();
        }
    }
    
    // ==================== NBT 序列化 ====================
    
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("energy", this.energy);
        tag.putBoolean("isGazing", this.isGazing);
        tag.putInt("gazingTargetCount", this.gazingTargetCount);
        tag.putBoolean("isAdmirerMarked", this.isAdmirerMarked);
        tag.putBoolean("hasTransformed", this.hasTransformed);
    }
    
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.energy = tag.contains("energy") ? tag.getInt("energy") : 0;
        this.isGazing = tag.contains("isGazing") && tag.getBoolean("isGazing");
        this.gazingTargetCount = tag.contains("gazingTargetCount") ? tag.getInt("gazingTargetCount") : 0;
        this.isAdmirerMarked = tag.contains("isAdmirerMarked") && tag.getBoolean("isAdmirerMarked");
        this.hasTransformed = tag.contains("hasTransformed") && tag.getBoolean("hasTransformed");
    }
}