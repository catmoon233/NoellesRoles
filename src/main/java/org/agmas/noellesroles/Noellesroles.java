package org.agmas.noellesroles;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerPsychoComponent;
import dev.doctor4t.trainmurdermystery.cca.PlayerShopComponent;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import dev.doctor4t.trainmurdermystery.event.AllowPlayerDeath;
import dev.doctor4t.trainmurdermystery.event.CanSeePoison;
import dev.doctor4t.trainmurdermystery.game.GameConstants;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import dev.doctor4t.trainmurdermystery.game.ShopContent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import dev.doctor4t.trainmurdermystery.index.TMMSounds;
import dev.doctor4t.trainmurdermystery.util.AnnounceWelcomePayload;
import dev.doctor4t.trainmurdermystery.util.ShopEntry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
import org.agmas.noellesroles.component.BoxerPlayerComponent;
import org.agmas.noellesroles.component.ModComponents;
import org.agmas.noellesroles.component.PuppeteerPlayerComponent;
import org.agmas.noellesroles.component.StalkerPlayerComponent;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.roles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.commands.ConfigCommand;
import org.agmas.noellesroles.commands.SetRoleMaxCommand;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.roles.framing.FramingShopEntry;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.packet.*;
import org.agmas.noellesroles.roles.recaller.RecallerPlayerComponent;
import org.agmas.noellesroles.repack.HSRConstants;
import org.agmas.noellesroles.repack.HSRItems;
import org.agmas.noellesroles.repack.HSRSounds;
import org.agmas.noellesroles.roles.voodoo.VoodooPlayerComponent;
import org.agmas.noellesroles.roles.vulture.VulturePlayerComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;

import static org.agmas.noellesroles.RicesRoleRhapsody.findAttackerWithWeapon;
import static org.agmas.noellesroles.RicesRoleRhapsody.onRoleAssigned;

public class Noellesroles implements ModInitializer {

    public static String MOD_ID = "noellesroles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //public static Role SHERIFF = TMMRoles.registerRole(new Role(SHERIFF_ID, new Color(0, 0, 255).getRGB(),true,false, Role.MoodType.REAL, TMMRoles.VIGILANTE.getMaxSprintTime(),false));

    // ==================== 网络包ID定义 ====================
    public static final CustomPayload.Id<MorphC2SPacket> MORPH_PACKET = MorphC2SPacket.ID;
    public static final CustomPayload.Id<SwapperC2SPacket> SWAP_PACKET = SwapperC2SPacket.ID;
    public static final CustomPayload.Id<AbilityC2SPacket> ABILITY_PACKET = AbilityC2SPacket.ID;
    public static final CustomPayload.Id<VultureEatC2SPacket> VULTURE_PACKET = VultureEatC2SPacket.ID;
    public static final CustomPayload.Id<ThiefStealC2SPacket> THIEF_PACKET = ThiefStealC2SPacket.ID;
    public static final ArrayList<Role> VANNILA_ROLES = new ArrayList<>();
    public static final ArrayList<Identifier> VANNILA_ROLE_IDS = new ArrayList<>();
    public static final CustomPayload.Id<ExecutionerSelectTargetC2SPacket> EXECUTIONER_SELECT_TARGET_PACKET = ExecutionerSelectTargetC2SPacket.ID;

    // ==================== 商店项目列表 ====================
    public static ArrayList<ShopEntry> FRAMING_ROLES_SHOP = new ArrayList<>();
    // ==================== 阴谋家商店 ====================
    public static ArrayList<ShopEntry> CONSPIRATOR_SHOP = new ArrayList<>();
    // ==================== 滑头鬼商店 ====================
    public static ArrayList<ShopEntry> SLIPPERY_GHOST_SHOP = new ArrayList<>();
    // ==================== 工程师商店 ====================
    public static ArrayList<ShopEntry> ENGINEER_SHOP = new ArrayList<>();
    // ==================== 邮差商店 ====================
    public static ArrayList<ShopEntry> POSTMAN_SHOP = new ArrayList<>();

    private static boolean gunsCooled = false;

    public static @NotNull Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
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
        Harpymodloader.setRoleMaximum(ModRoles.BANDIT_ID, 2);
        Harpymodloader.setRoleMaximum(ModRoles.DOCTOR_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.ATTENDANT_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.POISONER_ID, 1);

        // 注册商店
        shopRegister();
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
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.LOCKPICK.getDefaultStack(), 50, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(ModItems.DELUSION_VIAL.getDefaultStack(), 30, ShopEntry.Type.POISON));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.FIRECRACKER.getDefaultStack(), 5, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.NOTE.getDefaultStack(), 5, ShopEntry.Type.TOOL));
    }

    /**
     * 初始化商店
     */
    public static void initShops() {
        // 阴谋家商店
        CONSPIRATOR_SHOP.add(new ShopEntry(
                ModItems.CONSPIRACY_PAGE.getDefaultStack(),
                125,
                ShopEntry.Type.TOOL
        ));

        CONSPIRATOR_SHOP.add(new ShopEntry(
                dev.doctor4t.trainmurdermystery.index.TMMItems.KNIFE.getDefaultStack(),
                100,
                ShopEntry.Type.TOOL
        ));

        CONSPIRATOR_SHOP.add(new ShopEntry(
                dev.doctor4t.trainmurdermystery.index.TMMItems.REVOLVER.getDefaultStack(),
                175,
                ShopEntry.Type.WEAPON
        ));

        CONSPIRATOR_SHOP.add(new ShopEntry(
                dev.doctor4t.trainmurdermystery.index.TMMItems.LOCKPICK.getDefaultStack(),
                50,
                ShopEntry.Type.TOOL
        ));

        // 滑头鬼商店
        // 空包弹 - 150金币
        SLIPPERY_GHOST_SHOP.add(new ShopEntry(
                ModItems.BLANK_CARTRIDGE.getDefaultStack(),
                150,
                ShopEntry.Type.TOOL
        ));

        // 烟雾弹 - 300金币
        SLIPPERY_GHOST_SHOP.add(new ShopEntry(
                ModItems.SMOKE_GRENADE.getDefaultStack(),
                300,
                ShopEntry.Type.TOOL
        ));

        // 撬锁器 - 50金币 (原版杀手商店物品)
        SLIPPERY_GHOST_SHOP.add(new ShopEntry(
                dev.doctor4t.trainmurdermystery.index.TMMItems.LOCKPICK.getDefaultStack(),
                50,
                ShopEntry.Type.TOOL
        ));

        // 关灯 - 300金币 (原版杀手商店物品)
        SLIPPERY_GHOST_SHOP.add(new ShopEntry(
                dev.doctor4t.trainmurdermystery.index.TMMItems.BLACKOUT.getDefaultStack(),
                300,
                ShopEntry.Type.TOOL
        ));

        // 工程师商店
        // 加固门 - 75金币
        ENGINEER_SHOP.add(new ShopEntry(
                ModItems.REINFORCEMENT.getDefaultStack(),
                75,
                ShopEntry.Type.TOOL
        ));

        // 警报陷阱 - 150金币
        ENGINEER_SHOP.add(new ShopEntry(
                ModItems.ALARM_TRAP.getDefaultStack(),
                150,
                ShopEntry.Type.TOOL
        ));

        // 邮差商店
        // 传递盒 - 250金币
        POSTMAN_SHOP.add(new ShopEntry(
                ModItems.DELIVERY_BOX.getDefaultStack(),
                250,
                ShopEntry.Type.TOOL
        ));
    }

    private void shopRegister() {
        initShops();
        ShopContent.customEntries.put(
                ModRoles.POISONER_ID, HSRConstants.POISONER_SHOP_ENTRIES
        );

        ShopContent.customEntries.put(
                ModRoles.SWAPPER_ID, ShopContent.defaultEntries
        );
//        ShopContent.customEntries.put(
//                POISONER_ID, ShopContent.defaultEntries
//        );
        ShopContent.customEntries.put(
                ModRoles.BANDIT_ID, HSRConstants.BANDIT_SHOP_ENTRIES
        );
        ShopContent.customEntries.put(
                ModRoles.JESTER_ID, Noellesroles.FRAMING_ROLES_SHOP
        );
        {
        List<ShopEntry> entries = new ArrayList<>();
        entries.add(new ShopEntry(ModItems.DEFENSE_VIAL.getDefaultStack(), 250, ShopEntry.Type.POISON));

        ShopContent.customEntries.put(
                ModRoles.BARTENDER_ID, entries
        );
        }
        {
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(TMMItems.FIRECRACKER.getDefaultStack(), 75, ShopEntry.Type.TOOL));

            ShopContent.customEntries.put(
                ModRoles.NOISEMAKER_ID, entries
        );
            ShopContent.customEntries.put(
                    ModRoles.EXECUTIONER_ID, ShopContent.defaultEntries
            );

        }
//        {
//            List<ShopEntry> entries = new ArrayList<>();
//            entries.add(new ShopEntry(ModItems.SHERIFF_GUN_MAINTENANCE.getDefaultStack(), 150, ShopEntry.Type.TOOL));
//
//            ShopContent.customEntries.put(
//                SHERIFF_ID, entries
//        );
//        }
        {
            List<ShopEntry> entries = new ArrayList<>();
            entries.add(new ShopEntry(Items.ITEM_FRAME.getDefaultStack(), 50, ShopEntry.Type.TOOL) {
                            @Override
                            public boolean onBuy(@NotNull PlayerEntity player) {
                                final var item = player.getWorld().getRegistryManager().get(Registries.ITEM.getKey()).get(Identifier.tryParse("exposure_polaroid:instant_color_slide"));
                                if (item != null) {
                                    final var defaultStack = item.getDefaultStack();
                                    player.giveItemStack(defaultStack);
                                    return true;
                                }
                                return false;
                            }
                        }
            );

            ShopContent.customEntries.put(
                ModRoles.PHOTOGRAPHER_ID, entries
        );
        }

        {
            ShopContent.customEntries.put(
                    ModRoles.CONSPIRATOR_ID, CONSPIRATOR_SHOP
            );
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.SLIPPERY_GHOST_ID, SLIPPERY_GHOST_SHOP
            );
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.ENGINEER_ID, ENGINEER_SHOP
            );
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.POSTMAN_ID, POSTMAN_SHOP
            );
        }
        ShopContent.customEntries.put(
                ModRoles.STALKER_ID, List.of(new ShopEntry(TMMItems.LOCKPICK.getDefaultStack(), 75, ShopEntry.Type.TOOL)));
    }

    public static void registerPackets1(){
        PayloadTypeRegistry.playC2S().register(ExecutionerSelectTargetC2SPacket.ID, ExecutionerSelectTargetC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(BroadcasterC2SPacket.ID, BroadcasterC2SPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BroadcastMessageS2CPacket.ID, BroadcastMessageS2CPacket.CODEC);

        PayloadTypeRegistry.playC2S().register(MorphC2SPacket.ID, MorphC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(AbilityC2SPacket.ID, AbilityC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SwapperC2SPacket.ID, SwapperC2SPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(VultureEatC2SPacket.ID, VultureEatC2SPacket.CODEC);
    }
    private void registerMaxRoleCount() {
        Harpymodloader.setRoleMaximum(ModRoles.CONDUCTOR_ID, NoellesRolesConfig.HANDLER.instance().conductorMax);
        Harpymodloader.setRoleMaximum(ModRoles.EXECUTIONER_ID, NoellesRolesConfig.HANDLER.instance().executionerMax);
        Harpymodloader.setRoleMaximum(ModRoles.VULTURE_ID, NoellesRolesConfig.HANDLER.instance().vultureMax);
        Harpymodloader.setRoleMaximum(ModRoles.JESTER_ID, NoellesRolesConfig.HANDLER.instance().jesterMax);
        Harpymodloader.setRoleMaximum(ModRoles.MORPHLING_ID, NoellesRolesConfig.HANDLER.instance().morphlingMax);
        Harpymodloader.setRoleMaximum(ModRoles.BARTENDER_ID, NoellesRolesConfig.HANDLER.instance().bartenderMax);
        Harpymodloader.setRoleMaximum(ModRoles.NOISEMAKER_ID, NoellesRolesConfig.HANDLER.instance().noisemakerMax);
        Harpymodloader.setRoleMaximum(ModRoles.PHANTOM_ID, NoellesRolesConfig.HANDLER.instance().phantomMax);
        Harpymodloader.setRoleMaximum(ModRoles.AWESOME_BINGLUS_ID, NoellesRolesConfig.HANDLER.instance().awesomeBinglusMax);
        Harpymodloader.setRoleMaximum(ModRoles.SWAPPER_ID, NoellesRolesConfig.HANDLER.instance().swapperMax);
        Harpymodloader.setRoleMaximum(ModRoles.VOODOO_ID, NoellesRolesConfig.HANDLER.instance().voodooMax);
        Harpymodloader.setRoleMaximum(ModRoles.CORONER_ID, NoellesRolesConfig.HANDLER.instance().coronerMax);
        Harpymodloader.setRoleMaximum(ModRoles.RECALLER_ID, NoellesRolesConfig.HANDLER.instance().recallerMax);
        Harpymodloader.setRoleMaximum(ModRoles.BROADCASTER_ID, NoellesRolesConfig.HANDLER.instance().broadcasterMax);
        Harpymodloader.setRoleMaximum(ModRoles.GAMBLER_ID, NoellesRolesConfig.HANDLER.instance().gamblerMax);
        Harpymodloader.setRoleMaximum(ModRoles.GHOST_ID, NoellesRolesConfig.HANDLER.instance().ghostMax);
        Harpymodloader.setRoleMaximum(ModRoles.THIEF_ID, NoellesRolesConfig.HANDLER.instance().thiefMax);
        Harpymodloader.setRoleMaximum(ModRoles.SHERIFF_ID, NoellesRolesConfig.HANDLER.instance().sheriffMax);
    }


    public void registerEvents() {

        AllowPlayerDeath.EVENT.register(((playerEntity, identifier) -> {
            if (identifier == GameConstants.DeathReasons.FELL_OUT_OF_TRAIN) return true;
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(playerEntity.getWorld());
            if (gameWorldComponent.isRole(playerEntity, ModRoles.JESTER)) {
                PlayerPsychoComponent component =  PlayerPsychoComponent.KEY.get(playerEntity);
                if (component.getPsychoTicks() > GameConstants.getInTicks(0,44)) {
                    return false;
                }
            }
            BartenderPlayerComponent bartenderPlayerComponent = BartenderPlayerComponent.KEY.get(playerEntity);
            if (bartenderPlayerComponent.armor > 0) {
                playerEntity.getWorld().playSound(playerEntity, playerEntity.getBlockPos(), TMMSounds.ITEM_PSYCHO_ARMOUR, SoundCategory.MASTER, 5.0F, 1.0F);
                bartenderPlayerComponent.armor--;
                return false;
            }
            return true;
        }));
        CanSeePoison.EVENT.register((player)->{
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
            if (gameWorldComponent.isRole((PlayerEntity) player, ModRoles.BARTENDER)) {
                return true;
            }
            return false;
        });

        ModdedRoleAssigned.EVENT.register((player,role)->{
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(player);
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(player.getWorld());
            abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;
            if (role.equals(ModRoles.BROADCASTER)) {
                abilityPlayerComponent.cooldown = 0;
            } else {
                abilityPlayerComponent.cooldown = NoellesRolesConfig.HANDLER.instance().generalCooldownTicks;
            }
            if (role.equals(ModRoles.EXECUTIONER)) {
                ExecutionerPlayerComponent executionerPlayerComponent = (ExecutionerPlayerComponent) ExecutionerPlayerComponent.KEY.get(player);
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
                    vulturePlayerComponent.bodiesRequired = (int)((player.getWorld().getPlayers().size()/3f) - Math.floor(player.getWorld().getPlayers().size()/6f));
                    vulturePlayerComponent.sync();
                }
            }
            if (role.equals(ModRoles.DOCTOR)) {
                player.giveItemStack(HSRItems.ANTIDOTE.getDefaultStack());
            }
            if (role.equals(ModRoles.PHOTOGRAPHER)) {
                {
                final var item = player.getWorld().getRegistryManager().get(Registries.ITEM.getKey()).get(Identifier.tryParse("exposure_polaroid:instant_camera"));
                if (item != null) {
                    final var defaultStack = item.getDefaultStack();
                    player.giveItemStack(defaultStack);
                }
                    {
                        final var item1 = player.getWorld().getRegistryManager().get(Registries.ITEM.getKey()).get(Identifier.tryParse("exposure:album"));
                        if (item1 != null) {
                            final var defaultStack = item1.getDefaultStack();
                            player.giveItemStack(defaultStack);
                        }
                    }
            }}

            if (role.equals(ModRoles.BANDIT)) {
                player.giveItemStack(HSRItems.BANDIT_REVOLVER.getDefaultStack());
                player.giveItemStack(TMMItems.CROWBAR.getDefaultStack());
            }

            if (role.equals(ModRoles.ATTENDANT)) {
                player.giveItemStack(ModItems.MASTER_KEY_P.getDefaultStack());
            }

            if (role.equals(ModRoles.GAMBLER)) {
                org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent gamblerPlayerComponent = org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent.KEY.get(player);
                gamblerPlayerComponent.reset();
                gamblerPlayerComponent.sync();
            }
            if (role.equals(ModRoles.GHOST)) {
                org.agmas.noellesroles.roles.ghost.GhostPlayerComponent ghostPlayerComponent = org.agmas.noellesroles.roles.ghost.GhostPlayerComponent.KEY.get(player);
                ghostPlayerComponent.reset();
                ghostPlayerComponent.sync();
            }
//            if (role.equals(SHERIFF)) {
//                player.giveItemStack(TMMItems.REVOLVER.getDefaultStack());
//                org.agmas.noellesroles.roles.sheriff.SheriffPlayerComponent sheriffPlayerComponent = org.agmas.noellesroles.roles.sheriff.SheriffPlayerComponent.KEY.get(player);
//                sheriffPlayerComponent.reset();
//                sheriffPlayerComponent.sync();
//            }
            if (role.equals(ModRoles.BETTER_VIGILANTE)) {
                player.giveItemStack(TMMItems.GRENADE.getDefaultStack());
            }
            if (role.equals(ModRoles.JESTER)) {
                player.giveItemStack(ModItems.FAKE_KNIFE.getDefaultStack());
                player.giveItemStack(ModItems.FAKE_REVOLVER.getDefaultStack());
            }
            if (role.equals(ModRoles.CONDUCTOR)) {
                player.giveItemStack(ModItems.MASTER_KEY.getDefaultStack());
                player.giveItemStack(Items.SPYGLASS.getDefaultStack());
            }
            if (role.equals(ModRoles.AWESOME_BINGLUS)) {
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
                player.giveItemStack(TMMItems.NOTE.getDefaultStack());
            }
            // 在角色分配时清除之前的跟踪者状态（如果有）
            // 但是如果跟踪者正在进化（切换角色），不清除状态
            StalkerPlayerComponent stalkerComp = ModComponents.STALKER.get(player);
            if (!stalkerComp.isActiveStalker()) {
                stalkerComp.clearAll();
            }

//            // 在角色分配时清除之前的傀儡师状态（如果有）
//            // 但是如果傀儡师正在操控假人（临时切换角色），不清除状态
//            PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(player);
//            if (!puppeteerComp.isPuppeteerMarked) {
//                puppeteerComp.clearAll();
//            }
            RicesRoleRhapsody.onRoleAssigned(player, role);


        });
        ServerTickEvents.END_SERVER_TICK.register(((server) -> {
            if (server.getPlayerManager().getCurrentPlayerCount() >= 8) {
                Harpymodloader.setRoleMaximum(ModRoles.VULTURE,1);
            } else {
                Harpymodloader.setRoleMaximum(ModRoles.VULTURE,0);
            }
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(server.getOverworld());
            if (gameWorldComponent.isRunning()) {
                if (!gunsCooled) {
                    int gunCooldownTicks = 30 * 20;
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        ItemCooldownManager itemCooldownManager = player.getItemCooldownManager();
                        itemCooldownManager.set(TMMItems.REVOLVER, gunCooldownTicks);
                        itemCooldownManager.set(ModItems.FAKE_REVOLVER, gunCooldownTicks);
                    }
                    gunsCooled = true;
                }
            } else {
                gunsCooled = false;
            }
        }));
        if (!NoellesRolesConfig.HANDLER.instance().shitpostRoles) {
            HarpyModLoaderConfig.HANDLER.load();
            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(ModRoles.AWESOME_BINGLUS_ID.getPath())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(ModRoles.AWESOME_BINGLUS_ID.getPath());
            }
            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(ModRoles.BETTER_VIGILANTE_ID.getPath())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(ModRoles.BETTER_VIGILANTE_ID.getPath());
            }
            if (!HarpyModLoaderConfig.HANDLER.instance().disabled.contains(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID.getPath())) {
                HarpyModLoaderConfig.HANDLER.instance().disabled.add(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID.getPath());
            }
            HarpyModLoaderConfig.HANDLER.save();
        }
//        // 监听角色分配事件 - 这是最重要的事件！
//        // 当玩家被分配角色时触发，可以在这里给予初始物品、设置初始状态等
//        ModdedRoleAssigned.EVENT.register((player, role) -> {
//
//        });

        // 监听玩家死亡事件 - 用于激活复仇者能力、拳击手反制和跟踪者免疫
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

            //onPlayerDeath(victim, deathReason);
            return true; // 允许死亡
        });

        // 示例：监听是否能看到毒药
        // CanSeePoison.EVENT.register((player) -> {
        //     GameWorldComponent gameWorld = GameWorldComponent.KEY.get(player.getWorld());
        //     if (gameWorld.isRole(player, ModRoles.YOUR_ROLE)) {
        //         return true;
        //     }
        //     return false;
        // });
    }
    /**
     * 处理拳击手无敌反制
     * 钢筋铁骨期间可以反弹任何死亡
     *
     * @param victim 受害者
     * @param deathReason 死亡原因
     * @return true 表示成功反制，应阻止死亡
     */
    private static boolean handleBoxerInvulnerability(PlayerEntity victim, Identifier deathReason) {
        if (victim == null || victim.getWorld().isClient()) return false;

        // 检查受害者是否是拳击手
        GameWorldComponent gameWorld = GameWorldComponent.KEY.get(victim.getWorld());
        if (!gameWorld.isRole(victim, ModRoles.BOXER)) return false;

        // 获取拳击手组件
        BoxerPlayerComponent boxerComponent = ModComponents.BOXER.get(victim);

        // 检查是否处于无敌状态
        if (!boxerComponent.isInvulnerable) return false;

        // 钢筋铁骨可以反弹任何死亡 - 不再限制死亡原因

        // 尝试找到攻击者（如果是刀或棍棒攻击）
        boolean isKnife = deathReason.equals(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.KNIFE);
        boolean isBat = deathReason.equals(dev.doctor4t.trainmurdermystery.game.GameConstants.DeathReasons.BAT);

        if (isKnife || isBat) {
            // 需要找到攻击者 - 遍历附近玩家找到持有对应武器的
            PlayerEntity attacker = findAttackerWithWeapon(victim, isKnife);

            if (attacker != null) {
                // 获取攻击者的武器
                ItemStack weapon = attacker.getMainHandStack();

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
     *
     * @param victim 受害者
     * @param deathReason 死亡原因
     * @return true 表示成功免疫，应阻止死亡
     */
    private static boolean handleStalkerImmunity(PlayerEntity victim, Identifier deathReason) {
        if (victim == null || victim.getWorld().isClient()) return false;

        // 获取跟踪者组件
        StalkerPlayerComponent stalkerComp = ModComponents.STALKER.get(victim);

        // 检查是否是活跃的跟踪者且处于二阶段或以上
        if (!stalkerComp.isActiveStalker()) return false;
        if (stalkerComp.phase < 2) return false;

        // 检查免疫是否已使用
        if (stalkerComp.immunityUsed) return false;

        // 消耗免疫
        stalkerComp.immunityUsed = true;
        stalkerComp.sync();

        // 播放音效
        victim.getWorld().playSound(null, victim.getBlockPos(),
                dev.doctor4t.trainmurdermystery.index.TMMSounds.ITEM_PSYCHO_ARMOUR,
                SoundCategory.MASTER, 5.0F, 1.0F);

        // 发送消息
        if (victim instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.sendMessage(
                    Text.translatable("message.noellesroles.stalker.immunity_triggered")
                            .formatted(Formatting.GREEN, Formatting.BOLD),
                    true
            );
        }

        return true;
    }

    /**
     * 处理傀儡师死亡
     * 假人死亡时返回本体，本体死亡时真正死亡
     *
     * @param victim 受害者
     * @param deathReason 死亡原因
     * @return true 表示假人死亡（阻止真正死亡），false 表示正常处理
     */
    private static boolean handlePuppeteerDeath(PlayerEntity victim, Identifier deathReason) {
        if (victim == null || victim.getWorld().isClient()) return false;

        // 获取傀儡师组件
        PuppeteerPlayerComponent puppeteerComp = ModComponents.PUPPETEER.get(victim);

        // 检查是否是活跃的傀儡师
        if (!puppeteerComp.isActivePuppeteer()) return false;

        // 检查是否正在操控假人
        if (!puppeteerComp.isControllingPuppet) return false;

        // 假人死亡，返回本体
        puppeteerComp.onPuppetDeath();

        return true; // 阻止真正死亡
    }


    public void registerPackets() {
//        ServerPlayNetworking.registerGlobalReceiver(ThiefStealC2SPacket.ID, (payload, context) -> {
//            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(context.player().getWorld());
//            if (!gameWorldComponent.isRole(context.player(), ModRoles.THIEF)) {
//                return;
//            }
//            AbilityPlayerComponent abilityComponent = AbilityPlayerComponent.KEY.get(context.player());
//            if (abilityComponent.cooldown > 0) {
//                return;
//            }
//            ThiefPlayerComponent thiefComponent = ThiefPlayerComponent.KEY.get(context.player());
//            boolean hasBlackout = thiefComponent.hasBlackoutEffect;
//            PlayerEntity targetPlayer = context.player().getWorld().getPlayerByUuid(payload.target());
//            if (targetPlayer == null) {
//                return;
//            }
//            if (context.player().distanceTo(targetPlayer) > 2.0D) {
//                return;
//            }
//            boolean isTargetAlive = GameFunctions.isPlayerAliveAndSurvival(targetPlayer);
//            if (!hasBlackout && isTargetAlive) {
//                return;
//            }
//            if (hasBlackout && !isTargetAlive) {
//                return;
//            }
//            PlayerShopComponent targetShop = PlayerShopComponent.KEY.get(targetPlayer);
//
//            int stolenCoins = targetShop.balance;
//            if (stolenCoins > 0) {
//                PlayerShopComponent thiefShop = PlayerShopComponent.KEY.get(context.player());
//                targetShop.balance = 0;
//                thiefShop.balance = thiefShop.balance + stolenCoins;
//                targetShop.sync();
//                thiefShop.sync();
//                abilityComponent.setCooldown(GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().thiefStealCooldown));
//                context.player().sendMessage(Text.translatable("message.thief.stole", stolenCoins), true);
//                thiefComponent.deactivateBlackout();
//
//                if (context.player() instanceof ServerPlayerEntity serverPlayer) {
//                    serverPlayer.networkHandler.sendPacket(new PlaySoundS2CPacket(
//                        Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY),
//                        SoundCategory.PLAYERS,
//                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
//                        1.0F, 0.9F + serverPlayer.getRandom().nextFloat() * 0.2F,
//                        serverPlayer.getRandom().nextLong()
//                    ));
//                }
//            }
//        });
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.MORPH_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());

            if (payload.player() == null) return;
            if (abilityPlayerComponent.cooldown > 0) return;
            if (context.player().getWorld().getPlayerByUuid(payload.player()) == null) return;

            if (gameWorldComponent.isRole(context.player(), ModRoles.VOODOO)) {
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().voodooCooldown);
                abilityPlayerComponent.sync();
                VoodooPlayerComponent voodooPlayerComponent = (VoodooPlayerComponent) VoodooPlayerComponent.KEY.get(context.player());
                voodooPlayerComponent.setTarget(payload.player());

            }
            if (gameWorldComponent.isRole(context.player(), ModRoles.MORPHLING)) {
                MorphlingPlayerComponent morphlingPlayerComponent = (MorphlingPlayerComponent) MorphlingPlayerComponent.KEY.get(context.player());
                morphlingPlayerComponent.startMorph(payload.player());
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.VULTURE_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());

            if (gameWorldComponent.isRole(context.player(), ModRoles.VULTURE) && GameFunctions.isPlayerAliveAndSurvival(context.player())) {
                if (abilityPlayerComponent.cooldown > 0) return;
                abilityPlayerComponent.sync();
                List<PlayerBodyEntity> playerBodyEntities = context.player().getWorld().getEntitiesByType(TypeFilter.equals(PlayerBodyEntity.class), context.player().getBoundingBox().expand(10), (playerBodyEntity -> {
                    return playerBodyEntity.getUuid().equals(payload.playerBody());
                }));
                if (!playerBodyEntities.isEmpty()) {
                    BodyDeathReasonComponent bodyDeathReasonComponent = BodyDeathReasonComponent.KEY.get(playerBodyEntities.getFirst());
                    if (!bodyDeathReasonComponent.vultured) {
                        abilityPlayerComponent.cooldown = GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().vultureEatCooldown);
                        VulturePlayerComponent vulturePlayerComponent = VulturePlayerComponent.KEY.get(context.player());
                        vulturePlayerComponent.bodiesEaten++;
                        vulturePlayerComponent.sync();
                        context.player().playSound(SoundEvents.ENTITY_PLAYER_BURP, 1.0F, 0.5F);
                        context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 40, 2));
                        if (vulturePlayerComponent.bodiesEaten >= vulturePlayerComponent.bodiesRequired) {
                            ArrayList<Role> shuffledKillerRoles = new ArrayList<>(TMMRoles.ROLES);
                            shuffledKillerRoles.removeIf(role ->role.identifier().equals(ModRoles.EXECUTIONER_ID) || Harpymodloader.VANNILA_ROLES.contains(role) || !role.canUseKiller() || HarpyModLoaderConfig.HANDLER.instance().disabled.contains(role.identifier().getPath()));
                            if (shuffledKillerRoles.isEmpty()) shuffledKillerRoles.add(TMMRoles.KILLER);
                            Collections.shuffle(shuffledKillerRoles);

                            PlayerShopComponent playerShopComponent = (PlayerShopComponent) PlayerShopComponent.KEY.get(context.player());
                            gameWorldComponent.addRole(context.player(),shuffledKillerRoles.getFirst());
                            ModdedRoleAssigned.EVENT.invoker().assignModdedRole(context.player(),shuffledKillerRoles.getFirst());
                            playerShopComponent.setBalance(100);
                            if (Harpymodloader.VANNILA_ROLES.contains(gameWorldComponent.getRole(context.player()))) {
                                ServerPlayNetworking.send((ServerPlayerEntity) context.player(), new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(TMMRoles.KILLER   ), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));
                            } else {
                                ServerPlayNetworking.send((ServerPlayerEntity) context.player(), new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(Harpymodloader.autogeneratedAnnouncements.get(gameWorldComponent.getRole(context.player()))), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));

                            }
                        }

                        bodyDeathReasonComponent.vultured = true;
                        bodyDeathReasonComponent.sync();
                    }
                }

            }
        });
        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.SWAP_PACKET, (payload, context) -> {
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            if (gameWorldComponent.isRole(context.player(), ModRoles.SWAPPER)) {
                if (payload.player() != null) {
                    if (context.player().getWorld().getPlayerByUuid(payload.player()) != null) {
                        if (payload.player2() != null) {
                            if (context.player().getWorld().getPlayerByUuid(payload.player2()) != null) {
                                PlayerEntity player1 = context.player().getWorld().getPlayerByUuid(payload.player2());
                                PlayerEntity player2 = context.player().getWorld().getPlayerByUuid(payload.player());
                                Vec3d swapperPos = context.player().getWorld().getPlayerByUuid(payload.player2()).getPos();
                                Vec3d swappedPos = context.player().getWorld().getPlayerByUuid(payload.player()).getPos();
                                if (!context.player().getWorld().isSpaceEmpty(player1)) return;
                                if (!context.player().getWorld().isSpaceEmpty(player2)) return;
                                context.player().getWorld().getPlayerByUuid(payload.player2()).refreshPositionAfterTeleport(swappedPos.x, swappedPos.y, swappedPos.z);
                                context.player().getWorld().getPlayerByUuid(payload.player()).refreshPositionAfterTeleport(swapperPos.x, swapperPos.y, swapperPos.z);
                            }
                        }
                    }
                }
                AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(1, 0);
                abilityPlayerComponent.sync();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.EXECUTIONER_SELECT_TARGET_PACKET, (payload, context) -> {
            // 检查是否启用了手动选择目标功能
            if (!NoellesRolesConfig.HANDLER.instance().executionerCanSelectTarget) {
                return; // 如果未启用，则忽略该数据包
            }

            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            if (gameWorldComponent.isRole(context.player(), ModRoles.EXECUTIONER)) {
                ExecutionerPlayerComponent executionerPlayerComponent = ExecutionerPlayerComponent.KEY.get(context.player());
                if (executionerPlayerComponent.targetSelected) return;

                if (payload.target() != null) {
                    PlayerEntity targetPlayer = context.player().getWorld().getPlayerByUuid(payload.target());
                    if (targetPlayer != null && GameFunctions.isPlayerAliveAndSurvival(targetPlayer)) {
                        if (gameWorldComponent.getRole(targetPlayer).isInnocent()) {
                            executionerPlayerComponent.setTarget(payload.target());
                        } else {
                            context.player().sendMessage(Text.translatable("message.error.executioner.invalid_target"), true);
                        }
                    } else {
                        context.player().sendMessage(Text.translatable("message.error.executioner.target_not_found"), true);
                    }
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(org.agmas.noellesroles.packet.BroadcasterC2SPacket.ID, (payload, context) -> {
            AbilityPlayerComponent abilityPlayerComponent = AbilityPlayerComponent.KEY.get(context.player());
            GameWorldComponent gameWorldComponent = GameWorldComponent.KEY.get(context.player().getWorld());
            PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());

            if (gameWorldComponent.isRole(context.player(), ModRoles.BROADCASTER)) {
                if (playerShopComponent.balance < 150) {
                    context.player().sendMessage(Text.translatable("message.noellesroles.insufficient_funds"), true);
                    if (context.player() instanceof ServerPlayerEntity) {
                        ServerPlayerEntity player = (ServerPlayerEntity) context.player();
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(Registries.SOUND_EVENT.getEntry(TMMSounds.UI_SHOP_BUY_FAIL), SoundCategory.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F, player.getRandom().nextLong()));
                    }
                    return;
                }
                String message = payload.message();
                if (message.length() > 256) {
                    message = message.substring(0, 256);
                }
                playerShopComponent.balance -= 150;
                playerShopComponent.sync();

                for (ServerPlayerEntity player : Objects.requireNonNull(context.player().getServer()).getPlayerManager().getPlayerList()) {
                    //Text broadcastText = Text.translatable("message.broadcaster.broadcast", context.player().getName(), Text.literal(message));
                    org.agmas.noellesroles.packet.BroadcastMessageS2CPacket packet = new org.agmas.noellesroles.packet.BroadcastMessageS2CPacket(Text.literal(message));
                    net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, packet);
                }
                abilityPlayerComponent.cooldown = 0;
                abilityPlayerComponent.sync();
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(Noellesroles.ABILITY_PACKET, (payload, context) -> {
            AbilityPlayerComponent abilityPlayerComponent = (AbilityPlayerComponent) AbilityPlayerComponent.KEY.get(context.player());
            GameWorldComponent gameWorldComponent = (GameWorldComponent) GameWorldComponent.KEY.get(context.player().getWorld());
            if (gameWorldComponent.isRole(context.player(), ModRoles.RECALLER) && abilityPlayerComponent.cooldown <= 0) {
                RecallerPlayerComponent recallerPlayerComponent = RecallerPlayerComponent.KEY.get(context.player());
                PlayerShopComponent playerShopComponent = PlayerShopComponent.KEY.get(context.player());
                if (!recallerPlayerComponent.placed) {
                    abilityPlayerComponent.cooldown = GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().recallerMarkCooldown);
                    recallerPlayerComponent.setPosition();
                }
                else if (playerShopComponent.balance >= 100) {
                    playerShopComponent.balance -= 100;
                    playerShopComponent.sync();
                    abilityPlayerComponent.cooldown = GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().recallerTeleportCooldown);
                    recallerPlayerComponent.teleport();
                }

            }
            if (gameWorldComponent.isRole(context.player(), ModRoles.PHANTOM) && abilityPlayerComponent.cooldown <= 0) {
                context.player().addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, NoellesRolesConfig.HANDLER.instance().phantomInvisibilityDuration * 20, 0, true, false, true));
                abilityPlayerComponent.cooldown = GameConstants.getInTicks(0, NoellesRolesConfig.HANDLER.instance().phantomInvisibilityCooldown);
            } else if (gameWorldComponent.isRole(context.player(), ModRoles.THIEF) && abilityPlayerComponent.cooldown <= 0) {

            }
        });
    }



}