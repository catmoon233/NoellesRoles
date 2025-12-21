package org.agmas.noellesroles.repack;

import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HSRConstants {
    public static int toxinPoisonTime = getInTicks(0, 15);
    float banditRevolverDropChance = 0.2F;
    public static Map<Item, Integer> ITEM_COOLDOWNS = new HashMap();
    public static   List<ShopEntry> POISONER_SHOP_ENTRIES = new ArrayList<>();
    public static   List<ShopEntry> BANDIT_SHOP_ENTRIES = new ArrayList<>();



    static {
        // 毒药/100
        POISONER_SHOP_ENTRIES.add(new ShopEntry(HSRItems.TOXIN.getDefaultStack(), 100, ShopEntry.Type.POISON));
        // 毒药瓶/75
        POISONER_SHOP_ENTRIES.add(new ShopEntry(TMMItems.POISON_VIAL.getDefaultStack(), 75, ShopEntry.Type.POISON));
        // 毒蝎子/50
        POISONER_SHOP_ENTRIES.add(new ShopEntry(TMMItems.SCORPION.getDefaultStack(), 50, ShopEntry.Type.POISON));
        // 爆竹/10
        POISONER_SHOP_ENTRIES.add(new ShopEntry(TMMItems.FIRECRACKER.getDefaultStack(), 10, ShopEntry.Type.TOOL));
        // 开锁器/100
        POISONER_SHOP_ENTRIES.add(new ShopEntry(TMMItems.LOCKPICK.getDefaultStack(), 100, ShopEntry.Type.TOOL));
        // 黑暗降临/150
        POISONER_SHOP_ENTRIES.add(new ShopEntry(TMMItems.BLACKOUT.getDefaultStack(), 150, ShopEntry.Type.TOOL) {
            public boolean onBuy(@NotNull PlayerEntity player) {
                return PlayerShopComponent.useBlackout(player);
            }
        });
        POISONER_SHOP_ENTRIES.add(new ShopEntry(new ItemStack(TMMItems.NOTE, 4), 10, ShopEntry.Type.TOOL));



        BANDIT_SHOP_ENTRIES.add(new ShopEntry(HSRItems.BANDIT_REVOLVER.getDefaultStack(), 175, ShopEntry.Type.WEAPON));
        BANDIT_SHOP_ENTRIES.add(new ShopEntry(TMMItems.KNIFE.getDefaultStack(), 250, ShopEntry.Type.WEAPON));
        BANDIT_SHOP_ENTRIES.add(new ShopEntry(TMMItems.GRENADE.getDefaultStack(), 350, ShopEntry.Type.WEAPON));
        BANDIT_SHOP_ENTRIES.add(new ShopEntry(TMMItems.SCORPION.getDefaultStack(), 40, ShopEntry.Type.POISON));
        BANDIT_SHOP_ENTRIES.add(new ShopEntry(TMMItems.CROWBAR.getDefaultStack(), 20, ShopEntry.Type.TOOL));
        BANDIT_SHOP_ENTRIES.add(new ShopEntry(TMMItems.FIRECRACKER.getDefaultStack(), 10, ShopEntry.Type.TOOL));
        BANDIT_SHOP_ENTRIES.add(new ShopEntry(TMMItems.BODY_BAG.getDefaultStack(), 200, ShopEntry.Type.TOOL));
        BANDIT_SHOP_ENTRIES.add(new ShopEntry(TMMItems.BLACKOUT.getDefaultStack(), 200, ShopEntry.Type.TOOL) {
            public boolean onBuy(@NotNull PlayerEntity player) {
                return PlayerShopComponent.useBlackout(player);
            }
        });
        BANDIT_SHOP_ENTRIES.add(new ShopEntry(new ItemStack(TMMItems.NOTE, 4), 10, ShopEntry.Type.TOOL));
    }

    static void init() {
        ITEM_COOLDOWNS.put(HSRItems.ANTIDOTE, getInTicks(2, 0));
        ITEM_COOLDOWNS.put(HSRItems.TOXIN, getInTicks(0, 50));
        ITEM_COOLDOWNS.put(HSRItems.BANDIT_REVOLVER, getInTicks(0, 40));
    }

    static int getInTicks(int minutes, int seconds) {
        return (minutes * 60 + seconds) * 20;
    }
}
