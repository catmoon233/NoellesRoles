package org.agmas.noellesroles.roles.thief;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class ThiefPlayerComponent implements AutoSyncedComponent {
    public static final ComponentKey<ThiefPlayerComponent> KEY = ComponentRegistry.getOrCreate(Identifier.of(Noellesroles.MOD_ID, "thief"), ThiefPlayerComponent.class);
    private final PlayerEntity player;
    public int blackoutPrice = 100; // First blackout costs 100
    public boolean hasBlackoutEffect = false;
    public boolean hasThiefsHonor = false; // Purchased at breathing point

    public ThiefPlayerComponent(PlayerEntity player) {
        this.player = player;
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void upgradeBlackoutPrice() {
        this.blackoutPrice += 50; // Price increases by 50 each time
        sync();
    }

    public void activateBlackout() {
        this.hasBlackoutEffect = true;
        sync();
    }

    public void deactivateBlackout() {
        this.hasBlackoutEffect = false;
        sync();
    }

    public void purchaseThiefsHonor() {
        this.hasThiefsHonor = true;
        sync();
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.putInt("blackoutPrice", this.blackoutPrice);
        tag.putBoolean("hasBlackoutEffect", this.hasBlackoutEffect);
        tag.putBoolean("hasThiefsHonor", this.hasThiefsHonor);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        this.blackoutPrice = tag.getInt("blackoutPrice");
        this.hasBlackoutEffect = tag.getBoolean("hasBlackoutEffect");
        this.hasThiefsHonor = tag.getBoolean("hasThiefsHonor");
    }
}