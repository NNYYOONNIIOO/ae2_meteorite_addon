package com.ae2_meteorite_addon;

import com.ae2_meteorite_addon.block.BlockBuddingCertusQuartz;
import com.ae2_meteorite_addon.block.BlockCertusQuartzBud;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = AE2MeteoriteAddon.MODID)
public class ModBlocks {

    public static final Block FLAWLESS_BUDDING_CERTUS_QUARTZ = new BlockBuddingCertusQuartz()
            .setRegistryName(AE2MeteoriteAddon.MODID, "flawless_budding_certus_quartz")
            .setUnlocalizedName("ae2_meteorite_addon.flawless_budding_certus_quartz")
            .setCreativeTab(ModCreativeTab.INSTANCE);

    public static final Block FLAWED_BUDDING_CERTUS_QUARTZ = new BlockBuddingCertusQuartz()
            .setRegistryName(AE2MeteoriteAddon.MODID, "flawed_budding_certus_quartz")
            .setUnlocalizedName("ae2_meteorite_addon.flawed_budding_certus_quartz")
            .setCreativeTab(ModCreativeTab.INSTANCE);

    public static final Block CHIPPED_BUDDING_CERTUS_QUARTZ = new BlockBuddingCertusQuartz()
            .setRegistryName(AE2MeteoriteAddon.MODID, "chipped_budding_certus_quartz")
            .setUnlocalizedName("ae2_meteorite_addon.chipped_budding_certus_quartz")
            .setCreativeTab(ModCreativeTab.INSTANCE);

    public static final Block DAMAGED_BUDDING_CERTUS_QUARTZ = new BlockBuddingCertusQuartz()
            .setRegistryName(AE2MeteoriteAddon.MODID, "damaged_budding_certus_quartz")
            .setUnlocalizedName("ae2_meteorite_addon.damaged_budding_certus_quartz")
            .setCreativeTab(ModCreativeTab.INSTANCE);

    public static final Block CERTUS_QUARTZ_CLUSTER = new BlockCertusQuartzBud(7, 3, 5, SoundType.STONE, true, null)
            .setRegistryName(AE2MeteoriteAddon.MODID, "certus_quartz_cluster")
            .setUnlocalizedName("ae2_meteorite_addon.certus_quartz_cluster")
            .setCreativeTab(ModCreativeTab.INSTANCE);

    public static final Block LARGE_CERTUS_QUARTZ_BUD = new BlockCertusQuartzBud(5, 3, 4, SoundType.STONE, false, CERTUS_QUARTZ_CLUSTER)
            .setRegistryName(AE2MeteoriteAddon.MODID, "large_certus_quartz_bud")
            .setUnlocalizedName("ae2_meteorite_addon.large_certus_quartz_bud")
            .setCreativeTab(ModCreativeTab.INSTANCE);

    public static final Block MEDIUM_CERTUS_QUARTZ_BUD = new BlockCertusQuartzBud(4, 3, 2, SoundType.STONE, false, LARGE_CERTUS_QUARTZ_BUD)
            .setRegistryName(AE2MeteoriteAddon.MODID, "medium_certus_quartz_bud")
            .setUnlocalizedName("ae2_meteorite_addon.medium_certus_quartz_bud")
            .setCreativeTab(ModCreativeTab.INSTANCE);

    public static final Block SMALL_CERTUS_QUARTZ_BUD = new BlockCertusQuartzBud(3, 4, 1, SoundType.STONE, false, MEDIUM_CERTUS_QUARTZ_BUD)
            .setRegistryName(AE2MeteoriteAddon.MODID, "small_certus_quartz_bud")
            .setUnlocalizedName("ae2_meteorite_addon.small_certus_quartz_bud")
            .setCreativeTab(ModCreativeTab.INSTANCE);

    private static final Block[] ALL_BLOCKS = {
            FLAWLESS_BUDDING_CERTUS_QUARTZ,
            FLAWED_BUDDING_CERTUS_QUARTZ,
            CHIPPED_BUDDING_CERTUS_QUARTZ,
            DAMAGED_BUDDING_CERTUS_QUARTZ,
            SMALL_CERTUS_QUARTZ_BUD,
            MEDIUM_CERTUS_QUARTZ_BUD,
            LARGE_CERTUS_QUARTZ_BUD,
            CERTUS_QUARTZ_CLUSTER
    };

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        for (Block block : ALL_BLOCKS) {
            registry.register(block);
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<net.minecraft.item.Item> event) {
        IForgeRegistry<net.minecraft.item.Item> registry = event.getRegistry();
        for (Block block : ALL_BLOCKS) {
            registry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
    }
}
