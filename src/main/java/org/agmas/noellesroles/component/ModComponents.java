package org.agmas.noellesroles.component;

import dev.doctor4t.trainmurdermystery.entity.PlayerBodyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.agmas.noellesroles.ConfigWorldComponent;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.roles.bartender.BartenderPlayerComponent;
import org.agmas.noellesroles.roles.broadcaster.BroadcasterPlayerComponent;
import org.agmas.noellesroles.roles.coroner.BodyDeathReasonComponent;
import org.agmas.noellesroles.roles.executioner.ExecutionerPlayerComponent;
import org.agmas.noellesroles.roles.gambler.GamblerPlayerComponent;
import org.agmas.noellesroles.roles.ghost.GhostPlayerComponent;
import org.agmas.noellesroles.roles.morphling.MorphlingPlayerComponent;
import org.agmas.noellesroles.roles.recaller.RecallerPlayerComponent;
import org.agmas.noellesroles.roles.thief.ThiefPlayerComponent;
import org.agmas.noellesroles.roles.voodoo.VoodooPlayerComponent;
import org.agmas.noellesroles.roles.vulture.VulturePlayerComponent;
import org.agmas.noellesroles.roles.manipulator.ManipulatorPlayerComponent;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;

/**
 * Cardinal Components API 组件注册
 *
 * 这个类在 fabric.mod.json 中被注册为 "cardinal-components" 入口点
 * 用于注册所有自定义的数据组件
 *
 * 组件用途：
 * - 存储玩家的技能冷却时间
 * - 存储角色特定的状态数据
 * - 在客户端和服务端之间同步数据
 *
 * 重要：所有 ComponentKey 必须在这里集中定义，以避免类加载顺序问题
 */
public class ModComponents implements EntityComponentInitializer , WorldComponentInitializer {

    // ==================== 组件键定义 ====================
    // 所有 ComponentKey 集中在这里定义，确保在 CCA 初始化时正确注册
    
    public static final ComponentKey<AbilityPlayerComponent> ABILITY = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "ability"),
        AbilityPlayerComponent.class
    );
    
    public static final ComponentKey<AvengerPlayerComponent> AVENGER = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "avenger"),
        AvengerPlayerComponent.class
    );
    
    public static final ComponentKey<ConspiratorPlayerComponent> CONSPIRATOR = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "conspirator"),
        ConspiratorPlayerComponent.class
    );
    
    public static final ComponentKey<SlipperyGhostPlayerComponent> SLIPPERY_GHOST = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "slippery_ghost"),
        SlipperyGhostPlayerComponent.class
    );
    
    public static final ComponentKey<TelegrapherPlayerComponent> TELEGRAPHER = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "telegrapher"),
        TelegrapherPlayerComponent.class
    );
    
    public static final ComponentKey<PostmanPlayerComponent> POSTMAN = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "postman"),
        PostmanPlayerComponent.class
    );
    
    public static final ComponentKey<DetectivePlayerComponent> DETECTIVE = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "detective"),
        DetectivePlayerComponent.class
    );
    
    public static final ComponentKey<BoxerPlayerComponent> BOXER = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "boxer"),
        BoxerPlayerComponent.class
    );
    
    public static final ComponentKey<StalkerPlayerComponent> STALKER = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "stalker"),
        StalkerPlayerComponent.class
    );
    
    public static final ComponentKey<AthletePlayerComponent> ATHLETE = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "athlete"),
        AthletePlayerComponent.class
    );
    
    public static final ComponentKey<AdmirerPlayerComponent> ADMIRER = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "admirer"),
        AdmirerPlayerComponent.class
    );
    
    public static final ComponentKey<TrapperPlayerComponent> TRAPPER = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "trapper"),
        TrapperPlayerComponent.class
    );
    
    public static final ComponentKey<StarPlayerComponent> STAR = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "star"),
        StarPlayerComponent.class
    );
    
    public static final ComponentKey<VeteranPlayerComponent> VETERAN = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "veteran"),
        VeteranPlayerComponent.class
    );
    
    public static final ComponentKey<SingerPlayerComponent> SINGER = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "singer"),
        SingerPlayerComponent.class
    );
    
    public static final ComponentKey<PsychologistPlayerComponent> PSYCHOLOGIST = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "psychologist"),
        PsychologistPlayerComponent.class
    );
    
    public static final ComponentKey<PuppeteerPlayerComponent> PUPPETEER = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "puppeteer"),
        PuppeteerPlayerComponent.class
    );
    
    public static final ComponentKey<ManipulatorPlayerComponent> MANIPULATOR = ComponentRegistry.getOrCreate(
        ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "manipulator"),
        ManipulatorPlayerComponent.class
    );

    public ModComponents() {
        // CCA 需要无参构造函数
    }
    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry worldComponentFactoryRegistry) {
        worldComponentFactoryRegistry.register(ConfigWorldComponent.KEY, ConfigWorldComponent::new);
    }
    @Override
    public void registerEntityComponentFactories(@NotNull EntityComponentFactoryRegistry registry) {

        // 注册通用技能组件 - 附加到玩家实体
        // RespawnCopyStrategy.NEVER_COPY 表示玩家重生时不保留数据（游戏开始时会重新初始化）
        registry.beginRegistration(Player.class, ABILITY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(AbilityPlayerComponent::new);

        // 注册复仇者组件 - 存储绑定目标和激活状态
        registry.beginRegistration(Player.class, AVENGER)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(AvengerPlayerComponent::new);

        // 注册阴谋家组件 - 存储目标和死亡倒计时
        registry.beginRegistration(Player.class, CONSPIRATOR)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ConspiratorPlayerComponent::new);

        // 注册滑头鬼组件 - 被动收入计时器
        registry.beginRegistration(Player.class, SLIPPERY_GHOST)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SlipperyGhostPlayerComponent::new);

        // 注册电报员组件 - 存储使用次数
        registry.beginRegistration(Player.class, TELEGRAPHER)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TelegrapherPlayerComponent::new);

        // 注册邮差组件 - 存储传递状态和物品
        registry.beginRegistration(Player.class, POSTMAN)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PostmanPlayerComponent::new);

        // 注册私家侦探组件 - 存储审查技能冷却和目标状态
        registry.beginRegistration(Player.class, DETECTIVE)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(DetectivePlayerComponent::new);

        // 注册拳击手组件 - 存储钢筋铁骨技能状态
        registry.beginRegistration(Player.class, BOXER)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(BoxerPlayerComponent::new);

        // 注册跟踪者组件 - 存储三阶段状态
        registry.beginRegistration(Player.class, STALKER)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(StalkerPlayerComponent::new);

        // 注册运动员组件 - 存储疾跑技能冷却和状态
        registry.beginRegistration(Player.class, ATHLETE)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(AthletePlayerComponent::new);

        // 注册慕恋者组件 - 存储能量和窥视状态
        registry.beginRegistration(Player.class, ADMIRER)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(AdmirerPlayerComponent::new);

        // 注册设陷者组件 - 存储陷阱和标记状态
        registry.beginRegistration(Player.class, TRAPPER)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(TrapperPlayerComponent::new);

        // 注册明星组件 - 存储发光状态和技能冷却
        registry.beginRegistration(Player.class, STAR)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(StarPlayerComponent::new);

        // 注册退伍军人组件 - 存储刀使用状态
        registry.beginRegistration(Player.class, VETERAN)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(VeteranPlayerComponent::new);

        // 注册歌手组件 - 存储音乐播放冷却状态
        registry.beginRegistration(Player.class, SINGER)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(SingerPlayerComponent::new);

        // 注册心理学家组件 - 存储治疗状态和冷却
        registry.beginRegistration(Player.class, PSYCHOLOGIST)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PsychologistPlayerComponent::new);

        // 注册傀儡师组件 - 存储收集尸体、假人操控状态
        registry.beginRegistration(Player.class, PUPPETEER)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(PuppeteerPlayerComponent::new);
        
        // 注册操纵师组件 - 存储被操纵目标和控制状态
        registry.beginRegistration(Player.class, MANIPULATOR)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .end(ManipulatorPlayerComponent::new);

        registry.beginRegistration(Player.class, GamblerPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(GamblerPlayerComponent::new);
        registry.beginRegistration(Player.class, MorphlingPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(MorphlingPlayerComponent::new);
        registry.beginRegistration(Player.class, BartenderPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(BartenderPlayerComponent::new);
        registry.beginRegistration(Player.class, VoodooPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(VoodooPlayerComponent::new);
        registry.beginRegistration(PlayerBodyEntity.class, BodyDeathReasonComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(BodyDeathReasonComponent::new);
        registry.beginRegistration(Player.class, org.agmas.noellesroles.AbilityPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(org.agmas.noellesroles.AbilityPlayerComponent::new);
        registry.beginRegistration(Player.class, ExecutionerPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(ExecutionerPlayerComponent::new);
        registry.beginRegistration(Player.class, RecallerPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(RecallerPlayerComponent::new);
        registry.beginRegistration(Player.class, BroadcasterPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(BroadcasterPlayerComponent::new);
        registry.beginRegistration(Player.class, GhostPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(GhostPlayerComponent::new);
        registry.beginRegistration(Player.class, VulturePlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(VulturePlayerComponent::new);
        registry.beginRegistration(Player.class, ThiefPlayerComponent.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(ThiefPlayerComponent::new);


        // ==================== 示例：注册更多组件 ====================
        //
        // 如果你的角色需要存储特定数据，可以在这里注册更多组件：
        //
        // 1. 先在上面定义 ComponentKey
        // public static final ComponentKey<ExampleRoleComponent> EXAMPLE = ComponentRegistry.getOrCreate(
        //     Identifier.of(Noellesroles.MOD_ID, "example"),
        //     ExampleRoleComponent.class
        // );
        //
        // 2. 然后在这里注册
        // registry.beginRegistration(PlayerEntity.class, EXAMPLE)
        // .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
        // .end(ExampleRoleComponent::new);

    }
}