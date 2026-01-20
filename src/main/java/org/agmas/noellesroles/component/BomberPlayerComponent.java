package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class BomberPlayerComponent implements AutoSyncedComponent {
    public static final int BOMB_COST = 100;
    private final Player player;

    public BomberPlayerComponent(Player player) {
        this.player = player;
    }

    public void buyBomb() {
        if (player.level().isClientSide)
            return;

        PlayerShopComponent shopComponent = PlayerShopComponent.KEY.get(player);
        if (shopComponent.balance >= BOMB_COST) {
            shopComponent.balance -= BOMB_COST;
            shopComponent.sync();

            ItemStack bombStack = ModItems.BOMB.getDefaultInstance();
            if (!player.getInventory().add(bombStack)) {
                player.drop(bombStack, false);
            }
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        // No persistent data needed for now
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
        // No persistent data needed for now
    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.player;
    }
}