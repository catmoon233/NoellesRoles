package org.agmas.noellesroles.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 玩家抽奖记录数据类
 * - 存储玩家识别信息
 * - 存储玩家抽奖次数及记录
 */
public class LotteryRecordData{
    // 抽奖记录项目
    public static class LotteryItemData {
        protected LotteryItemData(int id, long timeStamp) {
            this.id = id;
            this.timeStamp = timeStamp;
        }
        public static LotteryItemData fromNbt(CompoundTag tag) {
            return new LotteryItemData(
                    tag.getInt("Result ID"),
                    tag.getLong("Time Stamp")
            );
        }
        /** NBT 化*/
        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Result ID", id);
            tag.putLong("Time Stamp", timeStamp);
            return tag;
        }
        // 抽到的项目 ID
        protected final int id;
        protected final long timeStamp;
    }
    protected LotteryRecordData(int lotteryChance, UUID playerUuid, ArrayList<LotteryItemData> lotteryItems) {
        this.lotteryChance = lotteryChance;
        this.uuid = playerUuid;
        this.lotteryItems = lotteryItems;
    }
    public static LotteryRecordData fromNbt(CompoundTag tag)
    {
        LotteryRecordData data = new LotteryRecordData(
                tag.getInt("Lottery Chance"),
                tag.getUUID("Player UUID"),
                new ArrayList<>()
        );
        // Load History
        ListTag historyTag = tag.getList("Lottery History", Tag.TAG_COMPOUND);
        for(int i = 0; i < historyTag.size(); ++i)
        {
            CompoundTag itemTag = historyTag.getCompound(i);
            data.lotteryItems.add(LotteryItemData.fromNbt(itemTag));
        }
        return data;
    }
    /** NBT 化*/
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Player UUID", uuid);
        tag.putInt("Lottery Chance", lotteryChance);
        // 添加历史记录
        ListTag historyTag = new ListTag();
        for(LotteryItemData item : lotteryItems)
        {
            historyTag.add(item.toNbt());
        }
        tag.put("Lottery History", historyTag);
        return tag;
    }

    protected ArrayList<LotteryItemData> lotteryItems = new ArrayList<>();// 历史记录
    protected UUID uuid;
    protected int lotteryChance = 0;
}
