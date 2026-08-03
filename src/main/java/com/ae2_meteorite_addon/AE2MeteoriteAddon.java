package com.ae2_meteorite_addon;

import com.ae2_meteorite_addon.config.JsonConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = AE2MeteoriteAddon.MODID, name = AE2MeteoriteAddon.NAME, version = AE2MeteoriteAddon.VERSION,
        dependencies = "required-after:appliedenergistics2;required-after:mixinbooter@[10.0,)")
public class AE2MeteoriteAddon
{
    public static final String MODID = "ae2_meteorite_addon";
    public static final String NAME = "AE2 Meteorite Addon";
    public static final String VERSION = "1.0";

    @Mod.Instance(MODID)
    public static AE2MeteoriteAddon INSTANCE;

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        // Move config to subfolder
        File configDir = event.getSuggestedConfigurationFile().getParentFile();
        File subDir = new File(configDir, MODID);
        subDir.mkdirs();
        ModConfig.init(new File(subDir, MODID + ".cfg"));

        // Load JSON configs
        JsonConfigManager.load(configDir);

        // Create dynamic blocks from JSON configs
        DynamicBlockRegistry.createBlocks();

        // Force-load ModEventHandler to register its @SubscribeEvent methods
        // (@Mod.EventBusSubscriber only works if the class is loaded)
        Class<?> handler = ModEventHandler.class;
        logger.info("Loaded event handler: {}", handler.getName());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        logger.info("{} initialized!", NAME);
    }

    public static Logger getLogger() {
        return logger;
    }
}
