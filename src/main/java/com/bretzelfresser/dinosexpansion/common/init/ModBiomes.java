package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModBiomes {
    public static final ResourceKey<Biome> DINO_DEFAULT_KEY = create("dino_default");
    public static final ResourceKey<Biome> FERN_PLAINS = create("fern_plains");

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> configuredCarvers = context.lookup(Registries.CONFIGURED_CARVER);

        BiomeGenerationSettings genSettings = new BiomeGenerationSettings.Builder(placedFeatures, configuredCarvers).build();
        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder().build();

        BiomeSpecialEffects specialEffects = new BiomeSpecialEffects.Builder()
                .waterColor(4159204)
                .waterFogColor(329011)
                .fogColor(12638463)
                .skyColor(7907327)
                .build();

        Biome biome = new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.8F)
                .downfall(0.4F)
                .specialEffects(specialEffects)
                .mobSpawnSettings(spawnSettings)
                .generationSettings(genSettings)
                .build();

        context.register(DINO_DEFAULT_KEY, biome);
        context.register(FERN_PLAINS, DinoBiomes.makeFernPlains(placedFeatures, configuredCarvers));
    }


    public static ResourceKey<Biome> create(String name) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }
}
