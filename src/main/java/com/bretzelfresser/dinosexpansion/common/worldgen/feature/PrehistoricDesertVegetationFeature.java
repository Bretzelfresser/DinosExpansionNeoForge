package com.bretzelfresser.dinosexpansion.common.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class PrehistoricDesertVegetationFeature extends Feature<NoneFeatureConfiguration> {
    public PrehistoricDesertVegetationFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockState groundState = level.getBlockState(origin.below());
        if (!groundState.is(Blocks.SAND)) {
            return false;
        }

        float typeChoice = random.nextFloat();
        if (typeChoice < 0.35f) {
            // 1. Giant Branching Saguaro Cactus
            generateSaguaroCactus(level, origin, random);
        } else if (typeChoice < 0.65f) {
            // 2. Petrified Wood Log (Lying flat)
            generatePetrifiedLog(level, origin, random);
        } else if (typeChoice < 0.85f) {
            // 3. Dead Tree Stump (Standing trunk with stubby branches)
            generateDeadStump(level, origin, random);
        } else {
            // 4. Ground Patch (Red Sand / Orange Terracotta)
            generateGroundPatch(level, origin, random);
        }

        return true;
    }

    private void generateSaguaroCactus(WorldGenLevel level, BlockPos base, RandomSource random) {
        int height = random.nextInt(3) + 4; // 4 to 6 blocks tall
        BlockState cactus = Blocks.CACTUS.defaultBlockState();

        // Main trunk
        for (int y = 0; y < height; y++) {
            BlockPos p = base.above(y);
            if (level.isOutsideBuildHeight(p.getY())) break;
            level.setBlock(p, cactus, 2);
        }

        // Side arms (one or two)
        int armCount = random.nextInt(2) + 1;
        for (int i = 0; i < armCount; i++) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int armY = random.nextInt(2) + 2; // Arm starts at height 2 or 3
            BlockPos armStart = base.above(armY).relative(dir);
            BlockPos armUp = armStart.above();

            if (!level.isOutsideBuildHeight(armUp.getY())) {
                level.setBlock(armStart, cactus, 2);
                level.setBlock(armUp, cactus, 2);
                if (random.nextBoolean()) {
                    level.setBlock(armUp.above(), cactus, 2);
                }
            }
        }
    }

    private void generatePetrifiedLog(WorldGenLevel level, BlockPos base, RandomSource random) {
        int length = random.nextInt(3) + 3; // 3 to 5 blocks long
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockState log = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, dir.getAxis());

        for (int i = 0; i < length; i++) {
            BlockPos p = base.relative(dir, i);
            BlockPos below = p.below();
            if (level.getBlockState(below).is(Blocks.SAND)) {
                BlockPos target = random.nextBoolean() ? p : below;
                if (!level.isOutsideBuildHeight(target.getY())) {
                    level.setBlock(target, log, 2);
                }
            }
        }
    }

    private void generateDeadStump(WorldGenLevel level, BlockPos base, RandomSource random) {
        int height = random.nextInt(3) + 3; // 3 to 5 blocks tall
        BlockState wood = Blocks.STRIPPED_OAK_LOG.defaultBlockState();

        // Main stump
        for (int y = 0; y < height; y++) {
            BlockPos p = base.above(y);
            if (level.isOutsideBuildHeight(p.getY())) break;
            level.setBlock(p, wood, 2);
        }

        // Side branches using oak fences
        for (int y = 1; y < height; y++) {
            if (random.nextFloat() < 0.4f) {
                Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos fencePos = base.above(y).relative(dir);
                if (level.getBlockState(fencePos).isAir()) {
                    level.setBlock(fencePos, Blocks.OAK_FENCE.defaultBlockState(), 2);
                }
            }
        }
    }

    private void generateGroundPatch(WorldGenLevel level, BlockPos center, RandomSource random) {
        int patchRadius = random.nextInt(3) + 2; // 2 to 4 block radius
        BlockState patchState = random.nextBoolean() ? Blocks.RED_SAND.defaultBlockState() : Blocks.ORANGE_TERRACOTTA.defaultBlockState();
        
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        for (int x = -patchRadius; x <= patchRadius; x++) {
            for (int z = -patchRadius; z <= patchRadius; z++) {
                if (x * x + z * z <= patchRadius * patchRadius) {
                    mPos.set(center.getX() + x, center.getY(), center.getZ() + z);
                    BlockPos surf = getSurface(level, mPos);
                    if (surf != null) {
                        BlockPos floor = surf.below();
                        if (level.getBlockState(floor).is(Blocks.SAND)) {
                            level.setBlock(floor, patchState, 2);
                        }
                    }
                }
            }
        }
    }

    private BlockPos getSurface(WorldGenLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos(pos.getX(), level.getMaxBuildHeight(), pos.getZ());
        while (mPos.getY() > level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(mPos);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                return mPos.above().immutable();
            }
            mPos.move(Direction.DOWN);
        }
        return null;
    }
}
