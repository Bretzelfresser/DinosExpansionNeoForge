package com.bretzelfresser.dinosexpansion.common.worldgen.structure;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
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
import net.minecraft.world.level.levelgen.Heightmap;
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
            super(ModStructurePieces.OASIS_PIECE.get(), 1, new BoundingBox(pos).inflatedBy(size + 10).inflatedBy(0, 5, 0));
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

            DinosExpansion.LOGGER.debug("generating Oasis piece at: {}", this.center);

            // 1. Create a deterministic random source based on the center coordinates
            // to make sure palm tree placements/sizes are 100% consistent across all chunks that post-process this piece.
            RandomSource deterministicRandom = RandomSource.create(this.center.asLong());

            int radius = this.size;
            int depth = 2;

            // centerY is the height we sampled at findGenerationPoint, which is the surface height.
            // Our water level / flat basin Y is baseY.
            int baseY = this.center.getY() - 1;

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            // 2. Loop through columns to generate the flat recessed basin relative to baseY
            for (int xOffset = -radius - 3; xOffset <= radius + 3; xOffset++) {
                for (int zOffset = -radius - 3; zOffset <= radius + 3; zOffset++) {
                    double r = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
                    if (r > radius + 3.0) continue;

                    int x = this.center.getX() + xOffset;
                    int z = this.center.getZ() + zOffset;

                    // Query surface Y using heightmap - safe and fast, no chunk loading
                    int localSurfY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;

                    if (r < radius) {
                        // Pond Basin
                        double depthRatio = r / radius;
                        int localDepth = (int) Math.round(depth * (1.0 - depthRatio * depthRatio)) + 1;

                        int bottomLimit = Math.min(baseY - localDepth - 1, localSurfY - 1);
                        for (int currentY = baseY + 6; currentY >= bottomLimit; currentY--) {
                            mutablePos.set(x, currentY, z);
                            if (level.isOutsideBuildHeight(currentY)) continue;

                            // Crucial: Only modify blocks within the current chunk's generating box
                            if (box.isInside(mutablePos)) {
                                if (currentY > baseY) {
                                    // Clear air above the water
                                    level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2);
                                } else if (currentY >= baseY - localDepth) {
                                    // Place water
                                    level.setBlock(mutablePos, Blocks.WATER.defaultBlockState(), 2);
                                } else if (currentY == baseY - localDepth - 1) {
                                    // Bottom seal
                                    BlockState bottom = deterministicRandom.nextBoolean() ? Blocks.CLAY.defaultBlockState() : Blocks.MUD.defaultBlockState();
                                    level.setBlock(mutablePos, bottom, 2);
                                } else {
                                    // Support below the seal if terrain falls away
                                    level.setBlock(mutablePos, Blocks.DIRT.defaultBlockState(), 2);
                                }
                            }
                        }
                    } else if (r < radius + 2.5) {
                        // Pond border: place moss blocks, mud, grass, clay on the flat surface
                        int bottomLimit = Math.min(baseY - 3, localSurfY - 1);
                        for (int currentY = baseY + 6; currentY >= bottomLimit; currentY--) {
                            mutablePos.set(x, currentY, z);
                            if (level.isOutsideBuildHeight(currentY)) continue;

                            // Crucial: Only modify blocks within the current chunk's generating box
                            if (box.isInside(mutablePos)) {
                                if (currentY > baseY) {
                                    // Clear air above the border
                                    level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2);
                                } else if (currentY == baseY) {
                                    // Place border cover block
                                    BlockState cover;
                                    float f = deterministicRandom.nextFloat();
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
                                        if (deterministicRandom.nextFloat() < 0.2f) {
                                            level.setBlock(topPos, Blocks.FERN.defaultBlockState(), 2);
                                        } else if (deterministicRandom.nextFloat() < 0.1f) {
                                            if (r < radius + 1.2) {
                                                level.setBlock(topPos, Blocks.SUGAR_CANE.defaultBlockState(), 2);
                                                if (deterministicRandom.nextBoolean()) {
                                                    level.setBlock(topPos.above(), Blocks.SUGAR_CANE.defaultBlockState(), 2);
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Under-layer seal (dirt underneath the border cover) down to terrain level
                                    level.setBlock(mutablePos, Blocks.DIRT.defaultBlockState(), 2);
                                }
                            }
                        }
                    }
                }
            }

            // 3. Spawn Palm Trees standing flat at baseY + 1 on the border
            int palmCount = deterministicRandom.nextInt(2) + 2; // 2 to 3 palms
            DinosExpansion.LOGGER.debug("generating Oasis with {} palm trees", palmCount);
            for (int i = 0; i < palmCount; i++) {
                double angle = deterministicRandom.nextDouble() * 2.0 * Math.PI;
                double dist = radius + 0.8 + deterministicRandom.nextDouble() * 1.2;
                int px = this.center.getX() + (int) Math.round(Math.cos(angle) * dist);
                int pz = this.center.getZ() + (int) Math.round(Math.sin(angle) * dist);

                BlockPos palmBase = new BlockPos(px, baseY + 1, pz);
                generatePalmTree(level, palmBase, deterministicRandom, box);
            }
        }

        private void generatePalmTree(WorldGenLevel level, BlockPos base, RandomSource random, BoundingBox box) {
            int height = random.nextInt(4) + 6; // 6 to 9 blocks tall
            Direction leanDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos current = base;

            // Trunk wiggles/leans slightly
            for (int y = 0; y < height; y++) {
                if (level.isOutsideBuildHeight(current.getY())) break;

                level.setBlock(current, Blocks.JUNGLE_LOG.defaultBlockState(), 2);

                if (y > 2 && y % 3 == 0) {
                    // Shift horizontally and place an elbow connector log to ensure connection!
                    current = current.relative(leanDir);
                    if (box.isInside(current)) {
                        level.setBlock(current, Blocks.JUNGLE_LOG.defaultBlockState(), 2);
                    }
                }
                current = current.above();
            }

            // Canopy of leaves at the top
            BlockPos top = current.below();
            setLeaves(level, top.above(), box);

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos leafPos = top.relative(dir);
                setLeaves(level, leafPos, box);
                setLeaves(level, leafPos.relative(dir), box);
                setLeaves(level, leafPos.relative(dir).below(), box);

                Direction diag = dir.getClockWise();
                BlockPos diagPos = top.relative(dir).relative(diag);
                setLeaves(level, diagPos, box);
                setLeaves(level, diagPos.below(), box);
            }
        }

        private void setLeaves(WorldGenLevel level, BlockPos pos, BoundingBox box) {
            if (level.isOutsideBuildHeight(pos.getY())) return;
            if (box.isInside(pos)) {
                if (level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, Blocks.JUNGLE_LEAVES.defaultBlockState(), 2);
                }
            }
        }
    }
}
