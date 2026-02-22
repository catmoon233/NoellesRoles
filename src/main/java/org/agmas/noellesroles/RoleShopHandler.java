package org.agmas.noellesroles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.agmas.noellesroles.component.SingerPlayerComponent;
import org.agmas.noellesroles.repack.HSRConstants;
import org.agmas.noellesroles.repack.HSRItems;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.framing.FramingShopEntry;
import org.jetbrains.annotations.NotNull;

import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.game.ShopContent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import dev.doctor4t.trainmurdermystery.util.TMMItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;
import pro.fazeclan.river.stupid_express.constants.SERoles;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.agmas.noellesroles.utils.RoleUtils;

public class RoleShopHandler {
    // ==================== 商店项目列表 ====================
    public static ArrayList<ShopEntry> FRAMING_ROLES_SHOP = new ArrayList<>();
    // ==================== 阴谋家商店 ====================
    public static ArrayList<ShopEntry> CONSPIRATOR_SHOP = new ArrayList<>();
    // ==================== 柜子区商店 ====================
    public static ArrayList<ShopEntry> 柜子区的商店 = new ArrayList<>();
    // ==================== 滑头鬼商店 ====================
    public static ArrayList<ShopEntry> SLIPPERY_GHOST_SHOP = new ArrayList<>();
    // ==================== 工程师商店 ====================
    public static ArrayList<ShopEntry> ENGINEER_SHOP = new ArrayList<>();
    // ==================== 拳击手商店 ====================
    public static ArrayList<ShopEntry> BOXER_SHOP = new ArrayList<>();
    // ==================== 邮差商店 ====================
    public static ArrayList<ShopEntry> POSTMAN_SHOP = new ArrayList<>();
    // ==================== 心理学家商店 ====================
    public static ArrayList<ShopEntry> PSYCHOLOGIST_SHOP = new ArrayList<>();
    // ==================== 炸弹客商店 ====================
    public static ArrayList<ShopEntry> BOMBER_SHOP = new ArrayList<>();
    // ==================== 医生商店 ====================
    public static ArrayList<ShopEntry> DOCTOR_SHOP = new ArrayList<>();
    // ==================== 歌手商店 ====================
    public static ArrayList<ShopEntry> SINGER_SHOP = new ArrayList<>();
    // ==================== 乘务员商店 ====================
    public static ArrayList<ShopEntry> ATTENDANT_SHOP = new ArrayList<>();
    // ==================== 退伍军人商店 ====================
    public static ArrayList<ShopEntry> VETERAN_SHOP = new ArrayList<>();
    // ==================== 巡警商店 ====================
    public static ArrayList<ShopEntry> PATROLLER_SHOP = new ArrayList<>();
    // ==================== 年兽商店 ====================
    public static ArrayList<ShopEntry> NIAN_SHOU_SHOP = new ArrayList<>();
    // ==================== 魔术师商店 ====================
    public static ArrayList<ShopEntry> MAGICIAN_SHOP = new ArrayList<>();

    /**
     * 初始化框架角色商店
     */
    public static void initializeFramingShop() {
        FRAMING_ROLES_SHOP
                .add(new FramingShopEntry(ModItems.MASTER_KEY_P.getDefaultInstance(), 50, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP
                .add(new FramingShopEntry(ModItems.DELUSION_VIAL.getDefaultInstance(), 30, ShopEntry.Type.POISON));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.FIRECRACKER.getDefaultInstance(), 5, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.NOTE.getDefaultInstance(), 5, ShopEntry.Type.TOOL));
    }

    public static void shopRegister() {
        // 初始化框架角色商店
        initializeFramingShop();
        // 初始化其他角色商店
        initShops();
        ShopContent.register();
        {
            // 老人的商店
            var SHOP = new ArrayList<ShopEntry>();

            SHOP.add(new ShopEntry(ModItems.WHEELCHAIR.getDefaultInstance(), 100, ShopEntry.Type.TOOL));
            ShopContent.customEntries.put(ModRoles.OLDMAN.getIdentifier(), SHOP);
        }
        {
            // 死灵法师的商店
            var NECROMANCER_SHOP = new ArrayList<ShopEntry>();

            NECROMANCER_SHOP.add(new ShopEntry(TMMItems.LOCKPICK.getDefaultInstance(), 100, ShopEntry.Type.TOOL));
            ShopContent.customEntries.put(SERoles.NECROMANCER.getIdentifier(), NECROMANCER_SHOP);
        }
        {
            // 指挥官的商店
            var NECROMANCER_SHOP = new ArrayList<ShopEntry>();
            NECROMANCER_SHOP.add(new ShopEntry(TMMItems.LOCKPICK.getDefaultInstance(), 100, ShopEntry.Type.TOOL));
            ShopContent.customEntries.put(ModRoles.COMMANDER_ID, NECROMANCER_SHOP);
        }
        {
            // 游侠商店
            var shopEntries = new ArrayList<ShopEntry>();
            shopEntries.add(new ShopEntry(Items.CROSSBOW.getDefaultInstance(), 300, ShopEntry.Type.WEAPON) {
                @Override
                public boolean onBuy(@NotNull Player player) {
                    int itemCount = TMMItemUtils.hasItem(player, Items.CROSSBOW);
                    if (itemCount > 0)
                        return false;
                    ItemStack item = Items.CROSSBOW.getDefaultInstance();
                    item.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
                    return RoleUtils.insertStackInFreeSlot(player, item);
                }
            });

            final var PoisonArrow = Items.TIPPED_ARROW.getDefaultInstance();
            PoisonArrow.set(DataComponents.ITEM_NAME, Component.translatable("item.poison_arrow.name"));
            PoisonArrow.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.POISON));
            PoisonArrow.set(DataComponents.MAX_STACK_SIZE, 1);
            shopEntries.add(new ShopEntry(PoisonArrow, 100, ShopEntry.Type.WEAPON) {
                @Override
                public boolean onBuy(@NotNull Player player) {
                    int itemCount = TMMItemUtils.hasItem(player, Items.TIPPED_ARROW);
                    if (itemCount >= 2)
                        return false;
                    return RoleUtils.insertStackInFreeSlot(player, PoisonArrow.copy());
                }
            });

            final var SpectralArrow = Items.SPECTRAL_ARROW.getDefaultInstance();
            SpectralArrow.set(DataComponents.MAX_STACK_SIZE, 1);

            shopEntries.add(new ShopEntry(SpectralArrow, 50, ShopEntry.Type.WEAPON) {
                @Override
                public boolean onBuy(@NotNull Player player) {
                    int itemCount = TMMItemUtils.hasItem(player, Items.SPECTRAL_ARROW);
                    if (itemCount >= 2)
                        return false;
                    return RoleUtils.insertStackInFreeSlot(player, SpectralArrow.copy());
                }
            });
            ShopContent.customEntries.put(
                    ModRoles.ELF_ID, shopEntries);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.MANIPULATOR_ID, ShopContent.defaultEntries);
        }
        {
            var SPEED_SPLASH_POITION = Items.SPLASH_POTION.getDefaultInstance();
            var speedPotionList = List.of(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    60 * 20, // 持续时间（tick）
                    2, // 等级（0 = 速度 I）
                    false, // ambient（环境效果，如信标）
                    true, // showParticles（显示粒子）
                    true // showIcon（显示图标）
            ));
            var speedPotionContent = new PotionContents(Optional.empty(), Optional.of(53503), speedPotionList);
            SPEED_SPLASH_POITION.set(DataComponents.POTION_CONTENTS, speedPotionContent);
            var shopEntries = new ArrayList<ShopEntry>();
            shopEntries.add(new ShopEntry(SPEED_SPLASH_POITION, 275, ShopEntry.Type.WEAPON));
            ShopContent.customEntries.put(
                    ModRoles.ATHLETE_ID, shopEntries);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.EXECUTIONER_ID, 柜子区的商店);
        }
        {
            List<ShopEntry> entries = new ArrayList<>(ShopContent.defaultEntries);
            entries.add(new ShopEntry(
                    ModItems.HALLUCINATION_BOTTLE.getDefaultInstance(),
                    120,
                    ShopEntry.Type.TOOL));

            ShopContent.customEntries.put(
                    ModRoles.MORPHLING_ID, entries);
        }
        ShopContent.customEntries.put(
                ModRoles.POISONER_ID, HSRConstants.POISONER_SHOP_ENTRIES);

        ShopContent.customEntries.put(
                ModRoles.SWAPPER_ID, ShopContent.defaultEntries);

        // ShopContent.customEntries.put(
        // POISONER_ID, ShopContent.defaultEntries
        // );
        // ShopContent.customEntries.put(
        // ModRoles.BANDIT_ID, HSRConstants.BANDIT_SHOP_ENTRIES);
        ShopContent.customEntries.put(
                ModRoles.JESTER_ID, FRAMING_ROLES_SHOP);
        {
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(TMMItems.DEFENSE_VIAL.getDefaultInstance(), 200, ShopEntry.Type.POISON));

            ShopContent.customEntries.put(
                    ModRoles.BARTENDER_ID, entries);
        }
        {
            // 大嗓门商店已删除
        }

        // {
        // List<ShopEntry> entries = new ArrayList<>();
        // entries.add(new ShopEntry(ModItems.SHERIFF_GUN_MAINTENANCE.getDefaultStack(),
        // 150, ShopEntry.Type.TOOL));
        //
        // ShopContent.customEntries.put(
        // SHERIFF_ID, entries
        // );
        // }
        {
            List<ShopEntry> entries = new ArrayList<>();
            // 拍立得相机 - 75金币
            if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse("exposure_polaroid:instant_camera"))) {
                var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse("exposure_polaroid:instant_camera"));
                if (item != null) {
                    final var defaultInstance = item.getDefaultInstance();
                    entries.add(new ShopEntry(defaultInstance, 75, ShopEntry.Type.TOOL) {
                        @Override
                        public boolean onBuy(@NotNull Player player) {
                            player.addItem(defaultInstance.copy());
                            return true;
                        }
                    });
                }
            }
            // 拍立得相纸 - 75金币
            if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse("exposure_polaroid:instant_color_slide"))) {
                var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse("exposure_polaroid:instant_color_slide"));
                if (item != null) {
                    final var defaultInstance = item.getDefaultInstance();
                    entries.add(new ShopEntry(defaultInstance, 75, ShopEntry.Type.TOOL) {
                        @Override
                        public boolean onBuy(@NotNull Player player) {
                            player.addItem(defaultInstance.copy());
                            return true;
                        }
                    });
                }
            }

            ShopContent.customEntries.put(
                    ModRoles.PHOTOGRAPHER_ID, entries);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.AWESOME_BINGLUS_ID,
                    List.of(new ShopEntry(TMMItems.NOTE.getDefaultInstance(), 10, ShopEntry.Type.TOOL)));
        }

        {
            ShopContent.customEntries.put(
                    ModRoles.CONSPIRATOR_ID, CONSPIRATOR_SHOP);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.SLIPPERY_GHOST_ID, SLIPPERY_GHOST_SHOP);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.ENGINEER_ID, ENGINEER_SHOP);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.BOXER_ID, BOXER_SHOP);
        }
        {
            var shopEntries = new ArrayList<ShopEntry>();
            shopEntries.add(new ShopEntry(TMMItems.KNIFE.getDefaultInstance(), 200, ShopEntry.Type.TOOL));
            shopEntries.add(new ShopEntry(TMMItems.CROWBAR.getDefaultInstance(), 100, ShopEntry.Type.TOOL));
            shopEntries.add(new ShopEntry(TMMItems.LOCKPICK.getDefaultInstance(), 100, ShopEntry.Type.TOOL));
            shopEntries.add(new ShopEntry(TMMItems.GRENADE.getDefaultInstance(), 500, ShopEntry.Type.TOOL));
            shopEntries.add(new ShopEntry(TMMItems.NOTE.getDefaultInstance(), 15, ShopEntry.Type.TOOL));
            shopEntries.add(new ShopEntry(TMMItems.FIRECRACKER.getDefaultInstance(), 15, ShopEntry.Type.TOOL));
            ShopContent.customEntries.put(
                    ModRoles.CLEANER_ID,
                    shopEntries);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.ADMIRER_ID,
                    List.of(new ShopEntry(ModItems.MASTER_KEY_P.getDefaultInstance(), 150, ShopEntry.Type.TOOL)));
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.POSTMAN_ID, POSTMAN_SHOP);
        }

        ShopContent.customEntries.put(
                ModRoles.STALKER_ID,
                List.of(new ShopEntry(TMMItems.LOCKPICK.getDefaultInstance(), 75, ShopEntry.Type.TOOL)));

        // 心理学家商店
        {
            ShopContent.customEntries.put(
                    ModRoles.PSYCHOLOGIST_ID, PSYCHOLOGIST_SHOP);
        }

        // 乘务员商店
        {
            ShopContent.customEntries.put(
                    ModRoles.ATTENDANT_ID, ATTENDANT_SHOP);
        }

        // 操纵师商店

        {
            ShopContent.customEntries.put(
                    ModRoles.BOMBER_ID, BOMBER_SHOP);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.DOCTOR_ID, DOCTOR_SHOP);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.SINGER_ID, SINGER_SHOP);
        }

        // 退伍军人商店
        {
            VETERAN_SHOP.add(new ShopEntry(
                    TMMItems.KNIFE.getDefaultInstance(),
                    250,
                    ShopEntry.Type.WEAPON));
            ShopContent.customEntries.put(
                    ModRoles.VETERAN_ID, VETERAN_SHOP);
        }

        // 年兽商店
        {
            ShopContent.customEntries.put(
                    ModRoles.NIAN_SHOU_ID, NIAN_SHOU_SHOP);
        }

        // 魔术师商店
        {
            ShopContent.customEntries.put(
                    ModRoles.MAGICIAN_ID, MAGICIAN_SHOP);
        }
        // 风精灵
        {
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(Items.WIND_CHARGE.getDefaultInstance(), 50, ShopEntry.Type.TOOL));
            ShopContent.customEntries.put(
                    ModRoles.WIND_YAOSE_ID, entries);
        }
        // 厨师
        {
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(ModItems.PAN.getDefaultInstance(), 100, ShopEntry.Type.TOOL));
            ShopContent.customEntries.put(
                    ModRoles.CHEF_ID, entries);
        }
        // 警长商店
        {
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(TMMItems.BODY_BAG.getDefaultInstance(), 150, ShopEntry.Type.TOOL));
            ShopContent.customEntries.put(
                    TMMRoles.VIGILANTE.identifier(), entries);
        }
        // 巡警商店
        {
            ShopContent.customEntries.put(
                    ModRoles.PATROLLER_ID, PATROLLER_SHOP);
        }

        // 故障机器人商店
        {
            List<ShopEntry> glitchRobotShop = new ArrayList<>();
            // 夜视仪 - 150金币
            glitchRobotShop
                    .add(new ShopEntry(ModItems.NIGHT_VISION_GLASSES.getDefaultInstance(), 150, ShopEntry.Type.TOOL));
            // 萤石粉 - 50金币（修复夜视仪）
            glitchRobotShop.add(new ShopEntry(Items.GLOWSTONE_DUST.getDefaultInstance(), 50, ShopEntry.Type.TOOL) {
                @Override
                public boolean onBuy(@NotNull Player player) {
                    var head = player.getSlot(103).get();
                    if (head.is(ModItems.NIGHT_VISION_GLASSES)) {
                        int damage = head.getDamageValue();
                        if (damage >= 25) {
                            head.setDamageValue(damage - 25);
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    return true;
                }
            });
            ShopContent.customEntries.put(ModRoles.GLITCH_ROBOT_ID, glitchRobotShop);
        }
    }

    /**
     * 初始化商店
     */
    public static void initShops() {
        柜子区的商店.add(new ShopEntry(
                HSRItems.BANDIT_REVOLVER.getDefaultInstance(),
                130,
                ShopEntry.Type.TOOL));
        柜子区的商店.add(new ShopEntry(TMMItems.FIRECRACKER.getDefaultInstance(), TMMConfig.firecrackerPrice,
                ShopEntry.Type.TOOL));
        柜子区的商店.add(new ShopEntry(TMMItems.LOCKPICK.getDefaultInstance(), 80, ShopEntry.Type.TOOL));
        柜子区的商店.add(new ShopEntry(TMMItems.BODY_BAG.getDefaultInstance(), TMMConfig.bodyBagPrice, ShopEntry.Type.TOOL));
        柜子区的商店.add(new ShopEntry(TMMItems.GRENADE.getDefaultInstance(), TMMConfig.grenadePrice, ShopEntry.Type.TOOL));
        柜子区的商店.add(new ShopEntry(TMMItems.BLACKOUT.getDefaultInstance(), TMMConfig.blackoutPrice, ShopEntry.Type.TOOL) {
            public boolean onBuy(@NotNull Player player) {
                return PlayerShopComponent.useBlackout(player);
            }
        });

        // 阴谋家商店
        CONSPIRATOR_SHOP.add(new ShopEntry(
                ModItems.CONSPIRACY_PAGE.getDefaultInstance(),
                100,
                ShopEntry.Type.TOOL));

        CONSPIRATOR_SHOP.add(new ShopEntry(
                dev.doctor4t.trainmurdermystery.index.TMMItems.KNIFE.getDefaultInstance(),
                120,
                ShopEntry.Type.TOOL));

        CONSPIRATOR_SHOP.add(new ShopEntry(
                dev.doctor4t.trainmurdermystery.index.TMMItems.REVOLVER.getDefaultInstance(),
                200,
                ShopEntry.Type.WEAPON));

        CONSPIRATOR_SHOP.add(new ShopEntry(
                dev.doctor4t.trainmurdermystery.index.TMMItems.LOCKPICK.getDefaultInstance(),
                50,
                ShopEntry.Type.TOOL));

        // 滑头鬼商店
        // 空包弹 - 150金币
        SLIPPERY_GHOST_SHOP.add(new ShopEntry(
                ModItems.BLANK_CARTRIDGE.getDefaultInstance(),
                150,
                ShopEntry.Type.TOOL));

        // 烟雾弹 - 150金币
        SLIPPERY_GHOST_SHOP.add(new ShopEntry(
                ModItems.SMOKE_GRENADE.getDefaultInstance(),
                150,
                ShopEntry.Type.TOOL));

        // 撬锁器 - 50金币 (原版杀手商店物品)
        SLIPPERY_GHOST_SHOP.add(new ShopEntry(
                dev.doctor4t.trainmurdermystery.index.TMMItems.LOCKPICK.getDefaultInstance(),
                50,
                ShopEntry.Type.TOOL));

        // 关灯 - 300金币 (原版杀手商店物品)
        SLIPPERY_GHOST_SHOP.add(
                new ShopEntry(TMMItems.BLACKOUT.getDefaultInstance(), TMMConfig.blackoutPrice, ShopEntry.Type.TOOL) {
                    public boolean onBuy(@NotNull Player player) {
                        return PlayerShopComponent.useBlackout(player);
                    }
                });

        // 工程师商店
        // 加固门 - 30金币
        ENGINEER_SHOP.add(new ShopEntry(
                ModItems.REINFORCEMENT.getDefaultInstance(),
                30,
                ShopEntry.Type.TOOL));

        // 警报陷阱 - 75金币
        ENGINEER_SHOP.add(new ShopEntry(
                ModItems.ALARM_TRAP.getDefaultInstance(),
                75,
                ShopEntry.Type.TOOL));

        ENGINEER_SHOP.add(new ShopEntry(
                ModItems.MASTER_KEY_P.getDefaultInstance(),
                90,
                ShopEntry.Type.TOOL));

        ENGINEER_SHOP.add(new ShopEntry(
                ModItems.LOCK_ITEM.getDefaultInstance(),
                100,
                ShopEntry.Type.TOOL));

        // 年兽商店
        // 关灯 - 200金币
        NIAN_SHOU_SHOP.add(
                new ShopEntry(TMMItems.BLACKOUT.getDefaultInstance(), 200, ShopEntry.Type.TOOL) {
                    public boolean onBuy(@NotNull Player player) {
                        return PlayerShopComponent.useBlackout(player);
                    }
                });

        // 拳击手商店
        BOXER_SHOP.add(new ShopEntry(
                ModItems.BOXING_GLOVE.getDefaultInstance(),
                150,
                ShopEntry.Type.WEAPON));

        // 邮差商店
        // 传递盒 - 250金币
        POSTMAN_SHOP.add(new ShopEntry(
                ModItems.DELIVERY_BOX.getDefaultInstance(),
                150,
                ShopEntry.Type.TOOL));

        // 心理学家商店
        // 薄荷糖 - 100金币
        PSYCHOLOGIST_SHOP.add(new ShopEntry(
                ModItems.MINT_CANDIES.getDefaultInstance(),
                100,
                ShopEntry.Type.TOOL));
        // 炸弹客商店
        BOMBER_SHOP.add(new ShopEntry(
                TMMItems.GRENADE.getDefaultInstance(),
                275,
                ShopEntry.Type.WEAPON));
        BOMBER_SHOP.add(new ShopEntry(
                TMMItems.FIRECRACKER.getDefaultInstance(),
                25,
                ShopEntry.Type.TOOL));

        // 巡警商店
        // 左轮手枪 - 325金币
        PATROLLER_SHOP.add(new ShopEntry(
                TMMItems.REVOLVER.getDefaultInstance(),
                325,
                ShopEntry.Type.WEAPON));
        BOMBER_SHOP.add(new ShopEntry(
                TMMItems.LOCKPICK.getDefaultInstance(),
                80,
                ShopEntry.Type.TOOL));
        // 歌手商店
        for (int i = 1; i <= 4; i++) {
            ItemStack singer_shop_item = ModItems.SINGER_MUSIC_DISC.getDefaultInstance();
            singer_shop_item.set(DataComponents.ITEM_NAME,
                    Component.translatable("item.noellesroles.shop.singer.display_name.root",
                            Component.translatable("item.noellesroles.shop.singer.display_name." + i)
                                    .withStyle(ChatFormatting.GOLD))
                            .withStyle(ChatFormatting.AQUA));
            var lores = new ArrayList<Component>();
            lores.add(Component.translatable("item.noellesroles.shop.singer.lore",
                    Component.translatable("item.noellesroles.shop.singer.effect." + i)
                            .withStyle(ChatFormatting.YELLOW))
                    .withStyle(ChatFormatting.GRAY));
            singer_shop_item.set(DataComponents.LORE, new ItemLore(lores));
            final int idx = i;
            SINGER_SHOP.add(new ShopEntry(singer_shop_item, 100, ShopEntry.Type.TOOL) {
                public boolean onBuy(@NotNull Player player) {
                    return SingerPlayerComponent.buyDisc(player, idx);
                }
            });
        }

        // 医生商店
        DOCTOR_SHOP.add(new ShopEntry(
                ModItems.ANTIDOTE_REAGENT.getDefaultInstance(),
                50,
                ShopEntry.Type.TOOL));
        // 针管 - 75金币
        DOCTOR_SHOP.add(new ShopEntry(
                HSRItems.ANTIDOTE.getDefaultInstance(),
                75,
                ShopEntry.Type.TOOL));

        // 乘务员商店
        // 乘务员钥匙 - 75金币
        ATTENDANT_SHOP.add(new ShopEntry(ModItems.MASTER_KEY_P.getDefaultInstance(), 75, ShopEntry.Type.TOOL) {
            @Override
            public boolean onBuy(@NotNull Player player) {
                player.addItem(ModItems.MASTER_KEY_P.getDefaultInstance().copy());
                return true;
            }
        });
        // 手电筒（moonlight_lamp） - 150金币
        if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse("handheldmoon:moonlight_lamp"))) {
            final var moonlightLampItem = BuiltInRegistries.ITEM
                    .get(ResourceLocation.parse("handheldmoon:moonlight_lamp"));
            if (moonlightLampItem != null) {
                final var defaultInstance = moonlightLampItem.getDefaultInstance();
                ATTENDANT_SHOP.add(new ShopEntry(defaultInstance, 150, ShopEntry.Type.TOOL) {
                    @Override
                    public boolean onBuy(@NotNull Player player) {
                        player.addItem(defaultInstance.copy());
                        return true;
                    }
                });
            }
        }

        // 魔术师商店
        // 假刀 - 50金币
        MAGICIAN_SHOP.add(new ShopEntry(
                ModItems.FAKE_KNIFE.getDefaultInstance(),
                50,
                ShopEntry.Type.WEAPON));

        // 假撬棍 - 35金币
        MAGICIAN_SHOP.add(new ShopEntry(
                ModItems.FAKE_CROWBAR.getDefaultInstance(),
                35,
                ShopEntry.Type.WEAPON));

        // 假开锁器 - 80金币
        MAGICIAN_SHOP.add(new ShopEntry(
                ModItems.FAKE_LOCKPICK.getDefaultInstance(),
                80,
                ShopEntry.Type.WEAPON));

        // 鞭炮 - 30金币
        MAGICIAN_SHOP.add(new ShopEntry(
                TMMItems.FIRECRACKER.getDefaultInstance(),
                30,
                ShopEntry.Type.WEAPON));

        // 假裹尸袋 - 100金币
        MAGICIAN_SHOP.add(new ShopEntry(
                ModItems.FAKE_BODY_BAG.getDefaultInstance(),
                100,
                ShopEntry.Type.WEAPON));

        // 便签 - 100金币
        MAGICIAN_SHOP.add(new ShopEntry(
                TMMItems.NOTE.getDefaultInstance(),
                100,
                ShopEntry.Type.WEAPON));

        // 假枪 - 175金币
        MAGICIAN_SHOP.add(new ShopEntry(
                ModItems.FAKE_REVOLVER.getDefaultInstance(),
                175,
                ShopEntry.Type.WEAPON));

        // 假手雷 - 200金币
        MAGICIAN_SHOP.add(new ShopEntry(
                ModItems.FAKE_GRENADE.getDefaultInstance(),
                200,
                ShopEntry.Type.WEAPON));

        // 假疯狂模式 - 325金币
        MAGICIAN_SHOP.add(new ShopEntry(
                ModItems.FAKE_PSYCHO_MODE.getDefaultInstance(),
                325,
                ShopEntry.Type.WEAPON) {
            @Override
            public boolean onBuy(@NotNull Player player) {
                // 获得假球棒并启动假疯狂模式
                if (!player.addItem(ModItems.FAKE_BAT.getDefaultInstance().copy())) {
                    return false;
                }
                var magicianComponent = org.agmas.noellesroles.component.ModComponents.MAGICIAN.get(player);
                if (magicianComponent != null) {
                    magicianComponent.startFakePsycho();
                }
                return true;
            }
        });
    }
}
