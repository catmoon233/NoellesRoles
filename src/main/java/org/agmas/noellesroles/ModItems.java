package org.agmas.noellesroles;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.item.*;

public class ModItems {
    public static void init() {
        //GameConstants.ITEM_COOLDOWNS.put(FAKE_REVOLVER, GameConstants.getInTicks(0,8));
    }

    public static final Item FAKE_KNIFE = register(
            new FakeKnifeItem(new Item.Settings().maxCount(1)),
            "fake_knife"
    );
    public static final Item FAKE_REVOLVER = register(
            new FakeRevolverItem(new Item.Settings().maxCount(1).maxDamage(4)),
            "fake_revolver"
    );
    public static final Item MASTER_KEY = register(
            new Item(new Item.Settings().maxCount(1)),
            "master_key"
    );
    public static final Item MASTER_KEY_P = register(
            new MasterKeyItem(new Item.Settings().maxCount(1).maxDamage(5)),
            "master_key_p"
    );
    public static final Item DELUSION_VIAL = register(
            new Item(new Item.Settings().maxCount(1)),
            "delusion_vial"
    );
    public static final Item DEFENSE_VIAL = register(
            new Item(new Item.Settings().maxCount(1)),
            "defense_vial"
    );
    public static final Item ROLE_MINE = register(
            new Item(new Item.Settings().maxCount(1)),
            "role_mine"
    );

    /**
     * 阴谋之书页
     * - 阴谋家专属物品
     * - 在商店以250金币购买
     * - 右键使用打开玩家/角色选择GUI
     */
    public static final Item CONSPIRACY_PAGE = register(
            new ConspiracyPageItem(new Item.Settings().maxCount(1)),
            "conspiracy_page");

    /**
     * 空包弹
     * - 滑头鬼专属物品
     * - 在商店以100金币购买
     * - 右键对目标玩家使用，使其手中枪械进入30秒冷却
     */
    public static final Item BLANK_CARTRIDGE = register(
            new BlankCartridgeItem(new Item.Settings().maxCount(16)),
            "blank_cartridge");

    /**
     * 烟雾弹
     * - 滑头鬼专属物品
     * - 在商店以300金币购买
     * - 右键投掷，形成烟雾区域
     * - 进入烟雾的玩家获得失明效果
     * - 直接命中玩家时清空目标san值
     */
    public static final Item SMOKE_GRENADE = register(
            new SmokeGrenadeItem(new Item.Settings().maxCount(8)),
            "smoke_grenade");

    /**
     * 加固门道具
     * - 工程师专属物品
     * - 在商店以75金币购买
     * - 右键门：使门能够防御一次撬棍攻击
     * - 蹲下右键被卡住的门：解除卡住状态
     */
    public static final Item REINFORCEMENT = register(
            new ReinforcementItem(new Item.Settings().maxCount(16)),
            "reinforcement");

    /**
     * 警报陷阱
     * - 工程师专属物品
     * - 在商店以120金币购买
     * - 右键门：在门上放置警报陷阱
     * - 当撬棍使用时触发，发出响亮的警报声
     */
    public static final Item ALARM_TRAP = register(
            new AlarmTrapItem(new Item.Settings().maxCount(16)),
            "alarm_trap");

    /**
     * 传递盒
     * - 邮差专属物品
     * - 在商店以350金币购买
     * - 指针对准玩家并右键使用，打开传递界面
     * - 双方可以放入一样物品并交换
     */
    public static final Item DELIVERY_BOX = register(
            new DeliveryBoxItem(new Item.Settings().maxCount(8)),
            "delivery_box");


//    public static final Item SHERIFF_GUN_MAINTENANCE = register(
//            new SheriffGunMaintenanceItem(new Item.Settings().maxCount(1)),
//            "sheriff_gun_maintenance"
//    );
    
    public static Item register(Item item, String id) {
        // Create the identifier for the item.
        Identifier itemID = Identifier.of(Noellesroles.MOD_ID, id);

        // Register the item.
        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

        // Return the registered item!
        return registeredItem;
    }

}