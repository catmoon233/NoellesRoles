package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;

import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class GlitchRobotPlayerComponent implements RoleComponent, ServerTickingComponent, ClientTickingComponent {

    public static final ComponentKey<GlitchRobotPlayerComponent> KEY = ComponentRegistry
            .getOrCreate(Noellesroles.id("glitch_robot"), GlitchRobotPlayerComponent.class);

    private final Player player;
    public int glitchTimer = 0;

    public GlitchRobotPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public void reset() {
        this.glitchTimer = 0;
        this.sync();
    }

    @Override
    public void serverTick() {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, ModRoles.GLITCH_ROBOT)) {
            return;
        }

        // 故障计时器
        glitchTimer++;
        if (glitchTimer >= 1200) { // 1分钟
            glitchTimer = 0;
            // 缓慢 10 (Amplifier 9), 2秒 (40 ticks)
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 9, false, false, true));
        }
    }

    /**
     * 被击倒时调用，生成缓慢效果云
     */
    public void onKnockOut() {
        // 创建半径为4的缓慢2效果云，持续5秒（100 ticks）
        AreaEffectCloud cloud = new AreaEffectCloud(player.level(), player.getX(), player.getY(), player.getZ());
        cloud.setRadius(4.0F);
        cloud.setDuration(100); // 5秒
        cloud.setRadiusOnUse(0.0F);
        cloud.setRadiusPerTick(0.0F);
        cloud.setWaitTime(0);
        cloud.setParticle(null);
        cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false, true));
        player.level().addFreshEntity(cloud);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.glitchTimer = tag.getInt("glitchTimer");
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("glitchTimer", this.glitchTimer);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return this.player == player;
    }

    public void sync() {
        ModComponents.GLITCH_ROBOT.sync(this.player);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void clientTick() {
        if (!player.getSlot(103).get().is(ModItems.NIGHT_VISION_GLASSES))
            player.removeEffect(MobEffects.NIGHT_VISION);
    }
}