package com.bretzelfresser.dinosexpansion.common.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class PrehistoricFossilFeature extends Feature<NoneFeatureConfiguration> {
    public PrehistoricFossilFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Ensure we are placing on sand/solid ground
        BlockState groundState = level.getBlockState(origin.below());
        if (groundState.isAir() || groundState.is(Blocks.WATER)) {
            return false;
        }

        // Determine orientation
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction leftDir = direction.getCounterClockWise();
        Direction rightDir = direction.getClockWise();

        int length = random.nextInt(5) + 6; // 6 to 10 blocks long skeleton
        int startDepth = random.nextInt(2); // 0 or 1 block buried

        // 1. Spine and ribs
        for (int i = 0; i < length; i++) {
            BlockPos spinePos = origin.relative(direction, i).below(startDepth);
            
            // Draw spine block
            setBlockIfReplaceable(level, spinePos, Blocks.BONE_BLOCK.defaultBlockState());

            // Every 2 blocks, draw ribs
            if (i > 1 && i < length - 1 && i % 2 == 0) {
                int ribHeight = random.nextInt(2) + 2; // 2 to 3 blocks
                int ribWidth = random.nextInt(2) + 2;  // 2 to 3 blocks
                
                // Left rib arch
                drawRib(level, spinePos, leftDir, ribWidth, ribHeight);
                // Right rib arch
                drawRib(level, spinePos, rightDir, ribWidth, ribHeight);
            }
        }

        // 2. Skull at the end of the spine
        BlockPos skullCenter = origin.relative(direction, length).below(startDepth);
        drawSkull(level, skullCenter, direction);

        // 3. Scattered bone blocks around
        int scatteredCount = random.nextInt(4) + 2;
        for (int i = 0; i < scatteredCount; i++) {
            int rx = random.nextInt(9) - 4;
            int rz = random.nextInt(9) - 4;
            BlockPos rPos = origin.offset(rx, 0, rz);
            rPos = getSurface(level, rPos);
            if (rPos != null && level.getBlockState(rPos.below()).is(Blocks.SAND)) {
                if (random.nextBoolean()) {
                    setBlockIfReplaceable(level, rPos, Blocks.BONE_BLOCK.defaultBlockState());
                }
            }
        }

        return true;
    }

    private void drawRib(WorldGenLevel level, BlockPos start, Direction dir, int width, int height) {
        BlockPos current = start;
        // Move out
        for (int w = 1; w <= width; w++) {
            current = current.relative(dir);
            setBlockIfReplaceable(level, current, Blocks.BONE_BLOCK.defaultBlockState());
        }
        // Move down
        for (int h = 1; h <= height; h++) {
            current = current.below();
            setBlockIfReplaceable(level, current, Blocks.BONE_BLOCK.defaultBlockState());
        }
    }

    private void drawSkull(WorldGenLevel level, BlockPos pos, Direction direction) {
        // Place a small 2x2x2 bone block head with eye socket holes
        Direction left = direction.getCounterClockWise();
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    BlockPos bonePos = pos.relative(direction, x).above(y).relative(left, z);
                    
                    // Add some air holes for eyes/jaw
                    if (x == 1 && y == 1 && z == 0) {
                        continue; // skip eye
                    }
                    if (x == 1 && y == 0 && z == 1) {
                        continue; // skip jaw space
                    }
                    setBlockIfReplaceable(level, bonePos, Blocks.BONE_BLOCK.defaultBlockState());
                }
            }
        }
    }

    private void setBlockIfReplaceable(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.isOutsideBuildHeight(pos.getY())) return;
        BlockState current = level.getBlockState(pos);
        if (current.isAir() || current.is(Blocks.SAND) || current.is(Blocks.SANDSTONE) || current.is(Blocks.DEAD_BUSH)) {
            level.setBlock(pos, state, 2);
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
