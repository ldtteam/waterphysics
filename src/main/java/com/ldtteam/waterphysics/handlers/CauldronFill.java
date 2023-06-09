package com.ldtteam.waterphysics.handlers;

import com.ldtteam.waterphysics.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/**
 * Check if a water block should randomly start flowing.
 */
public class CauldronFill implements ITransformationHandler
{
    @Override
    public boolean transforms(final BlockState state)
    {
        return state.getBlock() == Blocks.CAULDRON || state.getBlock() == Blocks.WATER_CAULDRON;
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
        if (!Utils.tryMoveBelow(chunk.getLevel(), worldPos, true) && !Utils.tryMoveSidewards(chunk.getLevel(), worldPos, true))
        {
            Utils.tryMoveDiagonally(chunk.getLevel(), worldPos, true);
        }
    }
}
