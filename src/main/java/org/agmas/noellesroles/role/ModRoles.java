package org.agmas.noellesroles.role;

import dev.doctor4t.trainmurdermystery.api.Role;
import dev.doctor4t.trainmurdermystery.api.TMMRoles;
import dev.doctor4t.trainmurdermystery.client.gui.RoleAnnouncementTexts;
import net.minecraft.util.Identifier;
import org.agmas.harpymodloader.Harpymodloader;
import org.agmas.noellesroles.Noellesroles;

import java.awt.*;
import java.util.HashMap;

/**
 * 角色定义类
 * 
 * 在这里定义所有自定义角色
 * 
 * ==================== 角色参数说明 ====================
 * 
 * Role 构造函数参数：
 * 1. identifier      - 角色唯一标识符 (Identifier)
 * 2. color           - 角色颜色 (int RGB)，用于 UI 显示
 * 3. isInnocent      - 是否为无辜者阵营 (boolean)
 *                      true  = 乘客阵营（需要完成任务，被杀手视为目标）
 *                      false = 非乘客阵营
 * 4. canUseKiller    - 是否可以使用杀手功能 (boolean)
 *                      true  = 可以使用刀、地道、杀手聊天、杀手商店
 *                      false = 不能使用杀手功能
 * 5. moodType        - 心情类型 (Role.MoodType)
 *                      REAL = 真实心情（乘客用）
 *                      FAKE = 假心情（杀手用，不会真正疯狂）
 * 6. maxSprintTime   - 最大冲刺时间 (int)
 *                      使用 TMMRoles.CIVILIAN.getMaxSprintTime() 获取默认值
 *                      Integer.MAX_VALUE = 无限冲刺
 * 7. hideOnScoreboard - 是否在计分板上隐藏 (boolean)
 *                      true  = 隐藏（杀手/中立通常隐藏）
 *                      false = 显示（乘客通常显示）
 * 
 * ==================== 阵营类型 ====================
 * 
 * | 阵营     | isInnocent | canUseKiller | 说明 |
 * |----------|------------|--------------|------|
 * | 乘客     | true       | false        | 普通平民，需要完成任务 |
 * | 杀手     | false      | true         | 可以杀人，使用地道和杀手商店 |
 * | 中立     | false      | false        | 特殊胜利条件，无杀手能力 |
 * | 邪恶乘客 | true       | true         | 乘客阵营但有杀手能力（特殊） |
 */
public class ModRoles {
    
    // ==================== 角色 ID 定义 ====================
    // 建议格式：MOD_ID:role_name
    
    // 乘客阵营角色 ID
    public static Identifier JESTER_ID = Noellesroles.id("jester");
    public static Identifier CONDUCTOR_ID = Noellesroles.id("conductor");
    public static Identifier BARTENDER_ID = Noellesroles.id("bartender");
    public static Identifier NOISEMAKER_ID = Noellesroles.id("noisemaker");
    public static Identifier AWESOME_BINGLUS_ID = Noellesroles.id("awesome_binglus");
    public static Identifier VOODOO_ID = Noellesroles.id("voodoo");
    public static Identifier RECALLER_ID = Noellesroles.id("recaller");
    public static Identifier BETTER_VIGILANTE_ID = Noellesroles.id("better_vigilante");
    public static Identifier BROADCASTER_ID = Noellesroles.id("broadcaster");
    public static Identifier GHOST_ID = Noellesroles.id("ghost");
    public static Identifier PHOTOGRAPHER_ID = Noellesroles.id("photographer");
    public static Identifier DOCTOR_ID = Noellesroles.id("doctor");
    public static Identifier ATTENDANT_ID = Noellesroles.id("attendant");
    public static Identifier SHERIFF_ID = Noellesroles.id("sheriff");
    public static Identifier CORONER_ID = Noellesroles.id("coroner");
    
    // 杀手阵营角色 ID
    public static Identifier MORPHLING_ID = Noellesroles.id("morphling");
    public static Identifier PHANTOM_ID = Noellesroles.id("phantom");
    public static Identifier SWAPPER_ID = Noellesroles.id("swapper");
    public static Identifier EXECUTIONER_ID = Noellesroles.id("executioner");
    public static Identifier GAMBLER_ID = Noellesroles.id("gambler");
    public static Identifier THIEF_ID = Noellesroles.id("thief");
    public static Identifier POISONER_ID = Noellesroles.id("poisoner");
    public static Identifier BANDIT_ID = Noellesroles.id("bandit");
    public static Identifier THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID = Noellesroles.id("the_insane_damned_paranoid_killer");
    
    // 中立阵营角色 ID
    public static Identifier VULTURE_ID = Noellesroles.id("vulture");
    
    // 自定义模组角色 ID - 乘客阵营
    public static final Identifier AVENGER_ID = Noellesroles.id("avenger");
    public static final Identifier EXAMPLE_CREW_ID = Noellesroles.id("example_crew");
    public static final Identifier SLIPPERY_GHOST_ID = Noellesroles.id("slippery_ghost");
    public static final Identifier TELEGRAPHER_ID = Noellesroles.id("telegrapher");
    public static final Identifier ENGINEER_ID = Noellesroles.id("engineer");
    public static final Identifier BOXER_ID = Noellesroles.id("boxer");
    public static final Identifier POSTMAN_ID = Noellesroles.id("postman");
    public static final Identifier DETECTIVE_ID = Noellesroles.id("detective");
    public static final Identifier ATHLETE_ID = Noellesroles.id("athlete");
    public static final Identifier STAR_ID = Noellesroles.id("star");
    public static final Identifier VETERAN_ID = Noellesroles.id("veteran");
    public static final Identifier SINGER_ID = Noellesroles.id("singer");
    public static final Identifier PSYCHOLOGIST_ID = Noellesroles.id("psychologist");
    
    // 自定义模组角色 ID - 杀手阵营
    public static final Identifier CONSPIRATOR_ID = Noellesroles.id("conspirator");
    public static final Identifier TRAPPER_ID = Noellesroles.id("trapper");
    
    // 自定义模组角色 ID - 中立阵营
    public static final Identifier STALKER_ID = Noellesroles.id("stalker");
    public static final Identifier ADMIRER_ID = Noellesroles.id("admirer");
    public static final Identifier PUPPETEER_ID = Noellesroles.id("puppeteer");
    public static final Identifier EXAMPLE_KILLER_ID = Noellesroles.id("example_killer");
    public static final Identifier EXAMPLE_NEUTRAL_ID = Noellesroles.id("example_neutral");

    // ==================== 已注册角色定义 ====================
    // 乘客阵营角色
    public static Role JESTER = TMMRoles.registerRole(new Role(JESTER_ID, new Color(255,86,243).getRGB(), false, false, Role.MoodType.FAKE, Integer.MAX_VALUE, true));
    public static Role CONDUCTOR = TMMRoles.registerRole(new Role(CONDUCTOR_ID, new Color(255, 205, 84).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role BARTENDER = TMMRoles.registerRole(new Role(BARTENDER_ID, new Color(217,241,240).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role NOISEMAKER = TMMRoles.registerRole(new Role(NOISEMAKER_ID, new Color(200, 255, 0).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role AWESOME_BINGLUS = TMMRoles.registerRole(new Role(AWESOME_BINGLUS_ID, new Color(155, 255, 168).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role VOODOO = TMMRoles.registerRole(new Role(VOODOO_ID, new Color(128, 114, 253).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role RECALLER = TMMRoles.registerRole(new Role(RECALLER_ID, new Color(158, 255, 255).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role BETTER_VIGILANTE = TMMRoles.registerRole(new Role(BETTER_VIGILANTE_ID, new Color(0, 255, 255).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role BROADCASTER = TMMRoles.registerRole(new Role(BROADCASTER_ID, new Color(0, 255, 0).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), true));
    public static Role GHOST = TMMRoles.registerRole(new Role(GHOST_ID, new Color(200, 200, 200).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), true));
    public static Role PHOTOGRAPHER = TMMRoles.registerRole(new Role(PHOTOGRAPHER_ID, (new Color(0, 128, 255)).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role DOCTOR = TMMRoles.registerRole(new Role(DOCTOR_ID, (new Color(0, 255, 255)).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    public static Role ATTENDANT = TMMRoles.registerRole(new Role(ATTENDANT_ID, (new Color(198, 185, 36)).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));
    
    // 杀手阵营角色
    public static Role MORPHLING = TMMRoles.registerRole(new Role(MORPHLING_ID, new Color(170, 2, 61).getRGB(), false, true, Role.MoodType.FAKE, Integer.MAX_VALUE, true));
    public static Role PHANTOM = TMMRoles.registerRole(new Role(PHANTOM_ID, new Color(80, 5, 5, 192).getRGB(), false, true, Role.MoodType.FAKE, Integer.MAX_VALUE, true));
    public static Role SWAPPER = TMMRoles.registerRole(new Role(SWAPPER_ID, new Color(255, 255, 0).getRGB(), false, true, Role.MoodType.FAKE, Integer.MAX_VALUE, true));
    public static Role EXECUTIONER = TMMRoles.registerRole(new Role(EXECUTIONER_ID, new Color(74, 27, 5).getRGB(), false, true, Role.MoodType.FAKE, Integer.MAX_VALUE, true));
    public static Role GAMBLER = TMMRoles.registerRole(new Role(GAMBLER_ID, new Color(128, 0, 128).getRGB(), false, false, Role.MoodType.FAKE, TMMRoles.CIVILIAN.getMaxSprintTime(), true));
    public static Role THIEF = TMMRoles.registerRole(new Role(THIEF_ID, new Color(139, 69, 19).getRGB(), false, false, Role.MoodType.FAKE, TMMRoles.CIVILIAN.getMaxSprintTime(), true));
    public static Role POISONER = TMMRoles.registerRole(new Role(POISONER_ID, (new Color(115, 0, 57)).getRGB(), false, true, Role.MoodType.FAKE, Integer.MAX_VALUE, true));
    public static Role BANDIT = TMMRoles.registerRole(new Role(BANDIT_ID, (new Color(196, 54, 18)).getRGB(), false, true, Role.MoodType.FAKE, Integer.MAX_VALUE, true));
    public static Role THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES = TMMRoles.registerRole(new Role(THE_INSANE_DAMNED_PARANOID_KILLER_OF_DOOM_DEATH_DESTRUCTION_AND_WAFFLES_ID, new Color(255, 0, 0, 192).getRGB(), false, true, Role.MoodType.FAKE, Integer.MAX_VALUE, true));
    
    // 中立阵营角色
    public static Role VULTURE = TMMRoles.registerRole(new Role(VULTURE_ID, new Color(181, 103, 0).getRGB(), false, false, Role.MoodType.FAKE, TMMRoles.CIVILIAN.getMaxSprintTime(), true));
    public static Role CORONER = TMMRoles.registerRole(new Role(CORONER_ID, new Color(122, 122, 122).getRGB(), true, false, Role.MoodType.REAL, TMMRoles.CIVILIAN.getMaxSprintTime(), false));

    // ==================== 自定义角色对象定义 ====================
    
    /**
     * 复仇者角色
     * - 属于乘客阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 开局白板，没有任何物品
     * - 当绑定的玩家死亡时，获得左轮手枪并看到凶手
     * - 绑定方式：默认随机，可配置为瞄准绑定
     */
    public static Role AVENGER;
    
    /**
     * 阴谋家角色
     * - 属于杀手阵营 (isInnocent = false, canUseKiller = false)
     * - 可以在商店购买"书页"物品（250金币）
     * - 右键使用书页打开GUI：选择玩家头像，再选择角色
     * - 如果猜测正确，目标玩家40秒后死亡
     * - 猜测错误无惩罚，但书页消耗
     */
    public static Role CONSPIRATOR;
    
    /**
     * 示例乘客角色
     * - 属于乘客阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     */
    public static Role EXAMPLE_CREW;
    
    /**
     * 示例杀手角色
     * - 属于杀手阵营 (isInnocent = false)
     * - 可以使用杀手能力 (canUseKiller = true)
     * - 假心情系统
     * - 无限冲刺
     * - 在计分板上隐藏
     */
    public static Role EXAMPLE_KILLER;
    
    /**
     * 示例中立角色
     * - 不属于乘客阵营 (isInnocent = false)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 假心情系统
     * - 标准冲刺时间
     * - 在计分板上隐藏
     */
    public static Role EXAMPLE_NEUTRAL;
    
    /**
     * 滑头鬼角色
     * - 不属于乘客阵营 (isInnocent = false)
     * - 不能使用杀手能力 (canUseKiller = false)，但有专属商店
     * - 假心情系统
     * - 标准冲刺时间
     * - 在计分板上隐藏
     * - 被动技能：每10秒获取50金币
     * - 专属商店：空包弹(100)、烟雾弹(300)、撬锁器(50)、关灯(200)
     * - 胜利条件：与杀手同胜
     */
    public static Role SLIPPERY_GHOST;
    
    /**
     * 电报员角色
     * - 属于乘客阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     * - 特殊能力：可以打开GUI编辑匿名消息并发送给所有玩家
     * - 使用限制：3次
     */
    public static Role TELEGRAPHER;
    
    /**
     * 工程师角色
     * - 属于乘客阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     * - 专属商店：
     *   - 加固门道具(75金币)：右键门使其能防一次撬棍，蹲下右键被卡住的门可解除卡住
     *   - 警报陷阱(120金币)：放置在门上，撬棍触发时发出警报声
     */
    public static Role ENGINEER;
    
    /**
     * 拳击手角色
     * - 属于乘客阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     * - 技能"准备姿势"：
     *   - 开局冷却45秒
     *   - 使用后进入3秒攻击架势，获得"拳头"武器
     *   - 进入架势时有1秒无敌
     *   - 拳头左键：击退目标并造成4秒缓慢效果
     *   - 攻击间隔1.2秒
     *   - 使用后冷却80秒
     */
    public static Role BOXER;
    
    /**
     * 邮差角色
     * - 属于乘客阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     * - 技能"隐秘传递"：
     *   - 花费350金币购买传递盒
     *   - 指针对准玩家并右键使用，打开传递界面（一格）
     *   - 双方可以将一样物品放入并交给对方
     *   - 无使用次数限制，但每次需要购买传递盒
     */
    public static Role POSTMAN;
    
    /**
     * 私家侦探角色
     * - 属于乘客阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     * - 技能"审查"：
     *   - 花费100金币
     *   - 指针对准一名玩家并按下技能键
     *   - 可以查看目标玩家的物品栏界面
     *   - 如果目标玩家移动则会关闭界面
     *   - 使用后冷却60秒
     */
    public static Role DETECTIVE;
    
    /**
     * 跟踪者角色
     * - 初始为中立阵营 (isInnocent = false, canUseKiller = false)
     * - 假心情系统
     * - 无限冲刺（一阶段）
     * - 在计分板上隐藏
     * - 三阶段进化机制：
     *   - 一阶段（潜伏者）：群体窥视积累能量，满150能量进阶
     *   - 二阶段（觉醒猎手）：转为杀手阵营，获得刀和一次免疫，杀2人+30能量进阶
     *   - 三阶段（狂暴追击者）：蓄力突进处决，180秒倒计时
     */
    public static Role STALKER;
    
    /**
     * 运动员角色
     * - 属于乘客阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 无限冲刺时间 (Integer.MAX_VALUE)
     * - 在计分板上显示
     * - 技能"疾跑"：
     *   - 使用后获得20秒的速度效果（无粒子，不显示效果图标）
     *   - 使用后冷却120秒（2分钟）
     */
    public static Role ATHLETE;
    
    /**
     * 慕恋者角色
     * - 中立阵营 (isInnocent = false, canUseKiller = false)
     * - 假心情系统
     * - 标准冲刺时间
     * - 在计分板上隐藏
     * - 技能"群体窥视"：
     *   - 按住技能键观察视野内的玩家
     *   - 每名被观察玩家每秒 +1 能量
     *   - 满300能量后变为随机杀手角色
     */
    public static Role ADMIRER;
    
    /**
     * 设陷者角色
     * - 属于杀手阵营 (isInnocent = false, canUseKiller = true)
     * - 假心情系统
     * - 标准冲刺时间
     * - 在计分板上隐藏
     * - 技能"灾厄印记"：
     *   - 使用技能对准地面设置隐形陷阱
     *   - 隐形的灾厄印记，其他玩家踩中会触发
     *   - 触发效果：发出巨响暴露位置并发光，施加"标记"
     *   - 被标记的玩家被囚禁在原地3秒
     *   - 触发两次后囚禁延长到10秒
     *   - 触发三次后囚禁延长到25秒
     */
    public static Role TRAPPER;
    
    /**
     * 明星角色
     * - 属于乘客阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     * - 被动技能：每20秒自动发光2秒
     * - 主动技能"聚光灯"：
     *   - 使用后让10格范围内的玩家视野都看向自己
     *   - 30秒冷却
     */
    public static Role STAR;
    
    /**
     * 退伍军人角色
     * - 属于好人阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     * - 特殊能力：
     *   - 开局获得一把刀
     *   - 左键或右键击杀一人后刀消失
     */
    public static Role VETERAN;
    
    /**
     * 歌手角色
     * - 属于好人阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     * - 特殊能力：
     *   - 按技能键随机播放原版唱片音乐
     *   - 60秒冷却
     */
    public static Role SINGER;
    
    /**
     * 心理学家角色
     * - 属于好人阵营 (isInnocent = true)
     * - 不能使用杀手能力 (canUseKiller = false)
     * - 真实心情系统
     * - 标准冲刺时间
     * - 在计分板上显示
     * - 特殊能力：
     *   - san满时，使用技能对准一个人
     *   - 对方不动，超过10秒可以把对方san回复满
     *   - 3分钟冷却
     */
    public static Role PSYCHOLOGIST;
    
    /**
     * 傀儡师角色
     * - 初始为中立阵营 (isInnocent = false, canUseKiller = false)
     * - 假心情系统
     * - 标准冲刺时间
     * - 在计分板上隐藏
     * - 阶段一（收集阶段）：
     *   - 右键尸体回收（消失），10秒冷却
     *   - 收集人数 >= 游戏总人数/6 时变为杀手阵营
     * - 阶段二（杀手阶段）：
     *   - 无法再回收尸体
     *   - 使用技能制造假人（使用收集的尸体皮肤）
     *   - 操控假人时，随机获得杀手职业
     *   - 假人和本体物品栏独立
     *   - 假人死亡回到本体，本体死亡则真正死亡
     *   - 操控限时1分钟，技能冷却3分钟
     *   - 本体状态无法购买商店
     */
    public static Role PUPPETEER;
    
    public static HashMap<Role, RoleAnnouncementTexts.RoleAnnouncementText> roleRoleAnnouncementTextHashMap = new HashMap<>();

    /**
     * 初始化并注册所有角色
     * 在模组初始化时调用
     */
    public static void init() {
        
        // ==================== 注册乘客阵营角色 ====================
        
        // 复仇者角色 - 乘客阵营
        AVENGER = TMMRoles.registerRole(new Role(
            AVENGER_ID,                                   // 角色 ID
            new Color(139, 0, 0).getRGB(),                // 暗红色 - 代表复仇的血色
            true,                                         // isInnocent = 乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 乘客阵营示例
        EXAMPLE_CREW = TMMRoles.registerRole(new Role(
            EXAMPLE_CREW_ID,                              // 角色 ID
            new Color(100, 200, 100).getRGB(),            // 绿色 - 乘客
            true,                                         // isInnocent = 乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 滑头鬼角色 - 中立阵营（使用专属商店）
        SLIPPERY_GHOST = TMMRoles.registerRole(new Role(
            SLIPPERY_GHOST_ID,                            // 角色 ID
            new Color(128, 128, 128).getRGB(),            // 灰色 - 代表滑头鬼的隐匿
            false,                                        // isInnocent = 非乘客阵营
            false,                                        // canUseKiller = 无杀手能力（使用专属商店）
            Role.MoodType.FAKE,                           // 假心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            true                                          // 隐藏计分板
        ));
        
        // 电报员角色 - 乘客阵营
        TELEGRAPHER = TMMRoles.registerRole(new Role(
            TELEGRAPHER_ID,                               // 角色 ID
            new Color(218, 165, 32).getRGB(),             // 金黄色 - 代表电报纸张
            true,                                         // isInnocent = 乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 工程师角色 - 乘客阵营
        ENGINEER = TMMRoles.registerRole(new Role(
            ENGINEER_ID,                                  // 角色 ID
            new Color(255, 140, 0).getRGB(),              // 橙色 - 代表工程帽/工具
            true,                                         // isInnocent = 乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 拳击手角色 - 乘客阵营
        BOXER = TMMRoles.registerRole(new Role(
            BOXER_ID,                                     // 角色 ID
            new Color(220, 20, 60).getRGB(),              // 猩红色 - 代表热血/格斗
            true,                                         // isInnocent = 乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 邮差角色 - 乘客阵营
        POSTMAN = TMMRoles.registerRole(new Role(
            POSTMAN_ID,                                   // 角色 ID
            new Color(70, 130, 180).getRGB(),             // 钢蓝色 - 代表邮差制服
            true,                                         // isInnocent = 乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 私家侦探角色 - 乘客阵营
        DETECTIVE = TMMRoles.registerRole(new Role(
            DETECTIVE_ID,                                 // 角色 ID
            new Color(139, 90, 43).getRGB(),              // 棕色 - 代表侦探风衣
            true,                                         // isInnocent = 乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 运动员角色 - 乘客阵营
        ATHLETE = TMMRoles.registerRole(new Role(
            ATHLETE_ID,                                   // 角色 ID
            new Color(0, 191, 255).getRGB(),              // 天蓝色 - 代表运动/活力
            true,                                         // isInnocent = 乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            Integer.MAX_VALUE,                            // 无限冲刺
            false                                         // 不隐藏计分板
        ));
        
        // 明星角色 - 乘客阵营
        STAR = TMMRoles.registerRole(new Role(
            STAR_ID,                                      // 角色 ID
            new Color(255, 215, 0).getRGB(),              // 金色 - 代表明星的光芒
            true,                                         // isInnocent = 乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 退伍军人角色 - 好人阵营
        VETERAN = TMMRoles.registerRole(new Role(
            VETERAN_ID,                                   // 角色 ID
            new Color(85, 107, 47).getRGB(),              // 暗橄榄绿 - 代表军装颜色
            true,                                         // isInnocent = 好人阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 歌手角色 - 好人阵营
        SINGER = TMMRoles.registerRole(new Role(
            SINGER_ID,                                    // 角色 ID
            new Color(255, 105, 180).getRGB(),            // 热粉色 - 代表音乐与激情
            true,                                         // isInnocent = 好人阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // 心理学家角色 - 好人阵营
        PSYCHOLOGIST = TMMRoles.registerRole(new Role(
            PSYCHOLOGIST_ID,                              // 角色 ID
            new Color(64, 224, 208).getRGB(),             // 青绿色 - 代表心灵治愈
            true,                                         // isInnocent = 好人阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.REAL,                           // 真实心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            false                                         // 不隐藏计分板
        ));
        
        // ==================== 注册杀手阵营角色 ====================
        
        // 阴谋家角色 - 杀手阵营
        CONSPIRATOR = TMMRoles.registerRole(new Role(
            CONSPIRATOR_ID,                               // 角色 ID
            new Color(75, 0, 130).getRGB(),               // 深紫色 - 代表阴谋与神秘
            false,                                        // isInnocent = 非乘客阵营
            true,                                         // canUseKiller = 有杀手能力（可以使用地道、杀手聊天）
            Role.MoodType.FAKE,                           // 假心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            true                                          // 隐藏计分板
        ));
        
        // 杀手阵营示例
        EXAMPLE_KILLER = TMMRoles.registerRole(new Role(
            EXAMPLE_KILLER_ID,                            // 角色 ID
            new Color(200, 50, 50).getRGB(),              // 红色 - 杀手
            false,                                        // isInnocent = 非乘客阵营
            true,                                         // canUseKiller = 有杀手能力
            Role.MoodType.FAKE,                           // 假心情
            Integer.MAX_VALUE,                            // 无限冲刺
            true                                          // 隐藏计分板
        ));
        
        // 设陷者角色 - 杀手阵营
        TRAPPER = TMMRoles.registerRole(new Role(
            TRAPPER_ID,                                   // 角色 ID
            new Color(239, 69, 30).getRGB(),              // 棕色 - 代表陷阱与大地
            false,                                        // isInnocent = 非乘客阵营
            true,                                         // canUseKiller = 有杀手能力
            Role.MoodType.FAKE,                           // 假心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            true                                          // 隐藏计分板
        ));
        
        // ==================== 注册中立阵营角色 ====================
        
        // 中立阵营示例
        EXAMPLE_NEUTRAL = TMMRoles.registerRole(new Role(
            EXAMPLE_NEUTRAL_ID,                           // 角色 ID
            new Color(150, 100, 200).getRGB(),            // 紫色 - 中立
            false,                                        // isInnocent = 非乘客阵营
            false,                                        // canUseKiller = 无杀手能力
            Role.MoodType.FAKE,                           // 假心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            true                                          // 隐藏计分板
        ));
        
        // 跟踪者角色 - 杀手阵营（一开始就是杀手，通过阶段控制能力）
        STALKER = TMMRoles.registerRole(new Role(
            STALKER_ID,                                   // 角色 ID
            new Color(75, 0, 130).getRGB(),               // 暗紫色 #4B0082
            false,                                        // isInnocent = 非乘客阵营
            true,                                         // canUseKiller = 杀手阵营
            Role.MoodType.FAKE,                           // 假心情
            Integer.MAX_VALUE,                            // 无限冲刺
            true                                          // 隐藏计分板
        ));
        
        // 慕恋者角色 - 中立阵营
        ADMIRER = TMMRoles.registerRole(new Role(
            ADMIRER_ID,                                   // 角色 ID
            new Color(255,86,243).getRGB() ,false,false, Role.MoodType.FAKE,Integer.MAX_VALUE,true));
        
        // 傀儡师角色 - 中立阵营（初始）
        PUPPETEER = TMMRoles.registerRole(new Role(
            PUPPETEER_ID,                                 // 角色 ID
            new Color(148, 0, 211).getRGB(),              // 深紫罗兰色 - 代表操控与神秘
            false,                                        // isInnocent = 非乘客阵营
            false,                                        // canUseKiller = 无杀手能力（初始）
            Role.MoodType.FAKE,                           // 假心情
            TMMRoles.CIVILIAN.getMaxSprintTime(),         // 标准冲刺时间
            true                                          // 隐藏计分板
        ));
        
        // ==================== 设置角色数量限制 ====================
        // 某些角色可能需要限制每局游戏中的数量
        
        // 复仇者每局只能有 1 个
        Harpymodloader.setRoleMaximum(AVENGER_ID, 1);
        
        // 阴谋家每局只能有 1 个
        Harpymodloader.setRoleMaximum(CONSPIRATOR_ID, 1);
        
        // 示例角色设置为0（不使用）
        Harpymodloader.setRoleMaximum(EXAMPLE_NEUTRAL_ID, 0);
        Harpymodloader.setRoleMaximum(EXAMPLE_KILLER_ID, 0);
        Harpymodloader.setRoleMaximum(EXAMPLE_CREW_ID, 0);
        
        // 滑头鬼每局只能有 1 个
        Harpymodloader.setRoleMaximum(SLIPPERY_GHOST_ID, 1);
        
        // 电报员每局只能有 1 个
        Harpymodloader.setRoleMaximum(TELEGRAPHER_ID, 1);
        
        // 工程师每局只能有 1 个
        Harpymodloader.setRoleMaximum(ENGINEER_ID, 1);
        
        // 拳击手每局只能有 1 个
        Harpymodloader.setRoleMaximum(BOXER_ID, 1);
        
        // 邮差每局只能有 1 个
        Harpymodloader.setRoleMaximum(POSTMAN_ID, 1);
        
        // 私家侦探每局只能有 1 个
        Harpymodloader.setRoleMaximum(DETECTIVE_ID, 1);
        
        // 跟踪者每局只能有 1 个
        Harpymodloader.setRoleMaximum(STALKER_ID, 1);
        
        // 运动员每局只能有 1 个
        Harpymodloader.setRoleMaximum(ATHLETE_ID, 1);
        
        // 慕恋者每局只能有 1 个
        Harpymodloader.setRoleMaximum(ADMIRER_ID, 1);
        
        // 设陷者每局只能有 1 个
        Harpymodloader.setRoleMaximum(TRAPPER_ID, 1);
        
        // 明星每局只能有 1 个
        Harpymodloader.setRoleMaximum(STAR_ID, 1);
        
        // 退伍军人每局只能有 1 个
        Harpymodloader.setRoleMaximum(VETERAN_ID, 1);
        
        // 歌手每局只能有 1 个
        Harpymodloader.setRoleMaximum(SINGER_ID, 1);
        
        // 心理学家每局只能有 1 个
        Harpymodloader.setRoleMaximum(PSYCHOLOGIST_ID, 1);
        
        // 傀儡师每局只能有 1 个
        Harpymodloader.setRoleMaximum(PUPPETEER_ID, 1);
        
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 检查角色是否为乘客阵营
     */
    public static boolean isCrewRole(Role role) {
        return role.isInnocent();
    }
    
    /**
     * 检查角色是否为杀手阵营
     */
    public static boolean isKillerRole(Role role) {
        return role.canUseKiller();
    }
    
    /**
     * 检查角色是否为中立阵营
     */
    public static boolean isNeutralRole(Role role) {
        return !role.isInnocent() && !role.canUseKiller();
    }
}