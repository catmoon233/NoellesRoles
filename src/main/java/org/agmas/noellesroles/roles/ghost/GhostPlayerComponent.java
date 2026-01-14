package org.agmas.noellesroles.roles.ghost;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class GhostPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
    public static final ComponentKey<GhostPlayerComponent> KEY = ComponentRegistry.getOrCreate(ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "ghost"), GhostPlayerComponent.class);
    private final Player player;
    // 可以存储一些状态，例如是否正在隐身等，但小透明是被动能力，可能不需要
    public boolean isActive = true;
    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }
    public void reset() {
        this.isActive = true;
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
    }

    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putBoolean("isActive", this.isActive);
    }

    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.isActive = !tag.contains("isActive") || tag.getBoolean("isActive");
    }
}