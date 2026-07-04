package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.worldgen.structure.CaveDungeonPieces;
import com.bretzelfresser.dinosexpansion.common.worldgen.structure.CaveDungeonStructure;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, DinosExpansion.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<CaveDungeonStructure>> CAVE_DUNGEON =
            STRUCTURE_TYPES.register("cave_dungeon", () -> () -> CaveDungeonStructure.CODEC);


    public static final ResourceKey<Structure> CAVE_DUNGEON_STRUCTURE =
            ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "cave_dungeon"));

    public static final ResourceKey<StructureSet> CAVE_DUNGEON_SET =
            ResourceKey.create(Registries.STRUCTURE_SET, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "cave_dungeon_set"));


    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        ModStructurePieces.STRUCTURE_PIECE_TYPES.register(eventBus);
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        var biomes = context.lookup(Registries.BIOME);

        context.register(CAVE_DUNGEON_STRUCTURE, new CaveDungeonStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(Tags.Biomes.HAS_CAVE_DUNGEON),
                        Map.of(),
                        GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                        TerrainAdjustment.BEARD_THIN
                )
        ));
    }

    public static void bootstrapStructureSets(BootstrapContext<StructureSet> context) {
        var structures = context.lookup(Registries.STRUCTURE);

        context.register(CAVE_DUNGEON_SET, new StructureSet(
                List.of(StructureSet.entry(structures.getOrThrow(CAVE_DUNGEON_STRUCTURE))),
                new RandomSpreadStructurePlacement(
                        32,
                        8,
                        RandomSpreadType.LINEAR,
                        14357892
                )
        ));
    }
}
