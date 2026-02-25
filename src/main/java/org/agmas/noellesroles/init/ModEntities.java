package org.agmas.noellesroles.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.entity.*;


public class ModEntities {
    public static final EntityType<RoleMineEntity> ROLE_MINE_ENTITY_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "cube"),
            EntityType.Builder.of(RoleMineEntity::new, MobCategory.MISC).sized(0.75f, 0.75f).build("cube"));
    public static final EntityType<WheelchairEntity> WHEELCHAIR = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Noellesroles.id("wheelchair"),
            EntityType.Builder.of(WheelchairEntity::new, MobCategory.MISC).sized(0.8f, 0.2f)
                    .build("wheelchair"));

    @SuppressWarnings("deprecation")
    public static final EntityType<SmokeGrenadeEntity> SMOKE_GRENADE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "smoke_grenade"),
            FabricEntityTypeBuilder.<SmokeGrenadeEntity>create(MobCategory.MISC, SmokeGrenadeEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeBlocks(4)
                    .trackedUpdateRate(10)
                    .build());

    /**
     * 灾厄印记实体 - 设陷者专属隐形陷阱
     */
    @SuppressWarnings("deprecation")
    public static final EntityType<CalamityMarkEntity> CALAMITY_MARK = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "calamity_mark"),
            FabricEntityTypeBuilder.<CalamityMarkEntity>create(MobCategory.MISC, CalamityMarkEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5F, 0.1F))
                    .trackRangeBlocks(32)
                    .trackedUpdateRate(20)
                    .build());

    /**
     * 傀儡本体实体 - 傀儡师使用假人技能时生成的本体
     */
    @SuppressWarnings("deprecation")
    public static final EntityType<PuppeteerBodyEntity> PUPPETEER_BODY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "puppeteer_body"),
            FabricEntityTypeBuilder.<PuppeteerBodyEntity>create(MobCategory.MISC, PuppeteerBodyEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F)) // 玩家尺寸
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(2)
                    .build());

    /**
     * 操纵师本体实体 - 操纵师使用操控技能时生成的本体
     */
    @SuppressWarnings("deprecation")
    public static final EntityType<ManipulatorBodyEntity> MANIPULATOR_BODY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "manipulator_body"),
            FabricEntityTypeBuilder
                    .<ManipulatorBodyEntity>create(MobCategory.MISC, ManipulatorBodyEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F))
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(2)
                    .build());

    /**
     * 锁实体 - 保护门不被撬锁器打开
     */
    @SuppressWarnings("deprecation")
    public static final EntityType<LockEntity> LOCK_ENTITY = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "lock"),
            FabricEntityTypeBuilder.<LockEntity>create(MobCategory.MISC, LockEntity::new)
                    .dimensions(EntityDimensions.fixed(0.2F, 0.2F))
                    .trackRangeBlocks(32)
                    .build());

    /**
     * 初始化实体
     * 注册实体属性（LivingEntity 需要）
     */
    public static void init() {
        // 轮椅
        FabricDefaultAttributeRegistry.register(WHEELCHAIR, WheelchairEntity.createAttributes());
        // 注册傀儡本体实体属性（LivingEntity 必须注册属性才能生成）
        FabricDefaultAttributeRegistry.register(PUPPETEER_BODY, LivingEntity.createLivingAttributes());
        // 注册操纵师本体实体属性
        FabricDefaultAttributeRegistry.register(MANIPULATOR_BODY, LivingEntity.createLivingAttributes());
    }

}
