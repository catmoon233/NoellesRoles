package org.agmas.noellesroles;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.TMMConfig;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPsychoComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.event.AllowPlayerDeath;
import dev.doctor4t.trainmurdermystery.event.CanSeePoison;
import dev.doctor4t.trainmurdermystery.event.ShouldDropOnDeath;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.ShopContent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.component.*;
import org.agmas.noellesroles.entity.PuppeteerBodyEntity;
import org.agmas.noellesroles.repack.BanditRevolverShootPayload;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.commands.AdminFreeCamCommand;
import org.agmas.noellesroles.commands.BroadcastCommand;
import org.agmas.noellesroles.commands.ConfigCommand;
import org.agmas.noellesroles.commands.SetRoleMaxCommand;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.roles.framing.FramingShopEntry;
import org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.roles.noise_maker.NoiseMakerPlayerComponent;
import org.agmas.noellesroles.packet.*;
import org.agmas.noellesroles.roles.recaller.RecallerPlayerComponent;
import org.agmas.noellesroles.repack.HSRConstants;
import org.agmas.noellesroles.repack.HSRItems;
import org.agmas.noellesroles.repack.HSRSounds;
import org.agmas.noellesroles.roles.voodoo.VoodooPlayerComponent;
import org.agmas.noellesroles.roles.vulture.VulturePlayerComponent;
import org.agmas.noellesroles.utils.RoleUtils;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import org.agmas.noellesroles.entity.HallucinationAreaManager;
import org.agmas.noellesroles.entity.SmokeAreaManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

import static org.agmas.noellesroles.RicesRoleRhapsody.findAttackerWithWeapon;

public class Noellesroles implements ModInitializer {
    // public static Style RESETSTYLE = Style.EMPTY;

    public static String MOD_ID = "noellesroles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // public static Role SHERIFF = TMMRoles.registerRole(new Role(SHERIFF_ID, new
    // Color(0, 0, 255).getRGB(),true,false, Role.MoodType.REAL,
    // TMMRoles.VIGILANTE.getMaxSprintTime(),false));

    // ==================== 网络包ID定义 ====================
    public static final CustomPacketPayload.Type<MorphC2SPacket> MORPH_PACKET = MorphC2SPacket.ID;
    public static final CustomPacketPayload.Type<SwapperC2SPacket> SWAP_PACKET = SwapperC2SPacket.ID;
    public static final CustomPacketPayload.Type<AbilityC2SPacket> ABILITY_PACKET = AbilityC2SPacket.ID;
    public static final CustomPacketPayload.Type<OpenIntroPayload> OPEN_INTRO_PACKET = OpenIntroPayload.ID;
    public static final CustomPacketPayload.Type<VultureEatC2SPacket> VULTURE_PACKET = VultureEatC2SPacket.ID;
    public static final CustomPacketPayload.Type<ThiefStealC2SPacket> THIEF_PACKET = ThiefStealC2SPacket.ID;
    public static final CustomPacketPayload.Type<ManipulatorC2SPacket> MANIPULATOR_PACKET = ManipulatorC2SPacket.ID;
    public static final ArrayList<Role> VANNILA_ROLES = new ArrayList<>();
    public static final ArrayList<ResourceLocation> VANNILA_ROLE_IDS = new ArrayList<>();
    public static final CustomPacketPayload.Type<ExecutionerSelectTargetC2SPacket> EXECUTIONER_SELECT_TARGET_PACKET = ExecutionerSelectTargetC2SPacket.ID;
    public static final CustomPacketPayload.Type<InsaneKillerAbilityC2SPacket> INSANE_KILLER_ABILITY_PACKET = InsaneKillerAbilityC2SPacket.ID;
    public static final CustomPacketPayload.Type<RecorderC2SPacket> RECORDER_PACKET = RecorderC2SPacket.TYPE;
    public static final CustomPacketPayload.Type<MonitorMarkC2SPacket> MONITOR_MARK_PACKET = MonitorMarkC2SPacket.ID;

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

    private static boolean gunsCooled = false;
    // ==================== 初始物品配置 ====================
    public static final Map<Role, List<Supplier<ItemStack>>> INITIAL_ITEMS_MAP = new HashMap<>();

    public static List<Role> getEnableRoles() {
        ArrayList<Role> clone = new ArrayList<>(TMMRoles.ROLES.values());
        clone.removeIf(
                r -> {
                    if (HarpyModLoaderConfig.HANDLER.instance().disabled.contains(r.getIdentifier().toString()))
                        return true;
                    if (String.valueOf(r.identifier()).equals("trainmurdermystery:discovery_civilian"))
                        return true;
                    if (String.valueOf(r.identifier()).equals("trainmurdermystery:loose_end"))
                        return true;
                    return false;
                });
        return clone;
    }

    public static List<Role> getEnableKillerRoles() {
        ArrayList<Role> clone = new ArrayList<>(TMMRoles.ROLES.values());
        clone.removeIf(
                r -> !r.canUseKiller()
                        || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(r.getIdentifier().toString()));
        return clone;
    }

    public static @NotNull ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        HSRConstants.init();

        // 初始化模组角色列表
        ModRoles.init();
        // 初始化初始物品映射
        initializeInitialItems();

        // 初始化原版角色列表
        initializeVanillaRoles();

        // 初始化框架角色商店
        initializeFramingShop();

        // 加载配置
        NoellesRolesConfig.HANDLER.load();
        RicesRoleRhapsody.onInitialize1();

        // 初始化系统组件
        NRSounds.initialize();
        registerMaxRoleCount();

        // 注册事件处理器
        registerEvents();

        // 注册命令
        BroadcastCommand.register();
        SetRoleMaxCommand.register();
        ConfigCommand.register();

        // 注册网络包类型
        registerPackets1();

        // 注册网络处理器
        registerPackets();

        // 初始化HSR组件
        HSRItems.init();
        HSRSounds.init();

        // 设置角色最大数量
        Harpymodloader.setRoleMaximum(ModRoles.POISONER_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.DOCTOR_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.ATTENDANT_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.CORONER_ID, 1);

        // 注册商店
        shopRegister();
        TMM.canUseChatHud.add((role -> role.getIdentifier()
                .equals(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID)));
        TMM.canUseOtherPerson.add((role -> role.getIdentifier()
                .equals(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID)));
        TMM.canUseOtherPerson.add((role -> role.getIdentifier()
                .equals(ModRoles.MANIPULATOR_ID)));
        TMM.canCollide.add(a->{
            final var gameWorldComponent = GameWorldComponent.KEY.get(a.level());
            if (gameWorldComponent.isRole(a, ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)){
                if (InsaneKillerPlayerComponent.KEY.get( a).isActive){
                    return true;
                }
            }
            return false;
        });
        TMM.canCollide.add(a->{
            final var gameWorldComponent = GameWorldComponent.KEY.get(a.level());
            if (gameWorldComponent.isRole(a, ModRoles.GHOST)){
                if (a.hasEffect(MobEffects.INVISIBILITY)){
                    return true;
                }
            }
            return false;
        });
        TMM.canCollideEntity.add(entity->{
            return entity instanceof PuppeteerBodyEntity;
        });

        //同时出现
        Harpymodloader.Occupations_Roles.put(ModRoles.POISONER, ModRoles.DOCTOR);
    }

    /**
     * 初始化原版角色列表
     */
    private void initializeVanillaRoles() {
        VANNILA_ROLES.add(TMMRoles.KILLER);
        VANNILA_ROLES.add(TMMRoles.VIGILANTE);
        VANNILA_ROLES.add(TMMRoles.CIVILIAN);
        VANNILA_ROLES.add(TMMRoles.LOOSE_END);

        VANNILA_ROLE_IDS.add(TMMRoles.LOOSE_END.identifier());
        VANNILA_ROLE_IDS.add(TMMRoles.VIGILANTE.identifier());
        VANNILA_ROLE_IDS.add(TMMRoles.CIVILIAN.identifier());
        VANNILA_ROLE_IDS.add(TMMRoles.KILLER.identifier());
    }

    /**
     * 初始化框架角色商店
     */
    private void initializeFramingShop() {
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.LOCKPICK.getDefaultInstance(), 50, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP
                .add(new FramingShopEntry(ModItems.DELUSION_VIAL.getDefaultInstance(), 30, ShopEntry.Type.POISON));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.FIRECRACKER.getDefaultInstance(), 5, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.NOTE.getDefaultInstance(), 5, ShopEntry.Type.TOOL));
    }

    /**
     * 初始化初始物品映射
     */
    private void initializeInitialItems() {

        INITIAL_ITEMS_MAP.clear();
        // 故障机器人初始物品（无开局物品）
        INITIAL_ITEMS_MAP.put(ModRoles.GLITCH_ROBOT, new ArrayList<>());

        // 医生初始物品（不再有针管和解药）
        List<Supplier<ItemStack>> doctorItems = new ArrayList<>();
        doctorItems.add(() -> ModItems.DEFIBRILLATOR.getDefaultInstance());
        INITIAL_ITEMS_MAP.put(ModRoles.DOCTOR, doctorItems);

        // // 强盗初始物品
        // List<Supplier<ItemStack>> banditItems = new ArrayList<>();
        // banditItems.add(() -> HSRItems.BANDIT_REVOLVER.getDefaultInstance());
        // banditItems.add(() -> TMMItems.CROWBAR.getDefaultInstance());
        // // INITIAL_ITEMS_MAP.put(ModRoles.BANDIT, banditItems);

        // 乘务员初始物品
        List<Supplier<ItemStack>> attendantItems = new ArrayList<>();
        // 乘务员钥匙
        attendantItems.add(() -> ModItems.MASTER_KEY_P.getDefaultInstance());
        // 使用延迟加载方式添加 handheldmoon 模组的物品（如果可用）
        attendantItems.add(() -> {
            final var moonlightLampItem = BuiltInRegistries.ITEM
                    .get(ResourceLocation.tryParse("handheldmoon:moonlight_lamp"));
            if (moonlightLampItem != Items.AIR) {
                return moonlightLampItem.getDefaultInstance();
            }
            return null; // 如果物品不存在，返回null
        });
        INITIAL_ITEMS_MAP.put(ModRoles.ATTENDANT, attendantItems);

        // 心理学家初始物品（不再有薄荷糖）
        List<Supplier<ItemStack>> psychologistItems = new ArrayList<>();
        INITIAL_ITEMS_MAP.put(ModRoles.PSYCHOLOGIST, psychologistItems);

        // 记录员初始物品
        List<Supplier<ItemStack>> recorderItems = new ArrayList<>();
        recorderItems.add(() -> ModItems.WRITTEN_NOTE.getDefaultInstance());
        INITIAL_ITEMS_MAP.put(ModRoles.RECORDER, recorderItems);

        // 小丑初始物品
        List<Supplier<ItemStack>> jesterItems = new ArrayList<>();
        jesterItems.add(() -> ModItems.FAKE_KNIFE.getDefaultInstance());
        jesterItems.add(() -> ModItems.FAKE_REVOLVER.getDefaultInstance());
        INITIAL_ITEMS_MAP.put(ModRoles.JESTER, jesterItems);

        // 列车长初始物品
        List<Supplier<ItemStack>> conductorItems = new ArrayList<>();
        conductorItems.add(() -> ModItems.MASTER_KEY.getDefaultInstance());
        conductorItems.add(() -> Items.SPYGLASS.getDefaultInstance());
        INITIAL_ITEMS_MAP.put(ModRoles.CONDUCTOR, conductorItems);

        // Awesome Binglus 初始物品
        List<Supplier<ItemStack>> awesomeBinglusItems = new ArrayList<>();
        // 添加16个便签
        for (int i = 0; i < 4; i++) {
            awesomeBinglusItems.add(() -> TMMItems.NOTE.getDefaultInstance());
        }
        INITIAL_ITEMS_MAP.put(ModRoles.AWESOME_BINGLUS, awesomeBinglusItems);

    }

    /**
     * 为玩家添加指定角色的初始物品
     * 
     * @param player 玩家
     * @param role   角色
     */
    public static void addInitialItemsForRole(Player player, Role role) {
        List<Supplier<ItemStack>> itemSuppliers = INITIAL_ITEMS_MAP.get(role);
        if (itemSuppliers != null) {

            for (Supplier<ItemStack> itemSupplier : itemSuppliers) {
                ItemStack itemStack = itemSupplier.get();
                if (itemStack != null && !itemStack.isEmpty()) {
                    player.addItem(itemStack.copy());
                }
            }
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
        柜子区的商店.add(new ShopEntry(ModItems.MASTER_KEY_P.getDefaultInstance(), 60, ShopEntry.Type.TOOL));
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
                175,
                ShopEntry.Type.TOOL));

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
    }

    private void shopRegister() {
        initShops();
        ShopContent.register();
        {
            ShopContent.customEntries.put(
                    ModRoles.MANIPULATOR_ID, ShopContent.defaultEntries);
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.EXECUTIONER_ID, 柜子区的商店);
        }
        {
            List<ShopEntry> entries = new ArrayList<>(ShopContent.defaultEntries);
            entries.add(new ShopEntry(
                    ModItems.HALLUCINATION_BOTTLE.getDefaultInstance(),
                    175,
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
                ModRoles.JESTER_ID, Noellesroles.FRAMING_ROLES_SHOP);
        {
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(ModItems.DEFENSE_VIAL.getDefaultInstance(), 200, ShopEntry.Type.POISON));

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
            // 立拍得相纸 - 25金币
            if (BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse("exposure_polaroid:instant_color_slide"))) {
                var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse("exposure_polaroid:instant_color_slide"));
                if (item != null) {
                    final var defaultInstance = item.getDefaultInstance();
                    entries.add(new ShopEntry(defaultInstance, 25, ShopEntry.Type.TOOL) {
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
                    ModRoles.AWESOME_BINGLUS_ID, List.of(new ShopEntry(TMMItems.NOTE.getDefaultInstance(), 10, ShopEntry.Type.TOOL)));
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
        // 故障机器人商店
        {
            List<ShopEntry> glitchRobotShop = new ArrayList<>();
            // 夜视仪 - 150金币
            glitchRobotShop.add(new ShopEntry(ModItems.NIGHT_VISION_GLASSES.getDefaultInstance(), 150, ShopEntry.Type.TOOL));
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

    public static void registerPackets1() {
        PayloadTypeRegistry.playC2S().register(ExecutionerSelectTargetC2SPacket.ID,
                ExecutionerSelectTargetC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(BroadcasterC2SPacket.ID, BroadcasterC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BroadcastMessageS2CPacket.ID, BroadcastMessageS2CPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(PlayerResetS2CPacket.ID, PlayerResetS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(PlayerResetS2CPacket.ID, PlayerResetS2CPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(GamblerSelectRoleC2SPacket.ID, GamblerSelectRoleC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(GamblerSelectRoleC2SPacket.ID, GamblerSelectRoleC2SPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(MorphC2SPacket.ID, MorphC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenIntroPayload.ID, OpenIntroPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenIntroPayload.ID, OpenIntroPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AbilityC2SPacket.ID, AbilityC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SwapperC2SPacket.ID, SwapperC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(VultureEatC2SPacket.ID, VultureEatC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(ManipulatorC2SPacket.ID, ManipulatorC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenLockGuiC2SPacket.ID, OpenLockGuiC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenLockGuiC2SPacket.ID, OpenLockGuiC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(BanditRevolverShootPayload.ID,
                BanditRevolverShootPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(BanditRevolverShootPayload.ID,
                new BanditRevolverShootPayload.Receiver());
        PayloadTypeRegistry.playC2S().register(InsaneKillerAbilityC2SPacket.ID, InsaneKillerAbilityC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RecorderC2SPacket.TYPE, RecorderC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(MonitorMarkC2SPacket.ID, MonitorMarkC2SPacket.CODEC);
    }

    private void registerMaxRoleCount() {
        Harpymodloader.setRoleMaximum(ModRoles.CONDUCTOR_ID, NoellesRolesConfig.HANDLER.instance().conductorMax);
        Harpymodloader.setRoleMaximum(ModRoles.MANIPULATOR, 0);
        Harpymodloader.setRoleMaximum(ModRoles.EXECUTIONER_ID, NoellesRolesConfig.HANDLER.instance().executionerMax);
        Harpymodloader.setRoleMaximum(ModRoles.VULTURE_ID, NoellesRolesConfig.HANDLER.instance().vultureMax);
        Harpymodloader.setRoleMaximum(ModRoles.JESTER_ID, NoellesRolesConfig.HANDLER.instance().jesterMax);
        Harpymodloader.setRoleMaximum(ModRoles.MORPHLING_ID, NoellesRolesConfig.HANDLER.instance().morphlingMax);
        Harpymodloader.setRoleMaximum(ModRoles.BARTENDER_ID, NoellesRolesConfig.HANDLER.instance().bartenderMax);
        Harpymodloader.setRoleMaximum(ModRoles.NOISEMAKER_ID, NoellesRolesConfig.HANDLER.instance().noisemakerMax);
        Harpymodloader.setRoleMaximum(ModRoles.PHANTOM_ID, NoellesRolesConfig.HANDLER.instance().phantomMax);
        Harpymodloader.setRoleMaximum(ModRoles.AWESOME_BINGLUS_ID,
                NoellesRolesConfig.HANDLER.instance().awesomeBinglusMax);
        Harpymodloader.setRoleMaximum(ModRoles.SWAPPER_ID, NoellesRolesConfig.HANDLER.instance().swapperMax);
        Harpymodloader.setRoleMaximum(ModRoles.VOODOO_ID, NoellesRolesConfig.HANDLER.instance().voodooMax);
        Harpymodloader.setRoleMaximum(ModRoles.CORONER_ID, NoellesRolesConfig.HANDLER.instance().coronerMax);
        Harpymodloader.setRoleMaximum(ModRoles.RECALLER_ID, NoellesRolesConfig.HANDLER.instance().recallerMax);
        Harpymodloader.setRoleMaximum(ModRoles.BROADCASTER_ID, NoellesRolesConfig.HANDLER.instance().broadcasterMax);
        Harpymodloader.setRoleMaximum(ModRoles.GAMBLER_ID, NoellesRolesConfig.HANDLER.instance().gamblerMax);
        Harpymodloader.setRoleMaximum(ModRoles.GLITCH_ROBOT_ID, NoellesRolesConfig.HANDLER.instance().glitchRobotMax);
        Harpymodloader.setRoleMaximum(ModRoles.GHOST_ID, NoellesRolesConfig.HANDLER.instance().ghostMax);
        // Harpymodloader.setRoleMaximum(ModRoles.THIEF_ID, 0);
        Harpymodloader.setRoleMaximum(ModRoles.SHERIFF_ID, NoellesRolesConfig.HANDLER.instance().sheriffMax);
        Harpymodloader.setRoleMaximum(ModRoles.BOMBER_ID, 1);
    }

    public void registerEvents() {
        ShouldDropOnDeath.EVENT.register(((itemStack) -> {
            final var key = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
            if ("exposure:album".equals(key) || "exposure:photograph".equals(key)) {
                return true;
            }

            return false;
        }));

        AllowPlayerDeath.EVENT.register(((playerEntity, identifier) -> {
            if (identifier == GameConstants.DeathReasons.FELL_OUT_OF_TRAIN)
                return true;
            if (identifier.getPath().equals("disconnected"))
                return true;
            if (identifier.getPath().equals("ignited"))
                return true;
            if (identifier.getPath().equals("failed_ignite"))
                return true;
            if (identifier.getPath().equals("heart_attack"))
                return true;
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(playerEntity.level());
            if (gameWorldComponent.isRole(playerEntity, ModRoles.JESTER)) {
                PlayerPsychoComponent component = PlayerPsychoComponent.KEY.get(playerEntity);
                if (component.getPsychoTicks() > GameConstants.getInTicks(0, 44)) {
                    return false;
                }
            }
            BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(playerEntity);
            if (bartenderPlayerComponent.getArmor() > 0) {

                playerEntity.level().playSound(playerEntity, playerEntity.blockPosition(),
                        TMMSounds.ITEM_PSYCHO_ARMOUR, SoundSource.MASTER, 5.0F, 1.0F);
                bartenderPlayerComponent.removeArmor();
                return false;
            }
            if (gameWorldComponent.isRole(playerEntity,
                    ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                final var insaneKillerPlayerComponent = InsaneKillerPlayerComponent.KEY.get(playerEntity);
                insaneKillerPlayerComponent.reset();
                insaneKillerPlayerComponent.sync();
            }
            if (gameWorldComponent.isRole(playerEntity, ModRoles.BETTER_VIGILANTE)) {
                final var betterVigilantePlayerComponent = BetterVigilantePlayerComponent.KEY.get(playerEntity);
                betterVigilantePlayerComponent.reset();
                betterVigilantePlayerComponent.sync();
            }
            return true;
        }));
        CanSeePoison.EVENT.register((player) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.level());
            if (gameWorldComponent.isRole((Player) player, ModRoles.BARTENDER)) {
                return true;
            }
            if (gameWorldComponent.isRole((Player) player, ModRoles.POISONER)) {
                return true;
            }
            return false;
        });
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            if (server.getPlayerCount() >= 10) {
                Harpymodloader.setRoleMaximum(ModRoles.RECORDER, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.RECORDER, 0);
            }
            if (server.getPlayerCount() > 24) {
                Harpymodloader.setRoleMaximum(ModRoles.PATROLLER, 2);
            } else if (server.getPlayerCount() > 10) {
                Harpymodloader.setRoleMaximum(ModRoles.PATROLLER, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.PATROLLER, 0);
            }
        }));
        ModdedRoleAssigned.EVENT.register((player, role) -> {
            if (role.identifier().equals(TMMRoles.KILLER.identifier())) {
                player.addItem(TMMItems.KNIFE.getDefaultInstance().copy());
                return;
            }
            if (role.identifier().equals(TMMRoles.VIGILANTE.identifier())) {
                player.addItem(TMMItems.REVOLVER.getDefaultInstance().copy());
                return;
            }
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY
                    .get(player);
            abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;

            if (role.equals(ModRoles.BROADCASTER)) {
                abilityPlayerComponent.cooldown = 0;
                PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(player);
                playerShopComponent.setBalance(200);
                playerShopComponent.sync();
            } else {
                abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;
            }
            if (role.equals(ModRoles.EXECUTIONER)) {
                ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY
                        .get(player);
                executionerPlayerComponent.won = false;
                PlayerShopComponent playerShopComponent = (PlayerShopComponent) PlayerShopComponent.KEY.get(player);
                executionerPlayerComponent.reset();
                playerShopComponent.setBalance(100);
                executionerPlayerComponent.sync();
            }
            if (role.equals(ModRoles.VULTURE)) {
                if (VulturePlayerComponent.KEY.isProvidedBy(player)) {
                    VulturePlayerComponent vulturePlayerComponent = VulturePlayerComponent.KEY.get(player);
                    vulturePlayerComponent.reset();
                    vulturePlayerComponent.bodiesRequired = (int) ((player.level().players().size() / 3f)
                            - Math.floor(player.level().players().size() / 6f));
                    vulturePlayerComponent.sync();
                }
            }
            if (role.equals(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                final var insaneKillerPlayerComponent = InsaneKillerPlayerComponent.KEY.get(player);
                insaneKillerPlayerComponent.reset();
                insaneKillerPlayerComponent.sync();
            }
            if (role.equals(ModRoles.RECORDER)) {
                final var recorderPlayerComponent = RecorderPlayerComponent.KEY.get(player);
                recorderPlayerComponent.initializeRoles();
            }

            // 更新所有记录员的可用角色列表
            for (ServerPlayer p : player.getServer().getPlayerList().getPlayers()) {
                if (GameWorldComponent.KEY.get(p.level()).isRole(p, ModRoles.RECORDER)) {
                    RecorderPlayerComponent.KEY.get(p).updateAvailableRoles();
                }
            }
            if (role.equals(ModRoles.RECORDER)) {
                final var recorderPlayerComponent = RecorderPlayerComponent.KEY.get(player);
                recorderPlayerComponent.reset();
                recorderPlayerComponent.sync();
            }
            // 使用映射表添加初始物品
            addInitialItemsForRole(player, role);

            if (role.equals(ModRoles.GAMBLER)) {
                org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent gamblerPlayerComponent = org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent.KEY
                        .get(player);
                gamblerPlayerComponent.reset();
                gamblerPlayerComponent.sync();
            }

            if (role.equals(ModRoles.NOISEMAKER)) {
                org.agmas.noellesroles.roles.noise_maker.NoiseMakerPlayerComponent noiseMakerPlayerComponent = org.agmas.noellesroles.roles.noise_maker.NoiseMakerPlayerComponent.KEY
                        .get(player);
                noiseMakerPlayerComponent.reset();
                noiseMakerPlayerComponent.sync();
            }
            if (role.equals(ModRoles.GHOST)) {
                org.agmas.noellesroles.roles.ghost.GhostPlayerComponent ghostPlayerComponent = org.agmas.noellesroles.roles.ghost.GhostPlayerComponent.KEY
                        .get(player);
                ghostPlayerComponent.reset();
                ghostPlayerComponent.sync();
            }
            // 操纵师角色初始化
            if (role.equals(ModRoles.MANIPULATOR)) {
                ManipulatorPlayerComponent manipulatorPlayerComponent = ManipulatorPlayerComponent.KEY.get(player);
                manipulatorPlayerComponent.reset();
                manipulatorPlayerComponent.sync();
            }
            if (role.equals(ModRoles.BOMBER)) {
                BomberPlayerComponent bomberPlayerComponent = ModComponents.BOMBER.get(player);
                if (role.equals(ModRoles.MONITOR)) {
                    MonitorPlayerComponent monitorComponent = MonitorPlayerComponent.KEY.get(player);
                    monitorComponent.reset();
                    monitorComponent.sync();
                }
                // bomberPlayerComponent.reset(); // 如果有 reset 方法
                ModComponents.BOMBER.sync(player);
            }
            // if (role.equals(SHERIFF)) {
            // player.giveItemStack(TMMItems.REVOLVER.getDefaultStack());
            // org.agmas.noellesroles.roles.sheriff.SheriffPlayerComponent
            // sheriffPlayerComponent =
            // org.agmas.noellesroles.roles.sheriff.SheriffPlayerComponent.KEY.get(player);
            // sheriffPlayerComponent.reset();
            // sheriffPlayerComponent.sync();
            // }
            // 在角色分配时清除之前的跟踪者状态（如果有）
            // 但是如果跟踪者正在进化（切换角色），不清除状态
            StalkerPlayerComponent stalkerComp = ModComponents.STALKER.get(player);
            if (!stalkerComp.isActiveStalker()) {
                stalkerComp.clearAll();
            }

            // // 在角色分配时清除之前的傀儡师状态（如果有）
            // // 但是如果傀儡师正在操控假人（临时切换角色），不清除状态
            // PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(player);
            // if (!puppeteerComp.isPuppeteerMarked) {
            // puppeteerComp.clearAll();
            // }
            RicesRoleRhapsody.onRoleAssigned(player, role);

        });
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            // 更新烟雾区域和迷幻区域
            SmokeAreaManager.tick();
            HallucinationAreaManager.tick();

            if (server.getPlayerList().getPlayerCount() >= 8) {
                Harpymodloader.setRoleMaximum(ModRoles.VULTURE, 1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.VULTURE, 0);
            }
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(server.overworld());
            if (gameWorldComponent.isRunning()) {
                if (!gunsCooled) {
                    int gunCooldownTicks = 30 * 20;
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        ItemCooldowns itemCooldownManager = player.getCooldowns();
                        itemCooldownManager.addCooldown(TMMItems.REVOLVER, gunCooldownTicks);
                        itemCooldownManager.addCooldown(TMMItems.KNIFE, gunCooldownTicks);
                        itemCooldownManager.addCooldown(ModItems.FAKE_REVOLVER, gunCooldownTicks);
                    }
                    gunsCooled = true;
                }
            } else {
                gunsCooled = false;
            }
        }));
        if (!NoellesRolesConfig.HANDLER.instance().shitpostRoles) {
            HarpyModLoaderConfig.HANDLER.load();

            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(ModRoles.BETTER_VIGILANTE_ID.getPath())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(ModRoles.BETTER_VIGILANTE_ID.getPath());
            }

            HarpyModLoaderConfig.HANDLER.save();
        }
        // // 监听角色分配事件 - 这是最重要的事件！
        // // 当玩家被分配角色时触发，可以在这里给予初始物品、设置初始状态等
        // ModdedRoleAssigned.EVENT.register((player, role) -> {
        //
        // });

        // 监听玩家死亡事件 - 用于激活复仇者能力、拳击手反制、跟踪者免疫和操纵师死亡判定
        AllowPlayerDeath.EVENT.register((victim, deathReason) -> {
            // 检查拳击手无敌反制
            if (handleBoxerInvulnerability(victim, deathReason)) {
                return false; // 阻止死亡
            }

            // 检查跟踪者免疫
            if (handleStalkerImmunity(victim, deathReason)) {
                return false; // 阻止死亡
            }

            // 检查傀儡师假人状态
            if (handlePuppeteerDeath(victim, deathReason)) {
                return false; // 阻止死亡（假人死亡）
            }



            // 检查起搏器
            if (handleDefibrillator(victim)) {
                // 允许死亡，但已标记复活
            }

            // 检查医生死亡 - 传递针管
            handleDoctorDeath(victim);

            // 检查死亡惩罚
            handleDeathPenalty(victim);

            // 检查故障机器人 - 死亡时生成缓慢效果云
            handleGlitchRobotDeath(victim);

            // onPlayerDeath(victim, deathReason);
            return true; // 允许死亡
        });

        // 服务器Tick事件 - 处理复活
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                DefibrillatorComponent component = ModComponents.DEFIBRILLATOR.get(player);
                if (component.isDead && player.level().getGameTime() >= component.resurrectionTime) {
                    // 复活逻辑
                    player.setGameMode(net.minecraft.world.level.GameType.ADVENTURE);
                    if (component.deathPos != null) {
                        player.teleportTo(component.deathPos.x, component.deathPos.y, component.deathPos.z);
                    }
                    player.setHealth(player.getMaxHealth());

                    // 移除尸体
                    if (component.corpseEntityId != null) {
                        net.minecraft.world.entity.Entity entity = ((net.minecraft.server.level.ServerLevel) player
                                .level()).getEntity(component.corpseEntityId);
                        if (entity != null) {
                            entity.discard();
                        }
                    }

                    component.reset();
                    player.displayClientMessage(Component.translatable("message.noellesroles.defibrillator.revived"),
                            true);
                }

                // 检查死亡惩罚过期
                DeathPenaltyComponent penaltyComponent = ModComponents.DEATH_PENALTY.get(player);
                penaltyComponent.check();
            }
        });

        // 示例：监听是否能看到毒药
        // CanSeePoison.EVENT.register((player) -> {
        // GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        // if (gameWorld.isRole(player, ModRoles.YOUR_ROLE)) {
        // return true;
        // }
        // return false;
        // });
    }

    /**
     * 处理拳击手无敌反制
     * 钢筋铁骨期间可以反弹任何死亡
     *
     * @param victim      受害者
     * @param deathReason 死亡原因
     * @return true 表示成功反制，应阻止死亡
     */
    private static boolean handleBoxerInvulnerability(Player victim, ResourceLocation deathReason) {
        if (victim == null || victim.level().isClientSide())
            return false;

        // 检查受害者是否是拳击手
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
        if (!gameWorld.isRole(victim, ModRoles.BOXER))
            return false;

        // 获取拳击手组件
        BoxerPlayerComponent boxerComponent = ModComponents.BOXER.get(victim);

        // 检查是否处于无敌状态
        if (!boxerComponent.isInvulnerable)
            return false;

        // 钢筋铁骨可以反弹任何死亡 - 不再限制死亡原因

        // 尝试找到攻击者（如果是刀或棍棒攻击）
        boolean isKnife = deathReason.equals(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.KNIFE);
        boolean isBat = deathReason.equals(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.BAT);

        if (isKnife || isBat) {
            // 需要找到攻击者 - 遍历附近玩家找到持有对应武器的
            Player attacker = findAttackerWithWeapon(victim, isKnife);

            if (attacker != null) {
                // 获取攻击者的武器
                ItemStack weapon = attacker.getMainHandItem();

                // 执行反制（对刀和棍棒有额外效果）
                boxerComponent.handleCounterAttack(attacker, weapon);
            }
        }

        // 执行通用反制（反弹任何死亡）
        boxerComponent.handleAnyDeathCounter(deathReason);

        // 无敌状态下阻止任何死亡
        return true;
    }

    /**
     * 处理跟踪者免疫
     * 盾牌只在一阶段有效，进入二阶段后消失
     *
     * @param victim      受害者
     * @param deathReason 死亡原因
     * @return true 表示成功免疫，应阻止死亡
     */
    private static boolean handleStalkerImmunity(Player victim, ResourceLocation deathReason) {
        if (victim == null || victim.level().isClientSide())
            return false;

        // 获取跟踪者组件
        StalkerPlayerComponent stalkerComp = ModComponents.STALKER.get(victim);

        // 检查是否是活跃的跟踪者且处于一阶段（盾牌只在一阶段有效）
        if (!stalkerComp.isActiveStalker())
            return false;
        if (stalkerComp.phase != 1)
            return false;

        // 检查免疫是否已使用
        if (stalkerComp.immunityUsed)
            return false;

        // 消耗免疫
        stalkerComp.immunityUsed = true;
        stalkerComp.sync();

        // 播放音效
        victim.level().playSound(null, victim.blockPosition(),
                dev.doctor4t.trainmurdermystery.index.TMMSounds.ITEM_PSYCHO_ARMOUR,
                SoundSource.MASTER, 5.0F, 1.0F);

        // 发送消息
        if (victim instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.noellesroles.stalker.immunity_triggered")
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                    true);
        }

        return true;
    }

    /**
     * 处理傀儡师死亡
     * 假人死亡时返回本体，本体死亡时真正死亡
     *
     * @param victim      受害者
     * @param deathReason 死亡原因
     * @return true 表示假人死亡（阻止真正死亡），false 表示正常处理
     */
    private static boolean handlePuppeteerDeath(Player victim, ResourceLocation deathReason) {
        if (victim == null || victim.level().isClientSide())
            return false;

        // 获取傀儡师组件
        PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(victim);

        // 检查是否是活跃的傀儡师
        if (!puppeteerComp.isActivePuppeteer())
            return false;

        // 检查是否正在操控假人
        if (!puppeteerComp.isControllingPuppet)
            return false;

        // 假人死亡，返回本体
        puppeteerComp.onPuppetDeath();

        return true; // 阻止真正死亡
    }



    private static boolean handleDefibrillator(Player victim) {
        DefibrillatorComponent component = ModComponents.DEFIBRILLATOR.get(victim);
        if (component.hasProtection()) {
            component.triggerDeath(30 * 20, null, victim.position());
            return true;
        }
        return false;
    }

    private static void handleGlitchRobotDeath(Player victim) {
        if (victim == null || victim.level().isClientSide())
            return;

        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
        if (!gameWorldComponent.isRole(victim, ModRoles.GLITCH_ROBOT))
            return;

        GlitchRobotPlayerComponent.onKnockOut(victim);
        // TODO: BUG：药水效果云生成会导致玩家被踢
    }

    private static void handleDeathPenalty(Player victim) {
        GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(victim.level());
        boolean doctorAlive = false;
        boolean INSANE_alive = false;
        boolean CONSPIRATOR_alive = false;
        boolean limitView = false;
        for (Player player : victim.level().players()) {
            if (!GameFunctions.isPlayerAliveAndSurvival(player)) {
                continue;
            }
            if (gameWorldComponent.isRole(player, ModRoles.DOCTOR)) {
                doctorAlive = true;
            } else if (gameWorldComponent.isRole(player, ModRoles.CONSPIRATOR)) {
                CONSPIRATOR_alive = true;
            } else if (gameWorldComponent.isRole(player,
                    ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                INSANE_alive = true;
            }
            if (doctorAlive && INSANE_alive && CONSPIRATOR_alive) {
                break;
            }
        }
        if (INSANE_alive && CONSPIRATOR_alive) {
            limitView = true;
        }
        if (limitView) {
            DeathPenaltyComponent component = ModComponents.DEATH_PENALTY.get(victim);
            component.setPenalty(-1);
            victim.sendSystemMessage(
                    Component.translatable("message.noellesroles.penalty.limit.god_job_couple")
                            .withStyle(ChatFormatting.RED));
            victim.displayClientMessage(
                    Component.translatable("message.noellesroles.penalty.limit.god_job_couple")
                            .withStyle(ChatFormatting.RED),
                    true);
        } else if (doctorAlive) {
            DeathPenaltyComponent component = ModComponents.DEATH_PENALTY.get(victim);
            component.setPenalty(45 * 20);
            victim.displayClientMessage(
                    Component.translatable("message.noellesroles.doctor.penalty").withStyle(ChatFormatting.RED), true);
            victim.sendSystemMessage(
                    Component.translatable("message.noellesroles.doctor.penalty").withStyle(ChatFormatting.RED));
        }
    }

    /**
     * 处理医生死亡 - 将针管传递给另一名存活的平民
     */
    private static void handleDoctorDeath(Player victim) {
        if (victim == null || victim.level().isClientSide())
            return;

        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.level());
        if (!gameWorld.isRole(victim, ModRoles.DOCTOR))
            return;

        // 查找医生背包中的针管
        ItemStack antidote = null;
        for (int i = 0; i < victim.getInventory().getContainerSize(); i++) {
            ItemStack stack = victim.getInventory().getItem(i);
            if (stack.getItem() == org.agmas.noellesroles.repack.HSRItems.ANTIDOTE) {
                antidote = stack.copy();
                victim.getInventory().setItem(i, ItemStack.EMPTY);
                break;
            }
        }

        if (antidote == null || antidote.isEmpty())
            return;

        // 查找另一名存活的平民
        Player targetPlayer = null;
        for (Player player : victim.level().players()) {
            if (player == victim)
                continue;
            if (!GameFunctions.isPlayerAliveAndSurvival(player))
                continue;

            Role role = gameWorld.getRole(player);
            if (role != null && role.isInnocent()) {
                targetPlayer = player;
                break;
            }
        }

        // 如果找到存活的平民，传递针管
        if (targetPlayer != null) {
            targetPlayer.addItem(antidote);
            if (targetPlayer instanceof ServerPlayer serverTarget) {
                serverTarget.displayClientMessage(
                        Component.translatable("message.noellesroles.doctor.antidote_inherited",
                                victim.getName().getString())
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                        true);
            }
        }
    }

    public void registerPackets() {

        // ServerPlayNetworking.registerGlobalReceiver(ThiefStealC2SPacket.ID, (payload,
        // context) -> {
        // GameWorldComponent gameWorldComponent =
        // GameWorldComponent.KEY.get(context.player().getWorld());
        // if (!gameWorldComponent.isRole(context.player(), ModRoles.THIEF)) {
        // return;
        // }
        // AbilityPlayerComponent abilityComponent =
        // AbilityPlayerComponent.KEY.get(context.player());
        // if (abilityComponent.cooldown > 0) {
        // return;
        // }
        // ThiefPlayerComponent thiefComponent =
        // ThiefPlayerComponent.KEY.get(context.player());
        // boolean hasBlackout = thiefComponent.hasBlackoutEffect;
        // PlayerEntity targetPlayer =
        // context.player().getWorld().getPlayerByUuid(payload.target());
        // if (targetPlayer == null) {
        // return;
        // }
        // if (context.player().distanceTo(targetPlayer) > 2.0D) {
        // return;
        // }
        // boolean isTargetAlive = GameFunctions.isPlayerAliveAndSurvival(targetPlayer);
        // if (!hasBlackout && isTargetAlive) {
        // return;
        // }
        // if (hasBlackout && !isTargetAlive) {
        // return;
        // }
        // PlayerShopComponent targetShop = PlayerShopComponent.KEY.get(targetPlayer);
        //
        // int stolenCoins = targetShop.balance;
        // if (stolenCoins > 0) {
        // PlayerShopComponent thiefShop =
        // PlayerShopComponent.KEY.get(context.player());
        // targetShop.balance = 0;
        // thiefShop.balance = thiefShop.balance + stolenCoins;
        // targetShop.sync();
        // thiefShop.sync();
        // abilityComponent.setCooldown(GameConstants.getInTicks(0,
        // NoellesRolesConfig.HANDLER.instance().thiefStealCooldown));
        // context.player().sendMessage(Text.translatable("message.thief.stole",
        // stolenCoins), true);
        // thiefComponent.deactivateBlackout();
        //
        // if (context.player() instanceof ServerPlayerEntity serverPlayer) {
        // serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
        // Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY),
        // SoundCategory.PLAYERS,
        // serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
        // 1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
        // serverPlayer.getRandom().nextLong()
        // ));
        // }
        // }
        // });
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.MORPH_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(context.player().level());
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY
                    .get(context.player());

            if (payload.player() == null)
                return;
            if (abilityPlayerComponent.cooldown > 0)
                return;
            if (context.player().level().getPlayerByUUID(payload.player()) == null)
                return;

            if (gameWorldComponent.isRole(context.player(), ModRoles.VOODOO)) {
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                        NoellesRolesConfig.HANDLER.instance().voodooCooldown);
                abilityPlayerComponent.sync();
                VoodooPlayerComponent voodooPlayerComponent = (VoodooPlayerComponent) VoodooPlayerComponent.KEY
                        .get(context.player());
                voodooPlayerComponent.setTarget(payload.player());

            }
            if (gameWorldComponent.isRole(context.player(), ModRoles.MORPHLING)) {
                MorphlingPlayerComponent morphlingPlayerComponent = (MorphlingPlayerComponent) MorphlingPlayerComponent.KEY
                        .get(context.player());
                morphlingPlayerComponent.startMorph(payload.player());
            }
        });

        // 操纵师数据包处理
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.MANIPULATOR_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(context.player().level());
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY
                    .get(context.player());

            if (payload.player() == null)
                return;
            if (abilityPlayerComponent.cooldown > 0)
                return;
            if (context.player().level().getPlayerByUUID(payload.player()) == null)
                return;

            if (gameWorldComponent.isRole(context.player(), ModRoles.MANIPULATOR)) {
                // 设置操纵师的冷却时间（根据配置）
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                        NoellesRolesConfig.HANDLER.instance().manipulatorCooldown);
                abilityPlayerComponent.sync();

                // 获取操纵师组件并设置目标
                ManipulatorPlayerComponent manipulatorPlayerComponent = (ManipulatorPlayerComponent) ManipulatorPlayerComponent.KEY
                        .get(context.player());
                manipulatorPlayerComponent.setTarget(payload.player());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.VULTURE_PACKET, (payload, context) -> {
            final var player = context.player();
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(player.level());
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY
                    .get(player);

            if (gameWorldComponent.isRole(player, ModRoles.VULTURE)
                    && GameFunctions.isPlayerAliveAndSurvival(player)) {
                if (abilityPlayerComponent.cooldown > 0)
                    return;
                abilityPlayerComponent.sync();
                List<PlayerBodyEntity> playerBodyEntities = player.level().getEntities(
                        EntityTypeTest.forExactClass(PlayerBodyEntity.class), player.getBoundingBox().inflate(10),
                        (playerBodyEntity -> {
                            return playerBodyEntity.getUUID().equals(payload.playerBody());
                        }));
                if (!playerBodyEntities.isEmpty()) {
                    BodyDeathReasonComponent bodyDeathReasonComponent = BodyDeathReasonComponent.KEY
                            .get(playerBodyEntities.getFirst());
                    if (!bodyDeathReasonComponent.vultured) {
                        abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                                NoellesRolesConfig.HANDLER.instance().vultureEatCooldown);
                        VulturePlayerComponent vulturePlayerComponent = VulturePlayerComponent.KEY
                                .get(player);
                        vulturePlayerComponent.bodiesEaten++;
                        vulturePlayerComponent.sync();
                        player.playSound(SoundEvents.PLAYER_BURP, 1.0F, 0.5F);
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
                        if (vulturePlayerComponent.bodiesEaten >= vulturePlayerComponent.bodiesRequired) {
                            ArrayList<Role> shuffledKillerRoles = new ArrayList<>(getEnableKillerRoles());
                            shuffledKillerRoles.removeIf(role -> role.identifier().equals(ModRoles.EXECUTIONER_ID)
                                    || Harpymodloader.VANNILA_ROLES.contains(role) || !role.canUseKiller()
                                    || HarpyModLoaderConfig.HANDLER.instance().disabled
                                            .contains(role.identifier().getPath()));
                            if (shuffledKillerRoles.isEmpty())
                                shuffledKillerRoles.add(TMMRoles.KILLER);
                            Collections.shuffle(shuffledKillerRoles);

                            PlayerShopComponent playerShopComponent = (PlayerShopComponent) PlayerShopComponent.KEY
                                    .get(player);
                            final var first = shuffledKillerRoles.getFirst();
                            // gameWorldComponent.addRole(player, first);
                            // ModdedRoleAssigned.EVENT.invoker().assignModdedRole(player,
                            // first);
                            RoleUtils.changeRole(player, first);
                            playerShopComponent.setBalance(100);

                            RoleUtils.sendWelcomeAnnouncement(player);
                        }

                        bodyDeathReasonComponent.vultured = true;
                        bodyDeathReasonComponent.sync();
                    }
                }

            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.SWAP_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(context.player().level());
            if (gameWorldComponent.isRole(context.player(), ModRoles.SWAPPER)) {
                AbilityPlayerComponent abilityPlayerComponent = AbilityPlayerComponent.KEY.get(context.player());
                if (abilityPlayerComponent.cooldown > 0)
                    return;
                
                if (payload.player() != null && payload.player2() != null) {
                    if (context.player().level().getPlayerByUUID(payload.player()) != null &&
                            context.player().level().getPlayerByUUID(payload.player2()) != null) {

                        SwapperPlayerComponent swapperComponent = ModComponents.SWAPPER.get(context.player());
                        if (!swapperComponent.isSwapping) {
                            swapperComponent.startSwap(payload.player(), payload.player2());
                        }
                    }
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.EXECUTIONER_SELECT_TARGET_PACKET,
                (payload, context) -> {
                    // 检查是否启用了手动选择目标功能
                    if (!NoellesRolesConfig.HANDLER.instance().executionerCanSelectTarget) {
                        return; // 如果未启用，则忽略该数据包
                    }

                    GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                            .get(context.player().level());
                    if (gameWorldComponent.isRole(context.player(), ModRoles.EXECUTIONER)) {
                        ExecutionerPlayerComponent executionerPlayerComponent = ExecutionerPlayerComponent.KEY
                                .get(context.player());
                        if (executionerPlayerComponent.targetSelected)
                            return;

                        if (payload.target() != null) {
                            Player targetPlayer = context.player().level().getPlayerByUUID(payload.target());
                            if (targetPlayer != null && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                                if (gameWorldComponent.getRole(targetPlayer).isInnocent()) {
                                    executionerPlayerComponent.setTarget(payload.target());
                                } else {
                                    context.player().displayClientMessage(
                                            Component.translatable("message.error.executioner.invalid_target"), true);
                                }
                            } else {
                                context.player().displayClientMessage(
                                        Component.translatable("message.error.executioner.target_not_found"), true);
                            }
                        }
                    }
                });
        ServerPlayNetworking.registerGlobalReceiver(GamblerSelectRoleC2SPacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                GamblerPlayerComponent component = GamblerPlayerComponent.KEY.get(context.player());
                component.selectRole(payload.roleId());
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(org.agmas.noellesroles.packet.BroadcasterC2SPacket.ID,
                (payload, context) -> {
                    AbilityPlayerComponent abilityPlayerComponent = AbilityPlayerComponent.KEY.get(context.player());
                    GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(context.player().level());
                    PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());

                    if (gameWorldComponent.isRole(context.player(), ModRoles.BROADCASTER)) {

                        BroadcasterPlayerComponent comp = BroadcasterPlayerComponent.KEY.get(context.player());
                        String message = payload.message();
                        boolean onlySave = payload.onlySave();
                        if (onlySave) {
                            comp.setStoredStr(message);
                            return;
                        }
                        if (playerShopComponent.balance < 100) {
                            context.player().displayClientMessage(
                                    Component.translatable("message.noellesroles.insufficient_funds"),
                                    true);
                            comp.setStoredStr(message);
                            if (context.player() instanceof ServerPlayer) {
                                ServerPlayer player = (ServerPlayer) context.player();
                                player.connection.send(new ClientboundSoundPacket(
                                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(TMMSounds.UI_SHOP_BUY_FAIL),
                                        SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F,
                                        0.9F + player.getRandom().nextFloat() * 0.2F, player.getRandom().nextLong()));
                            }
                            return;
                        }
                        if (message.length() > 256) {
                            message = message.substring(0, 256);
                        }
                        if (comp != null) {
                            comp.setStoredStr("");
                        }
                        playerShopComponent.balance -= 100;
                        playerShopComponent.sync();

                        for (ServerPlayer player : Objects.requireNonNull(context.player().getServer())
                                .getPlayerList().getPlayers()) {
                            org.agmas.noellesroles.packet.BroadcastMessageS2CPacket packet = new org.agmas.noellesroles.packet.BroadcastMessageS2CPacket(
                                    Component.translatable("message.noellesroles.broadcaster.general",
                                            Component.literal(message).withStyle(ChatFormatting.WHITE))
                                            .withStyle(ChatFormatting.GREEN));
                            net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, packet);
                        }
                        abilityPlayerComponent.cooldown = 0;
                        abilityPlayerComponent.sync();
                    }
                });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.ABILITY_PACKET, (payload, context) -> {
            // 通用技能服务端处理
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY
                    .get(context.player());
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(context.player().level());
            if (gameWorldComponent.isRole(context.player(), ModRoles.GLITCH_ROBOT)) {

                if (!RoleUtils.isPlayerHasFreeSlot(context.player())) {
                    context.player().displayClientMessage(
                            Component.translatable("message.hotbar.full").withStyle(ChatFormatting.RED), true);
                    return;
                }
                if (!context.player().getSlot(103).get().is(ModItems.NIGHT_VISION_GLASSES)) {
                    context.player().displayClientMessage(
                            Component.translatable("info.glitch_robot.noglasses_on_head").withStyle(ChatFormatting.RED),
                            true);
                    return;
                }
                RoleUtils.insertStackInFreeSlot(context.player(), context.player().getSlot(103).get().copy());
                // RoleUtils.removeStackItem(context.player(), 103);
                context.player().getInventory().armor.set(3, ItemStack.EMPTY);
                context.player().displayClientMessage(
                        Component.translatable("info.glitch_robot.take_off_glasses.success")
                                .withStyle(ChatFormatting.GREEN),
                        true);
                context.player().removeEffect(MobEffects.NIGHT_VISION);
                return;
            }
            if (gameWorldComponent.isRole(context.player(), ModRoles.BOMBER)) {
                BomberPlayerComponent bomberPlayerComponent = ModComponents.BOMBER.get(context.player());
                bomberPlayerComponent.buyBomb();
                return;
            }
            if (gameWorldComponent.isRole(context.player(), ModRoles.NOISEMAKER)) {
                NoiseMakerPlayerComponent noiseMakerPlayerComponent = ModComponents.NOISEMAKER.get(context.player());
                noiseMakerPlayerComponent.useAbility();
                return;
            }
            if (gameWorldComponent.isRole(context.player(), ModRoles.GHOST)) {
                org.agmas.noellesroles.roles.ghost.GhostPlayerComponent ghostPlayerComponent = org.agmas.noellesroles.roles.ghost.GhostPlayerComponent.KEY
                        .get(context.player());
                ghostPlayerComponent.useAbility();
                return;
            }

            if (gameWorldComponent.isRole(context.player(), ModRoles.RECALLER)
                    && abilityPlayerComponent.cooldown <= 0) {
                RecallerPlayerComponent recallerPlayerComponent = RecallerPlayerComponent.KEY.get(context.player());
                PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());
                if (!recallerPlayerComponent.placed) {
                    abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                            NoellesRolesConfig.HANDLER.instance().recallerMarkCooldown);
                    recallerPlayerComponent.setPosition();
                } else if (playerShopComponent.balance >= 100) {
                    playerShopComponent.balance -= 100;
                    playerShopComponent.sync();
                    abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                            NoellesRolesConfig.HANDLER.instance().recallerTeleportCooldown);
                    recallerPlayerComponent.teleport();
                }

            }
            if (gameWorldComponent.isRole(context.player(), ModRoles.PHANTOM) && abilityPlayerComponent.cooldown <= 0) {
                context.player().addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
                        NoellesRolesConfig.HANDLER.instance().phantomInvisibilityDuration * 20, 0, true, false, true));
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0,
                        NoellesRolesConfig.HANDLER.instance().phantomInvisibilityCooldown);
            }
            // else if (gameWorldComponent.isRole(context.player(), ModRoles.THIEF)
            // && abilityPlayerComponent.cooldown <= 0) {
            //
            // }
        });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.INSANE_KILLER_ABILITY_PACKET, (payload, context) -> {
            ServerPlayer player = (ServerPlayer) context.player();
            InsaneKillerPlayerComponent component = InsaneKillerPlayerComponent.KEY.get(player);

            // 检查冷却
            if (component.cooldown > 0 && !component.isActive)
                return;

            component.toggleAbility();
            component.sync();
        });
        ServerPlayNetworking.registerGlobalReceiver(RecorderC2SPacket.TYPE, RecorderC2SPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.MONITOR_MARK_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY
                    .get(context.player().level());
            if (gameWorldComponent.isRole(context.player(), ModRoles.MONITOR)) {
                MonitorPlayerComponent monitorComponent = MonitorPlayerComponent.KEY.get(context.player());

                // 检查冷却
                if (monitorComponent.canUseAbility()) {
                    if (payload.target() != null) {
                        Player targetPlayer = context.player().level().getPlayerByUUID(payload.target());
                        if (targetPlayer != null && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                            // 标记目标
                            monitorComponent.markTarget(payload.target());

                            // 发送成功消息
                            context.player().displayClientMessage(
                                    Component
                                            .translatable("message.noellesroles.monitor.marked",
                                                    targetPlayer.getName().getString())
                                            .withStyle(ChatFormatting.AQUA),
                                    true);
                        } else {
                            context.player().displayClientMessage(
                                    Component.translatable("message.noellesroles.monitor.target_not_found"), true);
                        }
                    }
                } else {
                    // 冷却中
                    context.player().displayClientMessage(
                            Component.translatable("message.noellesroles.monitor.cooldown",
                                    String.format("%.1f", monitorComponent.getCooldownSeconds())),
                            true);
                }
            }
        });
    }

    /**
     * 获取指定角色的初始物品列表
     * 
     * @param role 角色
     * @return 初始物品列表
     */
    public static List<ItemStack> getInitialItemsForRole(Role role, Player player) {
        List<ItemStack> result = new ArrayList<>();
        List<Supplier<ItemStack>> itemSuppliers = INITIAL_ITEMS_MAP.get(role);
        if (itemSuppliers != null) {
            for (Supplier<ItemStack> itemSupplier : itemSuppliers) {
                ItemStack itemStack = itemSupplier.get();
                if (itemStack != null && !itemStack.isEmpty()) {
                    result.add(itemStack.copy());
                }
            }
        }
        return result;
    }
}