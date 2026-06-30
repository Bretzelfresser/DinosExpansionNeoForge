package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class ModPlacedFeatures {


    public static final ResourceKey<PlacedFeature> FERN_PLAINS_FERN = create("fern_plains_fern");
    public static final ResourceKey<PlacedFeature> TALL_PREHISTORIC_PINE_PLACED = create("tall_prehistoric_pine_placed");
    public static final ResourceKey<PlacedFeature> MEGA_PREHISTORIC_REDWOOD_PLACED = create("mega_prehistoric_redwood_placed");

    public static void generate(BootstrapContext<PlacedFeature> ctx){
        var configuredFeatureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);

        PlacementUtils.register(ctx, FERN_PLAINS_FERN, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.FERN_PATCH),
                RarityFilter.onAverageOnceEvery(1),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome()
                );

        PlacementUtils.register(ctx, TALL_PREHISTORIC_PINE_PLACED, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.TALL_PREHISTORIC_PINE),
                PlacementUtils.countExtra(8, 0.1f, 1),
                net.minecraft.world.level.levelgen.placement.InSquarePlacement.spread(),
                net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING),
                BiomeFilter.biome()
        );

        PlacementUtils.register(ctx, MEGA_PREHISTORIC_REDWOOD_PLACED, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.MEGA_PREHISTORIC_REDWOOD),
                PlacementUtils.countExtra(2, 0.1f, 1),
                net.minecraft.world.level.levelgen.placement.InSquarePlacement.spread(),
                net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP,
                PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING),
                BiomeFilter.biome()
        );
    }

    public static ResourceKey<PlacedFeature> create(String name){
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }
}
