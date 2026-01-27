package org.agmas.noellesroles;

import dev.doctor4t.trainmurdermystery.game.GameConstants;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ConfigWorldComponent implements RoleComponent, ServerTickingComponent {
    public static final ComponentKey<ConfigWorldComponent> KEY = ComponentRegistry.getOrCreate(ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "config"), ConfigWorldComponent.class);
    public boolean insaneSeesMorphs = true;
    public boolean naturalVoodoosAllowed = false;
    public int masterKeyVisibleCount = 0;
    public boolean masterKeyIsVisible = false;
    private final Level world;

    public void reset() {
        this.sync();
    }

    public ConfigWorldComponent(Level world) {
        this.world = world;
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    public void sync() {
        KEY.sync(this.world);
    }

    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        insaneSeesMorphs = NoellesRolesConfig.HANDLER.instance().insanePlayersSeeMorphs;
        naturalVoodoosAllowed = NoellesRolesConfig.HANDLER.instance().voodooNonKillerDeaths;
        masterKeyVisibleCount = NoellesRolesConfig.HANDLER.instance().playerCountToMakeConducterKeyVisible;
        tag.putBoolean("insaneSeesMorphs", this.insaneSeesMorphs);
        tag.putBoolean("naturalVoodoosAllowed", this.naturalVoodoosAllowed);
        tag.putBoolean("masterKeyIsVisible", this.masterKeyIsVisible);
        tag.putInt("masterKeyVisibleCount", this.masterKeyVisibleCount);
    }



    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        if (tag.contains("insaneSeesMorphs"))   this.insaneSeesMorphs = tag.getBoolean("insaneSeesMorphs");
        if (tag.contains("naturalVoodoosAllowed"))   this.naturalVoodoosAllowed = tag.getBoolean("naturalVoodoosAllowed");
        if (tag.contains("masterKeyIsVisible"))   this.masterKeyIsVisible = tag.getBoolean("masterKeyIsVisible");
        if (tag.contains("masterKeyVisibleCount"))   this.masterKeyVisibleCount = tag.getInt("masterKeyVisibleCount");
    }

    @Override
    public void serverTick() {
        if (NoellesRolesConfig.HANDLER.instance().playerCountToMakeConducterKeyVisible == 0) {
            masterKeyIsVisible = false;
        } else {
            if (world.getServer() != null)
                masterKeyIsVisible =  world.getServer().getPlayerList().getPlayerCount() >= NoellesRolesConfig.HANDLER.instance().playerCountToMakeConducterKeyVisible;
        }

    }
}
