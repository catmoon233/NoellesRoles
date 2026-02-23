package org.agmas.noellesroles.block;

import carpet.network.ServerNetworkHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.agmas.noellesroles.packet.OpenVendingMachinesScreenS2CPacket;

public class VendingMachines extends Block {
    public VendingMachines(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
    }
    public static void useeVendingMachines(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer){
            ServerPlayNetworking.send(serverPlayer, new OpenVendingMachinesScreenS2CPacket());
        }
    }
    public static void registerVendingMachines() {

    }
}
