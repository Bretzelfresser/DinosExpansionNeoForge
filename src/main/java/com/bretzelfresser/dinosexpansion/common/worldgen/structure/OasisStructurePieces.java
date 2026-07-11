package com.bretzelfresser.dinosexpansion.common.worldgen.structure;

import com.bretzelfresser.dinosexpansion.common.init.ModStructurePieces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.StructureManager;

public class OasisStructurePieces {

    public static class OasisPiece extends StructurePiece {
        private final BlockPos center;
        private final int size;

        public OasisPiece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructurePieces.OASIS_PIECE.get(), tag);
            this.size = tag.getInt("Size");
            this.center = new BlockPos(tag.getInt("CX"), tag.getInt("CY"), tag.getInt("CZ"));
        }

        public OasisPiece(BlockPos pos, int size) {
            super(ModStructurePieces.OASIS_PIECE.get(), 1, new BoundingBox(pos).inflatedBy(size + 4));
            this.center = pos;
            this.size = size;
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putInt("Size", this.size);
            tag.putInt("CX", this.center.getX());
            tag.putInt("CY", this.center.getY());
            tag.putInt("CZ", this.center.getZ());
        }

        @Override
        public void postProcess(
                WorldGenLevel level,
                StructureManager structureManager,
                ChunkGenerator generator,
                RandomSource random,
                BoundingBox box,
                ChunkPos chunkPos,
                BlockPos origin
        ) {
            int radius = this.size;
            int depth = 2;

            // 1. Scan the area to find the minimum surface Y level (minY)
            int minY = this.center.getY();
            boolean foundAnySurface = false;

            for (int xOffset = -radius - 3; xOffset <= radius + 3; xOffset++) {
                for (int zOffset = -radius - 3; zOffset <= radius + 3; zOffset++) {
                    if (xOffset * xOffset + zOffset * zOffset <= (radius + 3) * (radius + 3)) {
                        int x = this.center.getX() + xOffset;
                        int z = this.center.getZ() + zOffset;
                        BlockPos surfPos = getSurface(level, new BlockPos(x, this.center.getY(), z));
                        if (surfPos != null) {
                            int surfY = surfPos.getY() - 1;
                            if (!foundAnySurface) {
                                minY = surfY;
                                foundAnySurface = true;
                            } else {
                                minY = Math.min(minY, surfY);
                            }
                        }
                    }
                }
            }

            // Clamp minY to avoid pulling the oasis too deep
            minY = Math.max(minY, this.center.getY() - 4);

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            // 2. Loop through columns to generate the flat recessed basin relative to minY
            for (int xOffset = -radius - 3; xOffset <= radius + 3; xOffset++) {
                for (int zOffset = -radius - 3; zOffset <= radius + 3; zOffset++) {
                    double r = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
                    if (r > radius + 3.0) continue;

                    int x = this.center.getX() + xOffset;
                    int z = this.center.getZ() + zOffset;

                    if (r < radius) {
                        // Pond Basin
                        double depthRatio = r / radius;
                        int localDepth = (int) Math.round(depth * (1.0 - depthRatio * depthRatio)) + 1;

                        for (int yOffset = 6; yOffset >= -localDepth - 1; yOffset--) {
                            int currentY = minY + yOffset;
                            mutablePos.set(x, currentY, z);
                            if (level.isOutsideBuildHeight(currentY)) continue;

                            if (yOffset > 0) {
                                // Clear air above the water
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
                        // Pond border: place moss blocks, mud, grass, clay on the flat surface
                        for (int yOffset = 6; yOffset >= -3; yOffset--) {
                            int currentY = minY + yOffset;
                            mutablePos.set(x, currentY, z);
                            if (level.isOutsideBuildHeight(currentY)) continue;

                            if (yOffset > 0) {
                                // Clear air above the border
                                level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2);
                            } else if (yOffset == 0) {
                                // Place border cover block
                                BlockState cover;
                                float f = random.nextFloat();
                                if (f < 0.4f) {
                                    cover = Blocks.MOSS_BLOCK.defaultBlockState();
                                } else if (f < 0.7f) {
                                    cover = Blocks.MUD.defaultBlockState();
                                } else {
                                    cover = Blocks.GRASS_BLOCK.defaultBlockState();
                                }
                                level.setBlock(mutablePos, cover, 2);

                                // Place vegetation on top
                                BlockPos topPos = mutablePos.above();
                                if (level.getBlockState(topPos).isAir()) {
                                    if (random.nextFloat() < 0.2f) {
                                        level.setBlock(topPos, Blocks.FERN.defaultBlockState(), 2);
                                    } else if (random.nextFloat() < 0.1f) {
                                        if (r < radius + 1.2) {
                                            level.setBlock(topPos, Blocks.SUGAR_CANE.defaultBlockState(), 2);
                                            if (random.nextBoolean()) {
                                                level.setBlock(topPos.above(), Blocks.SUGAR_CANE.defaultBlockState(), 2);
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Under-layer seal (dirt underneath the border cover)
                                level.setBlock(mutablePos, Blocks.DIRT.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }

            // 3. Spawn Palm Trees standing flat at minY + 1 on the border
            int palmCount = random.nextInt(2) + 2; // 2 to 3 palms
            for (int i = 0; i < palmCount; i++) {
                double angle = random.nextDouble() * 2.0 * Math.PI;
                double dist = radius + 0.8 + random.nextDouble() * 1.2;
                int px = this.center.getX() + (int) Math.round(Math.cos(angle) * dist);
                int pz = this.center.getZ() + (int) Math.round(Math.sin(angle) * dist);
                
                BlockPos palmBase = new BlockPos(px, minY + 1, pz);
                BlockState baseState = level.getBlockState(palmBase.below());
                if (baseState.is(Blocks.MOSS_BLOCK) || baseState.is(Blocks.MUD) || baseState.is(Blocks.GRASS_BLOCK) || baseState.is(Blocks.SAND) || baseState.is(Blocks.DIRT)) {
                    generatePalmTree(level, palmBase, random);
                }
            }
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
}
