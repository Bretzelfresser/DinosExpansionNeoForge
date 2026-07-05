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

        // Set dirt at the base
        var basePositions = TrunkForm.SQUARE_WITH_CUTOUT_EDGES.calculateBase(pos, radius);
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
                int lengthAtY = radius + (int) Math.round(flareLength * (1.0 - progress));
                for (int i = 1; i <= lengthAtY; i++) {
                    BlockPos flarePos = pos.offset(dx * i, y, dz * i);
                    placeBlock(level, blockSetter, flarePos, config.woodProvider().getState(random, flarePos), true);
                    if (y == 0) {
                        setDirtAt(blockSetter, flarePos.below());
                    }
                }
            }
        }

        // 3. Tapered Trunk (Straight, vertical)
        for (int y = 0; y < totalHeight; y++) {
            double progress = (double) y / totalHeight;
            // Radius tapers towards the top
            int r = (int) Math.round(radius * Math.pow(1.0 - progress, 1.3));
            BlockPos centerPos = pos.above(y);
            var positions = TrunkForm.SQUARE_WITH_CUTOUT_EDGES.calculateBase(centerPos, r);
            positions.forEach(p -> {
                placeBlock(level, blockSetter, p, config.woodProvider().getState(random, p), true);
            });
        }

        // 4. Whorled branches
        int minBranchY = (int) (totalHeight * config.branchStartHeight().sample(random));
        int maxBranchY = totalHeight - 5;
        if (maxBranchY < minBranchY) {
            maxBranchY = minBranchY;
        }

        int branchInterval = Math.max(1, config.branchInterval().sample(random));
        int branchesPerInterval = config.branchesPerInterval().sample(random);
        int baseBranchLength = config.branchLength().sample(random);
        int foliageRadius = config.foliageRadius().sample(random);

        for (int y = minBranchY; y <= maxBranchY; y++) {
            if ((y - minBranchY) % branchInterval != 0) {
                continue;
            }

            double progressOfFoliage = (double) (y - minBranchY) / (totalHeight - minBranchY);
            // Length of branches tapers towards the top
            int length = (int) Math.round(baseBranchLength * (1.0 - progressOfFoliage * 0.8));
            if (length < 2) length = 2;

            int numBranches = branchesPerInterval;
            double spiralOffset = (y * 0.35) * Math.PI;

            for (int b = 0; b < numBranches; b++) {
                double angle = (b * 2.0 * Math.PI) / numBranches + spiralOffset + (random.nextDouble() - 0.5) * 0.2;
                
                // Redwood branches slope slightly downwards
                double slope = -0.1 - random.nextDouble() * 0.1;
                
                double dx = Math.cos(angle) * length;
                double dz = Math.sin(angle) * length;
                double dy = length * slope;

                BlockPos start = pos.above(y);
                BlockPos end = start.offset((int) Math.round(dx), (int) Math.round(dy), (int) Math.round(dz));

                // Draw branch wood
                drawBranch(level, blockSetter, random, start, end, config);

                // Place foliage along and at the tip of the branch
                placeBranchFoliage(level, blockSetter, random, start, end, foliageRadius, config);
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

    private void drawBranch(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos start, BlockPos end, RedwoodTreeConfiguration config) {
        int x1 = start.getX();
        int y1 = start.getY();
        int z1 = start.getZ();

        int x2 = end.getX();
        int y2 = end.getY();
        int z2 = end.getZ();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;

        int x = x1;
        int y = y1;
        int z = z1;

        // Draw branch line using 6-connected DDA
        while (x != x2 || y != y2 || z != z2) {
            if (x != x2) {
                x += sx;
            }
            if (z != z2) {
                z += sz;
            }
            if (y != y2) {
                y += sy;
            }
            BlockPos branchPos = new BlockPos(x, y, z);
            placeBlock(level, blockSetter, branchPos, config.woodProvider().getState(random, branchPos), false);
        }
    }

    private void placeBranchFoliage(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos start, BlockPos end, int tipRadius, RedwoodTreeConfiguration config) {
        // 1. Tip foliage: place a leafy blob at the end of the branch
        placeLeafBlob(level, blockSetter, random, end, tipRadius, config);

        // 2. Along-branch foliage: place flat sprays of leaves
        int x1 = start.getX();
        int y1 = start.getY();
        int z1 = start.getZ();

        int x2 = end.getX();
        int y2 = end.getY();
        int z2 = end.getZ();

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int dz = Math.abs(z2 - z1);

        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;

        int x = x1;
        int y = y1;
        int z = z1;

        int distance = 0;
        while (x != x2 || y != y2 || z != z2) {
            if (x != x2) x += sx;
            if (z != z2) z += sz;
            if (y != y2) y += sy;
            distance++;

            // Don't place leaves right next to the trunk
            if (distance > 2) {
                BlockPos p = new BlockPos(x, y, z);
                BlockPos[] spray = {
                    p,
                    p.north(), p.south(), p.east(), p.west(),
                    p.north().east(), p.north().west(), p.south().east(), p.south().west()
                };
                for (BlockPos leafPos : spray) {
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
