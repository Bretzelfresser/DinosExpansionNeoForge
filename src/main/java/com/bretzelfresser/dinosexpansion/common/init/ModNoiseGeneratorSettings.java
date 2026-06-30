package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.server.SurfaceRuleHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;

import java.util.List;

public class ModNoiseGeneratorSettings {
    public static final ResourceKey<NoiseGeneratorSettings> DINO_NOISE_SETTINGS_KEY = ResourceKey.create(
            Registries.NOISE_SETTINGS,
            ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "dino_dimension")
    );

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        var densityLookup = context.lookup(Registries.DENSITY_FUNCTION);
        var noiseLookup = context.lookup(Registries.NOISE);

        NoiseSettings noiseSettings = new NoiseSettings(-64, 384, 1, 2);
        DensityFunction zero = DensityFunctions.zero();

        NoiseRouter noiseRouter = new NoiseRouter(
                zero,
                zero,
                zero,
                zero,
                zero,
                zero,
                new DensityFunctions.HolderHolder(densityLookup.getOrThrow(ModDensityFunctions.CONTINENTS)),
                new DensityFunctions.HolderHolder(densityLookup.getOrThrow(ModDensityFunctions.EROSION)),
                new DensityFunctions.HolderHolder(densityLookup.getOrThrow(ModDensityFunctions.DEPTH)),//depth to stick biomes within depth ranges
                DensityFunctions.noise(noiseLookup.getOrThrow(ModNoiseParameters.RIVER_NOISE), 1, 0),//ridges will be misused for river detection
                new DensityFunctions.HolderHolder(densityLookup.getOrThrow(ModDensityFunctions.SURFACE_DENSITY_AQUAFIER)),
                new DensityFunctions.HolderHolder(densityLookup.getOrThrow(ModDensityFunctions.FINAL_DENSITY)),
                zero,
                zero,
                zero
        );

        context.register(DINO_NOISE_SETTINGS_KEY, new NoiseGeneratorSettings(
                noiseSettings,
                Blocks.STONE.defaultBlockState(), // defaultBlock
                Blocks.WATER.defaultBlockState(), // defaultFluid
                noiseRouter,
                SurfaceRuleHelper.generateDinoSurfaceRules(),
                List.of(), // spawnTarget
                63, // seaLevel
                false, // disableMobGeneration
                true, // aquifersEnabled
                false, // oreVeinsEnabled
                false // useLegacyRandomSource
        ));
    }
}
