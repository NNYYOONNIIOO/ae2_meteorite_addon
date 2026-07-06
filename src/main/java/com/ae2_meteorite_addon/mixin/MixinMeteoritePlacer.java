package com.ae2_meteorite_addon.mixin;

import appeng.api.AEApi;
import appeng.worldgen.MeteoritePlacer;
import appeng.worldgen.meteorite.IMeteoriteWorld;
import com.ae2_meteorite_addon.ModConfig;
import com.ae2_meteorite_addon.config.JsonConfigManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(value = MeteoritePlacer.class, remap = false)
public class MixinMeteoritePlacer {

    @Shadow(remap = false)
    private NBTTagCompound settings;

    @Inject(method = "spawnMeteorite(Lappeng/worldgen/meteorite/IMeteoriteWorld;III)Z",
            at = @At(value = "INVOKE",
                    target = "Lappeng/worldgen/MeteoritePlacer;placeMeteorite(Lappeng/worldgen/meteorite/IMeteoriteWorld;III)V",
                    shift = At.Shift.AFTER),
            remap = false)
    private void ae2_meteorite_addon$onSpawnMeteoriteInt(IMeteoriteWorld w, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        ae2_meteorite_addon$placeBuddingBlocks(w, x, y, z);
    }

    @Inject(method = "spawnMeteorite(Lappeng/worldgen/meteorite/IMeteoriteWorld;Lnet/minecraft/nbt/NBTTagCompound;)Z",
            at = @At(value = "INVOKE",
                    target = "Lappeng/worldgen/MeteoritePlacer;placeMeteorite(Lappeng/worldgen/meteorite/IMeteoriteWorld;III)V",
                    shift = At.Shift.AFTER),
            remap = false)
    private void ae2_meteorite_addon$onSpawnMeteoriteNBT(IMeteoriteWorld w, NBTTagCompound meteoriteBlob, CallbackInfoReturnable<Boolean> cir) {
        if (settings != null) {
            int x = settings.getInteger("x");
            int y = settings.getInteger("y");
            int z = settings.getInteger("z");
            ae2_meteorite_addon$placeBuddingBlocks(w, x, y, z);
        }
    }

    @Unique
    private void ae2_meteorite_addon$placeBuddingBlocks(IMeteoriteWorld w, int x, int y, int z) {
        if (!ModConfig.enableMeteoriteBudding) return;

        List<IBlockState> buddingStates = JsonConfigManager.getMeteoriteBuddingBlocks();
        if (buddingStates.isEmpty()) return;

        // Also add the AE2 quartz block as a possible spawn
        List<IBlockState> allOptions = new ArrayList<>(buddingStates);
        AEApi.instance().definitions().blocks().quartzBlock().maybeBlock().ifPresent(qb -> {
            allOptions.add(qb.getDefaultState());
        });

        Random random = w.getWorld().rand;

        List<int[]> validPositions = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    int bx = x + dx;
                    int by = y + dy;
                    int bz = z + dz;
                    Block existing = w.getBlock(bx, by, bz);
                    if (existing != Blocks.AIR && existing != Blocks.BEDROCK) {
                        validPositions.add(new int[]{bx, by, bz});
                    }
                }
            }
        }

        Collections.shuffle(validPositions, random);

        int count = Math.min(9, validPositions.size());
        for (int i = 0; i < count; i++) {
            int[] pos = validPositions.get(i);
            IBlockState chosen = allOptions.get(random.nextInt(allOptions.size()));
            w.setBlock(pos[0], pos[1], pos[2], chosen, 3);
        }
    }
}
