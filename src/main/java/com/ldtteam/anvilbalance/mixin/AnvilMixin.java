package com.ldtteam.anvilbalance.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public class AnvilMixin
{
    @Inject(method = "calculateIncreasedRepairCost", at = @At("RETURN"), cancellable = true)
    private static void adjRepairCostCurve(final int currentCost, final CallbackInfoReturnable<Integer> cir)
    {
        cir.setReturnValue(((int) (currentCost * 1.2)) + 1);
    }

    @ModifyArg(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/DataSlot;set(I)V "), index = 0)
    private int limitMaxCost(int cost)
    {
        return Math.min(cost, 10);
    }

    @ModifyVariable(method = "createResult", at = @At(value = "STORE"), name="i1")
    private int adjRepairEqualItemCost(final int oldValue, @Local(ordinal = 1) int j)
    {
        return (int) Math.ceil(oldValue / Math.max(2.0, j));
    }

    @ModifyVariable(method = "createResult", at = @At(value = "STORE"), name="l2")
    private int adjRepairItemCost(final int oldValue, @Local(ordinal = 1) int j, @Local(ordinal = 1) ItemStack itemstack1)
    {
        return Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / Math.max(4, j*2));
    }
}
