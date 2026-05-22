package com.ae2_meteorite_addon;

import com.ae2_meteorite_addon.ModConfig;
import com.ae2_meteorite_addon.entity.EntityBuddingRepair;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.Logger;

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
        ModConfig.init(event.getSuggestedConfigurationFile());

        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "budding_repair"),
                EntityBuddingRepair.class,
                "budding_repair",
                0,
                INSTANCE,
                64, 4, true
        );
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        logger.info("{} initialized!", NAME);
    }
}
