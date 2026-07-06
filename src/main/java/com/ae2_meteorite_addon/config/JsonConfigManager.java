package com.ae2_meteorite_addon.config;

import com.ae2_meteorite_addon.AE2MeteoriteAddon;
import com.ae2_meteorite_addon.block.BlockBudGeneric;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class JsonConfigManager {

    private static final Logger LOGGER = LogManager.getLogger("AE2MeteoriteAddon");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static List<BuddingEntry> buddingEntries = new ArrayList<>();
    private static List<SpawnEntry> spawnEntries = new ArrayList<>();
    private static List<DegenerateEntry> degenerateEntries = new ArrayList<>();
    private static List<DropEntry> dropEntries = new ArrayList<>();

    // Runtime lookup maps
    // budding: baseName (ae2_meteorite_addon:budding_certus_quartz) -> Block
    private static final Map<String, Block> buddingBlockMap = new HashMap<>();
    // bud: baseName_N (ae2_meteorite_addon:certus_quartz_bud_0) -> Block
    private static final Map<String, Block> budBlockMap = new HashMap<>();
    // bud baseName (ae2_meteorite_addon:certus_quartz_bud) -> List<Block> (indexed by variant)
    private static final Map<String, List<Block>> budBaseMap = new HashMap<>();
    private static final Map<String, Item> customItemMap = new HashMap<>();
    private static final Map<String, BuddingEntry> buddingEntryMap = new HashMap<>();
    private static final Map<String, BuddingEntry> budEntryMap = new HashMap<>();

    public static void load(File configDir) {
        File subDir = new File(configDir, AE2MeteoriteAddon.MODID);
        subDir.mkdirs();

        buddingEntries = loadJson(new File(subDir, "budding.json"),
                new TypeToken<List<BuddingEntry>>(){}.getType(), getDefaultBuddingJson());
        spawnEntries = loadJson(new File(subDir, "spawn.json"),
                new TypeToken<List<SpawnEntry>>(){}.getType(), getDefaultSpawnJson());
        degenerateEntries = loadJson(new File(subDir, "degenerate.json"),
                new TypeToken<List<DegenerateEntry>>(){}.getType(), getDefaultDegenerateJson());
        dropEntries = loadJson(new File(subDir, "drop.json"),
                new TypeToken<List<DropEntry>>(){}.getType(), getDefaultDropJson());
    }

    private static <T> List<T> loadJson(File file, Type type, String defaultJson) {
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(defaultJson);
            } catch (IOException e) {
                LOGGER.error("Failed to write default config: {}", file.getName(), e);
            }
        }
        try (FileReader reader = new FileReader(file)) {
            List<T> result = GSON.fromJson(reader, type);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            LOGGER.error("Failed to parse config: {}", file.getName(), e);
            return new ArrayList<>();
        }
    }

    public static void registerEntries() {
        for (BuddingEntry entry : buddingEntries) {
            String fullName = AE2MeteoriteAddon.MODID + ":" + entry.name;
            switch (entry.type) {
                case "block":
                    buddingEntryMap.put(fullName, entry);
                    break;
                case "bud":
                    budEntryMap.put(fullName, entry);
                    break;
            }
        }
    }

    public static void setBuddingBlock(String name, Block block) {
        buddingBlockMap.put(name, block);
    }

    public static void addBudBlock(String baseName, int variant, Block block) {
        String key = baseName + "_" + variant;
        budBlockMap.put(key, block);
        budBaseMap.computeIfAbsent(baseName, k -> new ArrayList<>());
        List<Block> list = budBaseMap.get(baseName);
        while (list.size() <= variant) {
            list.add(null);
        }
        list.set(variant, block);
    }

    public static void setCustomItem(String name, Item item) {
        customItemMap.put(name, item);
    }

    // === Lookup methods ===

    public static boolean isBuddingBlock(Block block) {
        return buddingBlockMap.containsValue(block);
    }

    public static boolean isBudBlock(Block block) {
        return budBlockMap.containsValue(block);
    }

    public static Block getBuddingBlock(String name) {
        return buddingBlockMap.get(name);
    }

    public static Block getBudBlockByVariant(String baseName, int variant) {
        List<Block> list = budBaseMap.get(baseName);
        if (list != null && variant >= 0 && variant < list.size()) {
            return list.get(variant);
        }
        return null;
    }

    public static List<Block> getBudBlocksByBase(String baseName) {
        List<Block> list = budBaseMap.get(baseName);
        return list != null ? list : Collections.emptyList();
    }

    public static BuddingEntry getBuddingEntry(String name) {
        return buddingEntryMap.get(name);
    }

    public static BuddingEntry getBudEntry(String name) {
        return budEntryMap.get(name);
    }

    public static List<BuddingEntry> getBuddingEntries() {
        return buddingEntries;
    }

    public static List<SpawnEntry> getSpawnEntries() {
        return spawnEntries;
    }

    public static List<DegenerateEntry> getDegenerateEntries() {
        return degenerateEntries;
    }

    public static List<DropEntry> getDropEntries() {
        return dropEntries;
    }

    /**
     * Find the spawn entry for a given budding block
     */
    public static SpawnEntry getSpawnEntry(Block buddingBlock, int variant) {
        for (SpawnEntry entry : spawnEntries) {
            if (entry.buddingBlock.equals(buddingBlock.getRegistryName().toString())) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Find the degenerate entry for a given budding block + variant
     */
    public static DegenerateEntry getDegenerateEntry(Block buddingBlock, int variant) {
        String sourceKey = buddingBlock.getRegistryName().toString() + ":" + variant;
        for (DegenerateEntry entry : degenerateEntries) {
            if (entry.source.equals(sourceKey)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Get the IBlockState for a block reference string like "modid:blockid:meta"
     */
    public static IBlockState parseBlockState(String ref) {
        if (ref == null) return null;
        String[] parts = ref.split(":");
        if (parts.length < 2) return null;
        String modid = parts[0];
        String blockId = parts[1];
        int meta = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        Block block = Block.REGISTRY.getObject(new ResourceLocation(modid, blockId));
        if (block == null) return null;
        return block.getStateFromMeta(meta);
    }

    /**
     * Get an ItemStack from a reference string like "modid:itemid:meta"
     */
    public static ItemStack parseItemStack(String ref) {
        if (ref == null) return ItemStack.EMPTY;
        String[] parts = ref.split(":");
        if (parts.length < 2) return ItemStack.EMPTY;
        String modid = parts[0];
        String itemId = parts[1];
        int meta = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        Item item = Item.REGISTRY.getObject(new ResourceLocation(modid, itemId));
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, 1, meta);
    }

    /**
     * Get the next growth stage for a bud block
     */
    public static IBlockState getNextBudStage(BlockBudGeneric budBlock, EnumFacing facing) {
        int currentIndex = budBlock.getVariantIndex();
        String baseName = AE2MeteoriteAddon.MODID + ":" + budBlock.getBaseName();

        for (SpawnEntry spawn : spawnEntries) {
            for (int i = 0; i < spawn.budStages.size(); i++) {
                SpawnEntry.BudStageConfig stage = spawn.budStages.get(i);
                if (stage.bud.equals(baseName) && stage.meta == currentIndex) {
                    if (i + 1 < spawn.budStages.size()) {
                        SpawnEntry.BudStageConfig next = spawn.budStages.get(i + 1);
                        Block nextBlock = getBudBlockByVariant(next.bud, next.meta);
                        if (nextBlock != null) {
                            return nextBlock.getDefaultState().withProperty(net.minecraft.block.BlockDirectional.FACING, facing);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the first bud stage for a budding block
     */
    public static IBlockState getFirstBudStage(Block buddingBlock) {
        for (SpawnEntry spawn : spawnEntries) {
            if (spawn.buddingBlock.equals(buddingBlock.getRegistryName().toString())) {
                if (!spawn.budStages.isEmpty()) {
                    SpawnEntry.BudStageConfig first = spawn.budStages.get(0);
                    Block budBlock = getBudBlockByVariant(first.bud, first.meta);
                    if (budBlock != null) {
                        return budBlock.getDefaultState().withProperty(net.minecraft.block.BlockDirectional.FACING, EnumFacing.UP);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get all budding blocks that should spawn in meteorites
     */
    public static List<IBlockState> getMeteoriteBuddingBlocks() {
        List<IBlockState> result = new ArrayList<>();
        for (SpawnEntry spawn : spawnEntries) {
            if (spawn.spawnInMeteorite) {
                Block block = buddingBlockMap.get(spawn.buddingBlock);
                if (block != null) {
                    BuddingEntry entry = buddingEntryMap.get(spawn.buddingBlock);
                    if (entry != null) {
                        for (int i = 0; i < entry.metaCount; i++) {
                            result.add(block.getStateFromMeta(i));
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get all acceleratable budding blocks
     */
    public static Set<Block> getAcceleratableBuddingBlocks() {
        Set<Block> result = new HashSet<>();
        for (SpawnEntry spawn : spawnEntries) {
            if (spawn.acceleratable) {
                Block block = buddingBlockMap.get(spawn.buddingBlock);
                if (block != null) {
                    result.add(block);
                }
            }
        }
        return result;
    }

    /**
     * Get the drop for a bud block. Checks drop.json first, then uses defaults.
     */
    public static ItemStack getBudDrop(BlockBudGeneric budBlock, int fortune) {
        String blockRef = budBlock.getRegistryName().toString();

        // Check drop.json first
        for (DropEntry drop : dropEntries) {
            if (drop.block.equals(blockRef)) {
                ItemStack stack = parseItemStack(drop.item);
                if (!stack.isEmpty()) {
                    int count = drop.count + (drop.fortuneBonus ? fortune : 0);
                    stack.setCount(Math.max(1, count));
                    return stack;
                }
            }
        }

        // Default drops based on isCluster
        BuddingEntry.BudVariantConfig config = budBlock.getVariantConfig();
        if (config == null) return ItemStack.EMPTY;

        if (config.isCluster) {
            Optional<ItemStack> crystal = appeng.api.AEApi.instance().definitions().materials().certusQuartzCrystal().maybeStack(1);
            if (crystal.isPresent()) {
                ItemStack drop = crystal.get().copy();
                drop.setCount(4 + fortune);
                return drop;
            }
        } else {
            Optional<ItemStack> dust = appeng.api.AEApi.instance().definitions().materials().certusQuartzDust().maybeStack(1);
            if (dust.isPresent()) {
                ItemStack drop = dust.get().copy();
                drop.setCount(1);
                return drop;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Get the drop for a budding block variant. Checks drop.json first.
     * Returns null if no drop.json override exists (use default logic).
     */
    public static ItemStack getBuddingDrop(Block buddingBlock, int variant, int fortune) {
        String blockRef = buddingBlock.getRegistryName().toString();

        for (DropEntry drop : dropEntries) {
            if (drop.block.equals(blockRef)) {
                ItemStack stack = parseItemStack(drop.item);
                if (!stack.isEmpty()) {
                    int count = drop.count + (drop.fortuneBonus ? fortune : 0);
                    stack.setCount(Math.max(1, count));
                    return stack;
                }
            }
        }
        return null;
    }

    // === Default JSON configs ===

    private static String getDefaultBuddingJson() {
        return "[\n" +
                "  {\n" +
                "    \"type\": \"block\",\n" +
                "    \"name\": \"budding_certus_quartz\",\n" +
                "    \"metaCount\": 4,\n" +
                "    \"hardness\": 3.0,\n" +
                "    \"resistance\": 5.0,\n" +
                "    \"harvestTool\": \"pickaxe\",\n" +
                "    \"harvestLevel\": 0\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\": \"bud\",\n" +
                "    \"name\": \"certus_quartz_bud\",\n" +
                "    \"metaCount\": 4,\n" +
                "    \"variants\": [\n" +
                "      {\"height\": 3, \"shrink\": 4, \"lightLevel\": 1, \"isCluster\": false},\n" +
                "      {\"height\": 4, \"shrink\": 3, \"lightLevel\": 2, \"isCluster\": false},\n" +
                "      {\"height\": 5, \"shrink\": 3, \"lightLevel\": 4, \"isCluster\": false},\n" +
                "      {\"height\": 7, \"shrink\": 3, \"lightLevel\": 5, \"isCluster\": true}\n" +
                "    ]\n" +
                "  }\n" +
                "]\n";
    }

    private static String getDefaultSpawnJson() {
        return "[\n" +
                "  {\n" +
                "    \"buddingBlock\": \"ae2_meteorite_addon:budding_certus_quartz\",\n" +
                "    \"budStages\": [\n" +
                "      {\"bud\": \"ae2_meteorite_addon:certus_quartz_bud\", \"meta\": 0},\n" +
                "      {\"bud\": \"ae2_meteorite_addon:certus_quartz_bud\", \"meta\": 1},\n" +
                "      {\"bud\": \"ae2_meteorite_addon:certus_quartz_bud\", \"meta\": 2},\n" +
                "      {\"bud\": \"ae2_meteorite_addon:certus_quartz_bud\", \"meta\": 3}\n" +
                "    ],\n" +
                "    \"growChance\": 0.2,\n" +
                "    \"spawnInMeteorite\": true,\n" +
                "    \"acceleratable\": true\n" +
                "  }\n" +
                "]\n";
    }

    private static String getDefaultDegenerateJson() {
        return "[\n" +
                "  {\n" +
                "    \"source\": \"ae2_meteorite_addon:budding_certus_quartz:0\",\n" +
                "    \"probability\": 0.0,\n" +
                "    \"target\": \"ae2_meteorite_addon:budding_certus_quartz:0\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"source\": \"ae2_meteorite_addon:budding_certus_quartz:1\",\n" +
                "    \"probability\": 0.0833,\n" +
                "    \"target\": \"ae2_meteorite_addon:budding_certus_quartz:2\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"source\": \"ae2_meteorite_addon:budding_certus_quartz:2\",\n" +
                "    \"probability\": 0.0833,\n" +
                "    \"target\": \"ae2_meteorite_addon:budding_certus_quartz:3\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"source\": \"ae2_meteorite_addon:budding_certus_quartz:3\",\n" +
                "    \"probability\": 0.0833,\n" +
                "    \"target\": \"appliedenergistics2:quartz_block:0\"\n" +
                "  }\n" +
                "]\n";
    }

    private static String getDefaultDropJson() {
        return "[\n" +
                "  {\n" +
                "    \"block\": \"ae2_meteorite_addon:certus_quartz_bud_0\",\n" +
                "    \"item\": \"appliedenergistics2:material:2\",\n" +
                "    \"count\": 1,\n" +
                "    \"fortuneBonus\": false\n" +
                "  },\n" +
                "  {\n" +
                "    \"block\": \"ae2_meteorite_addon:certus_quartz_bud_1\",\n" +
                "    \"item\": \"appliedenergistics2:material:2\",\n" +
                "    \"count\": 1,\n" +
                "    \"fortuneBonus\": false\n" +
                "  },\n" +
                "  {\n" +
                "    \"block\": \"ae2_meteorite_addon:certus_quartz_bud_2\",\n" +
                "    \"item\": \"appliedenergistics2:material:2\",\n" +
                "    \"count\": 1,\n" +
                "    \"fortuneBonus\": false\n" +
                "  },\n" +
                "  {\n" +
                "    \"block\": \"ae2_meteorite_addon:certus_quartz_bud_3\",\n" +
                "    \"item\": \"appliedenergistics2:material:0\",\n" +
                "    \"count\": 4,\n" +
                "    \"fortuneBonus\": true\n" +
                "  }\n" +
                "]\n";
    }
}
