package com.ae2_meteorite_addon.client;

import com.ae2_meteorite_addon.AE2MeteoriteAddon;
import com.ae2_meteorite_addon.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = AE2MeteoriteAddon.MODID)
public class ClientRegistry {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        registerBlockItemModel(ModBlocks.FLAWLESS_BUDDING_CERTUS_QUARTZ);
        registerBlockItemModel(ModBlocks.FLAWED_BUDDING_CERTUS_QUARTZ);
        registerBlockItemModel(ModBlocks.CHIPPED_BUDDING_CERTUS_QUARTZ);
        registerBlockItemModel(ModBlocks.DAMAGED_BUDDING_CERTUS_QUARTZ);
        registerBudItemModel(ModBlocks.SMALL_CERTUS_QUARTZ_BUD);
        registerBudItemModel(ModBlocks.MEDIUM_CERTUS_QUARTZ_BUD);
        registerBudItemModel(ModBlocks.LARGE_CERTUS_QUARTZ_BUD);
        registerBudItemModel(ModBlocks.CERTUS_QUARTZ_CLUSTER);
    }

    private static void registerBlockItemModel(Block block) {
        Item item = Item.getItemFromBlock(block);
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(block.getRegistryName(), "normal"));
    }

    private static void registerBudItemModel(Block block) {
        Item item = Item.getItemFromBlock(block);
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(block.getRegistryName(), "facing=up"));
    }
}
