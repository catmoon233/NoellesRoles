package org.agmas.noellesroles.component;

import  org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 复仇者组件
 *
 * 功能：
 * - 存储绑定的目标玩家
 * - 当目标死亡时激活复仇能力
 * - 激活后可以看到凶手并获得武器
 */
public class AvengerPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    
    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<AvengerPlayerComponent> KEY = ModComponents.AVENGER;
    
    private final PlayerEntity player;
    
    // 绑定的目标玩家 UUID
    public UUID targetPlayer = null;
    
    // 是否已激活复仇能力
    public boolean activated = false;
    
    // 凶手的 UUID（目标被杀后记录）
    public UUID killerUuid = null;
    
    // 目标玩家的名字（用于 HUD 显示）
    public String targetName = "";
    
    // 是否已绑定目标（第一次使用后设置为 true）
    public boolean bound = false;
    
    public AvengerPlayerComponent(PlayerEntity player) {
        this.player = player;
    }
    
    /**
     * 重置组件状态
     */
    public void reset() {
        this.targetPlayer = null;
        this.activated = false;
        this.killerUuid = null;
        this.targetName = "";
        this.bound = false;
        this.sync();
    }
    
    /**
     * 绑定目标玩家
     * 
     * @param target 目标玩家 UUID
     * @param name 目标玩家名字
     */
    public void bindTarget(UUID target, String name) {
        this.targetPlayer = target;
        this.targetName = name;
        this.bound = true;
        this.sync();
        
        // 发送绑定消息
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.avenger.bound", name)
                    .formatted(Formatting.GOLD),
                false
            );
        }
    }
    
    /**
     * 随机绑定一个无辜玩家
     */
    public void bindRandomTarget() {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        List<UUID> innocentPlayers = new ArrayList<>();
        
        gameWorld.getRoles().forEach((uuid, role) -> {
            if (uuid.equals(player.getUuid())) return; // 排除自己
            PlayerEntity targetPlayer = player.getWorld().getPlayerByUuid(uuid);
            if (targetPlayer == null) return;
            if (role.isInnocent() && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                innocentPlayers.add(uuid);
            }
        });
        
        if (!innocentPlayers.isEmpty()) {
            Collections.shuffle(innocentPlayers);
            UUID targetUuid = innocentPlayers.get(0);
            PlayerEntity target = player.getWorld().getPlayerByUuid(targetUuid);
            if (target != null) {
                bindTarget(targetUuid, target.getName().getString());
            }
        }
    }
    
    /**
     * 激活复仇能力
     * 
     * @param killer 凶手的 UUID（可能为空，比如跌落死亡）
     */
    public void activate(UUID killer) {
        if (activated) return;
        
        this.activated = true;
        this.killerUuid = killer;
        
        if (player instanceof ServerPlayerEntity serverPlayer) {
            // 发送激活消息
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.avenger.activated", targetName)
                    .formatted(Formatting.RED, Formatting.BOLD),
                false
            );
            
            // 给予左轮手枪
            serverPlayer.giveItemStack(new ItemStack(TMMItems.REVOLVER));
            
            // 如果知道凶手，发送凶手信息
            if (killer != null) {
                PlayerEntity killerPlayer = player.getWorld().getPlayerByUuid(killer);
                if (killerPlayer != null) {
                    serverPlayer.sendMessage(
                        Text.translatable("message.noellesroles.avenger.killer_revealed", 
                            killerPlayer.getName().getString())
                            .formatted(Formatting.RED),
                        false
                    );
                }
            } else {
                serverPlayer.sendMessage(
                    Text.translatable("message.noellesroles.avenger.unknown_killer")
                        .formatted(Formatting.GRAY),
                    false
                );
            }
        }
        
        this.sync();
    }
    
    /**
     * 检查目标是否存活
     */
    public boolean isTargetAlive() {
        if (targetPlayer == null) return false;
        PlayerEntity target = player.getWorld().getPlayerByUuid(targetPlayer);
        return target != null && GameFunctions.isPlayerAliveAndSurvival(target);
    }
    
    /**
     * 获取凶手玩家名（用于 HUD 显示）
     */
    public String getKillerName() {
        if (killerUuid == null) return "";
        PlayerEntity killer = player.getWorld().getPlayerByUuid(killerUuid);
        return killer != null ? killer.getName().getString() : "";
    }
    
    public void sync() {
        ModComponents.AVENGER.sync(this.player);
    }
    
    @Override
    public void serverTick() {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        
        // 只有复仇者角色才处理
        if (!gameWorld.isRole(player, ModRoles.AVENGER)) return;
        
        // 如果已激活，不需要继续检测
        if (activated) return;
        
        // 如果没有绑定目标，不检测
        if (targetPlayer == null || !bound) return;
        
        // 检测目标是否死亡
        if (!isTargetAlive()) {
            // 目标已死亡，激活复仇能力
            // 注意：此时我们不知道凶手是谁，需要通过 Mixin 在死亡时记录
            // 这里只是备用检测
            activate(null);
        }
    }
    
    // ==================== NBT 序列化 ====================
    
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (targetPlayer != null) {
            tag.putUuid("targetPlayer", targetPlayer);
        }
        tag.putBoolean("activated", activated);
        if (killerUuid != null) {
            tag.putUuid("killerUuid", killerUuid);
        }
        tag.putString("targetName", targetName);
        tag.putBoolean("bound", bound);
    }
    
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.targetPlayer = tag.contains("targetPlayer") ? tag.getUuid("targetPlayer") : null;
        this.activated = tag.getBoolean("activated");
        this.killerUuid = tag.contains("killerUuid") ? tag.getUuid("killerUuid") : null;
        this.targetName = tag.getString("targetName");
        this.bound = tag.getBoolean("bound");
    }
}