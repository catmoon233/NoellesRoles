package org.agmas.noellesroles.repack;

import dev.doctor4t.trainmurdermystery.index.TMMItems;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.repack.items.AntidoteItem;
import org.agmas.noellesroles.repack.items.BanditRevolverItem;
import org.agmas.noellesroles.repack.items.MasterKeyItem;
import org.agmas.noellesroles.repack.items.ToxinItem;

public class HSRItems {
public static final Item ANTIDOTE = register(new AntidoteItem((new Item.Settings()).maxCount(1)), "antidote");
public static final Item TOXIN = register(new ToxinItem((new Item.Settings()).maxCount(1)), "toxin");
//public static final Item MASTER_KEY = register(new MasterKeyItem((new Item.Settings()).maxCount(1).maxDamage(3)), "master_key");
public static final Item BANDIT_REVOLVER = register(new BanditRevolverItem((new Item.Settings()).maxCount(1)), "bandit_revolver");

public static Item register(Item item, String id) {
    Identifier itemID = Identifier.of(Noellesroles.MOD_ID, id);
    return (Item) Registry.register(Registries.ITEM, itemID, item);
}

public static void init() {
    ItemGroupEvents.modifyEntriesEvent(TMMItems.EQUIPMENT_GROUP).register((ItemGroupEvents.ModifyEntries)(entries) -> {
        entries.add(ANTIDOTE);
        entries.add(TOXIN);
       // entries.add(MASTER_KEY);
    });
}
}
