package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.Noellesroles;
import dev.doctor4t.trainmurdermystery.api.RoleComponent;

public class BomberPlayerComponent implements RoleComponent {
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
        } else {
            player.displayClientMessage(Component.translatable("message.noellesroles.insufficient_funds"), true);
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSoundPacket(
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY_FAIL),
                        SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F,
                        0.9F + player.getRandom().nextFloat() * 0.2F, player.getRandom().nextLong()));
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
    public Player getPlayer() {
        return player;
    }
}