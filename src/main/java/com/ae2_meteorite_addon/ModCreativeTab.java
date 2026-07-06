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
        // Use first budding block variant 0
        if (!DynamicBlockRegistry.getAllBlocks().isEmpty()) {
            return new ItemStack(DynamicBlockRegistry.getAllBlocks().get(0), 1, 0);
        }
        return ItemStack.EMPTY;
    }
}
