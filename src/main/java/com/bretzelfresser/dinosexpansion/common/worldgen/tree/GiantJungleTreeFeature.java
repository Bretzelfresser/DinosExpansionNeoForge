package com.bretzelfresser.dinosexpansion.common.worldgen.tree;

import com.bretzelfresser.dinosexpansion.util.TrunkForm;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class GiantJungleTreeFeature extends Feature<GiantJungleTreeConfiguration> {

    public GiantJungleTreeFeature(Codec<GiantJungleTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<GiantJungleTreeConfiguration> featurePlaceContext) {
        var config = featurePlaceContext.config();
        var pos = featurePlaceContext.origin();
        var random = featurePlaceContext.random();
        var level = featurePlaceContext.level();

        // Safe bounds for 3x3 chunks grid
        int minX = ((pos.getX() >> 4) - 1) * 16;
        int maxX = ((pos.getX() >> 4) + 2) * 16 - 1;
        int minZ = ((pos.getZ() >> 4) - 1) * 16;
        int maxZ = ((pos.getZ() >> 4) + 2) * 16 - 1;

        // 1. Ground Verification
        BlockPos basePos = pos.below();
        if (!isDirtOrGrass(level, basePos)) {
            return false;
        }

        int radius = Math.max(0, config.radius().sample(random));
        int totalHeight = Math.round((float) radius * config.radiusToHeightFactor().sample(random));

        // Size-scaled lean and wiggle configuration
        double leanX = (random.nextDouble() - 0.5) * 2 * (totalHeight * 0.1); // max lean is 10% of height
        double leanZ = (random.nextDouble() - 0.5) * 2 * (totalHeight * 0.1);
        double wiggleAmpX = random.nextDouble() * (radius * 0.5); // max wiggle amplitude is 50% of radius
        double wiggleAmpZ = random.nextDouble() * (radius * 0.5);
        double wiggleFreq = random.nextDouble() * 0.3 + 0.5; // 0.5 to 0.8 cycles (gradual sweep)

        BiConsumer<BlockPos, BlockState> blockSetter = (p, state) -> {
            if (p.getX() >= minX && p.getX() <= maxX && p.getZ() >= minZ && p.getZ() <= maxZ) {
                level.setBlock(p, state, 19);
            }
        };

        // Set dirt at the base
        var basePositions = TrunkForm.SQUARE_WITH_CUTOUT_EDGES.calculateBase(pos, radius);
        basePositions.forEach(p -> {
            setDirtAt(blockSetter, p.below());
            setDirtAt(blockSetter, p.below().below());
        });

        // 2. Root Flares (Buttress Roots) at the corners
        int[][] diagonals = { {1, 1}, {1, -1}, {-1, 1}, {-1, -1} };
        for (int[] diag : diagonals) {
            int dx = diag[0];
            int dz = diag[1];
            for (int y = 0; y <= 3; y++) {
                int flareLength = radius + 2 - y;
                // Start from center to guarantee seamless connection
                for (int i = 1; i <= flareLength; i++) {
                    BlockPos centerAtY = getTrunkCenter(pos, y, totalHeight, leanX, leanZ, wiggleAmpX, wiggleAmpZ, wiggleFreq, minX, maxX, minZ, maxZ);
                    BlockPos flarePos = centerAtY.offset(dx * i, 0, dz * i);
                    placeBlock(level, blockSetter, flarePos, config.woodProvider().getState(random, flarePos), true, minX, maxX, minZ, maxZ);
                    if (y == 0) {
                        setDirtAt(blockSetter, flarePos.below());
                    }
                }
            }
        }

        // 3. Tapered Trunk along 3D DDA Line segments
        for (int y = 0; y < totalHeight; y++) {
            BlockPos startCenter = getTrunkCenter(pos, y, totalHeight, leanX, leanZ, wiggleAmpX, wiggleAmpZ, wiggleFreq, minX, maxX, minZ, maxZ);
            BlockPos endCenter = getTrunkCenter(pos, y + 1, totalHeight, leanX, leanZ, wiggleAmpX, wiggleAmpZ, wiggleFreq, minX, maxX, minZ, maxZ);

            double progressStart = (double) y / totalHeight;
            double progressEnd = (double) (y + 1) / totalHeight;

            int rStart = (int) Math.round(radius * Math.pow(1.0 - progressStart, 1.3));
            int rEnd = (int) Math.round(radius * Math.pow(1.0 - progressEnd, 1.3));

            drawTrunkSegment(level, blockSetter, random, startCenter, endCenter, rStart, rEnd, config, minX, maxX, minZ, maxZ);
        }

        // 4. 3D Branching Math
        int minBranchY = (int) (totalHeight * config.branchStartHeight().sample(random));
        int maxBranchY = totalHeight - 4;
        if (maxBranchY < minBranchY) {
            maxBranchY = minBranchY;
        }

        int branchCount = config.branchCount().sample(random);
        List<BlockPos> branchTips = new ArrayList<>();
        List<Integer> foliageRadii = new ArrayList<>();

        for (int i = 0; i < branchCount; i++) {
            // Distribute angles evenly
            double baseAngle = (i * 2.0 * Math.PI) / branchCount;
            double angle = baseAngle + (random.nextDouble() - 0.5) * (0.25 * Math.PI);

            // Distribute height
            int branchY;
            if (maxBranchY > minBranchY) {
                branchY = minBranchY + (int) ((double) i / branchCount * (maxBranchY - minBranchY)) + random.nextInt(3) - 1;
                branchY = Mth.clamp(branchY, minBranchY, maxBranchY);
            } else {
                branchY = minBranchY;
            }

            int length = config.branchLength().sample(random);
            int fRadius = config.foliageRadius().sample(random);
            int buffer = fRadius + 2;

            BlockPos start = getTrunkCenter(pos, branchY, totalHeight, leanX, leanZ, wiggleAmpX, wiggleAmpZ, wiggleFreq, minX, maxX, minZ, maxZ);

            // Calculate boundary distance and clamp branch length
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double maxLength = length;

            if (cos > 0) {
                maxLength = Math.min(maxLength, (double) (maxX - buffer - start.getX()) / cos);
            } else if (cos < 0) {
                maxLength = Math.min(maxLength, (double) (minX + buffer - start.getX()) / cos);
            }

            if (sin > 0) {
                maxLength = Math.min(maxLength, (double) (maxZ - buffer - start.getZ()) / sin);
            } else if (sin < 0) {
                maxLength = Math.min(maxLength, (double) (minZ + buffer - start.getZ()) / sin);
            }

            int clampedLength = Math.max(2, (int) Math.floor(maxLength));

            double dx = cos * clampedLength;
            double dz = sin * clampedLength;
            // Upward slope: 30% to 60% of length
            double dy = clampedLength * (0.3 + random.nextDouble() * 0.3);

            BlockPos end = start.offset((int) Math.round(dx), (int) Math.round(dy), (int) Math.round(dz));

            branchTips.add(end);
            foliageRadii.add(fRadius);

            // Draw branch using a 6-connected DDA voxel traversal to prevent disconnections
            drawBranch(level, blockSetter, random, start, end, radius, config, minX, maxX, minZ, maxZ);
        }

        // 5. Crown and Branch Foliage
        // Branch tip foliage
        for (int i = 0; i < branchTips.size(); i++) {
            placeLeafBlob(level, blockSetter, random, branchTips.get(i), foliageRadii.get(i), config, minX, maxX, minZ, maxZ);
        }

        // Top crown foliage
        BlockPos crownCenter = getTrunkCenter(pos, totalHeight, totalHeight, leanX, leanZ, wiggleAmpX, wiggleAmpZ, wiggleFreq, minX, maxX, minZ, maxZ);
        int crownRadius = config.foliageRadius().sample(random) + 1;
        placeLeafBlob(level, blockSetter, random, crownCenter, crownRadius, config, minX, maxX, minZ, maxZ);
        // Overlapping blobs for organic crown look
        placeLeafBlob(level, blockSetter, random, crownCenter.offset(random.nextInt(3) - 1, -1, random.nextInt(3) - 1), crownRadius - 1, config, minX, maxX, minZ, maxZ);
        placeLeafBlob(level, blockSetter, random, crownCenter.offset(random.nextInt(3) - 1, 1, random.nextInt(3) - 1), crownRadius - 1, config, minX, maxX, minZ, maxZ);

        return true;
    }

    private void placeLeafBlob(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos center, int radius, GiantJungleTreeConfiguration config, int minX, int maxX, int minZ, int maxZ) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distSq = (x * x) + (y * y * 1.3) + (z * z);
                    if (distSq <= radius * radius) {
                        BlockPos leafPos = center.offset(x, y, z);
                        if (isReplaceable(level, leafPos, minX, maxX, minZ, maxZ)) {
                            BlockState leafState = config.foliageProvider().getState(random, leafPos);
                            blockSetter.accept(leafPos, leafState);

                            float vinesChance = config.vinesChance().sample(random);
                            if (random.nextFloat() < vinesChance) {
                                placeHangingVines(level, blockSetter, random, leafPos, minX, maxX, minZ, maxZ);
                            }
                        }
                    }
                }
            }
        }
    }

    private void placeHangingVines(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos leafPos, int minX, int maxX, int minZ, int maxZ) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = leafPos.relative(dir);
            if (adjacent.getX() >= minX && adjacent.getX() <= maxX && adjacent.getZ() >= minZ && adjacent.getZ() <= maxZ) {
                if (level.isStateAtPosition(adjacent, BlockState::isAir)) {
                    BlockState vineState = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(dir.getOpposite()), true);
                    blockSetter.accept(adjacent, vineState);

                    BlockPos downPos = adjacent.below();
                    int vineLength = random.nextInt(4) + 1;
                    for (int i = 0; i < vineLength; i++) {
                        if (downPos.getX() >= minX && downPos.getX() <= maxX && downPos.getZ() >= minZ && downPos.getZ() <= maxZ) {
                            if (level.isStateAtPosition(downPos, BlockState::isAir)) {
                                blockSetter.accept(downPos, vineState);
                                downPos = downPos.below();
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    private boolean isReplaceable(WorldGenLevel level, BlockPos pos, int minX, int maxX, int minZ, int maxZ) {
        if (pos.getX() < minX || pos.getX() > maxX || pos.getZ() < minZ || pos.getZ() > maxZ) {
            return false;
        }
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

    private void placeBlock(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, BlockPos pos, BlockState state, boolean force, int minX, int maxX, int minZ, int maxZ) {
        if (force || isReplaceable(level, pos, minX, maxX, minZ, maxZ)) {
            blockSetter.accept(pos, state);
        }
    }

    protected static void setDirtAt(BiConsumer<BlockPos, BlockState> blockSetter, BlockPos pos) {
        blockSetter.accept(pos, Blocks.DIRT.defaultBlockState());
    }

    private BlockPos getTrunkCenter(BlockPos origin, int y, int totalHeight, double leanX, double leanZ, double wiggleAmpX, double wiggleAmpZ, double freq, int minX, int maxX, int minZ, int maxZ) {
        if (totalHeight <= 0) return origin.offset(0, y, 0);
        double progress = (double) y / totalHeight;
        double factor = progress * progress;
        double lx = factor * leanX;
        double lz = factor * leanZ;
        double wx = Math.sin(progress * Math.PI * freq) * wiggleAmpX * factor;
        double wz = Math.sin(progress * Math.PI * freq) * wiggleAmpZ * factor;
        int targetX = origin.getX() + (int) Math.round(lx + wx);
        int targetZ = origin.getZ() + (int) Math.round(lz + wz);
        // Clamp to safe region to avoid trunk clipping
        targetX = Mth.clamp(targetX, minX + 4, maxX - 4);
        targetZ = Mth.clamp(targetZ, minZ + 4, maxZ - 4);
        return new BlockPos(targetX, origin.getY() + y, targetZ);
    }

    private void drawTrunkSegment(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos start, BlockPos end, int radiusStart, int radiusEnd, GiantJungleTreeConfiguration config, int minX, int maxX, int minZ, int maxZ) {
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

        int totalSteps = dx + dy + dz;
        int step = 0;

        placeTrunkSlice(level, blockSetter, random, new BlockPos(x, y, z), step, totalSteps, radiusStart, radiusEnd, config, minX, maxX, minZ, maxZ);

        while (x != x2 || y != y2 || z != z2) {
            if (x != x2) {
                x += sx;
                step++;
                placeTrunkSlice(level, blockSetter, random, new BlockPos(x, y, z), step, totalSteps, radiusStart, radiusEnd, config, minX, maxX, minZ, maxZ);
            }
            if (y != y2) {
                y += sy;
                step++;
                placeTrunkSlice(level, blockSetter, random, new BlockPos(x, y, z), step, totalSteps, radiusStart, radiusEnd, config, minX, maxX, minZ, maxZ);
            }
            if (z != z2) {
                z += sz;
                step++;
                placeTrunkSlice(level, blockSetter, random, new BlockPos(x, y, z), step, totalSteps, radiusStart, radiusEnd, config, minX, maxX, minZ, maxZ);
            }
        }
    }

    private void placeTrunkSlice(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos center, int step, int totalSteps, int radiusStart, int radiusEnd, GiantJungleTreeConfiguration config, int minX, int maxX, int minZ, int maxZ) {
        double t = totalSteps > 0 ? (double) step / totalSteps : 0.0;
        int radius = (int) Math.round(Mth.lerp(t, (float) radiusStart, (float) radiusEnd));
        var positions = TrunkForm.SQUARE_WITH_CUTOUT_EDGES.calculateBase(center, radius);
        positions.forEach(p -> {
            placeBlock(level, blockSetter, p, config.woodProvider().getState(random, p), true, minX, maxX, minZ, maxZ);
        });
    }

    private void drawBranch(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos start, BlockPos end, int baseRadius, GiantJungleTreeConfiguration config, int minX, int maxX, int minZ, int maxZ) {
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

        int totalSteps = dx + dy + dz;
        int step = 0;

        placeBranchBlock(level, blockSetter, random, new BlockPos(x, y, z), step, totalSteps, baseRadius, config, minX, maxX, minZ, maxZ);

        while (x != x2 || y != y2 || z != z2) {
            if (x != x2) {
                x += sx;
                step++;
                placeBranchBlock(level, blockSetter, random, new BlockPos(x, y, z), step, totalSteps, baseRadius, config, minX, maxX, minZ, maxZ);
            }
            if (z != z2) {
                z += sz;
                step++;
                placeBranchBlock(level, blockSetter, random, new BlockPos(x, y, z), step, totalSteps, baseRadius, config, minX, maxX, minZ, maxZ);
            }
            if ((step > totalSteps - y2 + y - 1 || random.nextFloat() < 0.7f) && y != y2) {
                y += sy;
                step++;
                placeBranchBlock(level, blockSetter, random, new BlockPos(x, y, z), step, totalSteps, baseRadius, config, minX, maxX, minZ, maxZ);
            }
        }
    }

    private void placeBranchBlock(WorldGenLevel level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos pos, int step, int totalSteps, int baseRadius, GiantJungleTreeConfiguration config, int minX, int maxX, int minZ, int maxZ) {
        double t = totalSteps > 0 ? (double) step / totalSteps : 0.0;
        int thickness = Math.min(baseRadius, (t < 0.33) ? 1 : 0);
        BlockState logState = config.woodProvider().getState(random, pos);

        if (thickness > 0) {
            placeBlock(level, blockSetter, pos, logState, false, minX, maxX, minZ, maxZ);
            placeBlock(level, blockSetter, pos.north(), logState, false, minX, maxX, minZ, maxZ);
            placeBlock(level, blockSetter, pos.south(), logState, false, minX, maxX, minZ, maxZ);
            placeBlock(level, blockSetter, pos.east(), logState, false, minX, maxX, minZ, maxZ);
            placeBlock(level, blockSetter, pos.west(), logState, false, minX, maxX, minZ, maxZ);
        } else {
            placeBlock(level, blockSetter, pos, logState, false, minX, maxX, minZ, maxZ);
        }
    }
}
