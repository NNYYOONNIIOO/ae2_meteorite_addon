package com.ae2_meteorite_addon.block;

import appeng.api.AEApi;
import com.ae2_meteorite_addon.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockCertusQuartzBud extends Block {

    public static final net.minecraft.block.properties.PropertyDirection FACING = BlockDirectional.FACING;

    private final boolean isCluster;
    private final int lightLevel;
    private final Block nextStage;
    private final AxisAlignedBB upAabb;
    private final AxisAlignedBB downAabb;
    private final AxisAlignedBB northAabb;
    private final AxisAlignedBB southAabb;
    private final AxisAlignedBB eastAabb;
    private final AxisAlignedBB westAabb;

    public BlockCertusQuartzBud(int height, int shrink, int lightLevel, SoundType soundType, boolean isCluster, Block nextStage) {
        super(Material.GLASS);
        this.isCluster = isCluster;
        this.lightLevel = lightLevel;
        this.nextStage = nextStage;

        float s = shrink / 16.0f;
        float h = height / 16.0f;
        float ns = (16 - shrink) / 16.0f;

        this.upAabb = new AxisAlignedBB(s, 0.0, s, ns, h, ns);
        this.downAabb = new AxisAlignedBB(s, 1.0 - h, s, ns, 1.0, ns);
        this.northAabb = new AxisAlignedBB(s, s, 1.0 - h, ns, ns, 1.0);
        this.southAabb = new AxisAlignedBB(s, s, 0.0, ns, ns, h);
        this.eastAabb = new AxisAlignedBB(0.0, s, s, h, ns, ns);
        this.westAabb = new AxisAlignedBB(1.0 - h, s, s, 1.0, ns, ns);

        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
        setSoundType(soundType);
        setHardness(1.5f);
        setResistance(1.5f);
    }

    @Override
    public int getLightValue(IBlockState state) {
        return lightLevel;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case UP: return upAabb;
            case DOWN: return downAabb;
            case NORTH: return northAabb;
            case SOUTH: return southAabb;
            case EAST: return eastAabb;
            case WEST: return westAabb;
            default: return FULL_BLOCK_AABB;
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (canPlaceOnFace(world, pos, facing)) {
            return getDefaultState().withProperty(FACING, facing);
        }
        for (EnumFacing f : EnumFacing.values()) {
            if (canPlaceOnFace(world, pos, f)) {
                return getDefaultState().withProperty(FACING, f);
            }
        }
        return getDefaultState();
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (canPlaceOnFace(world, pos, facing)) {
                return true;
            }
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
        if (block instanceof BlockBuddingCertusQuartz) {
            return true;
        }
        return AEApi.instance().definitions().blocks().quartzBlock().maybeBlock()
                .map(qb -> block == qb).orElse(false);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        EnumFacing facing = state.getValue(FACING);
        BlockPos supportPos = pos.offset(facing.getOpposite());
        IBlockState supportState = world.getBlockState(supportPos);
        if (!isValidSupport(supportState)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (isCluster) {
            return AEApi.instance().definitions().materials().certusQuartzCrystal().maybeItem().orElse(null);
        }
        return null;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        if (isCluster) {
            return Math.max(4, 4 + random.nextInt(fortune + 1));
        }
        return 0;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (isCluster) {
            return Math.max(4, 4 + random.nextInt(fortune + 1));
        }
        return 0;
    }

    @Override
    public int damageDropped(IBlockState state) {
        if (isCluster) {
            return AEApi.instance().definitions().materials().certusQuartzCrystal().maybeStack(1)
                    .map(ItemStack::getMetadata).orElse(0);
        }
        return 0;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.values()[meta & 7]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
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

    public Block getNextStage() {
        return nextStage;
    }
}
