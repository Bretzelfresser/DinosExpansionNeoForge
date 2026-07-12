package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.worldgen.structure.CaveDungeonPieces;
import com.bretzelfresser.dinosexpansion.common.worldgen.structure.CaveDungeonStructure;
import com.bretzelfresser.dinosexpansion.common.worldgen.structure.OasisStructure;
import com.bretzelfresser.dinosexpansion.common.worldgen.structure.OasisStructurePieces;
import com.bretzelfresser.dinosexpansion.util.FeaturePlacementUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
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

    public static final DeferredHolder<StructureType<?>, StructureType<OasisStructure>> OASIS =
            STRUCTURE_TYPES.register("oasis", () -> () -> OasisStructure.CODEC);


    public static final ResourceKey<Structure> CAVE_DUNGEON_STRUCTURE =
            ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "cave_dungeon"));

    public static final ResourceKey<Structure> OASIS_STRUCTURE =
            ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "oasis"));

    public static final ResourceKey<StructureSet> CAVE_DUNGEON_SET =
            ResourceKey.create(Registries.STRUCTURE_SET, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "cave_dungeon_set"));

    public static final ResourceKey<StructureSet> OASIS_SET =
            ResourceKey.create(Registries.STRUCTURE_SET, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "oasis_set"));


    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        ModStructurePieces.STRUCTURE_PIECE_TYPES.register(eventBus);
    }

    public static void bootstrapStructures(BootstrapContext<Structure> context) {
        var biomes = context.lookup(Registries.BIOME);
        var noises = context.lookup(Registries.NOISE);
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);


        context.register(CAVE_DUNGEON_STRUCTURE, new CaveDungeonStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(Tags.Biomes.HAS_CAVE_DUNGEON),
                        Map.of(),
                        GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                        TerrainAdjustment.NONE
                )
        ));

        context.register(OASIS_STRUCTURE, new OasisStructure(
                new Structure.StructureSettings(
                        biomes.getOrThrow(Tags.Biomes.HAS_OASIS),
                        Map.of(),
                        GenerationStep.Decoration.SURFACE_STRUCTURES,
                        TerrainAdjustment.NONE
                ),
                new OasisStructurePieces.OasisConfiguration(
                        UniformInt.of(5, 10),
                        UniformInt.of(2, 4),
                        UniformFloat.of(5f, 15f),
                        BlockStateProvider.simple(Blocks.WATER),
                        new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                                .add(Blocks.MUD.defaultBlockState(), 1)
                                .add(Blocks.CLAY.defaultBlockState(), 1)
                                .add(Blocks.DIRT.defaultBlockState(), 2)
                        ),
                        new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                                .add(Blocks.MUD.defaultBlockState(), 2)
                                .add(Blocks.SAND.defaultBlockState(), 1)
                                .add(Blocks.GRASS_BLOCK.defaultBlockState(), 4)
                        ),
                        SimpleWeightedRandomList.<Holder<PlacedFeature>>builder()
                                .add(PlacementUtils.inlinePlaced(configuredFeatures.getOrThrow(VegetationFeatures.PATCH_SUGAR_CANE),
                                        PlacementUtils.isEmpty(),
                                        PlacementUtils.filteredByBlockSurvival(Blocks.SUGAR_CANE)
                                ), 1)//25%
                                .add(FeaturePlacementUtils.wrapHolder(configuredFeatures.getOrThrow(ModConfiguredFeatures.NONE)), 4)
                                .add(PlacementUtils.inlinePlaced(Holder.direct(new ConfiguredFeature<>(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder().add(Blocks.FERN.defaultBlockState(), 9).add(Blocks.LARGE_FERN.defaultBlockState(), 1).build())))),
                                                        PlacementUtils.isEmpty(),
                                                        PlacementUtils.filteredByBlockSurvival(Blocks.FERN)),
                                                2)
                                .build()
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
                        RandomSpreadType.TRIANGULAR,
                        14357892
                )
        ));

        context.register(OASIS_SET, new StructureSet(
                List.of(StructureSet.entry(structures.getOrThrow(OASIS_STRUCTURE))),
                new RandomSpreadStructurePlacement(
                        32,
                        8,
                        RandomSpreadType.LINEAR,
                        58219438
                )
        ));
    }
}
