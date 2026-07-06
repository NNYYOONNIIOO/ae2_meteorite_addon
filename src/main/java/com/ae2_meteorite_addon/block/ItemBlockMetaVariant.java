package com.ae2_meteorite_addon.block;

import com.ae2_meteorite_addon.AE2MeteoriteAddon;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockMetaVariant extends ItemBlock {

    public ItemBlockMetaVariant(Block block) {
        super(block);
        setHasSubtypes(true);
        setRegistryName(block.getRegistryName());
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + stack.getMetadata();
    }
}
