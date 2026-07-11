package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.worldgen.structure.CaveDungeonPieces;
import com.bretzelfresser.dinosexpansion.common.worldgen.structure.OasisStructurePieces;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructurePieces {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, DinosExpansion.MODID);


    public static final DeferredHolder<StructurePieceType, StructurePieceType> CAVE_DUNGEON_PIECE =
            STRUCTURE_PIECE_TYPES.register("cave_dungeon_piece", () -> CaveDungeonPieces.CaveDungeonEntrancePiece::new);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> OASIS_PIECE =
            STRUCTURE_PIECE_TYPES.register("oasis_piece", () -> OasisStructurePieces.OasisPiece::new);
}
