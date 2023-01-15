package com.ldtteam.waterphysics.mixin;

import com.ldtteam.waterphysics.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin
{

    @Inject(method = "canConvertToSource", cancellable = true, at = @At("TAIL"))
    public void neighborChanged(final CallbackInfoReturnable<Boolean> cir)
    {
        cir.setReturnValue(false);
    }
}
