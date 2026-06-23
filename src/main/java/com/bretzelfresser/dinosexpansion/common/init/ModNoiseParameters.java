package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class ModNoiseParameters {

    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENTS = create("continents");
    public static final ResourceKey<NormalNoise.NoiseParameters> DEEP_UNDERGROUND_CAVES = create("deep_underground_caves");
    public static final ResourceKey<NormalNoise.NoiseParameters> DEEP_UNDERGROUND_CAVE_SIZE = create("deep_underground_cave_size");
    public static final ResourceKey<NormalNoise.NoiseParameters> DINO_NOISE_KEY = create("dino_noise");


    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(CONTINENTS, new NormalNoise.NoiseParameters(-10, 1.0, 1.0, 0, 0.75, 0, 0.25));
        context.register(DEEP_UNDERGROUND_CAVES, new NormalNoise.NoiseParameters(-6, 1.0, 1.0, 0, 0.2, 0, 0.25));
        context.register(DEEP_UNDERGROUND_CAVE_SIZE, new NormalNoise.NoiseParameters(-5, 1.0 ));
    }


    public static ResourceKey<NormalNoise.NoiseParameters> create(String name){
        return ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }
}
