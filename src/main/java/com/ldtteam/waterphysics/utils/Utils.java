package com.ldtteam.waterphysics.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import static net.minecraft.world.level.block.Block.*;

public class Utils
{
    /**
     * Get the worldPos from a relative chunkpos.
     * @param chunk the relative chunk.
     * @param section its section.
     * @param relativePos the relative pos.
     * @return the in world pos.
     */
    public static BlockPos getWorldPos(final LevelChunk chunk, final LevelChunkSection section, final BlockPos relativePos)
    {
        return new BlockPos(chunk.getPos().getMinBlockX() + relativePos.getX(), section.bottomBlockY() + relativePos.getY(), chunk.getPos().getMinBlockZ() + relativePos.getZ());
    }

    /**
     * Checks if the block is loaded for block access
     *
     * @param world world to use
     * @param pos   position to check
     * @return true if block is accessible/loaded
     */
    public static boolean isBlockLoaded(final LevelAccessor world, final BlockPos pos)
    {
        return isChunkLoaded(world, pos.getX() >> 4, pos.getZ() >> 4);
    }

    /**
     * Returns whether a chunk is fully loaded
     *
     * @param world world to check on
     * @param x     chunk position
     * @param z     chunk position
     * @return true if loaded
     */
    public static boolean isChunkLoaded(final LevelAccessor world, final int x, final int z)
    {
        if (world.getChunkSource() instanceof ServerChunkCache)
        {
            final ChunkHolder holder = ((ServerChunkCache) world.getChunkSource()).chunkMap.visibleChunkMap.get(ChunkPos.asLong(x, z));
            if (holder != null)
            {
                return holder.getFullChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left().isPresent();
            }

            return false;
        }
        return world.getChunk(x, z, ChunkStatus.FULL, false) != null;
    }

    /**
     * Get the blockstate in a chunk in a section. Fall back to asking world if outside of the chunk we're ticking.
     * @param chunk the chunk we're ticking.
     * @param pos the relative pos in the chunk section to get the blockstate from.
     * @param sectionId the id of the section.
     * @return the blockstate or, if not in a loaded chunk or outside of world height a barrier block.
     */
    public static BlockState getBlockState(final LevelChunk chunk, final BlockPos pos, final int sectionId)
    {
        if (pos.getX() >= 16 || pos.getZ() >= 16 || pos.getX() < 0 || pos.getZ() < 0)
        {
            final BlockPos worldPos = Utils.getWorldPos(chunk, chunk.getSections()[sectionId], pos);
            if (Utils.isBlockLoaded(chunk.getLevel(), worldPos))
            {
                return chunk.getLevel().getBlockState(worldPos);
            }
            else
            {
                return Blocks.BARRIER.defaultBlockState();
            }
        }

        if (pos.getY() < 0)
        {
            if (sectionId > 0)
            {
                final int sectionDif = (int) Math.floor(pos.getY() / 16.0);
                if (sectionId+sectionDif >= 0)
                {
                    return chunk.getSections()[sectionId + sectionDif].getBlockState(pos.getX(), (sectionDif * -1 * 16) + pos.getY(), pos.getZ());
                }
            }

            return Blocks.BARRIER.defaultBlockState();
        }
        else if (pos.getY() >= 16)
        {
            if (sectionId + 1 < chunk.getSections().length)
            {
                final int sectionDif = (int) (pos.getY() / 16.0);
                if (sectionDif + sectionId < chunk.getSections().length)
                {
                    return chunk.getSections()[sectionId + sectionDif].getBlockState(pos.getX(), pos.getY() - (sectionDif * 16), pos.getZ());
                }
            }
            return Blocks.BARRIER.defaultBlockState();
        }
        else
        {
            return chunk.getSections()[sectionId].getBlockState(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    /**
     * Try moving the water to below.
     * @param level the level it is in.
     * @param pos the pos the water is at.
     * @return false if couldn't move
     */
    public static boolean tryMoveBelow(final Level level, final BlockPos pos, final boolean allowAir)
    {
        final BlockState state = level.getBlockState(pos.below());
        if (canMoveSourceIntoState(state, allowAir))
        {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
            level.setBlock(pos.below(), Blocks.WATER.defaultBlockState(), UPDATE_ALL);
            return true;
        }
        return false;
    }

    /**
     * Try moving the water sidewards.
     * @param level the level it is in.
     * @param pos the pos the water is at.
     * @return false if couldn't move
     */
    public static boolean tryMoveSidewards(final Level level, final BlockPos pos, final boolean allowAir)
    {
        final BlockState initialAboveState = level.getBlockState(pos.above());
        if (initialAboveState.getBlock() != Blocks.WATER || !initialAboveState.getFluidState().isSource())
        {
            return false;
        }

        for (final Direction direction : Direction.Plane.HORIZONTAL)
        {
            final BlockState state = level.getBlockState(pos.relative(direction));
            final BlockState aboveState = level.getBlockState(pos.relative(direction).above());

            if (canMoveSourceIntoState(state, allowAir) && canMoveSourceIntoState(aboveState, allowAir))
            {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
                level.setBlock(pos.relative(direction), Blocks.WATER.defaultBlockState(), UPDATE_ALL);
                return true;
            }
        }

        // if air next to it and water above, move next to it
        // cannot move to a position where water is above
        return false;
    }

    /**
     * Try moving the water diagonally.
     * @param level the level it is in.
     * @param pos the pos the water is at.
     */
    public static void tryMoveDiagonally(final Level level, final BlockPos pos, final boolean allowAir)
    {
        final BlockState initialAboveState = level.getBlockState(pos.above());
        if (initialAboveState.getMaterial().isSolid())
        {
            return;
        }

        for (int i = 2; i <= 7; i++)
        {
            for (final Direction direction : Direction.Plane.HORIZONTAL)
            {
                boolean canMove = true;
                for (int j = 1; j < i; j++)
                {
                    final BlockState dirState = level.getBlockState(pos.relative(direction, j));
                    if (!canMoveSourceIntoState(dirState, allowAir))
                    {
                       canMove = false;
                    }
                }

                if (canMove)
                {
                    final BlockState state = level.getBlockState(pos.relative(direction, i).below());
                    final BlockState aboveState = level.getBlockState(pos.relative(direction, i));

                    if (canMoveSourceIntoState(state, allowAir) && canMoveSourceIntoState(aboveState, true))
                    {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
                        level.setBlock(pos.relative(direction, i).below(), Blocks.WATER.defaultBlockState(), UPDATE_ALL);
                        return;
                    }
                }
            }
        }

        BlockPos progressPos = getBlockInRandomDirection(pos, level.getRandom());
        for (int i = 0; i < level.getRandom().nextInt(10); i++)
        {
            final BlockState state = level.getBlockState(progressPos.below());
            final BlockState aboveState = level.getBlockState(progressPos);
            if (canMoveSourceIntoState(aboveState, false))
            {
                if (canMoveSourceIntoState(state, allowAir))
                {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), UPDATE_ALL);
                    level.setBlock(progressPos.below(), Blocks.WATER.defaultBlockState(), UPDATE_ALL);
                    return;
                }
                progressPos = getBlockInRandomDirection(progressPos, level.getRandom());
            }
        }
    }

    /**
     * Get a block in a random direction.
     * @param pos the position we're at.
     * @param random a random source.
     * @return the random pos.
     */
    public static BlockPos getBlockInRandomDirection(final BlockPos pos, final RandomSource random)
    {
        return pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
    }

    /**
     * Check if we can move a water source into this position.
     * @param state the state to check.
     * @param allowAir if air is allowed.
     * @return true if so.
     */
    public static boolean canMoveSourceIntoState(final BlockState state, final boolean allowAir)
    {
        return (allowAir && state.isAir()) || (state.getBlock() == Blocks.WATER && !state.getFluidState().isSource());
    }
}
