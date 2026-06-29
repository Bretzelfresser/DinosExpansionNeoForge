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
    public static final ResourceKey<DensityFunction> CLIFF_VARIANCE = create("cliff_variance");
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

        var refCliffVariance = context.register(CLIFF_VARIANCE, DensityFunctions.noise(noiseLookup.getOrThrow(ModNoiseParameters.BEACH_CLIFF_VARIANCE), 1f, 0f));
        var cliffVariance = wrap(refCliffVariance);

        // Spline for C = -0.05 point:
        // If variance is low (< -0.2f), we want beach profile (0.0f).
        // If variance is high (> 0.2f), we want cliff profile (-0.31f).
        var pointAtNeg05 = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(refCliffVariance))
                .addPoint(-1f, -0.01f, 0.0f)
                .addPoint(1f, -0.45f, 0.0f)
                .build();




        var riverNoise = DensityFunctions.noise(noiseLookup.getOrThrow(ModNoiseParameters.RIVER_NOISE), 1, 0);

        float erosionMin = 0.04f;
        float erosionMax = 0.52f;

        var riverNegativeCliffSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(riverNoise)))
                .addPoint(-1f, erosionMin, 0.0f)
                .addPoint(-.2f, erosionMin, 0.0f)
                .addPoint(0f, -.2f, 0.0f)
                .addPoint(0.2f, erosionMin, 0.0f)
                .addPoint(1f, erosionMin, 0.0f)
                .build();

        var riverPositiveCliffSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(riverNoise)))
                .addPoint(-1f, erosionMax, 0.0f)
                .addPoint(-.2f, erosionMax, 0.0f)
                .addPoint(0f, -.2f, 0.0f)
                .addPoint(.2f, erosionMax, 0.0f)
                .addPoint(1f, erosionMax, 0.0f)
                .build();

        // Spline for C = 0.05 point:
        // If variance is low (< -0.2f), we want beach profile (0.04f).
        // If variance is high (> 0.2f), we want cliff profile (0.52f).
        var pointAtPos05 = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(refCliffVariance))
                .addPoint(-1f, riverNegativeCliffSpline)
                .addPoint(-.4f, riverNegativeCliffSpline)
                .addPoint(1f, riverPositiveCliffSpline)
                .build();

        // depthSpline = 0.0 at Y = 63 (sea level).
        // Since Y ranges from 3 to 156, the fraction at Y = 63 is (63-3)/(156-3) = 60/153 = 20/51.
        // Choosing minVal = 1.0d and maxVal = -1.55d results in 1.0 + (20/51) * (-2.55) = 1.0 - 1.0 = 0.0.
        var depthSpline = DensityFunctions.yClampedGradient(3, 156, 1d, -1.55d);

        var surfaceContinentalSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(refContinents))
                .addPoint(-1.0f, -1f, 0.0f)     // Deep Ocean (Y ~ 3)
                .addPoint(-0.2f, -0.45f, 0.0f)  // Ocean rising (Y ~ 26)
                .addPoint(-0.05f, pointAtNeg05)   // Dynamic beach starting point
                .addPoint(0.05f, pointAtPos05)    // Dynamic beach ending point
                .addPoint(0.2f, pointAtPos05)    // Steep cliff top (Y = 110)
                .addPoint(0.7f, 0.65f, 0.0f)     // Wide flat plateau/plains (Y = 113)
                .addPoint(1.0f, 1f, 0.0f)      // Towering prehistoric mountains (Y ~ 233)
                .build();

        var surfaceContinentalSplineFunction = DensityFunctions.spline(surfaceContinentalSpline);

        //var surfaceSpline = DensityFunctions.add(surfaceContinentalSplineFunction, TerrainFeatures.makeRiver(noiseLookup, densityLookup));


        context.register(SURFACE_DENSITY_AQUAFIER, DensityFunctions.add(DensityFunctions.constant(0.3f), DensityFunctions.add(depthSpline, surfaceContinentalSplineFunction)));
        var surfaceWithoutCaves = DensityFunctions.add(depthSpline, surfaceContinentalSplineFunction);
        var caves = makeCaves(noiseLookup, densityLookup);
        context.register(FINAL_DENSITY, DensityFunctions.min(surfaceWithoutCaves, caves));
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
        caves = DensityFunctions.min(caves, TerrainFeatures.makeNoodleCave(noiseGetter, densityFunctionLookup));
        return DensityFunctions.rangeChoice(caves, -20, 0, caves, DensityFunctions.constant(10e5));
    }


    private static DensityFunction wrap(Holder<DensityFunction> holder) {
        return new DensityFunctions.HolderHolder(holder);
    }
}
