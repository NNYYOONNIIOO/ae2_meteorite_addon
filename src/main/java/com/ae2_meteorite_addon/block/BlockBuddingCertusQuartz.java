package com.ae2_meteorite_addon.block;

import appeng.api.AEApi;
import com.ae2_meteorite_addon.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockBuddingCertusQuartz extends Block {

    public BlockBuddingCertusQuartz() {
        super(Material.ROCK);
        setHardness(3.0f);
        setResistance(5.0f);
        setHarvestLevel("pickaxe", 0);
        setTickRandomly(true);
        setSoundType(SoundType.STONE);
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (random.nextInt(5) != 0) {
            return;
        }

        EnumFacing facing = EnumFacing.values()[random.nextInt(6)];
        BlockPos budPos = pos.offset(facing);
        IBlockState budState = world.getBlockState(budPos);

        if (budState.getBlock().isAir(budState, world, budPos)) {
            IBlockState smallBudState = ModBlocks.SMALL_CERTUS_QUARTZ_BUD.getDefaultState()
                    .withProperty(BlockCertusQuartzBud.FACING, facing);
            world.setBlockState(budPos, smallBudState, 3);
            tryDegrade(world, pos, state, random);
        } else if (budState.getBlock() instanceof BlockCertusQuartzBud) {
            BlockCertusQuartzBud bud = (BlockCertusQuartzBud) budState.getBlock();
            Block nextStage = bud.getNextStage();
            if (nextStage != null) {
                IBlockState nextState = nextStage.getDefaultState()
                        .withProperty(BlockCertusQuartzBud.FACING, budState.getValue(BlockCertusQuartzBud.FACING));
                world.setBlockState(budPos, nextState, 3);
                tryDegrade(world, pos, state, random);
            }
        }
    }

    private void tryDegrade(World world, BlockPos pos, IBlockState state, Random random) {
        if (this == ModBlocks.FLAWLESS_BUDDING_CERTUS_QUARTZ) {
            return;
        }
        if (random.nextInt(12) != 0) {
            return;
        }
        Block degraded = getDegradedBlock();
        if (degraded != null) {
            world.setBlockState(pos, degraded.getDefaultState(), 3);
        }
    }

    private Block getDegradedBlock() {
        if (this == ModBlocks.FLAWED_BUDDING_CERTUS_QUARTZ) {
            return ModBlocks.CHIPPED_BUDDING_CERTUS_QUARTZ;
        }
        if (this == ModBlocks.CHIPPED_BUDDING_CERTUS_QUARTZ) {
            return ModBlocks.DAMAGED_BUDDING_CERTUS_QUARTZ;
        }
        if (this == ModBlocks.DAMAGED_BUDDING_CERTUS_QUARTZ) {
            return AEApi.instance().definitions().blocks().quartzBlock().maybeBlock().orElse(null);
        }
        return null;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == ModBlocks.FLAWLESS_BUDDING_CERTUS_QUARTZ) {
            return Item.getItemFromBlock(ModBlocks.FLAWED_BUDDING_CERTUS_QUARTZ);
        }
        Block degraded = getDegradedBlock();
        if (degraded != null) {
            return Item.getItemFromBlock(degraded);
        }
        return AEApi.instance().definitions().blocks().quartzBlock().maybeItem().orElse(null);
    }

    @Override
    public int damageDropped(IBlockState state) {
        if (this == ModBlocks.FLAWLESS_BUDDING_CERTUS_QUARTZ) {
            return 0;
        }
        Block degraded = getDegradedBlock();
        if (degraded != null) {
            return 0;
        }
        return AEApi.instance().definitions().blocks().quartzBlock().maybeStack(1)
                .map(ItemStack::getMetadata).orElse(0);
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, net.minecraft.entity.player.EntityPlayer player) {
        if (this == ModBlocks.FLAWLESS_BUDDING_CERTUS_QUARTZ) {
            return false;
        }
        return true;
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (world.isRemote) {
            return;
        }

        if (this == ModBlocks.FLAWLESS_BUDDING_CERTUS_QUARTZ) {
            spawnAsEntity(world, pos, new ItemStack(ModBlocks.FLAWED_BUDDING_CERTUS_QUARTZ));
            return;
        }

        net.minecraft.entity.player.EntityPlayer player = net.minecraftforge.common.ForgeHooks
                .getCraftingPlayer();
        boolean hasSilkTouch = false;
        if (player != null) {
            ItemStack heldItem = player.getHeldItemMainhand();
            hasSilkTouch = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, heldItem) > 0;
        }

        if (hasSilkTouch) {
            spawnAsEntity(world, pos, new ItemStack(this));
        } else {
            Block degraded = getDegradedBlock();
            if (degraded != null) {
                spawnAsEntity(world, pos, new ItemStack(degraded));
            }
        }
    }

    @Override
    public net.minecraft.block.material.EnumPushReaction getMobilityFlag(IBlockState state) {
        return net.minecraft.block.material.EnumPushReaction.DESTROY;
    }
}
