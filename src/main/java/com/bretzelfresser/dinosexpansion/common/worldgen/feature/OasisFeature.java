package com.bretzelfresser.dinosexpansion.common.worldgen.feature;

import com.bretzelfresser.dinosexpansion.common.init.ModBiomes;
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

public class OasisFeature extends Feature<NoneFeatureConfiguration> {
    public OasisFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // 1. Edge Prevention Check: Check surrounding offsets.
        // If any surrounding block (16 blocks away in cardinal directions) is not BONE_DESERT, abort.
        int[] offsets = {-16, 16};
        for (int dx : offsets) {
            BlockPos checkPos = origin.offset(dx, 0, 0);
            if (!level.getBiome(checkPos).is(ModBiomes.BONE_DESERT)) {
                return false;
            }
        }
        for (int dz : offsets) {
            BlockPos checkPos = origin.offset(0, 0, dz);
            if (!level.getBiome(checkPos).is(ModBiomes.BONE_DESERT)) {
                return false;
            }
        }

        // 2. Pond & Basin Configuration
        int radius = random.nextInt(3) + 4; // 4 to 6 radius
        int depth = 2;

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // Carve and place pond
        for (int xOffset = -radius - 3; xOffset <= radius + 3; xOffset++) {
            for (int zOffset = -radius - 3; zOffset <= radius + 3; zOffset++) {
                double r = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
                if (r > radius + 3.0) continue;

                int x = origin.getX() + xOffset;
                int z = origin.getZ() + zOffset;

                // Find surface
                BlockPos surfPos = getSurface(level, new BlockPos(x, origin.getY(), z));
                if (surfPos == null) continue;
                int surfY = surfPos.getY() - 1;

                if (r < radius) {
                    // We are inside the pond basin
                    double depthRatio = r / radius;
                    int localDepth = (int) Math.round(depth * (1.0 - depthRatio * depthRatio)) + 1;

                    // Carve air above water, place water inside, place clay/mud/moss at the bottom
                    for (int yOffset = 4; yOffset >= -localDepth - 1; yOffset--) {
                        int currentY = surfY + yOffset;
                        mutablePos.set(x, currentY, z);
                        if (yOffset > 0) {
                            level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2);
                        } else if (yOffset >= -localDepth) {
                            // Place water
                            level.setBlock(mutablePos, Blocks.WATER.defaultBlockState(), 2);
                        } else {
                            // Bottom seal
                            BlockState bottom = random.nextBoolean() ? Blocks.CLAY.defaultBlockState() : Blocks.MUD.defaultBlockState();
                            level.setBlock(mutablePos, bottom, 2);
                        }
                    }
                } else if (r < radius + 2.5) {
                    // Pond border: place moss blocks, mud, grass, clay on the surface
                    for (int yOffset = 2; yOffset >= -2; yOffset--) {
                        int currentY = surfY + yOffset;
                        mutablePos.set(x, currentY, z);
                        BlockState original = level.getBlockState(mutablePos);
                        if (!original.isAir() && original.getFluidState().isEmpty()) {
                            BlockState cover;
                            if (random.nextFloat() < 0.5f) {
                                cover = Blocks.MOSS_BLOCK.defaultBlockState();
                            } else if (random.nextFloat() < 0.7f) {
                                cover = Blocks.MUD.defaultBlockState();
                            } else {
                                cover = Blocks.GRASS_BLOCK.defaultBlockState();
                            }
                            level.setBlock(mutablePos, cover, 2);
                            // Set dirt underneath
                            level.setBlock(mutablePos.below(), Blocks.DIRT.defaultBlockState(), 2);
                            
                            // Place decorations on top
                            BlockPos topPos = mutablePos.above();
                            if (level.getBlockState(topPos).isAir()) {
                                if (random.nextFloat() < 0.2f) {
                                    level.setBlock(topPos, Blocks.FERN.defaultBlockState(), 2);
                                } else if (random.nextFloat() < 0.1f) {
                                    // Sugar cane at the water edge
                                    if (r < radius + 1.2) {
                                        level.setBlock(topPos, Blocks.SUGAR_CANE.defaultBlockState(), 2);
                                        if (random.nextBoolean()) {
                                            level.setBlock(topPos.above(), Blocks.SUGAR_CANE.defaultBlockState(), 2);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        // 3. Spawn a couple of custom Palm Trees
        int palmCount = random.nextInt(2) + 2; // 2 to 3 palms
        for (int i = 0; i < palmCount; i++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;
            double dist = radius + 1.2 + random.nextDouble() * 1.2;
            int px = origin.getX() + (int) Math.round(Math.cos(angle) * dist);
            int pz = origin.getZ() + (int) Math.round(Math.sin(angle) * dist);
            
            BlockPos palmBase = getSurface(level, new BlockPos(px, origin.getY(), pz));
            if (palmBase != null) {
                BlockState baseState = level.getBlockState(palmBase.below());
                if (baseState.is(Blocks.MOSS_BLOCK) || baseState.is(Blocks.MUD) || baseState.is(Blocks.GRASS_BLOCK) || baseState.is(Blocks.SAND)) {
                    generatePalmTree(level, palmBase, random);
                }
            }
        }

        return true;
    }

    private void generatePalmTree(WorldGenLevel level, BlockPos base, RandomSource random) {
        int height = random.nextInt(4) + 6; // 6 to 9 blocks tall
        Direction leanDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos current = base;

        // Trunk wiggles/leans slightly
        for (int y = 0; y < height; y++) {
            if (y > 2 && y % 3 == 0) {
                current = current.relative(leanDir);
            }
            if (level.isOutsideBuildHeight(current.getY())) break;
            level.setBlock(current, Blocks.JUNGLE_LOG.defaultBlockState(), 2);
            current = current.above();
        }

        // Canopy of leaves at the top
        BlockPos top = current.below();
        setLeaves(level, top.above());
        
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos leafPos = top.relative(dir);
            setLeaves(level, leafPos);
            setLeaves(level, leafPos.relative(dir));
            setLeaves(level, leafPos.relative(dir).below());
            
            Direction diag = dir.getClockWise();
            BlockPos diagPos = top.relative(dir).relative(diag);
            setLeaves(level, diagPos);
            setLeaves(level, diagPos.below());
        }
    }

    private void setLeaves(WorldGenLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos.getY())) return;
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Blocks.JUNGLE_LEAVES.defaultBlockState(), 2);
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
