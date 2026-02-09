package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMEntities;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.NRSounds;
import org.agmas.noellesroles.role.ModRoles;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class NianShouPlayerComponent implements RoleComponent, ServerTickingComponent {

    public static final ComponentKey<NianShouPlayerComponent> KEY = ModComponents.NIAN_SHOU;

    private final Player player;

    // 红包数量
    private int redPacketCount = 0;

    // 任务完成计数（每完成2个任务获得1个红包）
    private int tasksCompleted = 0;

    // 黑暗护盾试剂触发标志（一局游戏只能触发一次）
    private boolean darknessShieldTriggered = false;

    // 黑暗速度效果冷却（ticks）
    private int speedEffectCooldown = 0;

    // 黑暗状态标志
    private boolean inDarkness = false;

    // 恭喜发财播放状态
    private boolean gongXiFaCaiPlaying = false;

    // 恭喜发财播放计时
    private int gongXiFaCaiTimer = 0;

    public NianShouPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public void reset() {
        this.redPacketCount = 0;
        this.tasksCompleted = 0;
        this.darknessShieldTriggered = false;
        this.speedEffectCooldown = 0;
        this.inDarkness = false;
        this.gongXiFaCaiPlaying = false;
        this.gongXiFaCaiTimer = 0;
        ModComponents.NIAN_SHOU.sync(this.player);
    }

    @Override
    public void clear() {
        this.reset();
    }

    public int getRedPacketCount() {
        return redPacketCount;
    }

    public boolean isGongXiFaCaiPlaying() {
        return gongXiFaCaiPlaying;
    }

    public void addRedPacket() {
        this.redPacketCount++;
        ModComponents.NIAN_SHOU.sync(this.player);
    }

    public void useRedPacket() {
        if (redPacketCount > 0) {
            redPacketCount--;
            ModComponents.NIAN_SHOU.sync(this.player);
        }
    }

    public void onTaskCompleted() {
        this.tasksCompleted++;
        if (this.tasksCompleted >= 2) {
            this.tasksCompleted = 0;
            this.redPacketCount++;
            ModComponents.NIAN_SHOU.sync(this.player);
            if (player instanceof ServerPlayer sp) {
                sp.displayClientMessage(
                    Component.translatable("message.noellesroles.nianshou.red_packet_earned")
                        .withStyle(ChatFormatting.GOLD),
                    true);
            }
        }
    }

    @Override
    public void serverTick() {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, ModRoles.NIAN_SHOU))
            return;

        // 检查黑暗环境并触发护盾和速度
        checkDarkness();

        // 检查鞭炮数量
        checkFirecrackers();

        // 检查游戏时间，触发恭喜发财
        checkGongXiFaCai();

        // 处理恭喜发财播放计时
        if (gongXiFaCaiPlaying) {
            gongXiFaCaiTimer++;
            // 1分13秒 = 73秒 = 73 * 20 = 1460 ticks
            if (gongXiFaCaiTimer >= 1460) {
                // 停止播放
                gongXiFaCaiPlaying = false;
                gongXiFaCaiTimer = 0;
                ModComponents.NIAN_SHOU.sync(this.player);
            }
        }
    }

    private void checkDarkness() {
        // 检查是否在黑暗环境下（光照等级 < 2）
        int lightLevel = player.level().getRawBrightness(player.blockPosition(), net.minecraft.world.level.LightLayer.BLOCK.ordinal());

        if (lightLevel < 2) {
            if (!inDarkness) {
                // 刚进入黑暗
                inDarkness = true;

                // 进入黑暗时，给予护盾试剂（一局一次）
                if (!darknessShieldTriggered) {
                    darknessShieldTriggered = true;
                    player.getInventory().add(new ItemStack(TMMItems.DEFENSE_VIAL));

                    if (player instanceof ServerPlayer sp) {
                        sp.displayClientMessage(
                            Component.translatable("message.noellesroles.nianshou.darkness_shield")
                                .withStyle(ChatFormatting.GOLD),
                            true);
                    }
                    ModComponents.NIAN_SHOU.sync(this.player);
                }
            }

            // 在黑暗中持续给予速度二效果（每10秒刷新一次）
            if (speedEffectCooldown <= 0) {
                if (player instanceof ServerPlayer sp) {
                    // 给予速度二效果（10秒 = 200 ticks）
                    sp.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                        200, // 10秒
                        1 // 速度等级2
                    ));
                    sp.displayClientMessage(
                        Component.translatable("message.noellesroles.nianshou.darkness_speed")
                            .withStyle(ChatFormatting.GOLD),
                        true);
                }
                // 重置冷却时间（10秒 = 200 ticks）
                speedEffectCooldown = 200;
            } else {
                speedEffectCooldown--;
            }
        } else {
            if (inDarkness) {
                // 离开黑暗环境
                inDarkness = false;
            }
        }
    }

    private void checkFirecrackers() {
        // 检查年兽5格半径内的鞭炮实体数量
        if (player.level() instanceof ServerLevel serverLevel) {
            int firecrackerCount = 0;
            for (var entity : serverLevel.getEntities(TMMEntities.FIRECRACKER, entity -> true)) {
                if (entity.distanceTo(player) <= 5) {
                    firecrackerCount++;
                }
            }

            // 如果有12个或更多鞭炮，年兽死亡（除岁成功）
            if (firecrackerCount >= 12) {
                if (player instanceof ServerPlayer sp) {
                    // 发送死亡消息
                    for (Player p : player.level().players()) {
                        p.displayClientMessage(
                            Component.translatable("message.noellesroles.nianshou.death_by_firecrackers", player.getName())
                                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                            true);
                    }
                    // 杀死年兽（使用鞭炮死亡原因）
                    GameFunctions.killPlayer(player, true, null, Noellesroles.id("nianshou_firecrackers"));
                }
            }
        }
    }

    private void checkGongXiFaCai() {
        // 检查游戏时间是否剩余5分钟（300秒 = 6000 ticks）
        if (player.level() instanceof ServerLevel serverLevel) {
            // 获取游戏剩余时间
            int remainingTime = dev.doctor4t.trainmurdermystery.cca.GameTimeComponent.KEY.get(serverLevel).getTime();

            // 剩余5分钟（300秒 = 6000 ticks）且未播放过
            // 只有在剩余时间刚变为6000 ticks时触发（避免重复触发）
            if (remainingTime == 6000 && !gongXiFaCaiPlaying) {
                // 检查年兽是否存活
                GameWorldComponent gameWorld = GameWorldComponent.KEY.get(serverLevel);
                boolean hasAliveNianShou = false;
                for (Player p : serverLevel.players()) {
                    if (gameWorld.isRole(p, ModRoles.NIAN_SHOU) && !GameFunctions.isPlayerEliminated(p)) {
                        hasAliveNianShou = true;
                        break;
                    }
                }

                if (hasAliveNianShou) {
                    // 播放恭喜发财
                    gongXiFaCaiPlaying = true;
                    gongXiFaCaiTimer = 0;

                    // 播放音乐
                    serverLevel.playSound(null, player.blockPosition(), NRSounds.GONGXI_FACAI, net.minecraft.sounds.SoundSource.MUSIC, 1.0F, 1.0F);

                    // 为所有存活玩家发放100金币并回满san值
                    for (Player p : serverLevel.players()) {
                        if (!GameFunctions.isPlayerEliminated(p)) {
                            if (p instanceof ServerPlayer sp) {
                                dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent shopComponent = dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent.KEY.get(p);
                                shopComponent.addToBalance(100);

                                // 回满san值
                                dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent moodComponent = dev.doctor4t.trainmurdermystery.cca.PlayerMoodComponent.KEY.get(p);
                                moodComponent.setMood(1f);

                                sp.displayClientMessage(
                                    Component.translatable("message.noellesroles.nianshou.gongxi_facai_reward")
                                        .withStyle(ChatFormatting.GOLD),
                                    true);
                            }
                        }
                    }

                    // 年兽获胜检查会在游戏结算时处理
                    ModComponents.NIAN_SHOU.sync(this.player);
                }
            }
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.redPacketCount = tag.getInt("redPacketCount");
        this.tasksCompleted = tag.getInt("tasksCompleted");
        this.darknessShieldTriggered = tag.getBoolean("darknessShieldTriggered");
        this.speedEffectCooldown = tag.getInt("speedEffectCooldown");
        this.inDarkness = tag.getBoolean("inDarkness");
        this.gongXiFaCaiPlaying = tag.getBoolean("gongXiFaCaiPlaying");
        this.gongXiFaCaiTimer = tag.getInt("gongXiFaCaiTimer");
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("redPacketCount", redPacketCount);
        tag.putInt("tasksCompleted", tasksCompleted);
        tag.putBoolean("darknessShieldTriggered", darknessShieldTriggered);
        tag.putInt("speedEffectCooldown", speedEffectCooldown);
        tag.putBoolean("inDarkness", inDarkness);
        tag.putBoolean("gongXiFaCaiPlaying", gongXiFaCaiPlaying);
        tag.putInt("gongXiFaCaiTimer", gongXiFaCaiTimer);
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
