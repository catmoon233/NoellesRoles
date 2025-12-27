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
import net.minecraft.item.Items;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.Vec3d;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.events.ModdedRoleAssigned;
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
import org.agmas.noellesroles.roles.thief.ThiefPlayerComponent;
import org.agmas.noellesroles.roles.voodoo.VoodooPlayerComponent;
import org.agmas.noellesroles.roles.vulture.VulturePlayerComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;

import static org.agmas.noellesroles.RicesRoleRhapsody.initShops;

public class Noellesroles implements ModInitializer {

    public static String MOD_ID = "noellesroles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //public static Role SHERIFF = TMMRoles.registerRole(new Role(SHERIFF_ID, new Color(0, 0, 255).getRGB(),true,false, Role.MoodType.REAL, TMMRoles.VIGILANTE.getMaxSprintTime(),false));

    public static final CustomPayload.Id<MorphC2SPacket> MORPH_PACKET = MorphC2SPacket.ID;
    public static final CustomPayload.Id<SwapperC2SPacket> SWAP_PACKET = SwapperC2SPacket.ID;
    public static final CustomPayload.Id<AbilityC2SPacket> ABILITY_PACKET = AbilityC2SPacket.ID;
    public static final CustomPayload.Id<VultureEatC2SPacket> VULTURE_PACKET = VultureEatC2SPacket.ID;
    public static final CustomPayload.Id<ThiefStealC2SPacket> THIEF_PACKET = ThiefStealC2SPacket.ID;
    public static final ArrayList<Role> VANNILA_ROLES = new ArrayList<>();
    public static final ArrayList<Identifier> VANNILA_ROLE_IDS = new ArrayList<>();
    public static final CustomPayload.Id<ExecutionerSelectTargetC2SPacket> EXECUTIONER_SELECT_TARGET_PACKET = ExecutionerSelectTargetC2SPacket.ID;

    public static ArrayList<ShopEntry> FRAMING_ROLES_SHOP = new ArrayList<>();

    private static boolean gunsCooled = false;

    public static @NotNull Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }
    @Override
    public void onInitialize() {

        VANNILA_ROLES.add(TMMRoles.KILLER);
        VANNILA_ROLES.add(TMMRoles.VIGILANTE);
        VANNILA_ROLES.add(TMMRoles.CIVILIAN);
        VANNILA_ROLES.add(TMMRoles.LOOSE_END);

        VANNILA_ROLE_IDS.add(TMMRoles.LOOSE_END.identifier());
        VANNILA_ROLE_IDS.add(TMMRoles.VIGILANTE.identifier());
        VANNILA_ROLE_IDS.add(TMMRoles.CIVILIAN.identifier());
        VANNILA_ROLE_IDS.add(TMMRoles.KILLER.identifier());

        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.LOCKPICK.getDefaultStack(), 50, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(ModItems.DELUSION_VIAL.getDefaultStack(), 30, ShopEntry.Type.POISON));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.FIRECRACKER.getDefaultStack(), 5, ShopEntry.Type.TOOL));
        FRAMING_ROLES_SHOP.add(new FramingShopEntry(TMMItems.NOTE.getDefaultStack(), 5, ShopEntry.Type.TOOL));

        NoellesRolesConfig.HANDLER.load();
//        ModItems.init();
        NRSounds.initialize();
        registerMaxRoleCount();


        registerEvents();
        SetRoleMaxCommand.register();
        ConfigCommand.register();
        registerPackets();
        HSRItems.init();

        HSRSounds.init();


        Harpymodloader.setRoleMaximum(ModRoles.POISONER_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.BANDIT_ID, 2);
        Harpymodloader.setRoleMaximum(ModRoles.DOCTOR_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.ATTENDANT_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.POISONER_ID, 1);

        shopRegister();


//        PayloadTypeRegistry.playC2S().register(AntidoteUsePayload.ID, AntidoteUsePayload.CODEC);
//        PayloadTypeRegistry.playC2S().register(ToxinUsePayload.ID, ToxinUsePayload.CODEC);
//        PayloadTypeRegistry.playC2S().register(BanditRevolverShootPayload.ID, BanditRevolverShootPayload.CODEC);
//        ServerPlayNetworking.registerGlobalReceiver(AntidoteUsePayload.ID, new AntidoteUsePayload.Receiver());
//        ServerPlayNetworking.registerGlobalReceiver(ToxinUsePayload.ID, new ToxinUsePayload.Receiver());
//        ServerPlayNetworking.registerGlobalReceiver(BanditRevolverShootPayload.ID, new BanditRevolverShootPayload.Receiver());
        //NoellesRolesEntities.init();

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
                    ModRoles.CONSPIRATOR_ID, RicesRoleRhapsody.CONSPIRATOR_SHOP
            );
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.SLIPPERY_GHOST_ID, RicesRoleRhapsody.SLIPPERY_GHOST_SHOP
            );
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.ENGINEER_ID, RicesRoleRhapsody.ENGINEER_SHOP
            );
        }
        {
            ShopContent.customEntries.put(
                    ModRoles.POSTMAN_ID, RicesRoleRhapsody.POSTMAN_SHOP
            );
        }
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
                                ServerPlayNetworking.send((ServerPlayerEntity) context.player(), new AnnounceWelcomePayload(RoleAnnouncementTexts.ROLE_ANNOUNCEMENT_TEXTS.indexOf(TMMRoles.KILLER), gameWorldComponent.getAllKillerTeamPlayers().size(), 0));
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
