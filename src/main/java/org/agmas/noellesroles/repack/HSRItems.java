package org.agmas.noellesroles.repack;

import dev.doctor4t.trainmurdermystery.index.TMMItems;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.repack.items.AntidoteItem;
import org.agmas.noellesroles.repack.items.BanditRevolverItem;
import org.agmas.noellesroles.repack.items.MasterKeyItem;
import org.agmas.noellesroles.repack.items.ToxinItem;

public class HSRItems {
public static final Item ANTIDOTE = register(new AntidoteItem((new Item.Properties()).stacksTo(1)), "antidote");
public static final Item TOXIN = register(new ToxinItem((new Item.Properties()).stacksTo(1)), "toxin");
//public static final Item MASTER_KEY = register(new MasterKeyItem((new Item.Settings()).maxCount(1).maxDamage(3)), "master_key");
public static final Item BANDIT_REVOLVER = register(new BanditRevolverItem((new Item.Properties()).stacksTo(1)), "bandit_revolver");

public static Item register(Item item, String id) {
    ResourceLocation itemID = ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, id);
    return (Item) Registry.register(BuiltInRegistries.ITEM, itemID, item);
}

public static void init() {
    ItemGroupEvents.modifyEntriesEvent(TMMItems.EQUIPMENT_GROUP).register((ItemGroupEvents.ModifyEntries)(entries) -> {
        entries.accept(ANTIDOTE);
        entries.accept(TOXIN);
       // entries.add(MASTER_KEY);
    });
}
}
