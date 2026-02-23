package org.agmas.noellesroles;

import com.mojang.serialization.Codec;
import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.NotNull;

public interface ModDataComponentTypes {
   DataComponentType<String> COOKED = register("cooked", (stringBuilder) -> {
      return stringBuilder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8);
   });
   private static <T> DataComponentType<T> register(String name, @NotNull UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Noellesroles.id(name), builderOperator.apply(DataComponentType.builder()).build());
    }
}
