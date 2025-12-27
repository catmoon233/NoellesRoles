package org.agmas.noellesroles.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

/**
 * 电报员组件
 *
 * 功能：
 * - 存储剩余使用次数（最多6次）
 * - 管理匿名消息发送
 */
public class TelegrapherPlayerComponent implements AutoSyncedComponent {
    
    /** 组件键 - 用于从玩家获取此组件 */
    public static final ComponentKey<TelegrapherPlayerComponent> KEY = ModComponents.TELEGRAPHER;
    
    // 最大使用次数
    public static final int MAX_USES = 6;
    
    private final PlayerEntity player;
    
    // 剩余使用次数
    public int remainingUses = MAX_USES;
    
    public TelegrapherPlayerComponent(PlayerEntity player) {
        this.player = player;
    }
    
    /**
     * 重置组件状态
     */
    public void reset() {
        this.remainingUses = MAX_USES;
        this.sync();
    }
    
    /**
     * 检查是否还有剩余次数
     */
    public boolean hasUsesRemaining() {
        return remainingUses > 0;
    }
    
    /**
     * 使用一次能力
     * @return true 如果成功使用
     */
    public boolean useAbility() {
        if (!hasUsesRemaining()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(
                    Text.translatable("message.noellesroles.telegrapher.no_uses")
                        .formatted(Formatting.RED),
                    false
                );
            }
            return false;
        }
        
        remainingUses--;
        this.sync();
        return true;
    }
    
    /**
     * 发送匿名消息给所有玩家（使用Title显示）
     * @param message 要发送的消息
     */
    public void sendAnonymousMessage(String message) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        
        // 检查是否还有使用次数
        if (!useAbility()) {
            return;
        }
        
        // 创建Title和Subtitle文本
        Text titleText = Text.translatable("message.noellesroles.telegrapher.anonymous")
            .formatted(Formatting.DARK_AQUA, Formatting.BOLD);
        Text subtitleText = Text.literal(message).formatted(Formatting.WHITE);
        
        // 向所有玩家显示Title（包括生存模式玩家）
        for (ServerPlayerEntity targetPlayer : serverPlayer.getServer().getPlayerManager().getPlayerList()) {
            // 使用showTitle方法显示标题
            // 参数：fadeIn(淡入), stay(停留), fadeOut(淡出) - 单位：tick
            targetPlayer.networkHandler.sendPacket(
                new net.minecraft.network.packet.s2c.play.TitleS2CPacket(titleText)
            );
            targetPlayer.networkHandler.sendPacket(
                new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(subtitleText)
            );
            targetPlayer.networkHandler.sendPacket(
                new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 60, 10)
            );
        }
        
        // 向发送者确认
        serverPlayer.sendMessage(
            Text.translatable("message.noellesroles.telegrapher.sent", remainingUses)
                .formatted(Formatting.GREEN),
            false
        );
    }
    
    public void sync() {
        ModComponents.TELEGRAPHER.sync(this.player);
    }
    
    // ==================== NBT 序列化 ====================
    
    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("remainingUses", remainingUses);
    }
    
    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.remainingUses = tag.getInt("remainingUses");
    }
}