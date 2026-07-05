package com.bretzelfresser.dinosexpansion.common.init;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class DinoBiomes {


    public static Biome makeRedwoodForest(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);

        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder().build();


        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JUNGLE);
        BiomeSpecialEffects specialEffects = new BiomeSpecialEffects.Builder()
                .waterColor(3832426)
                .waterFogColor(5077600)
                .fogColor(12638463)
                .skyColor(calculateSkyColor(0.8F))
                .backgroundMusic(music)
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

    public static Biome makePrimordialJungle(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);

        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.GIANT_JUNGLE_TREE);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.SMALL_JUNGLE_TREE);

        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder().build();


        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JUNGLE);
        BiomeSpecialEffects specialEffects = new BiomeSpecialEffects.Builder()
                .waterColor(3832426)
                .waterFogColor(5077600)
                .fogColor(12638463)
                .skyColor(calculateSkyColor(0.8F))
                .backgroundMusic(music)
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

    public static Biome makeDeltaMangrove(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);


        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder().build();


        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SWAMP);
        BiomeSpecialEffects specialEffects = new BiomeSpecialEffects.Builder()
                .waterColor(4159204)
                .waterFogColor(329011)
                .fogColor(12638463)
                .skyColor(7907327)
                .skyColor(calculateSkyColor(0.8F))
                .backgroundMusic(music)
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

    public static Biome makeFernPlains(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup) {
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);

        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.FERN_PLAINS_FERN);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PREHISTORIC_PINE_PLACED);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.MEGA_PREHISTORIC_REDWOOD_PLACED);
        //genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.TEST_TREE_PLACED);

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

    public static Biome makePrehistoricCoast(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);


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


    public static int calculateSkyColor(float temperature) {
        float $$1 = temperature / 3.0F;
        $$1 = Mth.clamp($$1, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - $$1 * 0.05F, 0.5F + $$1 * 0.1F, 1.0F);
    }
}
