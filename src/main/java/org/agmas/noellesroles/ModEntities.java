package org.agmas.noellesroles;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.entity.CalamityMarkEntity;
import org.agmas.noellesroles.entity.PuppeteerBodyEntity;
import org.agmas.noellesroles.entity.RoleMineEntity;
import org.agmas.noellesroles.entity.SmokeGrenadeEntity;

public class ModEntities {
    public static final EntityType<RoleMineEntity> ROLE_MINE_ENTITY_ENTITY_TYPE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Noellesroles.MOD_ID, "cube"),
            EntityType.Builder.create(RoleMineEntity::new, SpawnGroup.MISC).dimensions(0.75f, 0.75f).build("cube")
    );

    public static final EntityType<SmokeGrenadeEntity> SMOKE_GRENADE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Noellesroles.MOD_ID, "smoke_grenade"),
            FabricEntityTypeBuilder.<SmokeGrenadeEntity>create(SpawnGroup.MISC, SmokeGrenadeEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeBlocks(4)
                    .trackedUpdateRate(10)
                    .build());

    /**
     * 灾厄印记实体 - 设陷者专属隐形陷阱
     */
    public static final EntityType<CalamityMarkEntity> CALAMITY_MARK = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Noellesroles.MOD_ID, "calamity_mark"),
            FabricEntityTypeBuilder.<CalamityMarkEntity>create(SpawnGroup.MISC, CalamityMarkEntity::new)
                    .dimensions(EntityDimensions.fixed(0.5F, 0.1F))
                    .trackRangeBlocks(32)
                    .trackedUpdateRate(20)
                    .build());

    /**
     * 傀儡本体实体 - 傀儡师使用假人技能时生成的本体
     */
    public static final EntityType<PuppeteerBodyEntity> PUPPETEER_BODY = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Noellesroles.MOD_ID, "puppeteer_body"),
            FabricEntityTypeBuilder.<PuppeteerBodyEntity>create(SpawnGroup.MISC, PuppeteerBodyEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 1.8F)) // 玩家尺寸
                    .trackRangeBlocks(64)
                    .trackedUpdateRate(2)
                    .build());

    /**
     * 初始化实体
     * 注册实体属性（LivingEntity 需要）
     */
    public static void init() {
        // 注册傀儡本体实体属性（LivingEntity 必须注册属性才能生成）
        FabricDefaultAttributeRegistry.register(PUPPETEER_BODY, LivingEntity.createLivingAttributes());
    }


}
