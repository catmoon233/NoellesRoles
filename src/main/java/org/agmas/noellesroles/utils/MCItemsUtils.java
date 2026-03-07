package org.agmas.noellesroles.utils;

import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import dev.doctor4t.trainmurdermystery.util.TMMItemUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MCItemsUtils extends TMMItemUtils {
    public static @Nullable ItemStack getFirstMatchedItem(Player player, Item item) {
        return getFirstMatchedItem(player, (it) -> it.is(item));
    }

    public static @Nullable ItemStack getFirstMatchedItem(Player player, TagKey<Item> item) {
        return getFirstMatchedItem(player, (it) -> it.is(item));
    }

    public static @Nullable ItemStack getFirstMatchedItem(Player player, Predicate<ItemStack> predicate) {
        for (ItemStack item : player.containerMenu.getItems()) {
            if(predicate.test(item)){
                return item;
            }
        }
        return null;
    }
}
