package com.bretzelfresser.dinosexpansion.common.worldgen.structure;

import com.bretzelfresser.dinosexpansion.common.init.ModStructurePieces;
import com.bretzelfresser.dinosexpansion.util.BoundingBoxUtils;
import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.phys.Vec3;

import java.awt.color.ProfileDataException;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CaveDungeonPieces {

    public static class Circle implements Predicate<BlockPos>{
        private final BlockPos center;
        protected final double radius;

        public Circle(BlockPos center, double radius) {
            this.center = center;
            this.radius = radius;
        }

        @Override
        public boolean test(BlockPos blockPos) {
            return blockPos.distSqr(center) <= radius * radius;
        }
    }

    public static class CaveDungeonEntrancePiece extends StructurePiece {

        protected final IntProvider radiusWalkway;

        public CaveDungeonEntrancePiece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructurePieces.CAVE_DUNGEON_PIECE.get(), tag);
            DynamicOps<Tag> dynamicops = context.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            radiusWalkway = IntProvider.CODEC.parse(dynamicops, tag).getOrThrow();

        }

        public CaveDungeonEntrancePiece(BoundingBox box, IntProvider radiusWalkway, int genDepth) {
            super(ModStructurePieces.CAVE_DUNGEON_PIECE.get(), genDepth, box);
            this.radiusWalkway = radiusWalkway;
            this.setOrientation(Direction.NORTH);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            DynamicOps<Tag> dynamicops = context.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            IntProvider.CODEC.encode(this.radiusWalkway, dynamicops, tag).getOrThrow();
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
            var attachPos = new BlockPos(random.nextBoolean() ? boundingBox.minX() : boundingBox.maxX(), boundingBox.minY(), random.nextBoolean() ? boundingBox.maxZ() : boundingBox.minZ());

            int radius = this.radiusWalkway.sample(random);

            List<Circle> walkCircles = new ArrayList<>();
            walkCircles.add(new Circle(center, radius));

            BlockPos currentPos = center;
            int steps = 0;

            // Run the walk towards attachPos
            while (currentPos.distSqr(attachPos) > (radius * radius) && steps < 150) {
                steps++;

                // Vector pointing directly from currentPos to attachPos
                Vec3 vecToAttachPos = Vec3.atLowerCornerOf(attachPos.subtract(currentPos)).normalize().scale(6.0);

                // Random wiggle vector
                Vec3 randomWalkVector = new Vec3(
                    random.nextIntBetweenInclusive(-3, 3),
                    random.nextIntBetweenInclusive(-3, 3),
                    random.nextIntBetweenInclusive(-3, 3)
                );

                // Combine steering and wiggle
                Vec3 totalWalkVector = randomWalkVector.add(vecToAttachPos).normalize().scale(3);

                currentPos = BlockPos.containing(totalWalkVector.add(Vec3.atLowerCornerOf(currentPos)));

                // Clamp currentPos inside boundingBox to prevent carving outside our structure piece
                currentPos = new BlockPos(
                    Mth.clamp(currentPos.getX(), boundingBox.minX() + 1, boundingBox.maxX() - 1),
                    Mth.clamp(currentPos.getY(), boundingBox.minY() + 1, boundingBox.maxY() - 1),
                    Mth.clamp(currentPos.getZ(), boundingBox.minZ() + 1, boundingBox.maxZ() - 1)
                );

                radius = this.radiusWalkway.sample(random);
                walkCircles.add(new Circle(currentPos, radius));
            }

            // Carve blocks
            BoundingBoxUtils.forEachPos(this.boundingBox, pos -> {
                // Ensure we only modify blocks within the structure piece's bounding box and the current chunk box
                if (pos.getX() >= box.minX() && pos.getX() <= box.maxX() &&
                    pos.getY() >= box.minY() && pos.getY() <= box.maxY() &&
                    pos.getZ() >= box.minZ() && pos.getZ() <= box.maxZ()) {

                    boolean tested = false;
                    for (Circle c : walkCircles) {
                        if (c.test(pos)) {
                            tested = true;
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                            break;
                        }
                    }
                    if (!tested){
                        //level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
                    }
                }
            });

            //place  a block at the center for orientation
            //level.setBlock(center, Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        }
    }
}
