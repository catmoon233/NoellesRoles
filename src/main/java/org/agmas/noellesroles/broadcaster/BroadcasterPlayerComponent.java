package org.agmas.noellesroles.broadcaster;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
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
        Identifier.of(Noellesroles.MOD_ID, "broadcaster"),
        BroadcasterPlayerComponent.class
    );

    public BroadcasterPlayerComponent(PlayerEntity player) {
        super(player);
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeToNbt(tag, registryLookup);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.readFromNbt(tag, registryLookup);
    }
}