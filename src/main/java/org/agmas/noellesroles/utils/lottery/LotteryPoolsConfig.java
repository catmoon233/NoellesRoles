package org.agmas.noellesroles.utils.lottery;

import java.util.List;

public class LotteryPoolsConfig {
    public static class PoolConfig{
        public static class QualityListItemConfig {
            public Double getProbability() {
                return probability;
            }
            public void setProbability(Double probability) {
                this.probability = probability;
            }
            public List<String> getQualityListConfig() {
                return qualityListConfig;
            }
            public void setQualityListConfig(List<String> qualityListConfig) {
                this.qualityListConfig = qualityListConfig;
            }
            /** 该品质的概率 */
            private Double probability;
            /** 该品质的物品列表 */
            private List<String> qualityListConfig;
        }
        public int getPoolID() {
            return poolID;
        }
        public void setPoolID(int poolID) {
            this.poolID = poolID;
        }
        public void setEnable(boolean enable) {
            this.enable = enable;
        }
        public boolean isEnable() {
            return enable;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public List<QualityListItemConfig> getQualityListGroupConfig() {
            return qualityListGroupConfig;
        }
        public void setQualityListGroupConfig(List<QualityListItemConfig> qualityListGroupConfig) {
            this.qualityListGroupConfig = qualityListGroupConfig;
        }
        private int poolID;
        private boolean enable;
        private String name;
        private String type;
        private List<QualityListItemConfig> qualityListGroupConfig;
    }
    public List<PoolConfig> getPools() {
        return pools;
    }
    public void setPools(List<PoolConfig> pools) {
        this.pools = pools;
    }
    private List<PoolConfig> pools;
}
