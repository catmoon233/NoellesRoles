package org.agmas.noellesroles.roles.ghost;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class GhostPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<GhostPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "ghost"), GhostPlayerComponent.class);
    private final Player player;
    public boolean isActive = true;
    public int cooldown = 0;
    public int invisibilityTicks = 0;

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }

    public void reset() {
        this.isActive = true;
        this.cooldown = 0;
        this.invisibilityTicks = 0;
        this.sync();
    }

    public GhostPlayerComponent(Player player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void clientTick() {
    }

    public void serverTick() {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, ModRoles.GHOST)) {
            return;
        }
        if (!gameWorld.isRunning()) {
            return;
        }
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
        }
        if (invisibilityTicks > 0) {
            invisibilityTicks--;
        }
    }

    public void useAbility() {
        if (cooldown > 0) {
            player.displayClientMessage(
                    Component.translatable("message.noellesroles.ability_cooldown", (cooldown + 19) / 20), true);
            return;
        }

        PlayerShopComponent shopComponent = PlayerShopComponent.KEY.get(player);
        if (shopComponent.balance < 100) {
            player.displayClientMessage(Component.translatable("message.noellesroles.insufficient_funds"), true);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.level().playSound(null, serverPlayer.blockPosition(), TMMSounds.UI_SHOP_BUY_FAIL,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return;
        }

        shopComponent.balance -= 100;
        shopComponent.sync();

        cooldown = 400;
        invisibilityTicks = 160;
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0, false, false, true));

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.level().playSound(null, serverPlayer.blockPosition(), TMMSounds.UI_SHOP_BUY,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        sync();
    }

    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putBoolean("isActive", this.isActive);
        tag.putInt("cooldown", this.cooldown);
        tag.putInt("invisibilityTicks", this.invisibilityTicks);
    }

    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.isActive = !tag.contains("isActive") || tag.getBoolean("isActive");
        this.cooldown = tag.getInt("cooldown");
        this.invisibilityTicks = tag.getInt("invisibilityTicks");
    }
}