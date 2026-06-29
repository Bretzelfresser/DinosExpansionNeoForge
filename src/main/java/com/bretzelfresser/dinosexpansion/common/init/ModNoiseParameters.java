package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class ModNoiseParameters {

    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENTS = create("continents");
    public static final ResourceKey<NormalNoise.NoiseParameters> DEEP_UNDERGROUND_CAVES = create("deep_underground_caves");
    public static final ResourceKey<NormalNoise.NoiseParameters> DEEP_UNDERGROUND_CAVE_SIZE = create("deep_underground_cave_size");
    public static final ResourceKey<NormalNoise.NoiseParameters> UNDERGROUND_CAVES = create("underground_caves");
    public static final ResourceKey<NormalNoise.NoiseParameters> UNDERGROUND_CAVE_SIZE = create("underground_cave_size");
    public static final ResourceKey<NormalNoise.NoiseParameters> NOODLE_NOISE = create("noodle_noise");
    public static final ResourceKey<NormalNoise.NoiseParameters> NOODLE_THICKNESS_NOISE = create("noodle_thickness_noise");


    public static final ResourceKey<NormalNoise.NoiseParameters> RIVER_NOISE = create("river_noise");
    public static final ResourceKey<NormalNoise.NoiseParameters> RIVER_THICKNESS_NOISE = create("river_thickness_noise");
    public static final ResourceKey<NormalNoise.NoiseParameters> RIVER_DEPTH = create("river_depth");
    public static final ResourceKey<NormalNoise.NoiseParameters> RIVER_WARP_X = create("river_warp_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> RIVER_WARP_Z = create("river_warp_z");
    public static final ResourceKey<NormalNoise.NoiseParameters> NOODLE_CAVE_A = create("noodle_cave_a");
    public static final ResourceKey<NormalNoise.NoiseParameters> NOODLE_CAVE_B = create("noodle_cave_b");
    public static final ResourceKey<NormalNoise.NoiseParameters> CAVE_ENTRANCE_NOISE = create("cave_entrance_noise");
    public static final ResourceKey<NormalNoise.NoiseParameters> BEACH_CLIFF_VARIANCE = create("beach_cliff_variance");


    public static final ResourceKey<NormalNoise.NoiseParameters> DEEPSLATE_NOISE = create("deepslate_noise");

    public static final ResourceKey<NormalNoise.NoiseParameters> DINO_NOISE_KEY = create("dino_noise");


    public static void bootstrap(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(CONTINENTS, new NormalNoise.NoiseParameters(-10, 1.0, 1.0, 0, 0.75, 0, 0.25));
        context.register(DEEP_UNDERGROUND_CAVES, new NormalNoise.NoiseParameters(-6, 1.0, 1.0, 0, 0.2, 0, 0.25));
        context.register(DEEP_UNDERGROUND_CAVE_SIZE, new NormalNoise.NoiseParameters(-5, 1.0 ));
        context.register(UNDERGROUND_CAVES, new NormalNoise.NoiseParameters(-5, 1.0, 1));
        context.register(UNDERGROUND_CAVE_SIZE, new NormalNoise.NoiseParameters(-6, 1.0, 1));
        context.register(NOODLE_NOISE, new NormalNoise.NoiseParameters(-5, 1.0, 1));
        context.register(NOODLE_THICKNESS_NOISE, new NormalNoise.NoiseParameters(-5, 1.0, 1, 1));
        context.register(DEEPSLATE_NOISE, new NormalNoise.NoiseParameters(-2, 1.0, 1));


        context.register(RIVER_NOISE, new NormalNoise.NoiseParameters(-7, 1.0, 1));
        context.register(RIVER_DEPTH, new NormalNoise.NoiseParameters(-5, 1.0, 1));
        context.register(RIVER_THICKNESS_NOISE, new NormalNoise.NoiseParameters(-8, 1.0, 1));
        context.register(RIVER_WARP_X, new NormalNoise.NoiseParameters(-5, 1.0, 1.0));
        context.register(RIVER_WARP_Z, new NormalNoise.NoiseParameters(-5, 1.0, 1.0));
        context.register(NOODLE_CAVE_A, new NormalNoise.NoiseParameters(-6, 1.0, 1.0));
        context.register(NOODLE_CAVE_B, new NormalNoise.NoiseParameters(-6, 1.0, 1.0));
        context.register(CAVE_ENTRANCE_NOISE, new NormalNoise.NoiseParameters(-7, 1.0));
        context.register(BEACH_CLIFF_VARIANCE, new NormalNoise.NoiseParameters(-8, 1.0, 1.0));
    }


    public static ResourceKey<NormalNoise.NoiseParameters> create(String name){
        return ResourceKey.create(Registries.NOISE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }
}
