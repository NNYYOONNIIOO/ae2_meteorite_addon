package com.ae2_meteorite_addon.block;

import com.ae2_meteorite_addon.AE2MeteoriteAddon;
import com.ae2_meteorite_addon.ModCreativeTab;
import com.ae2_meteorite_addon.config.BuddingEntry;
import com.ae2_meteorite_addon.config.JsonConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Single-variant bud block. Each variant (small/medium/large/cluster) is a separate block instance.
 * Only has FACING property (6 states, well within 16 metadata limit).
 */
public class BlockBudGeneric extends Block {

    private final int variantIndex;
    private final String baseName;
    private final BuddingEntry.BudVariantConfig variantConfig;

    public BlockBudGeneric(String baseName, int variantIndex, BuddingEntry.BudVariantConfig config) {
        super(Material.GLASS);
        this.baseName = baseName;
        this.variantIndex = variantIndex;
        this.variantConfig = config;

        setDefaultState(blockState.getBaseState().withProperty(BlockDirectional.FACING, EnumFacing.UP));
        setSoundType(SoundType.STONE);
        setHardness(1.5f);
        setResistance(1.5f);
        setRegistryName(AE2MeteoriteAddon.MODID, baseName + "_" + variantIndex);
        setUnlocalizedName(AE2MeteoriteAddon.MODID + "." + baseName + "_" + variantIndex);
        setCreativeTab(ModCreativeTab.INSTANCE);
    }

    public int getVariantIndex() {
        return variantIndex;
    }

    public String getBaseName() {
        return baseName;
    }

    public BuddingEntry.BudVariantConfig getVariantConfig() {
        return variantConfig;
    }

    @Override
    public int getLightValue(IBlockState state) {
        return variantConfig != null ? variantConfig.lightLevel : 0;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (variantConfig == null) return FULL_BLOCK_AABB;

        float s = variantConfig.shrink / 16.0f;
        float h = variantConfig.height / 16.0f;
        float ns = (16 - variantConfig.shrink) / 16.0f;

        switch (state.getValue(BlockDirectional.FACING)) {
            case UP: return new AxisAlignedBB(s, 0.0, s, ns, h, ns);
            case DOWN: return new AxisAlignedBB(s, 1.0 - h, s, ns, 1.0, ns);
            case NORTH: return new AxisAlignedBB(s, s, 1.0 - h, ns, ns, 1.0);
            case SOUTH: return new AxisAlignedBB(s, s, 0.0, ns, ns, h);
            case EAST: return new AxisAlignedBB(0.0, s, s, h, ns, ns);
            case WEST: return new AxisAlignedBB(1.0 - h, s, s, 1.0, ns, ns);
            default: return FULL_BLOCK_AABB;
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (canPlaceOnFace(world, pos, facing)) {
            return getDefaultState().withProperty(BlockDirectional.FACING, facing);
        }
        for (EnumFacing f : EnumFacing.values()) {
            if (canPlaceOnFace(world, pos, f)) {
                return getDefaultState().withProperty(BlockDirectional.FACING, f);
            }
        }
        return getDefaultState();
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (canPlaceOnFace(world, pos, facing)) return true;
        }
        return false;
    }

    private boolean canPlaceOnFace(World world, BlockPos pos, EnumFacing facing) {
        BlockPos supportPos = pos.offset(facing.getOpposite());
        IBlockState supportState = world.getBlockState(supportPos);
        return isValidSupport(supportState) && supportState.isSideSolid(world, supportPos, facing);
    }

    private boolean isValidSupport(IBlockState state) {
        Block block = state.getBlock();
        return JsonConfigManager.isBuddingBlock(block);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        EnumFacing facing = state.getValue(BlockDirectional.FACING);
        BlockPos supportPos = pos.offset(facing.getOpposite());
        IBlockState supportState = world.getBlockState(supportPos);
        if (!isValidSupport(supportState)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (world.isRemote) return;
        ItemStack drop = JsonConfigManager.getBudDrop(this, fortune);
        if (!drop.isEmpty()) {
            spawnAsEntity(world, pos, drop);
        }
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockDirectional.FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getFront(meta & 7);
        return getDefaultState().withProperty(BlockDirectional.FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockDirectional.FACING).getIndex();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT;
    }
}
