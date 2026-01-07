package org.agmas.noellesroles.component;


import org.agmas.noellesroles.Noellesroles;
import  org.agmas.noellesroles.role.ModRoles;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
    
    private final Player player;
    
    // 当前目标玩家 UUID
    public UUID targetPlayer = null;
    
    // 猜测的角色 ID
    public ResourceLocation guessedRole = null;
    
    // 死亡倒计时（tick）
    public int deathCountdown = 0;
    
    // 是否猜测正确
    public boolean guessCorrect = false;
    
    // 目标玩家名字（用于显示）
    public String targetName = "";
    
    // 是否已成功击杀（用于判断胜利）
    public boolean hasKilled = false;
    
    public ConspiratorPlayerComponent(Player player) {
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
    public boolean makeGuess(UUID targetUuid, ResourceLocation roleId) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        
        Player target = player.level().getPlayerByUUID(targetUuid);
        if (target == null) return false;
        
        // 获取目标的实际角色
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
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
            
            serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.conspirator.correct", targetName)
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                false
            );
            
            // 通知目标玩家他们被诅咒了（但不告诉是谁）
            if (target instanceof ServerPlayer targetServer) {
                targetServer.displayClientMessage(
                    Component.translatable("message.noellesroles.conspirator.cursed")
                        .withStyle(ChatFormatting.DARK_PURPLE),
                    false
                );
            }
            
            this.sync();
            return true;
        } else {
            // 猜测错误
            this.guessCorrect = false;
            
            serverPlayer.displayClientMessage(
                Component.translatable("message.noellesroles.conspirator.wrong", targetName)
                    .withStyle(ChatFormatting.RED),
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
        Player target = player.level().getPlayerByUUID(targetPlayer);
        return target != null && GameFunctions.isPlayerAliveAndSurvival(target);
    }
    
    public void sync() {
        ModComponents.CONSPIRATOR.sync(this.player);
    }
    
    @Override
    public void serverTick() {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        
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
                    Player target = player.level().getPlayerByUUID(targetPlayer);
                    if (target instanceof ServerPlayer targetServer && GameFunctions.isPlayerAliveAndSurvival(target)) {
                        targetServer.displayClientMessage(
                            Component.translatable("message.noellesroles.conspirator.countdown", getCountdownSeconds())
                                .withStyle(ChatFormatting.DARK_PURPLE),
                            true
                        );
                    }
                }
            }
            
            // 倒计时结束，目标死亡
            if (deathCountdown <= 0) {
                Player target = player.level().getPlayerByUUID(targetPlayer);
                if (target != null && GameFunctions.isPlayerAliveAndSurvival(target)) {
                    // 使用心脏麻痹死因（隐藏真实原因）
                    ResourceLocation deathReason = Noellesroles.id("heart_attack");
                    GameFunctions.killPlayer(target, true, player, deathReason);
                    
                    this.hasKilled = true;
                    
                    // 通知阴谋家
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.displayClientMessage(
                            Component.translatable("message.noellesroles.conspirator.killed", targetName)
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
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
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (targetPlayer != null) {
            tag.putUUID("targetPlayer", targetPlayer);
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
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.targetPlayer = tag.contains("targetPlayer") ? tag.getUUID("targetPlayer") : null;
        this.guessedRole = tag.contains("guessedRole") ? ResourceLocation.tryParse(tag.getString("guessedRole")) : null;
        this.deathCountdown = tag.getInt("deathCountdown");
        this.guessCorrect = tag.getBoolean("guessCorrect");
        this.targetName = tag.getString("targetName");
        this.hasKilled = tag.getBoolean("hasKilled");
    }
}