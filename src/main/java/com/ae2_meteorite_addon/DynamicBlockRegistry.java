package com.ae2_meteorite_addon;

import com.ae2_meteorite_addon.block.BlockBuddingGeneric;
import com.ae2_meteorite_addon.block.BlockBudGeneric;
import com.ae2_meteorite_addon.block.ItemBlockMetaVariant;
import com.ae2_meteorite_addon.config.BuddingEntry;
import com.ae2_meteorite_addon.config.JsonConfigManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = AE2MeteoriteAddon.MODID)
public class DynamicBlockRegistry {

    private static final List<Block> allBlocks = new ArrayList<>();
    private static final List<Item> allItems = new ArrayList<>();

    public static void createBlocks() {
        JsonConfigManager.registerEntries();

        for (BuddingEntry entry : JsonConfigManager.getBuddingEntries()) {
            String fullName = AE2MeteoriteAddon.MODID + ":" + entry.name;
            switch (entry.type) {
                case "block":
                    BlockBuddingGeneric buddingBlock = new BlockBuddingGeneric(entry.name, entry.metaCount);
                    buddingBlock.setHardness(entry.hardness);
                    buddingBlock.setResistance(entry.resistance);
                    buddingBlock.setHarvestLevel(entry.harvestTool, entry.harvestLevel);
                    allBlocks.add(buddingBlock);
                    JsonConfigManager.setBuddingBlock(fullName, buddingBlock);
                    break;
                case "bud":
                    if (entry.variants != null) {
                        for (int i = 0; i < entry.metaCount && i < entry.variants.size(); i++) {
                            BlockBudGeneric budBlock = new BlockBudGeneric(entry.name, i, entry.variants.get(i));
                            allBlocks.add(budBlock);
                            JsonConfigManager.addBudBlock(fullName, i, budBlock);
                        }
                    }
                    break;
                case "item":
                    break;
            }
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        for (Block block : allBlocks) {
            registry.register(block);
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        for (Block block : allBlocks) {
            if (block instanceof BlockBuddingGeneric) {
                // Budding blocks use ItemBlockMetaVariant for meta subtypes
                ItemBlock itemBlock = new ItemBlockMetaVariant(block);
                registry.register(itemBlock);
                allItems.add(itemBlock);
            } else if (block instanceof BlockBudGeneric) {
                // Bud blocks are separate per variant, simple ItemBlock
                ItemBlock itemBlock = new ItemBlock(block);
                itemBlock.setRegistryName(block.getRegistryName());
                registry.register(itemBlock);
                allItems.add(itemBlock);
            } else {
                ItemBlock itemBlock = new ItemBlock(block);
                itemBlock.setRegistryName(block.getRegistryName());
                registry.register(itemBlock);
                allItems.add(itemBlock);
            }
        }

        // Register custom items from budding.json
        for (BuddingEntry entry : JsonConfigManager.getBuddingEntries()) {
            if ("item".equals(entry.type)) {
                String fullName = AE2MeteoriteAddon.MODID + ":" + entry.name;
                Item customItem = new Item()
                        .setRegistryName(AE2MeteoriteAddon.MODID, entry.name)
                        .setUnlocalizedName(AE2MeteoriteAddon.MODID + "." + entry.name)
                        .setHasSubtypes(entry.metaCount > 1)
                        .setCreativeTab(ModCreativeTab.INSTANCE);
                registry.register(customItem);
                JsonConfigManager.setCustomItem(fullName, customItem);
                allItems.add(customItem);
            }
        }
    }

    public static List<Block> getAllBlocks() {
        return allBlocks;
    }

    public static List<Item> getAllItems() {
        return allItems;
    }
}
