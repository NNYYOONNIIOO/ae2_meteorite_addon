package com.ae2_meteorite_addon;

import com.ae2_meteorite_addon.config.FixEntry;
import com.ae2_meteorite_addon.config.JsonConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

/**
 * Handles fix.json in-world transformations (e.g., budding quartz repair).
 * Uses WorldTickEvent to periodically scan for EntityItems in liquid and
 * apply transformations defined in fix.json.
 */
@Mod.EventBusSubscriber(modid = AE2MeteoriteAddon.MODID)
public class ModEventHandler {

    // Per-dimension tick counters (a single static counter would be shared across all worlds,
    // causing the scan to only run for whichever dimension happened to be ticked when it hit 20)
    private static final Map<Integer, Integer> tickCounters = new HashMap<>();

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || event.phase != TickEvent.Phase.END) {
            return;
        }

        int dim = event.world.provider.getDimension();
        int counter = tickCounters.getOrDefault(dim, 0) + 1;
        if (counter < 20) {
            tickCounters.put(dim, counter);
            return;
        }
        tickCounters.put(dim, 0);

        World world = event.world;

        // Iterate loadedEntityList directly (avoids Guava Predicate issues with getEntities)
        List<Entity> entityListCopy = new ArrayList<>(world.loadedEntityList);

        for (Entity entity : entityListCopy) {
            if (entity instanceof EntityItem && !entity.isDead) {
                tryTransform((EntityItem) entity);
            }
        }
    }

    private static void tryTransform(EntityItem entityItem) {
        if (entityItem == null || entityItem.isDead || entityItem.world.isRemote) {
            return;
        }

        World world = entityItem.world;

        // Find liquid block at the entity's position
        BlockPos fluidPos = findLiquidAtEntity(world, entityItem);
        if (fluidPos == null) {
            return;
        }

        IBlockState fluidState = world.getBlockState(fluidPos);

        // Get all nearby entity items within 1.5 blocks
        AxisAlignedBB region = new AxisAlignedBB(
                entityItem.posX - 1.5, entityItem.posY - 1.5, entityItem.posZ - 1.5,
                entityItem.posX + 1.5, entityItem.posY + 1.5, entityItem.posZ + 1.5);
        List<EntityItem> nearbyItems = world.getEntitiesWithinAABB(EntityItem.class, region);

        // Collect all item stacks with their entity references
        List<ItemStack> stacks = new ArrayList<>();
        List<EntityItem> entities = new ArrayList<>();
        for (EntityItem ei : nearbyItems) {
            if (ei.isDead) continue;
            ItemStack s = ei.getItem();
            if (s.isEmpty()) continue;
            stacks.add(s);
            entities.add(ei);
        }

        // Try each fix entry
        for (FixEntry fix : JsonConfigManager.getFixEntries()) {
            // Check fluid matches
            if (!matchesFluid(fluidState, fix.fluid)) continue;

            // Check if all inputs are present
            List<Integer> matchedIndices = findMatchingInputs(stacks, fix.inputs);
            if (matchedIndices == null) continue;

            // Consume one of each matched input
            Set<EntityItem> toConsume = new HashSet<>();
            for (int idx : matchedIndices) {
                EntityItem ei = entities.get(idx);
                if (!toConsume.contains(ei)) {
                    ei.getItem().shrink(1);
                    if (ei.getItem().getCount() <= 0) {
                        ei.setDead();
                    }
                    toConsume.add(ei);
                }
            }

            // Consume fluid if needed
            if (fix.consumeFluid) {
                world.setBlockToAir(fluidPos);
            }

            // Spawn output
            ItemStack output = JsonConfigManager.parseItemStack(fix.output);
            if (!output.isEmpty()) {
                EntityItem resultEntity = new EntityItem(world, entityItem.posX, entityItem.posY, entityItem.posZ, output);
                world.spawnEntity(resultEntity);
            }
            return;
        }
    }

    /**
     * Find a liquid block at the entity's position.
     */
    private static BlockPos findLiquidAtEntity(World world, EntityItem entityItem) {
        AxisAlignedBB bbox = entityItem.getEntityBoundingBox();
        int minX = MathHelper.floor(bbox.minX);
        int minY = MathHelper.floor(bbox.minY);
        int minZ = MathHelper.floor(bbox.minZ);
        int maxX = MathHelper.floor(bbox.maxX);
        int maxY = MathHelper.floor(bbox.maxY);
        int maxZ = MathHelper.floor(bbox.maxZ);

        // Check all blocks covered by the bounding box
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).getMaterial().isLiquid()) {
                        return pos;
                    }
                }
            }
        }

        // Check the block below (items floating on surface)
        BlockPos below = new BlockPos(
                MathHelper.floor(entityItem.posX),
                MathHelper.floor(entityItem.posY - 0.1),
                MathHelper.floor(entityItem.posZ));
        if (world.getBlockState(below).getMaterial().isLiquid()) {
            return below;
        }

        return null;
    }

    private static boolean matchesFluid(IBlockState state, String fluidRef) {
        if (fluidRef == null) return false;
        Block block = state.getBlock();
        if (fluidRef.equals("minecraft:water")) {
            return block == Blocks.WATER || block == Blocks.FLOWING_WATER;
        }
        if (fluidRef.equals("minecraft:lava")) {
            return block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
        }
        ResourceLocation registryName = block.getRegistryName();
        return registryName != null && registryName.toString().equals(fluidRef);
    }

    private static List<Integer> findMatchingInputs(List<ItemStack> stacks, List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) return null;
        List<Integer> result = new ArrayList<>();
        boolean[] used = new boolean[stacks.size()];

        for (String inputRef : inputs) {
            boolean found = false;
            for (int i = 0; i < stacks.size(); i++) {
                if (used[i]) continue;
                if (matchesItem(stacks.get(i), inputRef)) {
                    result.add(i);
                    used[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) return null;
        }
        return result;
    }

    private static boolean matchesItem(ItemStack stack, String ref) {
        if (stack.isEmpty() || ref == null) return false;
        String[] parts = ref.split(":");
        if (parts.length < 2) return false;
        String modid = parts[0];
        String itemId = parts[1];
        int meta = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        Item item = Item.REGISTRY.getObject(new ResourceLocation(modid, itemId));
        if (item == null) return false;
        if (stack.getItem() != item) return false;
        if (meta == -1) return true;
        return stack.getMetadata() == meta;
    }
}
