package com.bretzelfresser.dinosexpansion.common.init;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class TerrainFeatures {

    public static DensityFunction makeRiver(HolderGetter<NormalNoise.NoiseParameters> noiseGetter, HolderGetter<DensityFunction> densityFunctionLookup) {
        // 1. Get the base warp noises (range from -1.0 to 1.0)
        var warpXRaw = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.RIVER_WARP_X), 1, 0);
        var warpZRaw = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.RIVER_WARP_Z), 1, 0);

        // 2. Amplify the warp intensity (e.g. 25.0 blocks of shift)
        // Higher values make the turns sharper and more chaotic; lower values keep it smoother.
        var warpX = DensityFunctions.mul(warpXRaw, DensityFunctions.constant(25.0d));
        var warpZ = DensityFunctions.mul(warpZRaw, DensityFunctions.constant(25.0d));

        // 3. Evaluate the river noise using the warped coordinates
        // shiftedNoise2d shifts X by warpX and Z by warpZ
        var riverNoise = DensityFunctions.shiftedNoise2d(
                warpX,
                warpZ,
                0.005d, // xzScale of the river noise (controls frequency/size of bends)
                noiseGetter.getOrThrow(ModNoiseParameters.RIVER_NOISE)
        );

        var riverThicknessNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.RIVER_THICKNESS_NOISE), 1, 0);
        var riverDepthNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.RIVER_DEPTH), 1, 0);

        // Modulate river width
        var widthModulator = DensityFunctions.add(DensityFunctions.constant(1.5d), riverThicknessNoise);
        var adjustedThickness = DensityFunctions.mul(riverNoise.abs(), widthModulator);

        // Sub-spline to vary depth depending on riverDepthNoise
        var depthModulationSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(riverDepthNoise)))
                .addPoint(-1.0f, -0.3f, 0.0f) // deep parts
                .addPoint(1.0f, -0.1f, 0.0f)  // shallow parts
                .build();

        // Main spline to model the river cross-section profile
        var riverSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(adjustedThickness)))
                .addPoint(0.0f, depthModulationSpline)  // center of the river (carved deepest)
                .addPoint(0.08f, -0.1f, 0.0f)           // river bed slope
                .addPoint(0.15f, 0.0f, 0.0f)            // river banks (no carving)
                .addPoint(1.0f, 0.0f, 0.0f)             // outside river
                .build();

        return DensityFunctions.spline(riverSpline);
    }
}
