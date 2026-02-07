package org.agmas.noellesroles.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.UUID;

/**
 * 抽奖管理器
 * - 实现抽奖功能
 * - 管理抽奖数据的调用
 */
public class LotteryManager {
    /**
     * 临时可抽奖项目管理器
     * TODO : 需要使用正确的可抽奖管理器管理
     */
    public static class TempLootAbleItemManager{
        // TODO: 作为皮肤查询使用，当皮肤系统建成后移动到皮肤管理器中
        private static final ResourceLocation[] skinList = {
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/lock.png"),
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/bomb.png"),
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/note.png"),
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/sp_knife.png"),
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/master_key.png"),
        };
        // 皮肤品质映射
        private static final int[] skinQualityList = {
                4,
                3,
                0,
                2,
                1,
        };
        private static final ResourceLocation[] qualityList = {
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/common_skin.png"),
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/uncommon_skin.png"),
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/rare_skin.png"),
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/epic_skin.png"),
                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/legendary_skin.png"),
        };
        // TODO : 这几个函数就是获取抽奖源的正确函数，当实装到皮肤系统中时需要替换这几个函数
        public static ResourceLocation getSkinResourceLocation(int index){
            return skinList[index];
        }
        public static ResourceLocation getQualityResourceLocation(int index){
            return qualityList[index];
        }
        public static int getSkinQuality(int index){
            return skinQualityList[index];
        }
        public static int getSkinListLen() {
            return skinList.length;
        }
        public static int getQualityListLen() {
            return qualityList.length;
        }
        public static int getSkinQualityListLen() {
            return skinQualityList.length;
        }
    }

    /**
     * 奖池数据对
     * - 太多Pair类了不知道用哪个，那就哪个都不要了
     */
    public static class Pair<T, U> {
        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }
        public T first;
        public U second;
    }

    private LotteryManager(){
        lootPool = new ArrayList<>();
        initPool();
    };
    public static LotteryManager getInstance(){
        if(instance == null)
            instance = new LotteryManager();
        return instance;
    }

    /** 初始化各个抽奖池 */
    private void initPool() {
        // 初始填充各个品质池
        for(int i = 0; i < TempLootAbleItemManager.getQualityListLen(); ++i)
            lootPool.add(new Pair<>(new ArrayList<>(), poolProbability[i]));
        // 为各个品质池添加抽奖项目
        for(int i = 0; i < TempLootAbleItemManager.getSkinQualityListLen(); ++i)
            lootPool.get(TempLootAbleItemManager.getSkinQuality(i)).first.add(i);
    }

    /** 检查玩家的抽奖次数 > 0*/
    public boolean canRoll(ServerPlayer player) {
        if(player == null)
            return false;

        LotteryRecordData lotteryRecordData = LotteryRecordStorage.getInstance().getPlayerLotteryRecord(player.getUUID());
        return lotteryRecordData.lotteryChance > 0;
    }
    /**
     * 抽奖一次
     *
     * @param server 基于服务器进行随机
     */
    public int rollOnce(MinecraftServer server) {
        RandomSource random = server.overworld().getRandom();
        return rollOnce(random);
    }
    /**
     * 抽奖一次
     *
     * @param player 基于玩家进行随机
     */
    public int rollOnce(ServerPlayer player) {
        RandomSource random = player.getRandom();
        return rollOnce(random);
    }
    /**
     * 抽奖一次
     *
     * @param randomSource 基于随机源进行随机
     */
    public int rollOnce(RandomSource randomSource)
    {
        int curNum = randomSource.nextInt(maxGranularity);// 0 ~ maxGranularity -1
        float level = 0.0f;
        for (Pair<ArrayList<Integer>, Float> arrayListFloatPair : lootPool) {
            level += arrayListFloatPair.second;
            if (curNum < level * maxGranularity) {
                ArrayList<Integer> curPool = arrayListFloatPair.first;
                // TODO : 为玩家解锁皮肤
                return curPool.get(randomSource.nextInt(curPool.size()));
            }
        }
        // TODO : [warn]抽奖失败，可以打印一下异常信息
        return -1;
    }
    /** 添加抽奖机会 */
    public void addOrDegreeLotteryChance(ServerPlayer player, int chance) {
        LotteryRecordStorage.getInstance().updatePlayerLotteryData(player.getUUID(),
                lotteryRecordData -> lotteryRecordData.lotteryChance += chance);
        LotteryRecordStorage.getInstance().savePlayerData(player.getUUID());
    }

    private static LotteryManager instance = null;
    /**
     * 各品质池概率
     * - 品质为由低到高，概率为从高到低
     * <p>
     * 由于品质及概率是人为规定，因此改了品质种类/概率要记得该数组
     * 这个数组作为硬编码改变奖池概率(单独放置主要为了阅读），如果从其他地方获取可以移除直接修改pool即可
     * TODO :如有需要可以使用配置文件调整（自己实现一下，在init里初始化一下），实现方式可以直接删除此数组，在初始化pool的时候直接读取并添加即可
     * TODO : 在抽卡动画中对结果附近的抽取进行加权（提升抽卡在金附近停下的概率，然鹅实际结果早已确定），提升抽卡期待和体验（就差一点了，过金运气不差）
     * </p>
     */
    private static final Float[] poolProbability = {
            .35f,
            .3f,
            .2f,
            .1f,
            .05f,
    };
    /**
     * 抽奖粒度
     * <p>
     * - 抽奖粒度越小，抽奖概率越准确
     * - 具体实现：抽取时生成0~该值的随机数，由pollProbability数组的百分比进行按顺序切分，直到找到对应的概率区间
     * </p>
     */
    private final int maxGranularity = 1000;
    /**
     * 奖池总表
     * <p>
     * - 为什么要用 Pair:各个奖池的列表应与概率绑定，
     * </p>
     */
    private ArrayList<Pair<ArrayList<Integer>, Float>> lootPool = null;
}
