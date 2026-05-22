package com.ae2_meteorite_addon.mixin;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.definitions.IItemDefinition;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.me.GridAccessException;
import appeng.parts.PartBasicState;
import appeng.parts.automation.PartAnnihilationPlane;
import appeng.util.item.AEItemStack;
import com.ae2_meteorite_addon.ModConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(value = PartAnnihilationPlane.class, remap = false)
public abstract class MixinPartAnnihilationPlane extends PartBasicState {

    @Shadow(remap = false)
    private IActionSource mySrc;

    @Shadow(remap = false)
    private boolean breaking;

    @Shadow(remap = false)
    private boolean isAccepting;

    @Invoker(value = "breakBlock", remap = false)
    protected abstract TickRateModulation ae2_meteorite_addon$invokeBreakBlock(boolean modulate);

    @Unique
    private int ae2_meteorite_addon$meteoriteTimer = 0;

    protected MixinPartAnnihilationPlane(ItemStack is) {
        super(is);
    }

    @Overwrite(remap = false)
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (this.breaking) {
            return TickRateModulation.URGENT;
        }
        this.isAccepting = true;
        TickRateModulation result = this.ae2_meteorite_addon$invokeBreakBlock(false);

        if (ModConfig.enableMeteoriteDust && this.getSide() == AEPartLocation.UP) {
            TileEntity te = this.getTile();
            if (te != null && te.getWorld() != null) {
                BlockPos targetPos = te.getPos().up();
                if (ae2_meteorite_addon$isAtWorldTop(te, targetPos)) {
                    this.ae2_meteorite_addon$meteoriteTimer += ticksSinceLastCall;
                    if (this.ae2_meteorite_addon$meteoriteTimer >= ModConfig.getMeteoriteDustIntervalTicks()) {
                        this.ae2_meteorite_addon$meteoriteTimer -= ModConfig.getMeteoriteDustIntervalTicks();
                        IItemDefinition skyDust = AEApi.instance().definitions().materials().skyDust();
                        Optional<ItemStack> skyDustStack = skyDust.maybeStack(1);
                        if (skyDustStack.isPresent()) {
                            try {
                                IStorageGrid storage = this.getProxy().getStorage();
                                IAEItemStack itemToStore = AEItemStack.fromItemStack(skyDustStack.get());
                                storage.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class))
                                        .injectItems(itemToStore, Actionable.MODULATE, this.mySrc);
                            } catch (GridAccessException e) {
                            }
                        }
                    }
                    if (result == TickRateModulation.IDLE) {
                        result = TickRateModulation.SLOWER;
                    }
                } else {
                    this.ae2_meteorite_addon$meteoriteTimer = 0;
                }
            }
        }

        return result;
    }

    @Unique
    private boolean ae2_meteorite_addon$isAtWorldTop(TileEntity te, BlockPos targetPos) {
        if (targetPos.getY() >= te.getWorld().getActualHeight()) {
            return true;
        }
        if (targetPos.getY() >= te.getWorld().getActualHeight() - 1) {
            IBlockState state = te.getWorld().getBlockState(targetPos);
            return state.getBlock().isAir(state, te.getWorld(), targetPos);
        }
        return false;
    }
}
