package com.ae2_meteorite_addon.entity;

import appeng.api.AEApi;
import com.ae2_meteorite_addon.block.BlockBuddingGeneric;
import com.ae2_meteorite_addon.block.BlockBudGeneric;
import com.ae2_meteorite_addon.config.BuddingEntry;
import com.ae2_meteorite_addon.config.JsonConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class EntityBuddingRepair extends EntityItem {

    private int transformTime = 0;

    public EntityBuddingRepair(World world) {
        super(world);
    }

    public EntityBuddingRepair(World world, double x, double y, double z, ItemStack stack) {
        super(world, x, y, z, stack);
        this.setNoDespawn();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.isDead || this.world.isRemote) {
            return;
        }

        int j = MathHelper.floor(this.posX);
        int i = MathHelper.floor((this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D);
        int k = MathHelper.floor(this.posZ);

        IBlockState state = this.world.getBlockState(new BlockPos(j, i, k));
        Material mat = state.getMaterial();

        if (mat.isLiquid()) {
            this.transformTime++;
            if (this.transformTime > 60) {
                if (!this.transform()) {
                    this.transformTime = 0;
                }
            }
        } else {
            this.transformTime = 0;
        }
    }

    private boolean transform() {
        ItemStack selfItem = this.getItem();
        if (!AEApi.instance().definitions().materials().certusQuartzCrystalCharged().isSameAs(selfItem)) {
            return false;
        }

        AxisAlignedBB region = new AxisAlignedBB(
                this.posX - 1, this.posY - 1, this.posZ - 1,
                this.posX + 1, this.posY + 1, this.posZ + 1);
        List<EntityItem> nearbyItems = this.world.getEntitiesWithinAABB(EntityItem.class, region);

        for (EntityItem entityItem : nearbyItems) {
            if (entityItem == this || entityItem.isDead) {
                continue;
            }
            ItemStack otherStack = entityItem.getItem();
            if (otherStack.isEmpty()) {
                continue;
            }

            ItemStack result = getRepairResult(otherStack);
            if (result != null) {
                this.getItem().shrink(1);
                if (this.getItem().getCount() <= 0) {
                    this.setDead();
                }

                entityItem.getItem().shrink(1);
                if (entityItem.getItem().getCount() <= 0) {
                    entityItem.setDead();
                }

                EntityItem resultEntity = new EntityItem(this.world, this.posX, this.posY, this.posZ, result);
                this.world.spawnEntity(resultEntity);

                return true;
            }
        }

        return false;
    }

    private ItemStack getRepairResult(ItemStack input) {
        if (!(input.getItem() instanceof ItemBlock)) {
            return null;
        }

        Block block = ((ItemBlock) input.getItem()).getBlock();
        int meta = input.getMetadata();

        // Check AE2 quartz block -> damaged budding (highest variant)
        Optional<Block> quartzBlock = AEApi.instance().definitions().blocks().quartzBlock().maybeBlock();
        if (quartzBlock.isPresent() && block == quartzBlock.get()) {
            for (BuddingEntry entry : JsonConfigManager.getBuddingEntries()) {
                if ("block".equals(entry.type)) {
                    Block buddingBlock = JsonConfigManager.getBuddingBlock("ae2_meteorite_addon:" + entry.name);
                    if (buddingBlock != null) {
                        return new ItemStack(buddingBlock, 1, entry.metaCount - 1);
                    }
                }
            }
        }

        // Check budding block -> upgrade one level (lower variant = better)
        if (block instanceof BlockBuddingGeneric) {
            if (meta > 0) {
                return new ItemStack(block, 1, meta - 1);
            }
        }

        return null;
    }
}
