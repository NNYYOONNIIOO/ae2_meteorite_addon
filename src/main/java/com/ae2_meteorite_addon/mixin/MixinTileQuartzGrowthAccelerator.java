package com.ae2_meteorite_addon.mixin;

import appeng.api.implementations.tiles.ICrystalGrowthAccelerator;
import appeng.tile.misc.TileQuartzGrowthAccelerator;
import com.ae2_meteorite_addon.ModConfig;
import com.ae2_meteorite_addon.block.BlockBuddingGeneric;
import com.ae2_meteorite_addon.config.JsonConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Random;
import java.util.Set;

@Mixin(value = TileQuartzGrowthAccelerator.class, remap = false)
public abstract class MixinTileQuartzGrowthAccelerator extends TileEntity implements ICrystalGrowthAccelerator, ITickable {

    @Unique
    private int ae2_meteorite_addon$tickCounter = 0;

    @Override
    public void update() {
        ae2_meteorite_addon$accelerateAdjacentBudding();
    }

    @Unique
    private void ae2_meteorite_addon$accelerateAdjacentBudding() {
        World world = this.getWorld();
        if (world == null || world.isRemote) {
            return;
        }

        if (!this.isPowered()) {
            return;
        }

        ae2_meteorite_addon$tickCounter++;
        if (ae2_meteorite_addon$tickCounter < ModConfig.growthAcceleratorSpeed) {
            return;
        }
        ae2_meteorite_addon$tickCounter = 0;

        BlockPos pos = this.getPos();
        Random random = world.rand;
        Set<Block> acceleratable = JsonConfigManager.getAcceleratableBuddingBlocks();

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adjacentPos = pos.offset(facing);
            IBlockState adjacentState = world.getBlockState(adjacentPos);
            Block adjacentBlock = adjacentState.getBlock();

            if (acceleratable.contains(adjacentBlock)) {
                adjacentBlock.randomTick(world, adjacentPos, adjacentState, random);
            }
        }
    }
}
