package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class ModLevelStems {
    public static final ResourceKey<LevelStem> DINO_LEVEL_STEM_KEY = ResourceKey.create(
            Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "dino_dimension")
    );

    public static final ResourceKey<Level> DINO_LEVEL_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "dino_dimension")
    );

    public static void bootstrap(BootstrapContext<LevelStem> context) {
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);
        HolderGetter<MultiNoiseBiomeSourceParameterList> biomeParams = context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST);

        context.register(DINO_LEVEL_STEM_KEY, new LevelStem(
                dimTypes.getOrThrow(ModDimensionTypes.DINO_DIM_TYPE_KEY),
                new NoiseBasedChunkGenerator(new FixedBiomeSource(biomes.getOrThrow(Biomes.PLAINS)), noiseSettings.getOrThrow(ModNoiseGeneratorSettings.DINO_NOISE_SETTINGS_KEY))
        ));
    }
}
