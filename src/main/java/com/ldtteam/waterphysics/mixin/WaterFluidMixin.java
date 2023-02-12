package com.ldtteam.waterphysics.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin extends FlowingFluid
{
    @Override
    public boolean canConvertToSource(FluidState state, Level level, BlockPos pos)
    {
        final Holder<Biome> biome = level.getBiome(pos);
        return biome.is(Biomes.OCEAN) || biome.is(Biomes.RIVER);
    }
}
