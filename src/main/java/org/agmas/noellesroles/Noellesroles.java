package org.agmas.noellesroles;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.cca.GameWorldComponent;
import dev.doctor4t.trainmurdermystery.entity.NoteEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import pro.fazeclan.river.stupid_express.constants.SEModifiers;

import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.harpymodloader.component.WorldModifierComponent;
import org.agmas.harpymodloader.config.HarpyModLoaderConfig;
import org.agmas.harpymodloader.modded_murder.RoleAssignmentManager;
import org.agmas.noellesroles.commands.*;
import org.agmas.noellesroles.component.*;
import org.agmas.noellesroles.entity.PuppeteerBodyEntity;
import org.agmas.noellesroles.role.ModRoles;
import org.agmas.noellesroles.blood.BloodMain;
import org.agmas.noellesroles.config.NoellesRolesConfig;
import org.agmas.noellesroles.roles.ghost.GhostPlayerComponent;
import org.agmas.noellesroles.repack.HSRConstants;
import org.agmas.noellesroles.repack.HSRItems;
import org.agmas.noellesroles.repack.HSRSounds;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class Noellesroles implements ModInitializer {
    public static final String MOD_ID = "noellesroles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ArrayList<Role> VANNILA_ROLES = new ArrayList<>();
    public static final ArrayList<ResourceLocation> VANNILA_ROLE_IDS = new ArrayList<>();

    public static boolean gunsCooled = false;
    // ==================== 初始物品配置 ====================

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
        Harpymodloader.HIDDEN_MODIFIERS.add(SEModifiers.REFUGEE.identifier().getPath());
        // 初始化模组角色列表
        ModRoles.init();
        // 初始化初始物品映射
        RoleInitialItems.initializeInitialItems();

        // 初始化原版角色列表
        initializeVanillaRoles();

        // 加载配置
        NoellesRolesConfig.HANDLER.load();
        RicesRoleRhapsody.onInitialize1();

        // 初始化系统组件
        NRSounds.initialize();
        registerMaxRoleCount();

        // 注册事件处理器
        ModEventsRegister.registerEvents();

        // 注册命令
        BroadcastCommand.register();
        AdminFreeCamCommand.register();
        SetRoleMaxCommand.register();
        ConfigCommand.register();
        LootCommand.register();
        DisplayItemCommand.register();

        // 注册网络包类型
        ModPackets.registerPackets();

        // 注册网络处理器
        ModPacketsReciever.registerPackets();
        // 初始化HSR组件
        HSRItems.init();
        HSRSounds.init();

        // 设置角色最大数量
        Harpymodloader.setRoleMaximum(ModRoles.POISONER_ID, 0);
        // 和医生一起生成
        Harpymodloader.setRoleMaximum(ModRoles.DOCTOR_ID, 0);
        Harpymodloader.setRoleMaximum(ModRoles.ATTENDANT_ID, 1);
        Harpymodloader.setRoleMaximum(ModRoles.CORONER_ID, 1);

        // 注册商店
        RoleShopHandler.shopRegister();
        TMM.canUseChatHud.add((role -> role.getIdentifier()
                .equals(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID)));
        TMM.canUseOtherPerson.add((role -> role.getIdentifier()
                .equals(ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID)));
        TMM.canUseOtherPerson.add((role -> role.getIdentifier()
                .equals(ModRoles.MANIPULATOR_ID)));
        TMM.canCollide.add(a -> {
            final var gameWorldComponent = GameWorldComponent.KEY.get(a.level());
            if (gameWorldComponent.isRole(a,
                    ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                if (InsaneKillerPlayerComponent.KEY.get(a).isActive) {
                    return true;
                }
            }
            return false;
        });
        TMM.canCollide.add(a -> {
            if (a.hasEffect(MobEffects.INVISIBILITY)) {
                return true;
            }
            return false;
        });
        TMM.cantPushableBy.add(entity -> {
            if (entity instanceof PuppeteerBodyEntity) {
                return true;
            }
            return false;
        });
        TMM.cantPushableBy.add(entity -> {
            if (entity instanceof Player serverPlayer) {
                if (serverPlayer.hasEffect(MobEffects.INVISIBILITY)) {
                    return true;
                } else {
                    var modifiers = WorldModifierComponent.KEY.get(serverPlayer.level());
                    if (modifiers.isModifier(serverPlayer.getUUID(), SEModifiers.FEATHER)) {
                        return true;
                    }
                    var gameComp = GameWorldComponent.KEY.get(serverPlayer.level());
                    if (gameComp != null) {
                        if (gameComp.isRole(serverPlayer,
                                ModRoles.THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES)) {
                            InsaneKillerPlayerComponent insaneKillerPlayerComponent = InsaneKillerPlayerComponent.KEY
                                    .get(serverPlayer);
                            if (insaneKillerPlayerComponent.isActive) {
                                return true;
                            }
                        }
                    }

                }
            }
            return false;
        });
        TMM.cantPushableBy.add(
                entity -> {
                    if (entity instanceof ServerPlayer serverPlayer) {
                        var gameComp = GameWorldComponent.KEY.get(serverPlayer.level());
                        if (gameComp != null) {
                            if (gameComp.isRole(serverPlayer, ModRoles.GHOST)) {
                                GhostPlayerComponent ghostPlayerComponent = GhostPlayerComponent.KEY.get(serverPlayer);
                                return ghostPlayerComponent.isActive;
                            }
                        }

                    }
                    return false;
                });
        TMM.canCollideEntity.add(entity -> {
            return entity instanceof PuppeteerBodyEntity;
        });
        TMM.cantPushableBy.add(entity -> {
            return (entity instanceof NoteEntity);
        });
        TMM.canDropItem.addAll(List.of(
                "exposure:stacked_photographs",
                "exposure:album",
                "exposure:photograph",
                "noellesroles:mint_candies"));

        // 同时出现
        RoleAssignmentManager.addOccupationRole(ModRoles.POISONER, ModRoles.DOCTOR);
        // 设置刀击中效果

        // 注册血液粒子工厂
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Noellesroles.id("deathblood"),
                BloodMain.BLOOD_PARTICLE);

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

    private void registerMaxRoleCount() {
        InitModRolesMax.registerStatics();
        InitModRolesMax.registerDynamic();
    }
}