package org.agmas.noellesroles.item;

import java.util.List;
import java.util.Optional;

import org.agmas.noellesroles.ModDataComponentTypes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class ChefFoodItem extends Item {
    private static Properties __warp_init(Properties properties) {
        var tag = new CompoundTag();
        var listTag = new ListTag();
        tag.put("effects", listTag);
        properties.component(ModDataComponentTypes.COOKED, tag);
        return properties;
    }

    public ChefFoodItem(Properties properties) {
        super(__warp_init(properties.food(new FoodProperties(10, 10, true, 1, Optional.empty(), List.of()))));
    }

}
