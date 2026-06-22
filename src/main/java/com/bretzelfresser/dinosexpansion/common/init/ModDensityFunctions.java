package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class ModDensityFunctions {

    public static final ResourceKey<DensityFunction> ZERO_DENSITY_KEY = create("zero");
    public static final ResourceKey<DensityFunction> CONTINENTS = create("continents");
    public static final ResourceKey<DensityFunction> FINAL_DENSITY = create("final_density");



    public static ResourceKey<DensityFunction> create(String name){
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

        var surfaceSpline = DensityFunctions.spline(CubicSpline.builder(new DensityFunctions.Spline.Coordinate(refContinents))
                .addPoint(-1f, -1f, 0)
                .addPoint(0f, 0f, 2)
                .addPoint(1f, 1f, 0f)

                .build());

        context.register(FINAL_DENSITY, DensityFunctions.add(depthSpline, surfaceSpline));
    }

    private static DensityFunction wrap(Holder<DensityFunction> holder) {
        return new DensityFunctions.HolderHolder(holder);
    }
}
