package com.ae2_meteorite_addon.block;

import com.ae2_meteorite_addon.AE2MeteoriteAddon;
import com.ae2_meteorite_addon.ModCreativeTab;
import com.ae2_meteorite_addon.config.BuddingEntry;
import com.ae2_meteorite_addon.config.DegenerateEntry;
import com.ae2_meteorite_addon.config.JsonConfigManager;
import com.ae2_meteorite_addon.config.SpawnEntry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockBuddingGeneric extends Block {

    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 3);

    private final int maxVariant;

    public BlockBuddingGeneric(String name, int metaCount) {
        super(Material.ROCK);
        this.maxVariant = metaCount - 1;
        setHardness(3.0f);
        setResistance(5.0f);
        setHarvestLevel("pickaxe", 0);
        setTickRandomly(true);
        setSoundType(SoundType.STONE);
        setRegistryName(AE2MeteoriteAddon.MODID, name);
        setUnlocalizedName(AE2MeteoriteAddon.MODID + "." + name);
        setCreativeTab(ModCreativeTab.INSTANCE);
        setDefaultState(blockState.getBaseState().withProperty(VARIANT, 0));
    }

    public int getMaxVariant() {
        return maxVariant;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VARIANT, Math.min(meta, maxVariant));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (int i = 0; i <= maxVariant; i++) {
            items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        SpawnEntry spawn = JsonConfigManager.getSpawnEntry(this, state.getValue(VARIANT));
        if (spawn == null) return;

        if (random.nextFloat() >= spawn.growChance) {
            return;
        }

        EnumFacing facing = EnumFacing.values()[random.nextInt(6)];
        BlockPos budPos = pos.offset(facing);
        IBlockState budState = world.getBlockState(budPos);

        if (budState.getBlock().isAir(budState, world, budPos)) {
            // Try to grow first bud stage
            IBlockState firstStage = JsonConfigManager.getFirstBudStage(this);
            if (firstStage != null) {
                IBlockState placed = firstStage.withProperty(net.minecraft.block.BlockDirectional.FACING, facing);
                world.setBlockState(budPos, placed, 3);
                tryDegrade(world, pos, state, random);
            }
        } else if (budState.getBlock() instanceof BlockBudGeneric) {
            // Try to advance bud to next stage
            BlockBudGeneric bud = (BlockBudGeneric) budState.getBlock();
            EnumFacing budFacing = budState.getValue(net.minecraft.block.BlockDirectional.FACING);

            // Find next stage in spawn config
            IBlockState nextState = JsonConfigManager.getNextBudStage(bud, budFacing);
            if (nextState != null) {
                world.setBlockState(budPos, nextState, 3);
                tryDegrade(world, pos, state, random);
            }
        }
    }

    private void tryDegrade(World world, BlockPos pos, IBlockState state, Random random) {
        int variant = state.getValue(VARIANT);
        DegenerateEntry entry = JsonConfigManager.getDegenerateEntry(this, variant);
        if (entry == null) return;

        if (random.nextFloat() >= entry.probability) {
            return;
        }

        // Check if source equals target (effectively no degradation)
        if (entry.source.equals(entry.target)) {
            return;
        }

        IBlockState targetState = JsonConfigManager.parseBlockState(entry.target);
        if (targetState != null) {
            world.setBlockState(pos, targetState, 3);
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (world.isRemote) return;

        int variant = state.getValue(VARIANT);
        DegenerateEntry entry = JsonConfigManager.getDegenerateEntry(this, variant);

        // Flawless (variant 0) always drops variant 1 (flawed)
        if (variant == 0) {
            spawnAsEntity(world, pos, new ItemStack(this, 1, 1));
            return;
        }

        // Check silk touch
        net.minecraft.entity.player.EntityPlayer player = net.minecraftforge.common.ForgeHooks.getCraftingPlayer();
        boolean hasSilkTouch = false;
        if (player != null) {
            ItemStack heldItem = player.getHeldItemMainhand();
            hasSilkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, heldItem) > 0;
        }

        if (hasSilkTouch) {
            spawnAsEntity(world, pos, new ItemStack(this, 1, variant));
        } else {
            // Drop degraded version
            if (entry != null && !entry.source.equals(entry.target)) {
                IBlockState targetState = JsonConfigManager.parseBlockState(entry.target);
                if (targetState != null) {
                    Block targetBlock = targetState.getBlock();
                    int targetMeta = targetBlock.getMetaFromState(targetState);
                    spawnAsEntity(world, pos, new ItemStack(targetBlock, 1, targetMeta));
                }
            } else {
                // No degradation rule, drop itself
                spawnAsEntity(world, pos, new ItemStack(this, 1, variant));
            }
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int variant = state.getValue(VARIANT);

        // Flawless (variant 0) always drops variant 1 (flawed)
        if (variant == 0) {
            drops.add(new ItemStack(this, 1, 1));
            return;
        }

        // Non-player break: drop degraded version (no silk touch)
        DegenerateEntry entry = JsonConfigManager.getDegenerateEntry(this, variant);
        if (entry != null && !entry.source.equals(entry.target)) {
            IBlockState targetState = JsonConfigManager.parseBlockState(entry.target);
            if (targetState != null) {
                Block targetBlock = targetState.getBlock();
                int targetMeta = targetBlock.getMetaFromState(targetState);
                drops.add(new ItemStack(targetBlock, 1, targetMeta));
                return;
            }
        }

        // No degradation rule, drop itself
        drops.add(new ItemStack(this, 1, variant));
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, net.minecraft.entity.player.EntityPlayer player) {
        int variant = state.getValue(VARIANT);
        if (variant == 0) return false; // Flawless cannot be silk touched
        return true;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, net.minecraft.entity.player.EntityPlayer player) {
        return new ItemStack(this, 1, state.getValue(VARIANT));
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}
