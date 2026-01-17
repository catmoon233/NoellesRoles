package org.agmas.noellesroles.component;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class InsaneKillerPlayerComponent implements AutoSyncedComponent, ServerTickingComponent {
    public static final ComponentKey<InsaneKillerPlayerComponent> KEY = ModComponents.INSANE_KILLER;
    private final Player player;

    public boolean isActive = false;
    public int cooldown = 0;

    public InsaneKillerPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }

    public void toggleAbility() {
        if (cooldown > 0 && !isActive)
            return;

        isActive = !isActive;
        if (!isActive) {
            cooldown = 30 * 20;
        }
        sync();
    }

    public void sync() {
        KEY.sync(this.player);
    }

    @Override
    public void serverTick() {
        if (cooldown > 0) {
            cooldown--;
            if (cooldown % 20 == 0)
                sync();
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putBoolean("isActive", isActive);
        tag.putInt("cooldown", cooldown);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        isActive = tag.contains("isActive") && tag.getBoolean("isActive");
        cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
    }
}