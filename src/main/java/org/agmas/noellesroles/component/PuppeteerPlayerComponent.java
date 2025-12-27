package org.agmas.noellesroles.component;


import org.agmas.noellesroles.ModEntities;
import org.agmas.noellesroles.entity.PuppeteerBodyEntity;
import  org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 傀儡师组件
 *
 * 管理傀儡师的两阶段机制：
 * - 阶段一（收集阶段）：右键回收尸体，10秒冷却
 * - 阶段二（杀手阶段）：制造假人，操控假人
 */
public class PuppeteerPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    
    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<PuppeteerPlayerComponent> KEY = ModComponents.PUPPETEER;
    
    // ==================== 常量定义 ====================
    
    /** 回收尸体冷却时间（10秒 = 200 tick） */
    public static final int COLLECT_COOLDOWN = 10 * 20;
    
    /** 假人操控时限（1分钟 = 1200 tick） */
    public static final int PUPPET_CONTROL_TIME = 60 * 20;
    
    /** 假人技能冷却时间（3分钟 = 3600 tick） */
    public static final int PUPPET_ABILITY_COOLDOWN = 180 * 20;
    
    // ==================== 状态变量 ====================
    
    private final PlayerEntity player;
    
    /** 当前阶段（1=收集阶段，2=杀手阶段） */
    public int phase = 0;
    
    /** 收集的尸体数量 */
    public int collectedBodies = 0;
    
    /** 收集的尸体玩家UUID列表（用于皮肤） */
    public List<UUID> collectedBodyUuids = new ArrayList<>();
    
    /** 回收冷却计时器（tick） */
    public int collectCooldown = 0;
    
    /** 技能冷却计时器（tick） */
    public int abilityCooldown = 0;
    
    /** 是否正在操控假人 */
    public boolean isControllingPuppet = false;
    
    /** 假人操控剩余时间（tick） */
    public int puppetControlTimer = 0;
    
    /** 本体位置 */
    public Vec3d originalPosition = Vec3d.ZERO;
    
    /** 本体旋转角度 */
    public float originalYaw = 0;
    public float originalPitch = 0;
    
    /** 本体实体（隐形盔甲架）的UUID */
    public UUID bodyEntityUuid = null;
    
    /** 假人实体UUID（已弃用，现在使用玩家本身作为假人） */
    public UUID puppetEntityUuid = null;
    
    /** 当前假人使用的皮肤UUID */
    public UUID puppetSkinUuid = null;
    
    /** 假人时的临时角色 */
    public Role puppetRole = null;
    
    /** 是否已标记为傀儡师 */
    public boolean isPuppeteerMarked = false;
    
    /** 本体物品栏存储 */
    public DefaultedList<ItemStack> originalInventory = DefaultedList.ofSize(41, ItemStack.EMPTY);
    
    /** 假人物品栏存储 */
    public DefaultedList<ItemStack> puppetInventory = DefaultedList.ofSize(41, ItemStack.EMPTY);
    
    /** 已使用的假人次数（对应收集的尸体数） */
    public int usedPuppetCount = 0;
    
    /**
     * 构造函数
     */
    public PuppeteerPlayerComponent(PlayerEntity player) {
        this.player = player;
    }
    
    /**
     * 重置组件状态
     * 在游戏开始时或角色分配时调用
     */
    public void reset() {
        this.phase = 1;
        this.collectedBodies = 0;
        this.collectedBodyUuids.clear();
        this.collectCooldown = 0;
        this.abilityCooldown = 0;
        this.isControllingPuppet = false;
        this.puppetControlTimer = 0;
        this.originalPosition = Vec3d.ZERO;
        this.originalYaw = 0;
        this.originalPitch = 0;
        this.puppetEntityUuid = null;
        this.puppetSkinUuid = null;
        this.puppetRole = null;
        this.isPuppeteerMarked = true;
        this.originalInventory = DefaultedList.ofSize(41, ItemStack.EMPTY);
        this.puppetInventory = DefaultedList.ofSize(41, ItemStack.EMPTY);
        this.usedPuppetCount = 0;
        this.sync();
    }
    
    /**
     * 完全清除组件状态（游戏结束时调用）
     */
    public void clearAll() {
        this.phase = 0;
        this.collectedBodies = 0;
        this.collectedBodyUuids.clear();
        this.collectCooldown = 0;
        this.abilityCooldown = 0;
        this.isControllingPuppet = false;
        this.puppetControlTimer = 0;
        this.originalPosition = Vec3d.ZERO;
        this.originalYaw = 0;
        this.originalPitch = 0;
        this.puppetEntityUuid = null;
        this.puppetSkinUuid = null;
        this.puppetRole = null;
        this.isPuppeteerMarked = false;
        this.originalInventory = DefaultedList.ofSize(41, ItemStack.EMPTY);
        this.puppetInventory = DefaultedList.ofSize(41, ItemStack.EMPTY);
        this.usedPuppetCount = 0;
        this.sync();
    }
    
    /**
     * 检查是否可以回收尸体
     */
    public boolean canCollectBody() {
        return phase == 1 && collectCooldown <= 0;
    }
    
    /**
     * 回收尸体
     * @param bodyOwnerUuid 尸体对应玩家的UUID
     * @param totalPlayers 游戏总人数
     */
    public void collectBody(UUID bodyOwnerUuid, int totalPlayers) {
        if (!canCollectBody()) return;
        
        // 添加到收集列表
        collectedBodies++;
        collectedBodyUuids.add(bodyOwnerUuid);
        
        // 设置冷却
        collectCooldown = COLLECT_COOLDOWN;
        
        // 计算阈值（总人数/6）
        int threshold = Math.max(1, totalPlayers / 6);
        
        // 发送消息（包含当前数量和阈值）
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.puppeteer.collected", collectedBodies, threshold)
                    .formatted(Formatting.LIGHT_PURPLE),
                true
            );
        }
        
        // 检查是否达到阈值
        if (collectedBodies >= threshold) {
            advanceToPhase2();
        }
        
        // 播放音效
        player.getWorld().playSound(null, player.getBlockPos(),
            SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5F, 1.5F);
        
        this.sync();
    }
    
    /**
     * 进入阶段二（杀手阶段）
     */
    public void advanceToPhase2() {
        this.phase = 2;
        
        if (player instanceof ServerPlayerEntity serverPlayer) {
            // 添加杀手角色标记（使傀儡师可以使用杀手功能）
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
            
            // 发送阶段转换消息
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.puppeteer.phase2_advance")
                    .formatted(Formatting.DARK_PURPLE, Formatting.BOLD),
                false
            );
            
            // 播放音效
            player.getWorld().playSound(null, player.getBlockPos(),
                SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.5F, 1.5F);
        }
        
        this.sync();
    }
    
    /**
     * 检查是否可以使用假人技能
     */
    public boolean canUsePuppetAbility() {
        return phase == 2 && 
               abilityCooldown <= 0 && 
               !isControllingPuppet && 
               usedPuppetCount < collectedBodies;
    }
    
    /**
     * 获取剩余可用假人次数
     */
    public int getRemainingPuppetUses() {
        return Math.max(0, collectedBodies - usedPuppetCount);
    }
    
    /**
     * 使用假人技能
     */
    public void usePuppetAbility() {
        if (!canUsePuppetAbility()) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        // 保存本体位置和朝向
        originalPosition = player.getPos();
        originalYaw = player.getYaw();
        originalPitch = player.getPitch();
        
        // 保存本体物品栏
        saveInventory(originalInventory);
        
        // 选择一个尸体皮肤
        if (!collectedBodyUuids.isEmpty()) {
            int skinIndex = usedPuppetCount % collectedBodyUuids.size();
            puppetSkinUuid = collectedBodyUuids.get(skinIndex);
        }
        
        // 随机选择杀手职业
        puppetRole = getRandomKillerRole();
        
        // 创建本体实体（傀儡本体标记）
        createBodyMarker(serverPlayer);
        
        // 清空当前物品栏，加载假人物品栏
        loadInventory(puppetInventory);
        
        // 设置操控状态
        isControllingPuppet = true;
        puppetControlTimer = PUPPET_CONTROL_TIME;
        usedPuppetCount++;
        
        // 设置玩家为假人角色（临时更改角色以获得杀手能力）
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        gameWorld.addRole(player, puppetRole);
        
        // 触发角色分配事件 - 初始化角色
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, puppetRole);
        
        // 如果是原版杀手，给予刀
        if (puppetRole.equals(TMMRoles.KILLER)) {
            player.giveItemStack(TMMItems.KNIFE.getDefaultStack());
        }
        
        // 发送消息（带操控时间）
        int controlSeconds = PUPPET_CONTROL_TIME / 20;
        serverPlayer.sendMessage(
            Text.translatable("message.noellesroles.puppeteer.puppet_activated", controlSeconds)
                .formatted(Formatting.DARK_PURPLE),
            false
        );
        
        // 播放音效
        player.getWorld().playSound(null, player.getBlockPos(),
            SoundEvents.ENTITY_EVOKER_CAST_SPELL, SoundCategory.PLAYERS, 1.0F, 1.0F);
        
        this.sync();
    }
    
    /**
     * 创建本体实体（傀儡本体）
     * 使用玩家模型和皮肤的自定义实体，可以被攻击
     */
    private void createBodyMarker(ServerPlayerEntity serverPlayer) {
        ServerWorld world = serverPlayer.getServerWorld();
        
        // 创建傀儡本体实体
        PuppeteerBodyEntity bodyEntity = new PuppeteerBodyEntity(ModEntities.PUPPETEER_BODY, world);
        bodyEntity.setPosition(originalPosition);
        bodyEntity.setYaw(originalYaw);
        bodyEntity.setPitch(originalPitch);
        bodyEntity.setOwner(serverPlayer);
        
        // 生成实体
        world.spawnEntity(bodyEntity);
        bodyEntityUuid = bodyEntity.getUuid();
    }
    
    /**
     * 移除本体标记实体
     */
    private void removeBodyMarker() {
        if (bodyEntityUuid == null) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;
        
        Entity entity = world.getEntity(bodyEntityUuid);
        if (entity != null) {
            entity.discard();
        }
        bodyEntityUuid = null;
    }
    
    /**
     * 获取随机杀手职业
     * 动态获取所有注册的杀手阵营角色（canUseKiller = true）
     */
    private Role getRandomKillerRole() {
        List<Role> killerRoles = getAvailableKillerRoles();
        
        if (killerRoles.isEmpty()) {
            // 如果没有可用的杀手角色，使用原版杀手
            killerRoles.add(TMMRoles.KILLER);
        }
        
        Random random = new Random();
        return killerRoles.get(random.nextInt(killerRoles.size()));
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
            // 排除傀儡师自己（避免傀儡师变成傀儡师）
            if (role.canUseKiller() && !role.identifier().equals(ModRoles.PUPPETEER_ID)) {
                killerRoles.add(role);
            }
        }
        
        return killerRoles;
    }
    
    /**
     * 保存物品栏到指定列表
     */
    private void saveInventory(DefaultedList<ItemStack> storage) {
        for (int i = 0; i < Math.min(storage.size(), player.getInventory().size()); i++) {
            storage.set(i, player.getInventory().getStack(i).copy());
        }
    }
    
    /**
     * 从指定列表加载物品栏
     */
    private void loadInventory(DefaultedList<ItemStack> storage) {
        player.getInventory().clear();
        for (int i = 0; i < Math.min(storage.size(), player.getInventory().size()); i++) {
            player.getInventory().setStack(i, storage.get(i).copy());
        }
    }
    
    /**
     * 假人死亡处理
     */
    public void onPuppetDeath() {
        if (!isControllingPuppet) return;
        
        // 返回本体
        returnToBody(false);
        
        // 发送消息
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.puppeteer.puppet_destroyed")
                    .formatted(Formatting.RED),
                true
            );
        }
    }
    
    /**
     * 本体死亡处理
     * 当本体标记被破坏或本体实体死亡时调用
     */
    public void onBodyDeath() {
        if (!isControllingPuppet) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        // 移除本体标记实体
        removeBodyMarker();
        
        // 清除假人状态，玩家真正死亡
        isControllingPuppet = false;
        puppetControlTimer = 0;
        puppetEntityUuid = null;
        puppetSkinUuid = null;
        puppetRole = null;
        
        // 发送消息
        serverPlayer.sendMessage(
            Text.translatable("message.noellesroles.puppeteer.body_died")
                .formatted(Formatting.DARK_RED, Formatting.BOLD),
            false
        );
        
        this.sync();
    }
    
    /**
     * 检查本体是否存活
     * 用于服务端tick检测
     */
    public boolean isBodyAlive() {
        if (!isControllingPuppet || bodyEntityUuid == null) return true;
        if (!(player.getWorld() instanceof ServerWorld world)) return true;
        
        Entity bodyMarker = world.getEntity(bodyEntityUuid);
        return bodyMarker != null && bodyMarker.isAlive();
    }
    
    /**
     * 返回本体
     * @param timeout 是否是超时返回
     */
    public void returnToBody(boolean timeout) {
        if (!isControllingPuppet) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        // 保存假人物品栏
        saveInventory(puppetInventory);
        
        // 移除本体标记实体
        removeBodyMarker();
        
        // 传送回本体位置
        serverPlayer.teleport(
            serverPlayer.getServerWorld(),
            originalPosition.x, originalPosition.y, originalPosition.z,
            originalYaw, originalPitch
        );
        
        // 恢复本体物品栏
        loadInventory(originalInventory);
        
        // 保存当前假人角色（用于清除）
        Role previousPuppetRole = puppetRole;
        
        // 清除假人状态
        isControllingPuppet = false;
        puppetControlTimer = 0;
        puppetEntityUuid = null;
        puppetSkinUuid = null;
        puppetRole = null;
        
        // 恢复为傀儡师角色
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        gameWorld.addRole(player, ModRoles.PUPPETEER);
        
        // 触发角色分配事件 - 这会通知所有模组的监听器清除之前的角色状态
        // 其他扩展模组（如 NoellesRoles）会在 onRoleAssigned 中检测角色变化并自动清除组件
        ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player, ModRoles.PUPPETEER);
        
        // 设置技能冷却
        abilityCooldown = PUPPET_ABILITY_COOLDOWN;
        
        // 发送消息
        if (timeout) {
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.puppeteer.puppet_timeout")
                    .formatted(Formatting.YELLOW),
                true
            );
        } else {
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.puppeteer.returned_to_body")
                    .formatted(Formatting.GREEN),
                true
            );
        }
        
        // 播放音效
        player.getWorld().playSound(null, player.getBlockPos(),
            SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        
        this.sync();
    }
    
    // clearPuppetRoleComponents() 已移除
    // 现在通过触发 ModdedRoleAssigned 事件来通知所有模组清除组件
    // 各模组在 onRoleAssigned 监听器中会检测角色变化并自动清除
    
    /**
     * 检查是否是活跃的傀儡师
     */
    public boolean isActivePuppeteer() {
        return isPuppeteerMarked && phase > 0;
    }
    
    /**
     * 获取回收冷却秒数
     */
    public float getCollectCooldownSeconds() {
        return collectCooldown / 20.0f;
    }
    
    /**
     * 获取技能冷却秒数
     */
    public float getAbilityCooldownSeconds() {
        return abilityCooldown / 20.0f;
    }
    
    /**
     * 获取假人操控剩余秒数
     */
    public float getPuppetControlSeconds() {
        return puppetControlTimer / 20.0f;
    }
    
    /**
     * 同步到客户端
     */
    public void sync() {
        ModComponents.PUPPETEER.sync(this.player);
    }
    
    // ==================== Tick 处理 ====================
    
    @Override
    public void serverTick() {
        if (!isActivePuppeteer()) return;
        
        // 检查玩家是否存活
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) return;
        
        // 处理回收冷却
        if (collectCooldown > 0) {
            collectCooldown--;
            if (collectCooldown == 0) {
                sync();
            }
        }
        
        // 处理技能冷却（每秒同步一次以更新UI）
        if (abilityCooldown > 0) {
            abilityCooldown--;
            // 每秒同步一次冷却状态到客户端
            if (abilityCooldown % 20 == 0 || abilityCooldown == 0) {
                sync();
            }
        }
        
        // 处理假人操控时间
        if (isControllingPuppet) {
            // 检查本体是否存活
            if (!isBodyAlive()) {
                onBodyDeath();
                return;
            }
            
            if (puppetControlTimer > 0) {
                puppetControlTimer--;
                
                // 每秒同步一次
                if (puppetControlTimer % 20 == 0) {
                    sync();
                }
                
                // 时间到，返回本体
                if (puppetControlTimer <= 0) {
                    returnToBody(true);
                }
            }
        }
    }
    
    // ==================== NBT 序列化 ====================
    
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("phase", this.phase);
        tag.putInt("collectedBodies", this.collectedBodies);
        
        // 保存收集的UUID列表
        NbtList uuidList = new NbtList();
        for (UUID uuid : collectedBodyUuids) {
            NbtCompound uuidTag = new NbtCompound();
            uuidTag.putUuid("uuid", uuid);
            uuidList.add(uuidTag);
        }
        tag.put("collectedBodyUuids", uuidList);
        
        tag.putInt("collectCooldown", this.collectCooldown);
        tag.putInt("abilityCooldown", this.abilityCooldown);
        tag.putBoolean("isControllingPuppet", this.isControllingPuppet);
        tag.putInt("puppetControlTimer", this.puppetControlTimer);
        
        tag.putDouble("originalPosX", this.originalPosition.x);
        tag.putDouble("originalPosY", this.originalPosition.y);
        tag.putDouble("originalPosZ", this.originalPosition.z);
        tag.putFloat("originalYaw", this.originalYaw);
        tag.putFloat("originalPitch", this.originalPitch);
        
        if (this.bodyEntityUuid != null) {
            tag.putUuid("bodyEntityUuid", this.bodyEntityUuid);
        }
        if (this.puppetEntityUuid != null) {
            tag.putUuid("puppetEntityUuid", this.puppetEntityUuid);
        }
        if (this.puppetSkinUuid != null) {
            tag.putUuid("puppetSkinUuid", this.puppetSkinUuid);
        }
        
        tag.putBoolean("isPuppeteerMarked", this.isPuppeteerMarked);
        tag.putInt("usedPuppetCount", this.usedPuppetCount);
    }
    
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.phase = tag.contains("phase") ? tag.getInt("phase") : 0;
        this.collectedBodies = tag.contains("collectedBodies") ? tag.getInt("collectedBodies") : 0;
        
        // 读取收集的UUID列表
        this.collectedBodyUuids.clear();
        if (tag.contains("collectedBodyUuids")) {
            NbtList uuidList = tag.getList("collectedBodyUuids", 10);
            for (int i = 0; i < uuidList.size(); i++) {
                NbtCompound uuidTag = uuidList.getCompound(i);
                if (uuidTag.contains("uuid")) {
                    this.collectedBodyUuids.add(uuidTag.getUuid("uuid"));
                }
            }
        }
        
        this.collectCooldown = tag.contains("collectCooldown") ? tag.getInt("collectCooldown") : 0;
        this.abilityCooldown = tag.contains("abilityCooldown") ? tag.getInt("abilityCooldown") : 0;
        this.isControllingPuppet = tag.contains("isControllingPuppet") && tag.getBoolean("isControllingPuppet");
        this.puppetControlTimer = tag.contains("puppetControlTimer") ? tag.getInt("puppetControlTimer") : 0;
        
        double posX = tag.contains("originalPosX") ? tag.getDouble("originalPosX") : 0;
        double posY = tag.contains("originalPosY") ? tag.getDouble("originalPosY") : 0;
        double posZ = tag.contains("originalPosZ") ? tag.getDouble("originalPosZ") : 0;
        this.originalPosition = new Vec3d(posX, posY, posZ);
        this.originalYaw = tag.contains("originalYaw") ? tag.getFloat("originalYaw") : 0;
        this.originalPitch = tag.contains("originalPitch") ? tag.getFloat("originalPitch") : 0;
        
        this.bodyEntityUuid = tag.contains("bodyEntityUuid") ? tag.getUuid("bodyEntityUuid") : null;
        this.puppetEntityUuid = tag.contains("puppetEntityUuid") ? tag.getUuid("puppetEntityUuid") : null;
        this.puppetSkinUuid = tag.contains("puppetSkinUuid") ? tag.getUuid("puppetSkinUuid") : null;
        
        this.isPuppeteerMarked = tag.contains("isPuppeteerMarked") && tag.getBoolean("isPuppeteerMarked");
        this.usedPuppetCount = tag.contains("usedPuppetCount") ? tag.getInt("usedPuppetCount") : 0;
    }
}