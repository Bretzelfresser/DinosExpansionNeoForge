package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class ModNoiseParameters {

    public static final ResourceKey<NormalNoise.NoiseParameters> DINO_NOISE_KEY = create("dino_noise");


    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(DINO_NOISE_KEY, new NormalNoise.NoiseParameters(-4, 1.0, 1.0, 1.0));
    }


    public static ResourceKey<NormalNoise.NoiseParameters> create(String name){
        return ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }
}
