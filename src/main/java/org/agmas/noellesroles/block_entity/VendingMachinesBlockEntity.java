// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package org.agmas.noellesroles.block_entity;

import dev.doctor4t.trainmurdermystery.block.CameraBlock;
import dev.doctor4t.trainmurdermystery.index.TMMBlockEntities;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VendingMachinesBlockEntity extends BlockEntity {

   private final List<ShopEntry> items = new ArrayList<>();

   public VendingMachinesBlockEntity(BlockPos pos, BlockState state) {
      super(TMMBlockEntities.SECURITY_MONITOR, pos, state);
   }

   public List<BlockPos> getCameraPositions() {
      return new ArrayList(this.cameraPositions);
   }

   public Direction getCameraDirectionAt(Level level, BlockPos pos) {
      BlockState cameraState = level.getBlockState(pos);
      return cameraState.getBlock() instanceof CameraBlock ? (Direction)cameraState.getValue(CameraBlock.FACING) : null;
   }

   public boolean removeCameraPosition(BlockPos pos) {
      boolean removed = this.cameraPositions.remove(pos);
      if (removed) {
         this.setChanged();
      }

      return removed;
   }

   public void clearCameraPositions() {
      if (!this.cameraPositions.isEmpty()) {
         this.cameraPositions.clear();
         this.setChanged();
      }

   }

   protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
      super.saveAdditional(compoundTag, provider);
      CompoundTag positionsTag = new CompoundTag();
      positionsTag.putInt("Size", this.cameraPositions.size());

      for(int i = 0; i < this.cameraPositions.size(); ++i) {
         BlockPos pos = (BlockPos)this.cameraPositions.get(i);
         positionsTag.putIntArray("Pos_" + i, new int[]{pos.getX(), pos.getY(), pos.getZ()});
      }

      compoundTag.put("CameraPositions", positionsTag);
   }

   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
      super.loadAdditional(tag, provider);
      if (tag.contains("CameraPositions")) {
         CompoundTag positionsTag = tag.getCompound("CameraPositions");
         int size = positionsTag.getInt("Size");
         this.cameraPositions.clear();

         for(int i = 0; i < size; ++i) {
            if (positionsTag.contains("Pos_" + i)) {
               int[] posArray = positionsTag.getIntArray("Pos_" + i);
               if (posArray.length == 3) {
                  this.cameraPositions.add(new BlockPos(posArray[0], posArray[1], posArray[2]));
               }
            }
         }
      }

   }
}
