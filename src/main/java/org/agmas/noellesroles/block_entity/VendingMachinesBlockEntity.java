// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package org.agmas.noellesroles.block_entity;

import dev.doctor4t.trainmurdermystery.util.ShopEntry;

import java.util.ArrayList;
import java.util.List;

import org.agmas.noellesroles.init.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VendingMachinesBlockEntity extends BlockEntity {

   private final List<ShopEntry> items = new ArrayList<>();

   public VendingMachinesBlockEntity(BlockPos pos, BlockState state) {
      super(ModBlocks.VENDING_MACHINES_BLOCK_ENTITY, pos, state);
   }

   public List<ShopEntry> getShops() {
      return new ArrayList<ShopEntry>(items);
   }

   public void clearItems() {
      if (!this.items.isEmpty()) {
         this.items.clear();
         this.setChanged();
      }

   }

   @Override
   protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
      super.saveAdditional(compoundTag, provider);
      ListTag list = new ListTag();

      for (int i = 0; i < this.items.size(); ++i) {
         CompoundTag entryTag = new CompoundTag();
         ShopEntry shopEntry = this.items.get(i);
         entryTag.putInt("price", shopEntry.price());
         CompoundTag shop = new CompoundTag();
         var itemStack = ItemStack.EMPTY;
         if (shopEntry.stack() != null) {
            itemStack = shopEntry.stack();
         }
         itemStack.save(this.level.registryAccess(), shop);
         entryTag.put("item", shop);
         list.add(entryTag);
      }
      compoundTag.put("shop", list);
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
      super.loadAdditional(tag, provider);
      items.clear();
      if (tag.contains("shop", Tag.TAG_LIST)) {
         ListTag shoptags = tag.getList("shop", Tag.TAG_LIST);
         for (var s : shoptags) {
            if (s.getId() == Tag.TAG_COMPOUND) {
               var entry = (CompoundTag) (s);
               int price = 0;
               ItemStack item = ItemStack.EMPTY;
               if (entry.contains("price")) {
                  price = entry.getInt("price");
               }
               if (entry.contains("item")) {
                  item = ItemStack.parse(this.level.registryAccess(), entry.get("item")).orElse(ItemStack.EMPTY);
               }
               items.add(new ShopEntry(item, price, ShopEntry.Type.TOOL));
            }
         }
      }
   }
}
