package org.agmas.noellesroles;

import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public interface ModDataComponentTypes {
   DataComponentType<CompoundTag> COOKED = register("cooked", (tagBuilder) -> {
      return tagBuilder.persistent(CompoundTag.CODEC);
   });
   private static <T> DataComponentType<T> register(String name, @NotNull UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Noellesroles.id(name), builderOperator.apply(DataComponentType.builder()).build());
    }
}
