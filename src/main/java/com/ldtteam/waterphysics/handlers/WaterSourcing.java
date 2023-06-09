package com.ldtteam.waterphysics.handlers;

import com.ldtteam.waterphysics.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import static net.minecraft.world.level.block.Block.UPDATE_ALL;

/**
 * Three sides sources can convert to source.
 */
public class WaterSourcing implements ITransformationHandler
{
    @Override
    public boolean transforms(final BlockState state)
    {
         return state.getBlock() == Blocks.WATER && !state.getFluidState().isSource() ;
    }

    @Override
    public boolean ready(final long worldTick, final LevelChunk chunk)
    {
        return chunk.getLevel().isRaining();
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        final LevelChunkSection section = chunk.getSections()[chunkSection];
        final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos, chunk.getSectionYFromSectionIndex(chunkSection));

        int waterCount = 0;
        for (final Direction direction : Direction.Plane.HORIZONTAL)
        {
            final BlockState state = Utils.getBlockState(chunk, relativePos.relative(direction), chunkSection);
            if (state.getFluidState() != null && state.getFluidState().isSource())
            {
                waterCount++;
            }
        }

        if (waterCount >= 3)
        {
            chunk.getLevel().setBlock(worldPos, Blocks.WATER.defaultBlockState(), UPDATE_ALL);
        }
    }
}
