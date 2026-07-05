package com.bretzelfresser.dinosexpansion.common.worldgen.tree;

import com.bretzelfresser.dinosexpansion.util.TrunkForm;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.function.BiConsumer;

public class RedwoodTreeFeature extends Feature<RedwoodTreeConfiguration> {

    public RedwoodTreeFeature(Codec<RedwoodTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RedwoodTreeConfiguration> featurePlaceContext) {
        var config = featurePlaceContext.config();
        var pos = featurePlaceContext.origin();
        var random = featurePlaceContext.random();
        var level = featurePlaceContext.level();

        // 1. Ground Verification
        BlockPos basePos = pos.below();
        if (!isDirtOrGrass(level, basePos)) {
            return false;
        }

        int radius = Math.max(0, config.radius().sample(random));
        int totalHeight = Math.round((float) radius * config.radiusToHeightFactor().sample(random));

        BiConsumer<BlockPos, BlockState> blockSetter = (p, state) -> {
            level.setBlock(p, state, 19);
        };

        // Set dirt at the base (5-log cross base)
        var basePositions = TrunkForm.SQUARE_WITH_CUTOUT_EDGES.calculateBase(pos, 1);
        basePositions.forEach(p -> {
            setDirtAt(blockSetter, p.below());
            setDirtAt(blockSetter, p.below().below());
        });

        // 2. Root Flares (Buttress Roots) in 8 directions
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
        int flareHeight = config.rootFlareHeight().sample(random);
        int flareLength = config.rootFlareLength().sample(random);
        for (int[] dir : directions) {
            int dx = dir[0];
            int dz = dir[1];
            for (int y = 0; y <= flareHeight; y++) {
                double progress = (double) y / (flareHeight + 1);
                // Trunk covers radius=1 cross. Flare goes beyond it.
                int lengthAtY = 1 + (int) Math.round(flareLength * (1.0 - progress));
                for (int i = 1; i <= lengthAtY; i++) {
                    BlockPos flarePos = pos.offset(dx * i, y, dz * i);
                    placeBlock(level, blockSetter, flarePos, config.woodProvider().getState(random, flarePos), true);
                    if (y == 0) {
                        setDirtAt(blockSetter, flarePos.below());
                    }
                }
            }
        }


        int minBranchY = (int) (totalHeight * config.branchStartHeight().sample(random));
        // 3. Trunk Placement (Constant cross shape, tapering at the very top)
        for (int y = 0; y < totalHeight; y++) {
            BlockPos sliceCenter = pos.above(y);
            int r = 2;
            if (y >= totalHeight - 5){
                r = 0;
            }else if (y >= minBranchY + 3){
                r = 1;
            }
            var positions = TrunkForm.SQUARE_WITH_CUTOUT_EDGES.calculateBase(sliceCenter, r);
            positions.forEach(p -> {
                placeBlock(level, blockSetter, p, config.woodProvider().getState(random, p), true);
            });
        }

        // 4. Whorled conifer branches

        int maxBranchY = totalHeight - 5;
        if (maxBranchY < minBranchY) {
            maxBranchY = minBranchY;
        }

        int branchInterval = Math.max(1, config.branchInterval().sample(random));
        int baseBranchLength = config.branchLength().sample(random);
        int foliageRadius = config.foliageRadius().sample(random);

        for (int y = minBranchY; y <= maxBranchY; y++) {
            if ((y - minBranchY) % branchInterval != 0) {
                continue;
            }

            double progressOfFoliage = (double) (y - minBranchY) / (totalHeight - minBranchY);
            // Length of branches tapers towards the top
            int length = (int) Math.round(baseBranchLength * (1.0 - progressOfFoliage * 0.8));
            if (length < 1) length = 1;

            int tipRadius = Math.max(1, (int) Math.round(foliageRadius * (1.0 - progressOfFoliage)));

            for (int[] dir : directions) {
                int dx = dir[0];
                int dz = dir[1];

                BlockPos start = pos.above(y);
                BlockPos end = start.offset(dx * length, 0, dz * length);

                // Draw branch wood
                for (int i = 1; i <= length; i++) {
                    BlockPos p = start.offset(dx * i, 0, dz * i);
                    placeBlock(level, blockSetter, p, config.woodProvider().getState(random, p), false);
                }

                // Place foliage along and at the tip of the branch
                placeBranchFoliage(level, blockSetter, random, start, end, tipRadius, config);
            }
        }

        // 5. Top crown foliage (pointy conifer top)
        BlockPos crownCenter = pos.above(totalHeight);
        for (int yOffset = -5; yOffset <= 3; yOffset++) {
            double progress = (double) (yOffset - (-5)) / 8.0;
            int coneRadius = (int) Math.round(3.0 * (1.0 - progress));
            if (coneRadius < 0) coneRadius = 0;
            
            BlockPos sliceCenter = crownCenter.above(yOffset);
            for (int x = -coneRadius; x <= coneRadius; x++) {
                for (int z = -coneRadius; z <= coneRadius; z++) {
                    if (x * x + z * z <= coneRadius * coneRadius) {
                        BlockPos leafPos = sliceCenter.offset(x, 0, z);
                        if (isReplaceable(level, leafPos)) {
                            blockSetter.accept(leafPos, config.foliageProvider().getState(random, leafPos));
                        }
                    }
                }
            }
        }

        return true;
    }

    private void placeBranchFoliage(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos start, BlockPos end, int tipRadius, RedwoodTreeConfiguration config) {
        // 1. Tip foliage: place a leafy blob at the end of the branch
        placeLeafBlob(level, blockSetter, random, end, tipRadius, config);

        // 2. Along-branch foliage: place flat sprays of leaves
        int x1 = start.getX();
        int z1 = start.getZ();

        int x2 = end.getX();
        int z2 = end.getZ();

        int sx = Integer.compare(x2, x1);
        int sz = Integer.compare(z2, z1);

        int x = x1;
        int z = z1;

        int distance = 0;
        while (x != x2 || z != z2) {
            x += sx;
            z += sz;
            distance++;

            // Don't place leaves right next to the trunk
            if (distance > 1) {
                BlockPos p = new BlockPos(x, start.getY(), z);
                
                // Flat spray at branch level (y)
                BlockPos[] sprayY = {
                    p,
                    p.north(), p.south(), p.east(), p.west(),
                    p.north().east(), p.north().west(), p.south().east(), p.south().west()
                };
                for (BlockPos leafPos : sprayY) {
                    if (isReplaceable(level, leafPos)) {
                        blockSetter.accept(leafPos, config.foliageProvider().getState(random, leafPos));
                    }
                }
                
                // Flat spray at level above (y + 1)
                BlockPos[] sprayAbove = {
                    p.above(),
                    p.above().north(), p.above().south(), p.above().east(), p.above().west()
                };
                for (BlockPos leafPos : sprayAbove) {
                    if (isReplaceable(level, leafPos)) {
                        blockSetter.accept(leafPos, config.foliageProvider().getState(random, leafPos));
                    }
                }
            }
        }
    }

    private void placeLeafBlob(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos center, int radius, RedwoodTreeConfiguration config) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distSq = (x * x) + (y * y * 1.8) + (z * z);
                    if (distSq <= radius * radius) {
                        BlockPos leafPos = center.offset(x, y, z);
                        if (isReplaceable(level, leafPos)) {
                            BlockState leafState = config.foliageProvider().getState(random, leafPos);
                            blockSetter.accept(leafPos, leafState);
                        }
                    }
                }
            }
        }
    }

    private boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.isAir() || !state.getFluidState().isEmpty() || state.is(BlockTags.REPLACEABLE_BY_TREES) || state.is(BlockTags.LEAVES);
    }

    private boolean isDirtOrGrass(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK);
    }

    private void placeBlock(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, BlockPos pos, BlockState state, boolean force) {
        if (force || isReplaceable(level, pos)) {
            blockSetter.accept(pos, state);
        }
    }

    protected static void setDirtAt(BiConsumer<BlockPos, BlockState> blockSetter, BlockPos pos) {
        blockSetter.accept(pos, Blocks.DIRT.defaultBlockState());
    }
}
