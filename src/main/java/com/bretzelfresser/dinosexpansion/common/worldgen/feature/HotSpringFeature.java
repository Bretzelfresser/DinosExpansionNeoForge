package com.bretzelfresser.dinosexpansion.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class HotSpringFeature extends Feature<HotSpringFeature.Configuration> {
    public HotSpringFeature(Codec<Configuration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        Configuration config = context.config();

        int size = Math.min(config.size().sample(context.random()), 12); // Clamped to ensure it stays in loaded chunks
        int depth = Math.min(config.depth().sample(context.random()), 8);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // Check if there is actual ground to carve
        BlockState originState = level.getBlockState(origin.below());
        if (originState.isAir() || !originState.getFluidState().isEmpty()) {
            return false;
        }

        // 1. Loop through a bounding box to generate the lake basin
        for (int xOffset = -size - 2; xOffset <= size + 2; xOffset++) {
            for (int zOffset = -size - 2; zOffset <= size + 2; zOffset++) {
                double r = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
                double theta = Math.atan2(zOffset, xOffset);
                // Distort the radius to make it look organic
                double limit = size * (1.0 + 0.15 * Math.sin(4 * theta) + 0.1 * Math.cos(7 * theta));

                // yOffset from 2 (to clear air above) down to -depth - 1
                for (int yOffset = 2; yOffset >= -depth - 1; yOffset--) {
                    int x = origin.getX() + xOffset;
                    int y = origin.getY() + yOffset;
                    int z = origin.getZ() + zOffset;
                    mutablePos.set(x, y, z);

                    if (level.isOutsideBuildHeight(y)) {
                        continue;
                    }

                    double depthRatio = yOffset >= 0 ? 0.0 : (double) (-yOffset) / depth;
                    double layerLimit = limit * (1.0 - 0.7 * depthRatio);

                    BlockState state = level.getBlockState(mutablePos);

                    if (r < layerLimit) {
                        if (yOffset >= 0) {
                            // Carve air above the lake
                            level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2);
                        } else if (yOffset >= -depth) { // Y = -1 down to -depth
                            // Place fluid (water)
                            BlockState fluidState = config.fluidProvider().getState(context.random(), mutablePos);
                            level.setBlock(mutablePos, fluidState, 2);
                        } else { // Y < -depth
                            // Place barrier at the bottom of the lake (seals the floor)
                            BlockState barrierState = config.barrierProvider().getState(context.random(), mutablePos);
                            level.setBlock(mutablePos, barrierState, 2);
                        }
                    } else if (r < layerLimit + 1.5) {
                        // Place barrier rim/borders
                        if (yOffset <= 0 && yOffset >= -depth) {
                            if (!state.isAir() && state.getFluidState().isEmpty()) {
                                BlockState barrierState = config.barrierProvider().getState(context.random(), mutablePos);
                                level.setBlock(mutablePos, barrierState, 2);
                            }
                        }
                    }
                }
            }
        }

        // 2. Smooth terrain blending (erosion) around the lake
        int blendRadius = size + 4;
        for (int xOffset = -blendRadius; xOffset <= blendRadius; xOffset++) {
            for (int zOffset = -blendRadius; zOffset <= blendRadius; zOffset++) {
                double r = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
                double theta = Math.atan2(zOffset, xOffset);
                double limit = size * (1.0 + 0.15 * Math.sin(4 * theta) + 0.1 * Math.cos(7 * theta));
                double edge = limit + 1.5;

                if (r >= edge && r < edge + 4.0) {
                    double t = (r - edge) / 4.0; // 0.0 at lake edge to 1.0 at outer blend boundary
                    int targetMaxHeight = origin.getY() + (int) Math.round(3.0 * t);

                    // Carve blocks above targetMaxHeight to AIR
                    for (int y = origin.getY() + 10; y > targetMaxHeight; y--) {
                        mutablePos.set(origin.getX() + xOffset, y, origin.getZ() + zOffset);
                        if (!level.getBlockState(mutablePos).isAir()) {
                            level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }

                    // Place a smooth cover block on the newly exposed surface
                    mutablePos.set(origin.getX() + xOffset, targetMaxHeight, origin.getZ() + zOffset);
                    BlockState surfaceState = level.getBlockState(mutablePos);
                    if (surfaceState.isAir() || !surfaceState.getFluidState().isEmpty()) {
                        BlockState cover = t < 0.3 ? config.barrierProvider().getState(context.random(), mutablePos) : Blocks.GRASS_BLOCK.defaultBlockState();
                        level.setBlock(mutablePos, cover, 2);
                        mutablePos.set(origin.getX() + xOffset, targetMaxHeight - 1, origin.getZ() + zOffset);
                        level.setBlock(mutablePos, Blocks.DIRT.defaultBlockState(), 2);
                    }
                }
            }
        }

        return true;
    }

    public static record Configuration(BlockStateProvider fluidProvider, BlockStateProvider barrierProvider, IntProvider size, IntProvider depth) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockStateProvider.CODEC.fieldOf("fluid_provider").forGetter(Configuration::fluidProvider),
                BlockStateProvider.CODEC.fieldOf("barrier_provider").forGetter(Configuration::barrierProvider),
                IntProvider.CODEC.fieldOf("size").forGetter(Configuration::size),
                IntProvider.CODEC.fieldOf("depth").forGetter(Configuration::depth)
        ).apply(instance, Configuration::new));
    }
}
