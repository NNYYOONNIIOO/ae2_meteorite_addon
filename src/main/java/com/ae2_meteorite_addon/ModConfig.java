package com.ae2_meteorite_addon;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class ModConfig {

    private static Configuration config;

    public static boolean enableMeteoriteDust = true;
    public static int meteoriteDustIntervalSeconds = 10;
    public static boolean enableMeteoriteBudding = true;
    public static boolean enableGrowthAccelerator = true;
    public static int growthAcceleratorSpeed = 10;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        load();
    }

    public static void load() {
        enableMeteoriteDust = config.getBoolean(
                "enableMeteoriteDust",
                Configuration.CATEGORY_GENERAL,
                true,
                "Enable annihilation plane producing meteorite dust at world height limit"
        );
        meteoriteDustIntervalSeconds = config.getInt(
                "meteoriteDustIntervalSeconds",
                Configuration.CATEGORY_GENERAL,
                10,
                1, 3600,
                "Interval in seconds for an upward-facing annihilation plane at world height limit to produce meteorite dust"
        );
        enableMeteoriteBudding = config.getBoolean(
                "enableMeteoriteBudding",
                Configuration.CATEGORY_GENERAL,
                true,
                "Enable budding certus quartz blocks generating in meteorites"
        );
        enableGrowthAccelerator = config.getBoolean(
                "enableGrowthAccelerator",
                Configuration.CATEGORY_GENERAL,
                true,
                "Enable growth accelerators triggering random ticks on adjacent blocks"
        );
        growthAcceleratorSpeed = config.getInt(
                "growthAcceleratorSpeed",
                Configuration.CATEGORY_GENERAL,
                10,
                1, 100,
                "Number of ticks between two growth accelerator random ticks"
        );
        if (config.hasChanged()) {
            config.save();
        }
    }

    public static int getMeteoriteDustIntervalTicks() {
        return meteoriteDustIntervalSeconds * 20;
    }
}
