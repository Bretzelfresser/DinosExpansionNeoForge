package com.bretzelfresser.dinosexpansion.common.worldgen.feature;

import com.bretzelfresser.dinosexpansion.util.FeaturePlacementUtils;
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

        // Find the lowest surface point in the lake area to prevent water from generating in air
        int minY = origin.getY();
        BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();

        for (int xOffset = -size; xOffset <= size; xOffset++) {
            for (int zOffset = -size; zOffset <= size; zOffset++) {
                if (xOffset * xOffset + zOffset * zOffset <= size * size) {
                    int x = origin.getX() + xOffset;
                    int z = origin.getZ() + zOffset;

                    // Find surface Y at this column
                    int ySurf = origin.getY();
                    for (int y = origin.getY() + 6; y >= origin.getY() - 10; y--) {
                        scanPos.set(x, y, z);
                        BlockState state = level.getBlockState(scanPos);
                        if (!state.isAir() && state.getFluidState().isEmpty()) {
                            ySurf = y;
                            break;
                        }
                    }
                    minY = Math.min(minY, ySurf);
                }
            }
        }

        // Clamp minY to avoid pulling the lake into deep caves/ravines
        minY = Math.max(minY, origin.getY() - 5);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // 1. Loop through a bounding box to generate the lake basin
        for (int xOffset = -size - 2; xOffset <= size + 2; xOffset++) {
            for (int zOffset = -size - 2; zOffset <= size + 2; zOffset++) {
                double r = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
                double theta = Math.atan2(zOffset, xOffset);
                // Distort the radius to make it look organic
                double limit = size * (1.0 + 0.15 * Math.sin(4 * theta) + 0.1 * Math.cos(7 * theta));

                // yOffset from 10 (to clear air above) down to -waterPondDepth - 1
                for (int yOffset = 10; yOffset >= -depth - 1; yOffset--) {
                    int x = origin.getX() + xOffset;
                    int y = minY + yOffset; // Relative to the lowest surface point minY
                    int z = origin.getZ() + zOffset;
                    mutablePos.set(x, y, z);

                    if (level.isOutsideBuildHeight(y)) {
                        continue;
                    }

                    double depthRatio = yOffset >= 0 ? 0.0 : (double) (-yOffset) / depth;
                    double layerLimit = limit * (1.0 - 0.7 * depthRatio);

                    BlockState state = level.getBlockState(mutablePos);

                    if (r < layerLimit) {
                        if (yOffset > 0) {
                            // Carve air above the lake (clears any slopes/hills above minY)
                            level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2);
                        } else if (yOffset >= -depth) { // Y = minY - 1 down to minY - waterPondDepth
                            // Place fluid (water)
                            try {
                                FeaturePlacementUtils.placeLiquid(config.fluidProvider, level, context.random(), mutablePos);
                            } catch (IllegalArgumentException e) {
                                throw new RuntimeException(e);
                            }
                        } else { // Y < minY - waterPondDepth
                            // Place barrier at the bottom of the lake (seals the floor)
                            BlockState barrierState = config.barrierProvider().getState(context.random(), mutablePos);
                            level.setBlock(mutablePos, barrierState, 2);
                        }
                    } else if (r < layerLimit + 1.5) {
                        // Place barrier rim/borders
                        if (yOffset <= 0 && yOffset >= -depth) {
                            if (state.getFluidState().isEmpty()) {
                                BlockState barrierState = config.barrierProvider().getState(context.random(), mutablePos);
                                level.setBlock(mutablePos, barrierState, 2);
                            }
                            //floor, leave it when there is a solid block, otherwise set our barrier block there too
                        }
                    }
                }
            }
        }

        return true;
    }

    public static record Configuration(BlockStateProvider fluidProvider, BlockStateProvider barrierProvider,
                                       IntProvider size, IntProvider depth) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockStateProvider.CODEC.fieldOf("fluid_provider").forGetter(Configuration::fluidProvider),
                BlockStateProvider.CODEC.fieldOf("barrier_provider").forGetter(Configuration::barrierProvider),
                IntProvider.CODEC.fieldOf("size").forGetter(Configuration::size),
                IntProvider.CODEC.fieldOf("waterPondDepth").forGetter(Configuration::depth)
        ).apply(instance, Configuration::new));
    }
}
