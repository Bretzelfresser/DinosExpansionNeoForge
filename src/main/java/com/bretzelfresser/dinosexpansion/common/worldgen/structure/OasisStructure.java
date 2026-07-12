package com.bretzelfresser.dinosexpansion.common.worldgen.structure;

import com.bretzelfresser.dinosexpansion.common.init.ModBiomes;
import com.bretzelfresser.dinosexpansion.common.init.ModStructures;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class OasisStructure extends Structure {
    public static final MapCodec<OasisStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            OasisStructurePieces.OasisConfiguration.CODEC.fieldOf("configuration").forGetter(s -> s.config)
    ).apply(instance, OasisStructure::new));

    private final OasisStructurePieces.OasisConfiguration config;

    public OasisStructure(StructureSettings settings, OasisStructurePieces.OasisConfiguration config) {
        super(settings);
        this.config = config;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        BlockPos center = context.chunkPos().getMiddleBlockPosition(64);

        // Edge check: Check if all cardinal offsets (16 blocks away) are in the BONE_DESERT biome.
        // Queries biome source directly, which is 100% safe and doesn't load chunks.
        int[] offsets = {-16, 16};
        for (int dx : offsets) {
            BlockPos checkPos = center.offset(dx, 0, 0);
            Holder<Biome> biomeHolder = context.biomeSource().getNoiseBiome(
                    checkPos.getX() >> 2,
                    64 >> 2,
                    checkPos.getZ() >> 2,
                    context.randomState().sampler()
            );
            if (!biomeHolder.is(ModBiomes.BONE_DESERT)) {
                return Optional.empty();
            }
        }
        for (int dz : offsets) {
            BlockPos checkPos = center.offset(0, 0, dz);
            Holder<Biome> biomeHolder = context.biomeSource().getNoiseBiome(
                    checkPos.getX() >> 2,
                    64 >> 2,
                    checkPos.getZ() >> 2,
                    context.randomState().sampler()
            );
            if (!biomeHolder.is(ModBiomes.BONE_DESERT)) {
                return Optional.empty();
            }
        }

        // Find surface Y
        int y = context.chunkGenerator().getFirstFreeHeight(
                center.getX(),
                center.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(),
                context.randomState()
        );
        BlockPos spawnPos = center.atY(y);

        return Optional.of(new GenerationStub(spawnPos, builder -> {
            var sampledConfig = this.config.sample(context.random());
            builder.addPiece(new OasisStructurePieces.OasisPiece(spawnPos, sampledConfig));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.OASIS.get();
    }
}
