package com.bretzelfresser.dinosexpansion.common.worldgen.structure;

import com.bretzelfresser.dinosexpansion.common.init.ModStructurePieces;
import com.bretzelfresser.dinosexpansion.util.CodecUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.StructureManager;

import java.util.HashMap;
import java.util.Map;

public class OasisStructurePieces {

    public record OasisVegetationEntry(Holder<PlacedFeature> feature, long seed) {
        public static final Codec<OasisVegetationEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                PlacedFeature.CODEC.fieldOf("feature").forGetter(OasisVegetationEntry::feature),
                Codec.LONG.fieldOf("seed").forGetter(OasisVegetationEntry::seed)
        ).apply(builder, OasisVegetationEntry::new));
    }

    public record OasisConfigured(
            int waterPondRadius,
            int waterPondDepth,
            float beachRadius,
            Pair<BlockStateProvider, Long> fluidState,
            Pair<BlockStateProvider, Long> waterGroundState,
            Pair<BlockStateProvider, Long> beachSurfaceBlockState,
            long vegetationPickerSeed,
            SimpleWeightedRandomList<OasisVegetationEntry> beachVegetationFeatures
    ) {

        private static final Codec<Pair<BlockStateProvider, Long>> PROVIDER_PAIR_CODEC =
                RecordCodecBuilder.create(builder -> builder.group(
                        BlockStateProvider.CODEC.fieldOf("provider").forGetter(Pair::getFirst),
                        Codec.LONG.fieldOf("seed").forGetter(Pair::getSecond)
                ).apply(builder, Pair::of));

        public static final Codec<OasisConfigured> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("waterPondRadius").forGetter(OasisConfigured::waterPondRadius),
                Codec.INT.fieldOf("waterPondDepth").forGetter(OasisConfigured::waterPondDepth),
                Codec.FLOAT.fieldOf("beachRadius").forGetter(OasisConfigured::beachRadius),
                PROVIDER_PAIR_CODEC.fieldOf("fluidState").forGetter(OasisConfigured::fluidState),
                PROVIDER_PAIR_CODEC.fieldOf("waterGroundState").forGetter(OasisConfigured::waterGroundState),
                PROVIDER_PAIR_CODEC.fieldOf("beachSurfaceBlockState").forGetter(OasisConfigured::beachSurfaceBlockState),
                Codec.LONG.fieldOf("vegetationPickerSeed").forGetter(OasisConfigured::vegetationPickerSeed),
                SimpleWeightedRandomList.wrappedCodecAllowingEmpty(OasisVegetationEntry.CODEC).fieldOf("beachVegetationFeatures").forGetter(OasisConfigured::beachVegetationFeatures)
        ).apply(builder, OasisConfigured::new));
    }


    public record OasisConfiguration(IntProvider waterPondRadius,
                                            IntProvider waterPondDepth,
                                            FloatProvider beachRadius,
                                            BlockStateProvider fluid,
                                            BlockStateProvider waterGround,
                                            BlockStateProvider beachSurfaceBlocks,
                                            SimpleWeightedRandomList<Holder<PlacedFeature>> beachVegetationFeatures){

        public static final Codec<OasisConfiguration> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                IntProvider.CODEC.fieldOf("waterPondRadius").forGetter(OasisConfiguration::waterPondRadius),
                IntProvider.CODEC.fieldOf("waterPondDepth").forGetter(OasisConfiguration::waterPondDepth),
                FloatProvider.CODEC.fieldOf("beachRadius").forGetter(OasisConfiguration::beachRadius),
                BlockStateProvider.CODEC.fieldOf("pondFluid").forGetter(OasisConfiguration::fluid),
                BlockStateProvider.CODEC.fieldOf("waterGround").forGetter(OasisConfiguration::waterGround),
                BlockStateProvider.CODEC.fieldOf("beachSurface").forGetter(OasisConfiguration::beachSurfaceBlocks),
                SimpleWeightedRandomList.wrappedCodecAllowingEmpty(PlacedFeature.CODEC).fieldOf("beachVegetations").orElse(SimpleWeightedRandomList.empty()).forGetter(OasisConfiguration::beachVegetationFeatures)
        ).apply(builder, OasisConfiguration::new));


        public OasisConfigured sample(RandomSource randomSource) {
            int radius = this.waterPondRadius.sample(randomSource);
            int depth = this.waterPondDepth.sample(randomSource);
            float beach = this.beachRadius.sample(randomSource);

            SimpleWeightedRandomList.Builder<OasisVegetationEntry> configuredPool = SimpleWeightedRandomList.builder();
            for (var wrapped : this.beachVegetationFeatures.unwrap()) {
                OasisVegetationEntry entry = new OasisVegetationEntry(wrapped.data(), randomSource.nextLong());
                configuredPool.add(entry, wrapped.getWeight().asInt());
            }

            return new OasisConfigured(
                    radius,
                    depth,
                    beach,
                    Pair.of(fluid, randomSource.nextLong()),
                    Pair.of(waterGround, randomSource.nextLong()),
                    Pair.of(beachSurfaceBlocks, randomSource.nextLong()),
                    randomSource.nextLong(),
                    configuredPool.build()
            );
        }
    }


    public static class OasisPiece extends StructurePiece {
        private final BlockPos center;
        private final OasisConfigured config;

        public OasisPiece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructurePieces.OASIS_PIECE.get(), tag);
            this.center = new BlockPos(tag.getInt("CX"), tag.getInt("CY"), tag.getInt("CZ"));
            var registryOps = context.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            this.config = OasisConfigured.CODEC.decode(registryOps, tag.get("config")).getOrThrow(errorMessage -> new IllegalStateException("Failed to decode OasisConfigured: " + errorMessage)).getFirst();

        }

        public OasisPiece(BlockPos pos, OasisConfigured config) {
            super(ModStructurePieces.OASIS_PIECE.get(), 1, new BoundingBox(pos).inflatedBy(config.waterPondRadius + Mth.ceil(config.beachRadius) + 10).inflatedBy(0, 5, 0));
            this.center = pos;
            this.config = config;
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putInt("CX", this.center.getX());
            tag.putInt("CY", this.center.getY());
            tag.putInt("CZ", this.center.getZ());

            var registryOps = context.registryAccess().createSerializationContext(NbtOps.INSTANCE);

            Tag encodedTag = OasisConfigured.CODEC.encodeStart(registryOps, this.config)
                    .getOrThrow(errorMessage -> new IllegalStateException("Failed to encode OasisConfigured: " + errorMessage));
            tag.put("config", encodedTag);
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

            // 1. Create a deterministic random source based on the center coordinates
            // to make sure palm tree placements/sizes are 100% consistent across all chunks that post-process this piece.
            var fallbackSeed = this.center.asLong();

            RandomSource fallbackForTrees = RandomSource.create(fallbackSeed);

            var fluidPickerRandom = RandomSource.create(this.config.fluidState.getSecond());
            var pondSurfacePickerRandom = RandomSource.create(this.config.waterGroundState.getSecond());
            var beachSurfacePickerRandom = RandomSource.create(this.config.beachSurfaceBlockState.getSecond());

            var vegetationPickerRandom = RandomSource.create(this.config.vegetationPickerSeed);

            int waterPondRadiusSqr = this.config.waterPondRadius * this.config.waterPondRadius;
            float pondBeachRadiusSqr = this.config.beachRadius * this.config.beachRadius;

            float totalRadius = this.config.waterPondRadius + this.config.beachRadius;
            double totalRadiusSqr = totalRadius * totalRadius;

            // centerY is the height we sampled at findGenerationPoint, which is the surface height.
            // Our water level / flat basin Y is baseY.
            int baseY = this.center.getY() - 1;

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

            // 2. Loop through columns to generate the flat recessed basin relative to baseY
            for (int xOffset = -this.config.waterPondRadius - Mth.ceil(this.config.beachRadius); xOffset <= this.config.waterPondRadius + Mth.ceil(this.config.beachRadius); xOffset++) {
                for (int zOffset = -this.config.waterPondRadius - Mth.ceil(this.config.beachRadius); zOffset <= this.config.waterPondRadius + Mth.ceil(this.config.beachRadius); zOffset++) {
                    double currentRadiusSqr = xOffset * xOffset + zOffset * zOffset;
                    //outside our overall radius
                    if (currentRadiusSqr > totalRadiusSqr) continue;

                    int x = this.center.getX() + xOffset;
                    int z = this.center.getZ() + zOffset;

                    // Query surface Y using heightmap - safe and fast, no chunk loading
                    int localSurfY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;

                    //inside the water pond
                    if (currentRadiusSqr < waterPondRadiusSqr) {
                        // Pond Basin
                        double depthRatio = currentRadiusSqr / waterPondRadiusSqr;
                        int localDepth = (int) Math.round(this.config.waterPondDepth * (1.0 - depthRatio * depthRatio)) + 1;

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
                                    level.setBlock(mutablePos, this.config.fluidState.getFirst().getState(fluidPickerRandom, mutablePos), 2);
                                } else if (currentY == baseY - localDepth - 1) {
                                    // Bottom seal
                                    BlockState bottom = this.config.waterGroundState.getFirst().getState(pondSurfacePickerRandom, mutablePos);
                                    level.setBlock(mutablePos, bottom, 2);
                                } else {
                                    // Support below the seal if terrain falls away
                                    level.setBlock(mutablePos, Blocks.DIRT.defaultBlockState(), 2);
                                }
                            }
                        }
                        //beach radius range
                    } else if (currentRadiusSqr < totalRadiusSqr) {
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
                                    BlockState cover = this.config.beachSurfaceBlockState.getFirst().getState(beachSurfacePickerRandom, mutablePos);
                                    level.setBlock(mutablePos, cover, 2);

                                    // Place vegetation on top
                                    BlockPos topPos = mutablePos.above();
                                    var placedFeatureEntry = this.config.beachVegetationFeatures.getRandom(vegetationPickerRandom).orElseThrow().data();
                                    var placedFeature = placedFeatureEntry.feature().value();
                                    placedFeature.place(level, generator, RandomSource.create(placedFeatureEntry.seed()), topPos);

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
            int palmCount = fallbackForTrees.nextInt(2) + 2; // 2 to 3 palms
            for (int i = 0; i < palmCount; i++) {
                double angle = fallbackForTrees.nextDouble() * 2.0 * Math.PI;
                double dist =  this.config.waterPondRadius + fallbackForTrees.nextDouble() * config.beachRadius;
                int px = this.center.getX() + (int) Math.round(Math.cos(angle) * dist);
                int pz = this.center.getZ() + (int) Math.round(Math.sin(angle) * dist);

                BlockPos palmBase = new BlockPos(px, baseY + 1, pz);
                generatePalmTree(level, palmBase, fallbackForTrees, box);
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
