package com.ldtteam.waterphysics.handlers;

import com.ldtteam.waterphysics.utils.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.gameevent.GameEvent;

import static net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL;

/**
 * Check if a water block should randomly start flowing.
 */
public class CauldronFill implements ITransformationHandler
{
    @Override
    public boolean transforms(final BlockState state)
    {
        return state.getBlock() == Blocks.WATER_CAULDRON || state.getBlock() == Blocks.CAULDRON;
    }

    @Override
    public boolean ready(final long worldTick, final LevelChunk chunk)
    {
        return chunk.getLevel().isRaining() && worldTick % 20 == 0;
    }

    @Override
    public void transformBlock(final BlockPos relativePos, final LevelChunk chunk, final int chunkSection, final BlockState input)
    {
        final LevelChunkSection section = chunk.getSections()[chunkSection];
        final BlockPos worldPos = Utils.getWorldPos(chunk, section, relativePos);

        if (input.getBlock() instanceof LayeredCauldronBlock)
        {
            if (!((LayeredCauldronBlock) Blocks.WATER_CAULDRON).isFull(input))
            {
                BlockState blockstate = input.setValue(LEVEL, input.getValue(LEVEL) + 1);
                chunk.getLevel().setBlockAndUpdate(worldPos, blockstate);
                chunk.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, worldPos, GameEvent.Context.of(blockstate));
            }
        }
        else if (input.getBlock() == Blocks.CAULDRON)
        {
            BlockState blockstate = Blocks.WATER_CAULDRON.defaultBlockState();
            chunk.getLevel().setBlockAndUpdate(worldPos, blockstate);
            chunk.getLevel().gameEvent(GameEvent.BLOCK_CHANGE, worldPos, GameEvent.Context.of(blockstate));
            chunk.getLevel().levelEvent(1047, worldPos, 0);
        }
    }
}
