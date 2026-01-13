package org.agmas.noellesroles;

import dev.doctor4t.trainmurdermystery.game.GameConstants;
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
import net.minecraft.world.entity.player.Player;

public class AbilityPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<AbilityPlayerComponent> KEY = ComponentRegistry.getOrCreate(ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "ability_no"), AbilityPlayerComponent.class);
    private final Player player;
    public int cooldown = 0;

    public void reset() {
        this.cooldown = 0;
        this.sync();
    }

    public AbilityPlayerComponent(Player player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void clientTick() {
    }

    public void serverTick() {
        if (this.cooldown > 0) {
            --this.cooldown;

            this.sync();
        }
    }

    public void setCooldown(int ticks) {
        this.cooldown = ticks;
        this.sync();
    }

    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putInt("cooldown", this.cooldown);
    }

    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
    }
}
