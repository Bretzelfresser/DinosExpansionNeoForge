package com.bretzelfresser.dinosexpansion.common.init;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import javax.annotation.Nullable;

public class DinoBiomes {

    public static Biome makeAncientGlacialTundra(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);


        var spawnSettings = new MobSpawnSettings.Builder();

        return biome(true, 0f, 0.5F, spawnSettings, genSettingsBuilder, null);
    }

    public static Biome makePetrifiedBadlands(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);


        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder().build();

        BiomeSpecialEffects specialEffects = new BiomeSpecialEffects.Builder()
                .waterColor(4159204)
                .waterFogColor(329011)
                .fogColor(12638463)
                .skyColor(calculateSkyColor(2.0F))
                .foliageColorOverride(10387789)
                .grassColorOverride(9470285)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BADLANDS))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(specialEffects)
                .mobSpawnSettings(spawnSettings)
                .generationSettings(genSettingsBuilder.build())
                .build();
    }

    public static Biome makeBoneDesert(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);

        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.PREHISTORIC_DESERT_VEGETATION_PLACED);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, ModPlacedFeatures.PREHISTORIC_FOSSIL_PLACED);

        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder().build();

        BiomeSpecialEffects specialEffects = new BiomeSpecialEffects.Builder()
                .waterColor(4159204)
                .waterFogColor(329011)
                .fogColor(12638463)
                .skyColor(calculateSkyColor(2.0F))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(specialEffects)
                .mobSpawnSettings(spawnSettings)
                .generationSettings(genSettingsBuilder.build())
                .build();
    }

    public static Biome makeGeyserValley(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);

        genSettingsBuilder.addFeature(GenerationStep.Decoration.LAKES, ModPlacedFeatures.GEYSER_HOT_SPRING_PLACED);

        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder().build();

        BiomeSpecialEffects specialEffects = new BiomeSpecialEffects.Builder()
                .waterColor(4159204)
                .waterFogColor(329011)
                .fogColor(12638463)
                .skyColor(calculateSkyColor(0.8F))
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

    public static Biome makeFoggySwamp(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);

        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_SWAMP);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_SWAMP);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.BROWN_MUSHROOM_SWAMP);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.RED_MUSHROOM_SWAMP);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE_SWAMP);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_NORMAL);
        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_DEAD_BUSH);

        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder().build();

        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SWAMP);
        BiomeSpecialEffects specialEffects = new BiomeSpecialEffects.Builder()
                .waterColor(6388580)
                .waterFogColor(2302743)
                .fogColor(12638463)
                .skyColor(calculateSkyColor(0.8F))
                .backgroundMusic(music)
                .foliageColorOverride(6975545)
                .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP)
                .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
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

    public static Biome makeRedwoodForest(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup){
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);

        genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.MEGA_PREHISTORIC_REDWOOD_PLACED);

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

        BiomeDefaultFeatures.addMangroveSwampVegetation(genSettingsBuilder);

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
        //genSettingsBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, ModPlacedFeatures.MEGA_PREHISTORIC_REDWOOD_PLACED);
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


    public static Biome makePrehistoricFrozenOcean(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup) {
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);
        var spawnSettings = new MobSpawnSettings.Builder();
        return biome(true, 0F, 0.5F, spawnSettings, genSettingsBuilder, null);
    }

    public static Biome makeDeepPrehistoricFrozenOcean(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup) {
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);
        var spawnSettings = new MobSpawnSettings.Builder();
        return biome(true, 0F, 0.5F, spawnSettings, genSettingsBuilder, null);
    }

    public static Biome makePrehistoricOcean(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup) {
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);
        var spawnSettings = new MobSpawnSettings.Builder();
        return biome(true, 0.5F, 0.5F, spawnSettings, genSettingsBuilder, null);
    }

    public static Biome makeDeepPrehistoricOcean(HolderGetter<PlacedFeature> placedFeatureLookup, HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup) {
        var genSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup);
        var spawnSettings = new MobSpawnSettings.Builder();
        return biome(true, 0.5F, 0.5F, spawnSettings, genSettingsBuilder, null);
    }

    public static int calculateSkyColor(float temperature) {
        float $$1 = temperature / 3.0F;
        $$1 = Mth.clamp($$1, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - $$1 * 0.05F, 0.5F + $$1 * 0.1F, 1.0F);
    }

    public static Biome biome(boolean hasPercipitation, float temperature, float downfall, MobSpawnSettings.Builder mobSpawnSettings, BiomeGenerationSettings.Builder generationSettings, @Nullable Music backgroundMusic) {
        return biome(hasPercipitation, temperature, downfall, 4159204, 329011, (Integer)null, (Integer)null, mobSpawnSettings, generationSettings, backgroundMusic);
    }

    public static Biome biome(boolean hasPrecipitation, float temperature, float downfall, int waterColor, int waterFogColor, @Nullable Integer grassColorOverride, @Nullable Integer foliageColorOverride, MobSpawnSettings.Builder mobSpawnSettings, BiomeGenerationSettings.Builder generationSettings, @Nullable Music backgroundMusic) {
        BiomeSpecialEffects.Builder biomespecialeffects$builder = (new BiomeSpecialEffects.Builder()).waterColor(waterColor).waterFogColor(waterFogColor).fogColor(12638463).skyColor(calculateSkyColor(temperature)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(backgroundMusic);
        if (grassColorOverride != null) {
            biomespecialeffects$builder.grassColorOverride(grassColorOverride);
        }

        if (foliageColorOverride != null) {
            biomespecialeffects$builder.foliageColorOverride(foliageColorOverride);
        }

        return (new Biome.BiomeBuilder()).hasPrecipitation(hasPrecipitation).temperature(temperature).downfall(downfall).specialEffects(biomespecialeffects$builder.build()).mobSpawnSettings(mobSpawnSettings.build()).generationSettings(generationSettings.build()).build();
    }
}
