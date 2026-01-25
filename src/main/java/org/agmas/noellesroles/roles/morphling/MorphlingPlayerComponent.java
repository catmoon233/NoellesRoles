package org.agmas.noellesroles.roles.morphling;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class MorphlingPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<MorphlingPlayerComponent> KEY = ComponentRegistry.getOrCreate(ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "morphling"), MorphlingPlayerComponent.class);
    private final Player player;
    public UUID disguise;
    public int morphTicks = 0;
    public int tickR = 0;

    public void reset() {
        this.stopMorph();
        this.sync();
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player1) {
        return player == player1;
    }

    public MorphlingPlayerComponent(Player player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void clientTick() {
    }

    public void serverTick() {
        if (this.morphTicks != 0) {
            ++tickR;
            if (this.morphTicks > 0) {
                if (disguise != null) {
                    if (player.level().getPlayerByUUID(disguise) != null) {
                        if (((ServerPlayer)player.level().getPlayerByUUID(disguise)).gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                            stopMorph();
                            return;
                        }
                    } else {
                        stopMorph();
                        return;
                    }
                } else {
                    stopMorph();
                    return;
                }

                if (--this.morphTicks == 0) {
                    this.stopMorph();
                }
            } else if (this.morphTicks < 0) {
                this.morphTicks++;
                if (this.morphTicks == 0) {
                    this.sync();
                }
            }

            if (tickR % 20 == 0) {
                this.sync();
            }
        }
    }

    public boolean startMorph(UUID id) {
        setMorphTicks(GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().morphlingMorphDuration));
        disguise = id;
        TMM.SERVER.getPlayerList().getPlayers().forEach(
                player -> {
                    final var morphlingPlayerComponent = KEY.get(player);
                    morphlingPlayerComponent.disguise = disguise;
                    KEY.sync(player);
                }
        );

        return true;
    }

    public void stopMorph() {
        this.morphTicks = -GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().morphlingMorphCooldown);
        TMM.SERVER.getPlayerList().getPlayers().forEach(
                player -> {
                    final var morphlingPlayerComponent = KEY.get(player);
                    morphlingPlayerComponent.disguise = disguise;
                    KEY.sync(player);
                }        );

    }

    public int getMorphTicks() {
        return this.morphTicks;
    }

    public void setMorphTicks(int ticks) {
        this.morphTicks = ticks;
        this.sync();
    }

    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("morphTicks", this.morphTicks);
        if (disguise == null) disguise = player.getUUID();
        tag.putUUID("disguise", this.disguise);
    }

    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.morphTicks = tag.contains("morphTicks") ? tag.getInt("morphTicks") : 0;
        this.disguise = tag.contains("disguise") ? tag.getUUID("disguise") : player.getUUID();
    }
}
