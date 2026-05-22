package com.ae2_meteorite_addon;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ModCreativeTab extends CreativeTabs {

    public static final ModCreativeTab INSTANCE = new ModCreativeTab();

    private ModCreativeTab() {
        super("ae2_meteorite_addon");
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(ModBlocks.FLAWLESS_BUDDING_CERTUS_QUARTZ);
    }
}
