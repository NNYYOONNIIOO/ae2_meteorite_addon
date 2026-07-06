package com.ae2_meteorite_addon.config;

import java.util.List;

public class BuddingEntry {
    public String type; // "block", "bud", "item"
    public String name; // registry name without namespace
    public int metaCount; // 1-4

    // block type properties
    public float hardness = 3.0f;
    public float resistance = 5.0f;
    public String harvestTool = "pickaxe";
    public int harvestLevel = 0;

    // bud type properties
    public List<BudVariantConfig> variants;

    // item type properties
    public List<String> displayNames;

    public static class BudVariantConfig {
        public int height;
        public int shrink;
        public int lightLevel;
        public boolean isCluster;
        public String drop; // "modid:itemid:meta" or null for AE2 defaults
        public int dropCount = 1;
        public boolean dropFortuneBonus = false;
    }
}
