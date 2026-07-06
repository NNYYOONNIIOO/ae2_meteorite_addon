package com.ae2_meteorite_addon.client;

import com.ae2_meteorite_addon.AE2MeteoriteAddon;
import com.ae2_meteorite_addon.DynamicBlockRegistry;
import com.ae2_meteorite_addon.block.BlockBuddingGeneric;
import com.ae2_meteorite_addon.block.BlockBudGeneric;
import com.ae2_meteorite_addon.config.BuddingEntry;
import com.ae2_meteorite_addon.config.JsonConfigManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = AE2MeteoriteAddon.MODID)
public class ClientRegistry {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Block block : DynamicBlockRegistry.getAllBlocks()) {
            Item item = Item.getItemFromBlock(block);
            if (item == null) continue;

            if (block instanceof BlockBuddingGeneric) {
                BlockBuddingGeneric budding = (BlockBuddingGeneric) block;
                for (int i = 0; i <= budding.getMaxVariant(); i++) {
                    ModelLoader.setCustomModelResourceLocation(item, i,
                            new ModelResourceLocation(block.getRegistryName(), "variant=" + i));
                }
            } else if (block instanceof BlockBudGeneric) {
                // Each bud variant is a separate block, only FACING property
                ModelLoader.setCustomModelResourceLocation(item, 0,
                        new ModelResourceLocation(block.getRegistryName(), "facing=up"));
            } else {
                ModelLoader.setCustomModelResourceLocation(item, 0,
                        new ModelResourceLocation(block.getRegistryName(), "normal"));
            }
        }

        // Register custom item models
        for (Item customItem : DynamicBlockRegistry.getAllItems()) {
            if (customItem instanceof ItemBlock) continue;
            if (customItem.getHasSubtypes()) {
                BuddingEntry entry = null;
                for (BuddingEntry e : JsonConfigManager.getBuddingEntries()) {
                    if ("item".equals(e.type) && customItem.getRegistryName().equals(
                            new ResourceLocation(AE2MeteoriteAddon.MODID, e.name))) {
                        entry = e;
                        break;
                    }
                }
                if (entry != null) {
                    for (int i = 0; i < entry.metaCount; i++) {
                        ModelLoader.setCustomModelResourceLocation(customItem, i,
                                new ModelResourceLocation(customItem.getRegistryName(), "inventory"));
                    }
                }
            } else {
                ModelLoader.setCustomModelResourceLocation(customItem, 0,
                        new ModelResourceLocation(customItem.getRegistryName(), "inventory"));
            }
        }
    }
}
