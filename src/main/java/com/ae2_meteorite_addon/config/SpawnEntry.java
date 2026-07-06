package com.ae2_meteorite_addon.config;

import java.util.List;

public class SpawnEntry {
    public String buddingBlock; // "ae2_meteorite_addon:budding_certus_quartz"
    public List<BudStageConfig> budStages;
    public float growChance = 0.2f; // 1/5
    public boolean spawnInMeteorite = false;
    public boolean acceleratable = true;

    public static class BudStageConfig {
        public String bud; // "ae2_meteorite_addon:certus_quartz_bud"
        public int meta; // 0-3
    }
}
