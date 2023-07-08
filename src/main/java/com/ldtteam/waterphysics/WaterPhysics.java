package com.ldtteam.waterphysics;

import com.ldtteam.waterphysics.handlers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.world.level.block.Block.UPDATE_ALL;

@Mod("waterphysics")
public class WaterPhysics
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "waterphysics";

    public WaterPhysics()
    {
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(this.getClass());
        ITransformationHandler.HANDLERS.add(new WaterFlow());
        ITransformationHandler.HANDLERS.add(new WaterSourcing());
    }

    @SubscribeEvent
    public static void BucketPlaceEvent(final FillBucketEvent event)
    {
        if (event.getLevel().isClientSide())
        {
            return;
        }

        if (event.getEmptyBucket().getItem() == Items.WATER_BUCKET && event.getTarget() instanceof BlockHitResult)
        {
            event.getLevel().scheduleTick(new BlockPos(((BlockHitResult) event.getTarget()).getBlockPos()), Fluids.WATER, 120);
        }
    }

    @SubscribeEvent
    public static void PistonPushEvent(final PistonEvent event)
    {
        if (event.getLevel().isClientSide())
        {
            return;
        }

        if (event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND)
        {
            final BlockState targetState = event.getLevel().getBlockState(event.getFaceOffsetPos().relative(event.getDirection()));
            final BlockState state = event.getLevel().getBlockState(event.getFaceOffsetPos());
            if (state.getFluidState() != null
                  && state.getFluidState().is(Fluids.WATER)
                  && state.getFluidState().isSource()
                  && (targetState.isAir() || targetState.getBlock() == Blocks.WATER && !targetState.getFluidState().isSource()))
            {
                event.getLevel().setBlock(event.getFaceOffsetPos().relative(event.getDirection()), Blocks.WATER.defaultBlockState(), UPDATE_ALL);
            }
        }
        else
        {
            final BlockState state = event.getLevel().getBlockState(event.getFaceOffsetPos().relative(event.getDirection()));
            if (state.getFluidState() != null
                  && state.getFluidState().is(Fluids.WATER)
                  && state.getFluidState().isSource()
                  && event.getLevel().getBlockState(event.getFaceOffsetPos()).isAir())
            {
                event.getLevel().setBlock(event.getFaceOffsetPos().relative(event.getDirection()), Blocks.AIR.defaultBlockState(), UPDATE_ALL);
                event.getLevel().setBlock(event.getFaceOffsetPos(), state, UPDATE_ALL);
            }
        }
    }
}
