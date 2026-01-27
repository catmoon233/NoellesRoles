package org.agmas.noellesroles.roles.noise_maker;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.role.ModRoles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class NoiseMakerPlayerComponent implements RoleComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<NoiseMakerPlayerComponent> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "noise_maker"), NoiseMakerPlayerComponent.class);
    private final Player player;
    public boolean isActive = true;
    public int cooldown = 0;

    @Override
    public Player getPlayer() {
        return player;
    }

    public void reset() {
        this.isActive = true;
        this.cooldown = 0;
        this.sync();
    }

    public NoiseMakerPlayerComponent(Player player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void clientTick() {
    }

    public void serverTick() {
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());
        if (!gameWorld.isRole(player, ModRoles.NOISEMAKER)) {
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
        if (cooldown % 20 == 0) {
            sync();
        }
    }

    public void useAbility() {
        if (cooldown > 0) {
            player.displayClientMessage(
                    Component.translatable("message.noellesroles.ability_cooldown", (cooldown) / 20), true);
            return;
        }

        // player.displayClientMessage(Component.translatable("message.noellesroles.insufficient_funds"),
        // true);
        player.addEffect(
                new MobEffectInstance(MobEffects.LUCK, 120, 0, false, false, false));

        cooldown = 1200;
        Component msg = Component.translatable("gui.noellesroles.noisemaker.ability").withStyle(ChatFormatting.AQUA,
                ChatFormatting.BOLD);
        if (player instanceof ServerPlayer serverPlayer) {
            player.level().playSound(null, serverPlayer.blockPosition(), SoundEvents.NOTE_BLOCK_HARP.value(),
                    SoundSource.PLAYERS, 2F, 0F);
            for (Player p : serverPlayer.level().players()) {
                if (p instanceof ServerPlayer sp)
                    sp.displayClientMessage(msg, true);
            }
            serverPlayer.displayClientMessage(msg, true);
        }

        sync();
    }

    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putBoolean("isActive", this.isActive);
        tag.putInt("cooldown", this.cooldown);
    }

    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.isActive = !tag.contains("isActive") || tag.getBoolean("isActive");
        this.cooldown = tag.getInt("cooldown");
    }
}