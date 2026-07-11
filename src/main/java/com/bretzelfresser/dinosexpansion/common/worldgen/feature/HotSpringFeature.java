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

        // Loop through a bounding box
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
                        if (yOffset < 0 && yOffset > -depth) {
                            // Place fluid (water)
                            BlockState fluidState = config.fluidProvider().getState(context.random(), mutablePos);
                            level.setBlock(mutablePos, fluidState, 2);
                        } else if (yOffset <= 0) {
                            // Place barrier at the bottom of the lake
                            BlockState barrierState = config.barrierProvider().getState(context.random(), mutablePos);
                            level.setBlock(mutablePos, barrierState, 2);
                        } else {
                            // Carve air above the lake
                            level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2);
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
