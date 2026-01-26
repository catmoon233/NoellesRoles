package org.agmas.noellesroles.roles.coroner;

import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.Noellesroles;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class BodyDeathReasonComponent implements RoleComponent, ServerTickingComponent {
    public static final ComponentKey<BodyDeathReasonComponent> KEY = ComponentRegistry.getOrCreate(ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "body_death_reason"), BodyDeathReasonComponent.class);
    public ResourceLocation deathReason = GameConstants.DeathReasons.GENERIC;
    public ResourceLocation playerRole = TMMRoles.CIVILIAN.identifier();
    public boolean vultured = false;
    public PlayerBodyEntity playerBodyEntity;

    public void reset() {
        this.sync();
    }

    public BodyDeathReasonComponent(PlayerBodyEntity playerBodyEntity) {
        this.playerBodyEntity = playerBodyEntity;
    }

    public void sync() {
        KEY.sync(this.playerBodyEntity);
    }

    public void writeToNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        tag.putString("deathReason", deathReason.toString());
        tag.putString("playerRole", playerRole.toString());
        tag.putBoolean("vultured", vultured);
    }

    public void readFromNbt(@NotNull CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.deathReason = ResourceLocation.parse(tag.getString("deathReason"));
        this.playerRole = ResourceLocation.parse(tag.getString("playerRole"));
        this.vultured = tag.getBoolean("vultured");
    }

    @Override
    public void serverTick() {

    }
}
