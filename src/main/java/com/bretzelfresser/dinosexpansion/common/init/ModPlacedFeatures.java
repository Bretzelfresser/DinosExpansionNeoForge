package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.*;

public class ModPlacedFeatures {


    public static final ResourceKey<PlacedFeature> GEYSER_HOT_SPRING_PLACED = create("geyser_hot_spring_placed");
    public static final ResourceKey<PlacedFeature> FERN_PLAINS_FERN = create("fern_plains_fern");
    public static final ResourceKey<PlacedFeature> PREHISTORIC_PINE_PLACED = create("prehistoric_pine_placed");
    public static final ResourceKey<PlacedFeature> MEGA_PREHISTORIC_REDWOOD_PLACED = create("mega_prehistoric_redwood_placed");
    public static final ResourceKey<PlacedFeature> GIANT_JUNGLE_TREE = create("giant_jungle_tree");
    public static final ResourceKey<PlacedFeature> SMALL_JUNGLE_TREE = create("small_jungle_tree");

    public static final ResourceKey<PlacedFeature> TEST_TREE_PLACED = create("test_tree_placed");

    public static void generate(BootstrapContext<PlacedFeature> ctx){
        var configuredFeatureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);

        //edge detection filter in 3D, basically preventing trees from spawning at steep surfaces
        var flatSurfacePredicate = BlockPredicate.allOf(
                BlockPredicate.solid(new Vec3i(0, -1, 0)),
                BlockPredicate.solid(new Vec3i(1, -1, 0)),
                BlockPredicate.solid(new Vec3i(-1, -1, 0)),
                BlockPredicate.solid(new Vec3i(0, -1, 1)),
                BlockPredicate.solid(new Vec3i(0, -1, -1))
        );
        var flatSurfaceFilter = BlockPredicateFilter.forPredicate(flatSurfacePredicate);

        PlacementUtils.register(ctx, GEYSER_HOT_SPRING_PLACED, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.GEYSER_HOT_SPRING),
                RarityFilter.onAverageOnceEvery(2),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        );

        PlacementUtils.register(ctx, FERN_PLAINS_FERN, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.FERN_PATCH),
                RarityFilter.onAverageOnceEvery(1),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome()
                );

        PlacementUtils.register(ctx, PREHISTORIC_PINE_PLACED, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.TALL_PREHISTORIC_PINE),
                RarityFilter.onAverageOnceEvery(6),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING),
                BiomeFilter.biome()
        );

        PlacementUtils.register(ctx, MEGA_PREHISTORIC_REDWOOD_PLACED, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.MEGA_PREHISTORIC_REDWOOD),
                PlacementUtils.countExtra(5, 0.05f, 4),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                flatSurfaceFilter,
                PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING),
                BiomeFilter.biome()
        );

        PlacementUtils.register(ctx, TEST_TREE_PLACED, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.TEST_TREE),
                RarityFilter.onAverageOnceEvery(20),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING),
                BiomeFilter.biome()
        );

        PlacementUtils.register(ctx, GIANT_JUNGLE_TREE, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.GIANT_JUNGLE_TREE),
                RarityFilter.onAverageOnceEvery(20),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                flatSurfaceFilter,
                PlacementUtils.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING),
                BiomeFilter.biome()
        );
        PlacementUtils.register(ctx, SMALL_JUNGLE_TREE, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.SMALL_JUNGLE_TREE),
                RarityFilter.onAverageOnceEvery(3),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                flatSurfaceFilter,
                PlacementUtils.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING),
                BiomeFilter.biome()
        );
    }

    public static ResourceKey<PlacedFeature> create(String name){
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }
}
