package org.agmas.noellesroles.roles.broadcaster;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.AbilityPlayerComponent;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

/**
 * 广播员玩家组件 - 管理广播员的广播技能
 *
 * <p>该组件负责同步客户端与服务器的冷却状态。
 */
public class BroadcasterPlayerComponent extends AbilityPlayerComponent {
    public static final ComponentKey<BroadcasterPlayerComponent> KEY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "broadcaster"),
        BroadcasterPlayerComponent.class
    );

    public BroadcasterPlayerComponent(Player player) {
        super(player);
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        super.writeToNbt(tag, registryLookup);
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        super.readFromNbt(tag, registryLookup);
    }
}