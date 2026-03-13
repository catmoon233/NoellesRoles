package org.agmas.noellesroles.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.entity.*;

/**
 * 模组实体注册
 */
public class ModEntities {

        /**
         * 傀戏傀儡实体类型
         */
        public static final EntityType<KuiXiPuppetEntity> KUIXI_PUPPET = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("kuixi_puppet"),
                        FabricEntityTypeBuilder.create(MobCategory.MISC, KuiXiPuppetEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.6f, 1.8f)) // 玩家大小
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(3)
                                        .build());

        /**
         * 投掷刀实体类型
         */
        public static final EntityType<ThrowingKnifeEntity> THROWING_KNIFE = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("throwing_knife"),
                        FabricEntityTypeBuilder.<ThrowingKnifeEntity>create(MobCategory.MISC, ThrowingKnifeEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(10)
                                        .build());

        /**
         * 烟雾弹实体类型
         */
        public static final EntityType<SmokeGrenadeEntity> SMOKE_GRENADE = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("smoke_grenade"),
                        FabricEntityTypeBuilder.<SmokeGrenadeEntity>create(MobCategory.MISC, SmokeGrenadeEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(10)
                                        .build());

        /**
         * 氯气弹实体类型
         */
        public static final EntityType<ChlorineBombEntity> CHLORINE_BOMB = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("chlorine_bomb"),
                        FabricEntityTypeBuilder.<ChlorineBombEntity>create(MobCategory.MISC, ChlorineBombEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(10)
                                        .build());

        /**
         * 净化弹实体类型
         */
        public static final EntityType<PurifyBombEntity> PURIFY_BOMB = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("purify_bomb"),
                        FabricEntityTypeBuilder.<PurifyBombEntity>create(MobCategory.MISC, PurifyBombEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(10)
                                        .build());

        /**
         * 闪光弹实体类型
         */
        public static final EntityType<FlashGrenadeEntity> FLASH_GRENADE = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("flash_grenade"),
                        FabricEntityTypeBuilder.<FlashGrenadeEntity>create(MobCategory.MISC, FlashGrenadeEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(10)
                                        .build());

        /**
         * 诱饵弹实体类型
         */
        public static final EntityType<DecoyGrenadeEntity> DECOY_GRENADE = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("decoy_grenade"),
                        FabricEntityTypeBuilder.<DecoyGrenadeEntity>create(MobCategory.MISC, DecoyGrenadeEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(10)
                                        .build());

        /**
         * 灾厄标记实体类型
         */
        public static final EntityType<CalamityMarkEntity> CALAMITY_MARK = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("calamity_mark"),
                        FabricEntityTypeBuilder.<CalamityMarkEntity>create(MobCategory.MISC, CalamityMarkEntity::new)
                                        .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(3)
                                        .build());

        /**
         * 傀儡师身体实体类型
         */
        public static final EntityType<PuppeteerBodyEntity> PUPPETEER_BODY = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("puppeteer_body"),
                        FabricEntityTypeBuilder.<PuppeteerBodyEntity>create(MobCategory.MISC, PuppeteerBodyEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(3)
                                        .build());

        /**
         * 操控者身体实体类型
         */
        public static final EntityType<ManipulatorBodyEntity> MANIPULATOR_BODY = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("manipulator_body"),
                        FabricEntityTypeBuilder
                                        .<ManipulatorBodyEntity>create(MobCategory.MISC, ManipulatorBodyEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.6f, 1.8f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(3)
                                        .build());

        /**
         * 锁实体类型
         */
        public static final EntityType<LockEntity> LOCK_ENTITY = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("lock_entity"),
                        FabricEntityTypeBuilder.<LockEntity>create(MobCategory.MISC, LockEntity::new)
                                        .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(3)
                                        .build());

        /**
         * 轮椅实体类型
         */
        public static final EntityType<WheelchairEntity> WHEELCHAIR = Registry.register(
                        BuiltInRegistries.ENTITY_TYPE,
                        Noellesroles.id("wheelchair"),
                        FabricEntityTypeBuilder.<WheelchairEntity>create(MobCategory.MISC, WheelchairEntity::new)
                                        .dimensions(EntityDimensions.fixed(1.0f, 1.0f))
                                        .trackRangeBlocks(64)
                                        .trackedUpdateRate(3)
                                        .build());

        /**
         * 初始化实体注册
         */
        public static void init() {
                // 实体类型已在静态初始化时注册
                Noellesroles.LOGGER.info("Registered mod entities");
        }
}
