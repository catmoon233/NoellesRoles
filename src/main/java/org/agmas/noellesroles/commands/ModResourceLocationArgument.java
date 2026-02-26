package org.agmas.noellesroles.commands;

import java.util.Arrays;
import java.util.Collection;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;

public class ModResourceLocationArgument implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

   public ModResourceLocationArgument() {
   }

   public static ResourceLocationArgument id() {
      return new ResourceLocationArgument();
   }

   public static ResourceLocation getId(CommandContext<CommandSourceStack> commandContext, String string) {
      return (ResourceLocation)commandContext.getArgument(string, ResourceLocation.class);
   }

   public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException {
      return ResourceLocation.read(stringReader);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
