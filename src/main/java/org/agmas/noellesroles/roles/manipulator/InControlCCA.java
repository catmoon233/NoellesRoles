package org.agmas.noellesroles.roles.manipulator;

import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

public class InControlCCA implements RoleComponent {
    public static final ComponentKey<InControlCCA> KEY = ComponentRegistry.getOrCreate(
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "in_control"),
            InControlCCA.class);


    public Player player ;
    public boolean isControlling = false;

    public InControlCCA(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player==this.player;
    }
    public void sync() {
        KEY.sync(player);
    }
    @Override
    public void reset() {
        isControlling = false;

    }

    @Override
    public void readFromNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        isControlling = compoundTag.getBoolean("isControlling");
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putBoolean("isControlling", isControlling);
    }
}
