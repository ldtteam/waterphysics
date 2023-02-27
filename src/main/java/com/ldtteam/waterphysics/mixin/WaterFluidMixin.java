package com.ldtteam.waterphysics.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin extends FlowingFluid
{
    @Inject(method = "canConvertToSource", cancellable = true, at = @At("TAIL"))
    public void canConvertToSource(final CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(false);
    }

    @Override
    public boolean canConvertToSource(FluidState state, LevelReader reader, BlockPos pos)
    {
        final Holder<Biome> biome = reader.getBiome(pos);
        return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER);
    }
}
