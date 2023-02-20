package com.ldtteam.waterphysics.mixin;

import com.ldtteam.waterphysics.WaterPhysics;
import com.ldtteam.waterphysics.handlers.ITransformationHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ChunkTickMixin
{
    /**
     * Moving random value.
     */
    private int randValue = RandomSource.create().nextInt();

    @Inject(method = "tickChunk", at = @At("HEAD"))
    public void WaterPhysicsTickChunk(final LevelChunk chunk, final int k, final CallbackInfo ci)
    {
        if (k <= 0 || chunk.getLevel().getGameTime() != 0 && chunk.getLevel().getRandom().nextInt(10) != 1)
        {
            return;
        }

        LevelChunkSection[] chunkSections = chunk.getSections();

        for (int sectionId = 0; sectionId < chunkSections.length; sectionId++)
        {
            LevelChunkSection levelchunksection = chunkSections[sectionId];

            for (int times = 0; times < k; times++)
            {
                if (chunk.getLevel().getRandom().nextInt(10) == 1)
                {
                    final BlockPos randomPos = this.getBlockRandomPos();
                    for (ITransformationHandler handler : ITransformationHandler.HANDLERS)
                    {
                        BlockState randomState = levelchunksection.getBlockState(randomPos.getX(), randomPos.getY(), randomPos.getZ());

                        if (handler.transforms(randomState) && handler.ready(chunk.getLevel().getGameTime() + times, chunk))
                        {
                            try
                            {
                                handler.transformBlock(randomPos, chunk, sectionId, randomState);
                            }
                            catch (final Exception ex)
                            {
                                WaterPhysics.LOGGER.warn("Oopsy", ex);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculate a pseudo-random block position based on vanilla.
     *
     * @return the random position.
     */
    private BlockPos getBlockRandomPos()
    {
        this.randValue = this.randValue * 3 + 1013904223;
        int i = this.randValue >> 2;
        return new BlockPos((i & 15), (i >> 16 & 15), (i >> 8 & 15));
    }
}
