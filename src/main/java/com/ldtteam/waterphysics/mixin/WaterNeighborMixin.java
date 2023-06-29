package com.ldtteam.waterphysics.mixin;

import com.ldtteam.waterphysics.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidState.class)
public abstract class WaterNeighborMixin
{
    @Shadow public abstract boolean isSource();

    @Shadow public abstract Fluid getType();

    @Inject(method = "tick", at = @At("TAIL"))
    public void waterPhysicsNeighborChanged(final Level level, final BlockPos pos, final CallbackInfo ci)
    {
        if (isSource() && getType() == Fluids.WATER && level.getBlockState(pos).getBlock() == Blocks.WATER && !Utils.tryMoveBelow(level, pos, false) && !Utils.tryMoveSidewards(level, pos, false))
        {
            Utils.tryMoveDiagonally(level, pos, false);
        }
    }
}
