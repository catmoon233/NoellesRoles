package org.agmas.noellesroles.repack.items;

import dev.doctor4t.trainmurdermystery.block.SmallDoorBlock;
import dev.doctor4t.trainmurdermystery.util.AdventureUsable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class MasterKeyItem extends Item implements AdventureUsable {
    public MasterKeyItem(Item.Properties settings) {
        super(settings);
    }

    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof SmallDoorBlock) {
            BlockPos lowerPos = state.getValue(SmallDoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            world.getBlockEntity(lowerPos);
            return InteractionResult.PASS;
        } else {
            return super.useOn(context);
        }
    }
}