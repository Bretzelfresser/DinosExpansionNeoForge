package com.bretzelfresser.dinosexpansion.common.init;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class DinoBiomes {


    public static Biome makeFernPlains(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup) {
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);

        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.FERN_PLAINS_FERN);

        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder().build();




        BiomeSpecialEffects specialEffects = new BiomeSpecialEffects.Builder()
                .waterColor(4159204)
                .waterFogColor(329011)
                .fogColor(12638463)
                .skyColor(7907327)
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.8F)
                .downfall(0.4F)
                .specialEffects(specialEffects)
                .mobSpawnSettings(spawnSettings)
                .generationSettings(genSettingsBuilder.build())
                .build();
    }
}
