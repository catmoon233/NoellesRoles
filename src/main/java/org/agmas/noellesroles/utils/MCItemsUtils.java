package org.agmas.noellesroles.utils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import dev.doctor4t.trainmurdermystery.util.TMMItemUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MCItemsUtils extends TMMItemUtils {

    public static List<Item> getItemsByTag(Level level, TagKey<Item> tag) {
        var opt2 = level.registryAccess()
                .registry(Registries.ITEM);
        if (opt2.isEmpty())
            return List.of();
        Optional<HolderSet.Named<Item>> holderSet = opt2.get()
                .getTag(tag);
        if (holderSet.isEmpty())
            return List.of();

        return holderSet.get().stream()
                .map(Holder::value)
                .toList();
    }

    public static @Nullable ItemStack getFirstMatchedItem(Player player, Item item) {
        return getFirstMatchedItem(player, (it) -> it.is(item));
    }

    public static @Nullable ItemStack getFirstMatchedItem(Player player, TagKey<Item> item) {
        return getFirstMatchedItem(player, (it) -> it.is(item));
    }

    public static @Nullable ItemStack getFirstMatchedItem(Player player, Predicate<ItemStack> predicate) {
        for (ItemStack item : player.containerMenu.getItems()) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }
}
