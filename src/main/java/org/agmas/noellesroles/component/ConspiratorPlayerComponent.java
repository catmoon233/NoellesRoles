package org.agmas.noellesroles.component;


import org.agmas.noellesroles.Noellesroles;
import  org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;

/**
 * 阴谋家组件
 *
 * 功能：
 * - 存储目标玩家和猜测的角色
 * - 管理死亡倒计时（40秒）
 * - 处理猜测结果
 */
public class ConspiratorPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    
    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<ConspiratorPlayerComponent> KEY = ModComponents.CONSPIRATOR;
    
    // 死亡倒计时：20秒 = 400 ticks
    public static final int DEATH_COUNTDOWN = 20 * 20;
    
    private final PlayerEntity player;
    
    // 当前目标玩家 UUID
    public UUID targetPlayer = null;
    
    // 猜测的角色 ID
    public Identifier guessedRole = null;
    
    // 死亡倒计时（tick）
    public int deathCountdown = 0;
    
    // 是否猜测正确
    public boolean guessCorrect = false;
    
    // 目标玩家名字（用于显示）
    public String targetName = "";
    
    // 是否已成功击杀（用于判断胜利）
    public boolean hasKilled = false;
    
    public ConspiratorPlayerComponent(PlayerEntity player) {
        this.player = player;
    }
    
    /**
     * 重置组件状态
     */
    public void reset() {
        this.targetPlayer = null;
        this.guessedRole = null;
        this.deathCountdown = 0;
        this.guessCorrect = false;
        this.targetName = "";
        this.hasKilled = false;
        this.sync();
    }
    
    /**
     * 进行猜测
     * 
     * @param targetUuid 目标玩家 UUID
     * @param roleId 猜测的角色 ID
     * @return true 如果猜测正确
     */
    public boolean makeGuess(UUID targetUuid, Identifier roleId) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return false;
        
        PlayerEntity target = player.getWorld().getPlayerByUuid(targetUuid);
        if (target == null) return false;
        
        // 获取目标的实际角色
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        Role actualRole = gameWorld.getRole(target);
        
        if (actualRole == null) return false;
        
        this.targetPlayer = targetUuid;
        this.guessedRole = roleId;
        this.targetName = target.getName().getString();
        
        // 检查是否猜测正确
        if (actualRole.identifier().equals(roleId)) {
            // 猜测正确！开始死亡倒计时
            this.guessCorrect = true;
            this.deathCountdown = DEATH_COUNTDOWN;
            
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.conspirator.correct", targetName)
                    .formatted(Formatting.GREEN, Formatting.BOLD),
                false
            );
            
            // 通知目标玩家他们被诅咒了（但不告诉是谁）
            if (target instanceof ServerPlayerEntity targetServer) {
                targetServer.sendMessage(
                    Text.translatable("message.noellesroles.conspirator.cursed")
                        .formatted(Formatting.DARK_PURPLE),
                    false
                );
            }
            
            this.sync();
            return true;
        } else {
            // 猜测错误
            this.guessCorrect = false;
            
            serverPlayer.sendMessage(
                Text.translatable("message.noellesroles.conspirator.wrong", targetName)
                    .formatted(Formatting.RED),
                false
            );
            
            // 重置目标，允许再次猜测（如果还有书页）
            this.targetPlayer = null;
            this.guessedRole = null;
            this.targetName = "";
            
            this.sync();
            return false;
        }
    }
    
    /**
     * 获取剩余倒计时（秒）
     */
    public int getCountdownSeconds() {
        return deathCountdown / 20;
    }
    
    /**
     * 检查目标是否存活
     */
    public boolean isTargetAlive() {
        if (targetPlayer == null) return false;
        PlayerEntity target = player.getWorld().getPlayerByUuid(targetPlayer);
        return target != null && GameFunctions.isPlayerAliveAndSurvival(target);
    }
    
    public void sync() {
        ModComponents.CONSPIRATOR.sync(this.player);
    }
    
    @Override
    public void serverTick() {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        
        // 只有阴谋家角色才处理
        if (!gameWorld.isRole(player, ModRoles.CONSPIRATOR)) return;
        
        // 如果有正在进行的死亡倒计时
        if (deathCountdown > 0 && guessCorrect && targetPlayer != null) {
            deathCountdown--;
            
            // 每秒同步一次
            if (deathCountdown % 20 == 0) {
                this.sync();
                
                // 每10秒提醒目标玩家
                if (deathCountdown % 200 == 0 && deathCountdown > 0) {
                    PlayerEntity target = player.getWorld().getPlayerByUuid(targetPlayer);
                    if (target instanceof ServerPlayerEntity targetServer && GameFunctions.isPlayerAliveAndSurvival(target)) {
                        targetServer.sendMessage(
                            Text.translatable("message.noellesroles.conspirator.countdown", getCountdownSeconds())
                                .formatted(Formatting.DARK_PURPLE),
                            true
                        );
                    }
                }
            }
            
            // 倒计时结束，目标死亡
            if (deathCountdown <= 0) {
                PlayerEntity target = player.getWorld().getPlayerByUuid(targetPlayer);
                if (target != null && GameFunctions.isPlayerAliveAndSurvival(target)) {
                    // 使用心脏麻痹死因（隐藏真实原因）
                    Identifier deathReason = Noellesroles.id("heart_attack");
                    GameFunctions.killPlayer(target, true, player, deathReason);
                    
                    this.hasKilled = true;
                    
                    // 通知阴谋家
                    if (player instanceof ServerPlayerEntity serverPlayer) {
                        serverPlayer.sendMessage(
                            Text.translatable("message.noellesroles.conspirator.killed", targetName)
                                .formatted(Formatting.GREEN, Formatting.BOLD),
                            false
                        );
                    }
                }
                
                // 重置状态，可以继续猜测
                this.targetPlayer = null;
                this.guessedRole = null;
                this.guessCorrect = false;
                this.targetName = "";
                this.sync();
            }
        }
    }
    
    // ==================== NBT 序列化 ====================
    
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (targetPlayer != null) {
            tag.putUuid("targetPlayer", targetPlayer);
        }
        if (guessedRole != null) {
            tag.putString("guessedRole", guessedRole.toString());
        }
        tag.putInt("deathCountdown", deathCountdown);
        tag.putBoolean("guessCorrect", guessCorrect);
        tag.putString("targetName", targetName);
        tag.putBoolean("hasKilled", hasKilled);
    }
    
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.targetPlayer = tag.contains("targetPlayer") ? tag.getUuid("targetPlayer") : null;
        this.guessedRole = tag.contains("guessedRole") ? Identifier.tryParse(tag.getString("guessedRole")) : null;
        this.deathCountdown = tag.getInt("deathCountdown");
        this.guessCorrect = tag.getBoolean("guessCorrect");
        this.targetName = tag.getString("targetName");
        this.hasKilled = tag.getBoolean("hasKilled");
    }
}