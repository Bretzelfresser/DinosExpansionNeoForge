package com.bretzelfresser.dinosexpansion.common.worldgen.structure;

import com.bretzelfresser.dinosexpansion.common.init.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class CaveDungeonStructure extends Structure {
    public static final MapCodec<CaveDungeonStructure> CODEC = simpleCodec(CaveDungeonStructure::new);

    public CaveDungeonStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        // Spawns underground at Y = 50 in the middle of the spawning chunk


        BlockPos.MutableBlockPos spawnPos = context.chunkPos().getMiddleBlockPosition(50).mutable();
        int y = context.chunkGenerator().getFirstFreeHeight(spawnPos.getX(), spawnPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        spawnPos.setY(y);

        return Optional.of(new GenerationStub(spawnPos, builder -> {
            builder.addPiece(new CaveDungeonPieces.CaveDungeonPiece(spawnPos));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CAVE_DUNGEON.get();
    }
}
