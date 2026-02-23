package org.agmas.noellesroles.init;

import dev.doctor4t.ratatouille.util.registrar.BlockRegistrar;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.block.VendingMachinesBlock;

import static dev.doctor4t.trainmurdermystery.index.TMMBlocks.DARK_STEEL;

public interface ModBlocks {
    public static ResourceKey<CreativeModeTab> BLOCK_CREATIVE_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            Noellesroles.id("block"));
    public static final BlockRegistrar registrar = new BlockRegistrar(Noellesroles.MOD_ID);

    Block TARNISHED_GOLD_STAIRS = registerBlock("vending_machines",
            new VendingMachinesBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STEEL).noOcclusion()));

    static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BLOCK_CREATIVE_GROUP, FabricItemGroup.builder()
                .title(Component.translatable("item_group.noellesroles.block")).icon(() -> {
                    return new ItemStack(TARNISHED_GOLD_STAIRS.asItem());
                }).build());
        registrar.registerEntries();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Block> T registerBlock(String id, T block) {
        return registrar.createWithItem(id, block, BLOCK_CREATIVE_GROUP);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Block> T registerBlock(String id, T block, Item.Properties settings) {
        return registrar.createWithItem(id, block, settings, BLOCK_CREATIVE_GROUP);
    }
}
