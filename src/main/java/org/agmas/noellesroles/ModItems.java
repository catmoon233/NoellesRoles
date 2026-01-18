package org.agmas.noellesroles;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.agmas.noellesroles.item.*;

public class ModItems {
    public static void init() {
        //GameConstants.ITEM_COOLDOWNS.put(FAKE_REVOLVER, GameConstants.getInTicks(0,8));
    }

    public static final Item FAKE_KNIFE = register(
            new FakeKnifeItem(new Item.Properties().stacksTo(1)),
            "fake_knife"
    );
    public static final Item FAKE_REVOLVER = register(
            new FakeRevolverItem(new Item.Properties().stacksTo(1).durability(4)),
            "fake_revolver"
    );
    public static final Item MASTER_KEY = register(
            new Item(new Item.Properties().stacksTo(1)),
            "master_key"
    );
    public static final Item MASTER_KEY_P = register(
            new MasterKeyItem(new Item.Properties().stacksTo(1).durability(5)),
            "master_key_p"
    );
    public static final Item DELUSION_VIAL = register(
            new Item(new Item.Properties().stacksTo(1)),
            "delusion_vial"
    );
    public static final Item DEFENSE_VIAL = register(
            new Item(new Item.Properties().stacksTo(1)),
            "defense_vial"
    );
    public static final Item ROLE_MINE = register(
            new Item(new Item.Properties().stacksTo(1)),
            "role_mine"
    );

    /**
     * 阴谋之书页
     * - 阴谋家专属物品
     * - 在商店以250金币购买
     * - 右键使用打开玩家/角色选择GUI
     */
    public static final Item CONSPIRACY_PAGE = register(
            new ConspiracyPageItem(new Item.Properties().stacksTo(1)),
            "conspiracy_page");

    /**
     * 空包弹
     * - 滑头鬼专属物品
     * - 在商店以100金币购买
     * - 右键对目标玩家使用，使其手中枪械进入30秒冷却
     */
    public static final Item BLANK_CARTRIDGE = register(
            new BlankCartridgeItem(new Item.Properties().stacksTo(16)),
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
            new SmokeGrenadeItem(new Item.Properties().stacksTo(8)),
            "smoke_grenade");

    /**
     * 加固门道具
     * - 工程师专属物品
     * - 在商店以75金币购买
     * - 右键门：使门能够防御一次撬棍攻击
     * - 蹲下右键被卡住的门：解除卡住状态
     */
    public static final Item REINFORCEMENT = register(
            new ReinforcementItem(new Item.Properties().stacksTo(16)),
            "reinforcement");

    /**
     * 警报陷阱
     * - 工程师专属物品
     * - 在商店以120金币购买
     * - 右键门：在门上放置警报陷阱
     * - 当撬棍使用时触发，发出响亮的警报声
     */
    public static final Item ALARM_TRAP = register(
            new AlarmTrapItem(new Item.Properties().stacksTo(16)),
            "alarm_trap");

    /**
     * 传递盒
     * - 邮差专属物品
     * - 在商店以350金币购买
     * - 指针对准玩家并右键使用，打开传递界面
     * - 双方可以放入一样物品并交换
     */
    public static final Item DELIVERY_BOX = register(
            new DeliveryBoxItem(new Item.Properties().stacksTo(8)),
            "delivery_box");

    /**
     * 迷幻瓶
     * - 迷幻师专属物品
     * - 在商店购买
     * - 右键使用，制造大量烟雾
     * - 20格范围内玩家视野会随机偏离视角
     * - 迷雾范围：20格
     * - 持续时间：3秒
     * - 触发间隔：1秒
     * - 耐久：2点
     */
    public static final Item HALLUCINATION_BOTTLE = register(
            new HallucinationBottleItem(new Item.Properties().stacksTo(1).durability(2)),
            "hallucination_bottle");

    /**
     * 薄荷糖
     * - 心理学家专属物品
     * - 游戏开始时给予一个
     * - 在商店可以花费100金币购买
     * - 吃掉时恢复0.35的san值（35%）
     */
    public static final Item MINT_CANDIES = register(
            new MintCandiesItem(new Item.Properties().stacksTo(16)),
            "mint_candies");

    /**
     * 锁
     * - 工程师专属物品
     * - 工程师商店购买
     * - 右键门：将门锁上，使用撬锁器时需要解锁，失败后损坏撬锁器
     * - 默认长度为6，如有需要以后可以利用json进行配置
     */
    public static final Item LOCK_ITEM = register(
            new LockItem(6, new Item.Properties().stacksTo(1)),
            "lock");

//    public static final Item SHERIFF_GUN_MAINTENANCE = register(
//            new SheriffGunMaintenanceItem(new Item.Settings().maxCount(1)),
//            "sheriff_gun_maintenance"
//    );
    
    public static Item register(Item item, String id) {
        // Create the identifier for the item.
        ResourceLocation itemID = ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, id);

        // Register the item.
        Item registeredItem = Registry.register(BuiltInRegistries.ITEM, itemID, item);

        // Return the registered item!
        return registeredItem;
    }

}