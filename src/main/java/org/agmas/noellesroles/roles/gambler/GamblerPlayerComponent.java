package org.agmas.noellesroles.roles.gambler;

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

public class GamblerPlayerComponent implements AutoSyncedComponent, ServerTickingComponent, ClientTickingComponent {
	public static final ComponentKey<GamblerPlayerComponent> KEY = ComponentRegistry.getOrCreate(ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "gambler"), GamblerPlayerComponent.class);
	private final Player player;
	public boolean usedAbility = false;
	@Override
	public boolean shouldSyncWith(ServerPlayer player) {
		return player == this.player;
	}
	public void reset() {
		this.usedAbility = false;
		this.sync();
	}

	public GamblerPlayerComponent(Player player) {
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
		tag.putBoolean("usedAbility", this.usedAbility);
	}

	public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
		this.usedAbility = tag.getBoolean("usedAbility");
	}
}