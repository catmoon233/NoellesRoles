package org.agmas.noellesroles.init;

import dev.doctor4t.ratatouille.util.registrar.BlockRegistrar;
import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.block.BranchBlock;
import dev.doctor4t.trainmurdermystery.block.SecurityMonitorBlock;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.block.VendingMachines;

import static dev.doctor4t.trainmurdermystery.index.TMMBlocks.DARK_STEEL;

public interface ModBlocks {
    BlockRegistrar registrar = new BlockRegistrar(Noellesroles.MOD_ID);
    Block TARNISHED_GOLD_STAIRS = registrar.createWithItem("vending_machines", new SecurityMonitorBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STEEL).noOcclusion()), TMMItems.DECORATION_GROUP);

    static void initialize() {

        registrar.registerEntries();


    }
}
