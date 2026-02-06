package org.agmas.noellesroles.roles.manipulator;

import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import org.agmas.noellesroles.Noellesroles;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class InControlCCA implements RoleComponent, ServerTickingComponent {
    public static final ComponentKey<InControlCCA> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "in_control"),
            InControlCCA.class);

    public Player controller;
    public Player player;
    public boolean isControlling = false;
    public int controlTimer;

    public InControlCCA(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }

    public void sync() {
        KEY.sync(player);
    }

    @Override
    public void reset() {
        this.controller = null;
        isControlling = false;
        controlTimer = 0;
    }

    @Override
    public void clear() {
        this.reset();
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        isControlling = compoundTag.getBoolean("isControlling");
        controlTimer = compoundTag.getInt("controlTimer");
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putBoolean("isControlling", isControlling);
        compoundTag.putInt("controlTimer", controlTimer);
    }

    public void stopControl() {
        this.isControlling = false;
        this.controlTimer = 0;
        this.controller = null;

        this.sync();
    }

    @Override
    public void serverTick() {
        if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
            this.reset();
            return;
        }
        if (isControlling) {
            if (controlTimer > 0) {
                --controlTimer;
                if (player.isShiftKeyDown()) {
                    --controlTimer;
                }
                if (controlTimer % 20 == 0) {
                    sync();
                }
            }

            if (controlTimer <= 0) {
                if (this.controller != null) {
                    var controllerComponent = ManipulatorPlayerComponent.KEY.get(this.controller);
                    if (controllerComponent != null)
                        controllerComponent.stopControl(true);
                    else
                        stopControl();
                } else {
                    stopControl();

                }
            }
        }
    }
}
