package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class ModPlacedFeatures {


    public static final ResourceKey<PlacedFeature> FERN_PLAINS_FERN = create("fern_plains_fern");

    public static void generate(BootstrapContext<PlacedFeature> ctx){
        var configuredFeatureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);

        PlacementUtils.register(ctx, FERN_PLAINS_FERN, configuredFeatureLookup.getOrThrow(ModConfiguredFeatures.FERN_PATCH),
                RarityFilter.onAverageOnceEvery(1),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome()
                );


    }

    public static ResourceKey<PlacedFeature> create(String name){
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }
}
