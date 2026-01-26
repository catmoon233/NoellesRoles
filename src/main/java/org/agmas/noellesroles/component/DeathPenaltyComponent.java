package org.agmas.noellesroles.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;

public class DeathPenaltyComponent implements RoleComponent {
    private final Player player;
    public long penaltyExpiry = 0;

    public DeathPenaltyComponent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    public void setPenalty(long durationTicks) {
        this.penaltyExpiry = player.level().getGameTime() + durationTicks;
        ModComponents.DEATH_PENALTY.sync(player);
    }

    public boolean hasPenalty() {
        return player.level().getGameTime() < penaltyExpiry;
    }

    public void reset() {
        this.penaltyExpiry = 0;
        ModComponents.DEATH_PENALTY.sync(player);
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.penaltyExpiry = tag.getLong("penaltyExpiry");
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putLong("penaltyExpiry", this.penaltyExpiry);
    }
}