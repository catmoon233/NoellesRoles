package org.agmas.noellesroles.utils.lottery;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.agmas.noellesroles.Noellesroles;
import org.agmas.noellesroles.utils.Pair;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 抽奖管理器
 * - 实现抽奖功能
 * - 管理抽奖数据的调用
 */
public class LotteryManager {
    public static class LotteryPool {
        public LotteryPool(int poolID, String name, String type,
                           List<LotteryPoolsConfig.PoolConfig.QualityListItemConfig> qualityListGroupConfigs) {
            this.poolID = poolID;
            this.name = name;
            this.type = type;
            this.qualityListGroupConfigs = new ArrayList<>();
            for(LotteryPoolsConfig.PoolConfig.QualityListItemConfig qualityListItemConfig : qualityListGroupConfigs)
            {
                this.qualityListGroupConfigs.add(
                        new Pair<>(qualityListItemConfig.getProbability(), qualityListItemConfig.getQualityListConfig())
                );
            }
        }
        /** 使用已有卡池类构造，为了防止形参冲突，与读取配置的构造相区别，可以接受不同的列表 */
        public LotteryPool(String name, int poolID, String type, List<Pair<Double, List<String>>> qualityListGroupConfigs) {
            this.poolID = poolID;
            this.name = name;
            this.type = type;
            this.qualityListGroupConfigs = qualityListGroupConfigs;
        }

        /**
         * 抽奖一次
         *
         * @param player 基于玩家进行随机:抽奖结果需要对玩家信息查询修改，因此必然有player参数传入，所以直接用玩家源
         * @return 返回抽奖结果：结果的品质和在该品质内的索引
         */
        public Pair<Integer, Integer> rollOnce(ServerPlayer player) {
            RandomSource randomSource = player.getRandom();
            int curNum = randomSource.nextInt(maxGranularity);// 0 ~ maxGranularity -1
            double level = 0.0f;
            for (int i = 0; i < qualityListGroupConfigs.size(); ++i) {
                level += qualityListGroupConfigs.get(i).first;
                if (curNum < level * maxGranularity) {
                    List<String> curQualityList = qualityListGroupConfigs.get(i).second;
                    // TODO : 为玩家解锁皮肤
                    // TODO : 处理重复皮肤
                    int resultIdx = randomSource.nextInt(curQualityList.size());
                    int resultQuality = i;
                    LotteryRecordStorage.getInstance().updatePlayerLotteryData(player.getUUID(),
                            lotteryRecordData -> lotteryRecordData.lotteryItems.add(
                                    new LotteryRecordData.LotteryItemData(
                                            this.poolID, resultQuality, curQualityList.get(resultIdx), System.currentTimeMillis())
                            ));
                    return new Pair<>(resultQuality, resultIdx);
                }
            }
            Noellesroles.LOGGER.warn("玩家UUID:" + player.getUUID() + "抽奖失败");
            return new Pair<>(-1, -1);
        }
        public int getPoolID() {
            return poolID;
        }
        public String getName() {
            return name;
        }
        public String getType() {
            return type;
        }
        public List<Pair<Double, List<String>>> getQualityListGroupConfigs() {
            return qualityListGroupConfigs;
        }
        private final int poolID;
        private final String name;
        private final String type;

        /**
         * 各品质卡池内容及其概率
         * - 品质为由低到高，概率为从高到低
         * <p>
         *  TODO : 在抽卡动画中对结果附近的抽取进行加权（提升抽卡在金附近停下的概率，然鹅实际结果早已确定），提升抽卡期待和体验（就差一点了，过金运气不差）
         * </p>
         */
        private final List<Pair<Double, List<String>>> qualityListGroupConfigs;
    }
//    /**
//     * 临时可抽奖项目管理器
//     * TODO : 需要使用正确的可抽奖管理器管理
//     */
//    public static class TempLootAbleItemManager{
//        // TODO: 作为皮肤查询使用，当皮肤系统建成后移动到皮肤管理器中
//        private static final ResourceLocation[] skinList = {
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/lock.png"),
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/bomb.png"),
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/note.png"),
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/sp_knife.png"),
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/item/master_key.png"),
//        };
//        // 皮肤品质映射
//        private static final int[] skinQualityList = {
//                4,
//                3,
//                0,
//                2,
//                1,
//        };
//        private static final ResourceLocation[] qualityList = {
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/common_skin.png"),
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/uncommon_skin.png"),
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/rare_skin.png"),
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/epic_skin.png"),
//                ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/legendary_skin.png"),
//        };
//        // TODO : 这几个函数就是获取抽奖源的正确函数，当实装到皮肤系统中时需要替换这几个函数
//        public static ResourceLocation getSkinResourceLocation(int index){
//            return skinList[index];
//        }
//        public static ResourceLocation getQualityResourceLocation(int index){
//            return qualityList[index];
//        }
//        public static int getSkinQuality(int index){
//            return skinQualityList[index];
//        }
//        public static int getSkinListLen() {
//            return skinList.length;
//        }
//        public static int getQualityListLen() {
//            return qualityList.length;
//        }
//        public static int getSkinQualityListLen() {
//            return skinQualityList.length;
//        }
//    }


    public ResourceLocation getQualityBgResourceLocation(int index) {
        return qualityBgList[index];
    }
    /** 检查玩家的抽奖次数 > 0*/
    public boolean canRoll(ServerPlayer player) {
        if(player == null)
            return false;

        LotteryRecordData lotteryRecordData = LotteryRecordStorage.getInstance().getPlayerLotteryRecord(player.getUUID());
        return lotteryRecordData.lotteryChance > 0;
    }
    /** 添加抽奖机会 */
    public void addOrDegreeLotteryChance(ServerPlayer player, int chance) {
        LotteryRecordStorage.getInstance().updatePlayerLotteryData(player.getUUID(),
                lotteryRecordData -> lotteryRecordData.lotteryChance += chance);
        LotteryRecordStorage.getInstance().savePlayerData(player.getUUID());
    }

    /**
     * 添加卡池
     * <p>
     *     NOTE:
     *      该函数用于客户端初次同步服务端卡池，而不是用来运行时添加卡池的；具体而言：
     *          指客户端首次打开抽奖界面会发送完整卡池信息，客户端保存缓存（仅内存），下次访问卡池信息时会从缓存中读取而不用再发送卡池信息
     *     TODO:未来需要将卡池信息缓存为本地文件，每次启动自动读取，调用时与服务器卡池信息文件hash值进行对比同步，进一步减少发包
     * </p>
     */
    public void addLotteryPool(LotteryPool lotteryPool)
    {
        lotteryPoolList.add(lotteryPool);
    }
    public List<Integer> getPoolIDs(){
        List<Integer> poolIDs = new ArrayList<>();
        for(LotteryPool lotteryPool : lotteryPoolList)
        {
            poolIDs.add(lotteryPool.getPoolID());
        }
        return poolIDs;
    }
    /** 获取指定 ID的卡池 */
    public LotteryPool getLotteryPool(int poolID)
    {
        for(LotteryPool lotteryPool : lotteryPoolList)
        {
            if(lotteryPool.getPoolID() == poolID)
            {
                return lotteryPool;
            }
        }
        return null;
    }
    public void setLotteryPoolByID(int poolID, LotteryPool lotteryPool)
    {
        for(int i = 0; i < lotteryPoolList.size(); ++i)
        {
            if(lotteryPoolList.get(i).poolID == poolID)
            {
                lotteryPoolList.set(i, lotteryPool);
                return;
            }
        }
    }
    private void init()
    {
        // 为了让单人测试也生效，暂时让客户端也能读取配置，并且未来可以加入本地数据存储，进行卡池信息hash值对比更新，也能减少数据传输量
//        // 仅运行在服务端
//        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
//        {
//            return;
//        }
        // 配置文件应该存放在主目录lottery_data目录下的lottery_pool.json文件中
        Path configPath = LotteryRecordStorage.getInstance().getLotteryDataDir().resolve("lottery_pool.json");
        LotteryPoolsConfig lotteryPoolsConfig = LotteryPoolsConfigParser.parse(configPath);
        if(lotteryPoolsConfig == null || lotteryPoolsConfig.getPools().isEmpty())
        {
            Noellesroles.LOGGER.error("No valid pool configuration found.");
            return;
        }
        for (LotteryPoolsConfig.PoolConfig poolConfig : lotteryPoolsConfig.getPools())
        {
            // 验证配置数据
            try{
                if(!poolConfig.isEnable())
                    continue;
                if(poolConfig.getName() == null || poolConfig.getName().isEmpty())
                {
                    Noellesroles.LOGGER.error("Pool name is null or empty.");
                    continue;
                }
                if(poolConfig.getType() == null || poolConfig.getType().isEmpty())
                {
                    Noellesroles.LOGGER.error("Pool type is null or empty.");
                    continue;
                }
                Double sumProbability = 0.0;
                for(LotteryPoolsConfig.PoolConfig.QualityListItemConfig qualityListItemConfig : poolConfig.getQualityListGroupConfig())
                {
                    if(qualityListItemConfig.getQualityListConfig() == null || qualityListItemConfig.getQualityListConfig().isEmpty())
                    {
                        Noellesroles.LOGGER.error("Quality list is null or empty.");
                        continue;
                    }
                    if(qualityListItemConfig.getProbability() == null || qualityListItemConfig.getProbability() <= 0)
                    {
                        Noellesroles.LOGGER.error("Quality list probability is null or <= 0.");
                        continue;
                    }
                    sumProbability += qualityListItemConfig.getProbability();
                }
                if(sumProbability != 1)
                {
                    Noellesroles.LOGGER.error("Quality list probability sum is not equal to 1.");
                    continue;
                }
                LotteryPool pool = new LotteryPool(
                        poolConfig.getPoolID(), poolConfig.getName(), poolConfig.getType(),
                        poolConfig.getQualityListGroupConfig());
                Noellesroles.LOGGER.info("Loaded pool:ID:{},Name{},Type{}", pool.poolID, pool.name, pool.type);
            }
            catch (Exception e){
                Noellesroles.LOGGER.error("Failed to parse pool config : {}", poolConfig.getName(), e);
            }
        }
        Noellesroles.LOGGER.info("Loaded {} pools.", lotteryPoolList.size());
    }

    private LotteryManager(){
        init();
    };
    public static LotteryManager getInstance(){
        if(instance == null)
            instance = new LotteryManager();
        return instance;
    }

    private static LotteryManager instance = null;

    /**
     * 抽奖粒度
     * <p>
     * - 抽奖粒度越小，抽奖概率越准确
     * - 具体实现：抽取时生成0~该值的随机数，由pollProbability数组的百分比进行按顺序切分，直到找到对应的概率区间
     * </p>
     */
    public static final int maxGranularity = 1000;
    private final ArrayList<LotteryPool> lotteryPoolList = new ArrayList<>();
    private final String defaultPoolItem = "coin_common";
    private static final ResourceLocation[] qualityBgList = {
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/common_skin.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/uncommon_skin.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/rare_skin.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/epic_skin.png"),
            ResourceLocation.fromNamespaceAndPath("noellesroles", "textures/gui/legendary_skin.png"),
    };
}
