package com.bretzelfresser.dinosexpansion.common.worldgen.structure;

import com.bretzelfresser.dinosexpansion.common.init.ModStructurePieces;
import com.bretzelfresser.dinosexpansion.common.init.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class CaveDungeonPieces {

    public static class CaveDungeonPiece extends StructurePiece {
        public CaveDungeonPiece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructurePieces.CAVE_DUNGEON_PIECE.get(), tag);
        }

        public CaveDungeonPiece(BlockPos pos) {
            super(ModStructurePieces.CAVE_DUNGEON_PIECE.get(), 0, new BoundingBox(pos.getX() - 10, pos.getY() - 10, pos.getZ() - 10, pos.getX() + 10, pos.getY() + 10, pos.getZ() + 10));
            this.setOrientation(Direction.NORTH);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            // Save state if needed
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
            BlockPos center = this.boundingBox.getCenter();

            // Simple carving logic: carve a spherical room
            for (int x = -10; x <= 10; x++) {
                for (int y = -10; y <= 10; y++) {
                    for (int z = -10; z <= 10; z++) {
                        BlockPos p = center.offset(x, y, z);
                        if (box.isInside(p)) {
                            double dist = x * x + y * y + z * z;
                            if (dist < 64.0) {
                                // Carve the cave interior
                                level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                            } else if (dist < 81.0 && level.getBlockState(p).isAir()) {
                                // Place supporting cobblestone wall shell
                                level.setBlock(p, Blocks.COBBLESTONE.defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }

            // Place altar in the center of the carved cave
            level.setBlock(center, Blocks.OBSIDIAN.defaultBlockState(), 2);
            level.setBlock(center.above(), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        }
    }
}
