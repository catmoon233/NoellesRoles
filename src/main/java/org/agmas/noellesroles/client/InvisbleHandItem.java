package org.agmas.noellesroles.client;

import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.event.AllowItemShowInHand;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.agmas.noellesroles.ModItems;
import org.agmas.noellesroles.role.ModRoles;
import java.util.Set;

public class InvisbleHandItem {

    private static final Set<Item> HIDDEN_ITEMS = Set.of(
            ModItems.DEFENSE_VIAL,
            ModItems.SMOKE_GRENADE,
            ModItems.BLANK_CARTRIDGE,
            ModItems.ALARM_TRAP,
            ModItems.HALLUCINATION_BOTTLE,
            ModItems.REINFORCEMENT,
            ModItems.CONSPIRACY_PAGE);

    public static void register() {
        AllowItemShowInHand.EVENT.register((player, itemStack) -> {
            GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.level());

            if (gameWorld.isRole(player, ModRoles.VETERAN) && itemStack.is(TMMItems.KNIFE)) {
                return ModItems.SP_KNIFE.getDefaultInstance();
            }

            // 隐藏指定的物品
            if (HIDDEN_ITEMS.contains(itemStack.getItem())) {
                return ItemStack.EMPTY;
            }
            return null; // 不修改
        });

    }
}
