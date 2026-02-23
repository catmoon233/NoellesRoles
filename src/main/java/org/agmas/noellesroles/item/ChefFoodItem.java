package org.agmas.noellesroles.item;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.agmas.noellesroles.ModDataComponentTypes;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

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

    public static void randomModel(ItemStack cooked_food) {
        Random random = new Random();
        int randomI = random.nextInt(1, 5);
        cooked_food.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(randomI));
    }

}
