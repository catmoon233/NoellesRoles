package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.api.RoleComponent;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.ModItems;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

public class PatrollerPlayerComponent implements RoleComponent, ServerTickingComponent {

    public static final ComponentKey<PatrollerPlayerComponent> KEY = ModComponents.PATROLLER;
    private final Player player;

    public PatrollerPlayerComponent(Player player) {
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void serverTick() {
        // 巡警的逻辑主要在死亡事件中触发，这里暂时不需要每tick运行
    }

    public void reset() {
        // 重置逻辑
    }

    public void sync() {
        KEY.sync(this.player);
    }

    public void onNearbyDeath() {
        if (player instanceof ServerPlayer serverPlayer) {
            // 给予左轮手枪
            serverPlayer.addItem(new ItemStack(TMMItems.REVOLVER));
            // 给予乘务员钥匙 (master_key_p)
            serverPlayer.addItem(new ItemStack(ModItems.MASTER_KEY_P));
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider registryLookup) {
    }
}