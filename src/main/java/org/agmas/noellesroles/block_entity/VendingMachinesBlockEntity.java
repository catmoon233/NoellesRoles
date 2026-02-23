// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package org.agmas.noellesroles.block_entity;

import dev.doctor4t.trainmurdermystery.util.ShopEntry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import org.agmas.noellesroles.init.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

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

   public void addItem(ShopEntry shopEntry) {
      this.items.add(shopEntry);
      this.setChanged();
      this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
              Block.UPDATE_CLIENTS);
      // 调试输出
      if (this.level != null && !this.level.isClientSide()) {
         System.out.println("[VendingMachine] 添加商品: " + shopEntry.stack().getDisplayName().getString() + 
                           " 价格: " + shopEntry.price() + 
                           " 物品为空: " + shopEntry.stack().isEmpty());
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
         ItemStack itemStack = shopEntry.stack();
         
         // 仿照BeveragePlateBlockEntity的序列化方式
         if (itemStack != null && !itemStack.isEmpty()) {
            entryTag.put("item", itemStack.save(provider));
         } else {
            // 如果物品为空，创建一个默认的空物品标签
            CompoundTag emptyItem = new CompoundTag();
            emptyItem.putString("id", "minecraft:air");
            emptyItem.putByte("Count", (byte) 0);
            entryTag.put("item", emptyItem);
         }
         list.add(entryTag);
      }
      compoundTag.put("shop", list);
   }
   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
      return this.saveWithoutMetadata(registryLookup);
   }

   @Override
   public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
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
                  try {
                     CompoundTag itemTag = entry.getCompound("item");
                     // 检查是否是有效的物品标签
                     if (itemTag.contains("id") && !itemTag.getString("id").equals("minecraft:air") && itemTag.getByte("Count") > 0) {
                        item = ItemStack.parse(this.level.registryAccess(), entry.get("item")).orElse(ItemStack.EMPTY);
                        // 验证解析后的物品
                        if (item.isEmpty()) {
                           System.out.println("[VendingMachine] 警告: 物品解析失败");
                        }
                     } else {
                        // 空物品或无效物品
                        item = ItemStack.EMPTY;
                        System.out.println("[VendingMachine] 检测到空物品或无效物品");
                     }
                  } catch (Exception e) {
                     System.out.println("[VendingMachine] 物品反序列化异常: " + e.getMessage());
                     item = ItemStack.EMPTY;
                  }
               }
               items.add(new ShopEntry(item, price, ShopEntry.Type.TOOL));
            }
         }
      }
   }
}
