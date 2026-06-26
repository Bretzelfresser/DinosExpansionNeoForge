package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class ModDensityFunctions {

    public static final ResourceKey<DensityFunction> ZERO_DENSITY_KEY = create("zero");
    public static final ResourceKey<DensityFunction> CONTINENTS = create("continents");
    public static final ResourceKey<DensityFunction> FINAL_DENSITY = create("final_density");
    public static final ResourceKey<DensityFunction> SURFACE_DENSITY_AQUAFIER = create("surface_density_aquafier");


    public static ResourceKey<DensityFunction> create(String name) {
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }

    public static void bootstrap(BootstrapContext<DensityFunction> context) {

        var noiseLookup = context.lookup(Registries.NOISE);
        var densityLookup = context.lookup(Registries.DENSITY_FUNCTION);

        context.register(ZERO_DENSITY_KEY, DensityFunctions.zero());

        var refContinents = context.register(CONTINENTS, DensityFunctions.noise(noiseLookup.getOrThrow(ModNoiseParameters.CONTINENTS), 1f, 0f));
        var continents = wrap(refContinents);

        //make it got a bit bigger so it doesnt completely crash when a noise is slightly over 1
        var depthSpline = DensityFunctions.spline(CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(DensityFunctions.yClampedGradient(-36, 156, -1.2d, 1.2d))))
                .addPoint(-1.2f, 1.2f, -1f)
                .addPoint(-1f, 1f, 0f)
                .addPoint(0f, 0f, -1f)
                .addPoint(1f, -1f, 0f)
                .addPoint(1.2f, -1.2f, -1f)
                .build());

        var surfaceContinentalSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(refContinents))
                .addPoint(-1f, -1f, 0)
                .addPoint(-0.1f, -0.7f, 0)
                .addPoint(0.1f, 0.7f, 0)
                .addPoint(1f, 1f, 0f)
                .build();

        var surfaceContinentalSplineFunction = DensityFunctions.spline(surfaceContinentalSpline);

        var surfaceSpline = DensityFunctions.add(surfaceContinentalSplineFunction, makeRiver(noiseLookup, densityLookup));


        context.register(SURFACE_DENSITY_AQUAFIER, DensityFunctions.add(DensityFunctions.constant(0.3f), DensityFunctions.add(depthSpline, surfaceSpline)));
        var surfaceWithoutCaves = DensityFunctions.add(depthSpline, surfaceSpline);
        var caves = makeCaves(noiseLookup, densityLookup);
        context.register(FINAL_DENSITY, DensityFunctions.min(surfaceWithoutCaves, caves));
    }


    private static DensityFunction makeRiver(HolderGetter<NormalNoise.NoiseParameters> noiseGetter, HolderGetter<DensityFunction> densityFunctionLookup) {
        var riverNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.RIVER_NOISE), 1, 0);
        var riverThicknessNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.RIVER_THICKNESS_NOISE), 1, 0);
        var riverDepthNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.RIVER_DEPTH), 1, 0);

        // Combine river path noise and thickness noise
        var riverThicknessAdded = DensityFunctions.add(riverNoise.abs(), riverThicknessNoise.abs());

        // Sub-spline to vary depth depending on riverDepthNoise
        var depthModulationSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(riverDepthNoise)))
                .addPoint(-1.0f, -0.3f, 0.0f) // deep parts
                .addPoint(1.0f, -0.1f, 0.0f)  // shallow parts
                .build();

        // Main spline to model the river cross-section profile
        var riverSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(riverThicknessAdded)))
                .addPoint(0.0f, depthModulationSpline)  // center of the river (carved deepest)
                .addPoint(0.08f, -0.1f, 0.0f)           // river bed slope
                .addPoint(0.15f, 0.0f, 0.0f)            // river banks (no carving)
                .addPoint(1.0f, 0.0f, 0.0f)             // outside river
                .build();

        return DensityFunctions.spline(riverSpline);
    }

    private static DensityFunction makeCaves(HolderGetter<NormalNoise.NoiseParameters> noiseGetter, HolderGetter<DensityFunction> densityFunctionLookup) {
        var undergroundCaveNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.DEEP_UNDERGROUND_CAVES), 1, 1);
        //controls the size of the cave
        var undergroundCaveSizeNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.DEEP_UNDERGROUND_CAVE_SIZE), 1, 1);
        var deepUndergroundCaveSizeSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(undergroundCaveSizeNoise)))
                .addPoint(-1f, -1f, 0)
                .addPoint(1f, 0f, 0)
                .build();


        var undergroundCaveSpline = DensityFunctions.spline(CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(undergroundCaveNoise)))
                .addPoint(-1f, deepUndergroundCaveSizeSpline)
                .addPoint(1f, 1f, 0)
                .build());


        var depthDeepUndergroundCavesSpline = DensityFunctions.spline(CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(DensityFunctions.yClampedGradient(-63, 0, -1d, 1d))))
                .addPoint(-1f, 0.5f, -2f)
                .addPoint(-0.3f, 0f, 0f)
                .addPoint(1f, 1.1f, .6f)
                .build());

        var caves = DensityFunctions.add(depthDeepUndergroundCavesSpline, undergroundCaveSpline);
        caves = DensityFunctions.min(caves, noodlesAndCaves(noiseGetter, densityFunctionLookup));
        return DensityFunctions.rangeChoice(caves, -20, 0, caves, DensityFunctions.constant(10e5));


    }

    private static DensityFunction noodlesAndCaves(HolderGetter<NormalNoise.NoiseParameters> noiseGetter, HolderGetter<DensityFunction> densityFunctionLookup) {
        float maxNoodleSize = 0.1f;

        var noodleGenerationSpline = DensityFunctions.spline(CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(DensityFunctions.yClampedGradient(-60, 100, -1, 1))))
                .addPoint(-1f, maxNoodleSize * .9f, 0)
                .addPoint(0, 0f, 0f)
                .addPoint(1f, maxNoodleSize * 1.6f, 0f)
                .build());

        var noodleNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.NOODLE_NOISE), 1, 1);

        var noodleSizeSpline = DensityFunctions.spline(CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.NOODLE_THICKNESS_NOISE)))))
                .addPoint(-1f, 0f, 0)
                .addPoint(0, maxNoodleSize, 0f)
                .addPoint(1f, 0f, 0f)
                .build());

        var noodleSize = DensityFunctions.rangeChoice(DensityFunctions.add(noodleNoise, DensityFunctions.add(DensityFunctions.mul(DensityFunctions.constant(-1), noodleSizeSpline), noodleGenerationSpline)), -1.2 - maxNoodleSize * 1.2f, -0.4f, DensityFunctions.constant(-1f), DensityFunctions.constant(1f));

        return DensityFunctions.add(noodleSize, noodleGenerationSpline);

    }


    private static DensityFunction wrap(Holder<DensityFunction> holder) {
        return new DensityFunctions.HolderHolder(holder);
    }
}
