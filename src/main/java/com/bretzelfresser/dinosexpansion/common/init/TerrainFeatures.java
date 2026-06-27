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
        var warpX = DensityFunctions.mul(warpXRaw, DensityFunctions.constant(6.0d));
        var warpZ = DensityFunctions.mul(warpZRaw, DensityFunctions.constant(6.0d));

        // 3. Evaluate the river noise using the warped coordinates
        // shiftedNoise2d shifts X by warpX and Z by warpZ
        var riverNoise = DensityFunctions.shiftedNoise2d(
                warpX,
                warpZ,
                1d, // xzScale of the river noise (controls frequency/size of bends)
                noiseGetter.getOrThrow(ModNoiseParameters.RIVER_NOISE)
        );

        var riverThicknessNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.RIVER_THICKNESS_NOISE), 1, 0);
        var riverDepthNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.RIVER_DEPTH), 1f, 0);


        //less means bigger rivers
        float riverThicknessModulator = 1.2f;
        // Modulate river width
        var widthModulator = DensityFunctions.add(DensityFunctions.constant(riverThicknessModulator), riverThicknessNoise);
        var adjustedThickness = DensityFunctions.mul(riverNoise.abs(), widthModulator);

        // Sub-spline to vary depth depending on riverDepthNoise
        var depthModulationSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(riverDepthNoise)))
                .addPoint(-1.0f, -0.2f, 0.0f) // deep parts
                .addPoint(1.0f, -0.05f, 0.0f)  // shallow parts
                .build();

        // Main spline to model the river cross-section profile
        var riverSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(adjustedThickness)))
                .addPoint(0.0f, depthModulationSpline)  // center of the river (carved deepest)
                .addPoint(0.25f, 0.0f, 0.0f)            // river banks (no carving)
                .addPoint(1.0f, 0.0f, 0.0f)             // outside river
                .addPoint(10.0f, 0.0f, 0.0f)
                .build();

        return DensityFunctions.spline(riverSpline);
    }

    public static DensityFunction makeNoodleCave(HolderGetter<NormalNoise.NoiseParameters> noiseGetter, HolderGetter<DensityFunction> densityFunctionLookup) {
        // 1. Evaluate two independent 3D noises (yScale = 1.0d makes it fully 3D)
        var noiseA = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.NOODLE_CAVE_A), 1.0d, 1.0d);
        var noiseB = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.NOODLE_CAVE_B), 1.0d, 1.0d);

        // 2. Noodle coordinate is the maximum of absolute values: max(|A|, |B|).
        // This value is 0.0 exactly along the intersection line (the center of the cave tube).
        var noodleDistance = DensityFunctions.max(noiseA.abs(), noiseB.abs());

        // 3. Model the cave profile. Inside the tube (< 0.08) we output a negative carving density.
        // Outside the tube (>= 0.08) we output 1.0 (positive) so it is ignored by min().
        var caveSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(noodleDistance)))
                .addPoint(0.0f, -1.5f, 0.0f)  // center of the cave (fully hollow)
                .addPoint(0.05f, -0.8f, 0.0f) // sloped cave walls
                .addPoint(0.08f, 1.0f, 0.0f)  // cave boundary (solid stone)
                .addPoint(1.0f, 1.0f, 0.0f)   // outside the cave
                .build();

        var noodleCaveDensity = DensityFunctions.spline(caveSpline);

        // 4. Height controller: Prevents caves from carving above Y = 80 (surface/sky).
        // Outputs 0.0 offset below Y = 50, transitioning to 2.5 offset at Y = 80.
        // Adding 2.5 to -1.5 center density results in +1.0 (solid), stopping all carving.
        var heightControllerSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(DensityFunctions.yClampedGradient(50, 80, 0.0d, 1.0d))))
                .addPoint(0.0f, 0.0f, 0.0f)  // deep underground: no height offset (full carving)
                .addPoint(1.0f, 2.5f, 0.0f)  // near surface / above ground: add 2.5 to prevent carving
                .build();
        var heightOffset = DensityFunctions.spline(heightControllerSpline);

        // 5. Entrance noise gate (2D noise, yScale = 0.0d)
        var entranceNoise = DensityFunctions.noise(noiseGetter.getOrThrow(ModNoiseParameters.CAVE_ENTRANCE_NOISE), 1.0d, 0.0d);

        // Spline that outputs 1.0 (keep caves underground) most of the time,
        // but drops to 0.0 (let caves break through) when entranceNoise is high.
        var entranceMaskSpline = CubicSpline.builder(new DensityFunctions.Spline.Coordinate(Holder.direct(entranceNoise)))
                .addPoint(-1.0f, 1.0f, 0.0f) // Keep underground
                .addPoint(0.4f, 1.0f, 0.0f)  // Keep underground
                .addPoint(1.0f, 0.0f, 0.0f)  // Create entrance
                .build();
        var entranceMask = DensityFunctions.spline(entranceMaskSpline);

        // Multiply the height offset by the mask.
        // If entranceMask is 0.0, the heightOffset is bypassed, letting caves carve up to Y = 80.
        var gatedHeightOffset = DensityFunctions.mul(heightOffset, entranceMask);

        return DensityFunctions.add(noodleCaveDensity, gatedHeightOffset);
    }
}
