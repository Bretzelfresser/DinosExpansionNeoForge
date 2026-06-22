package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class ModDensityFunctions {

    public static final ResourceKey<DensityFunction> ZERO_DENSITY_KEY = create("zero");


    public static ResourceKey<DensityFunction> create(String name){
        return ResourceKey.create(Registries.DENSITY_FUNCTION, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }

    public static void bootstrap(BootstrapContext<DensityFunction> context) {
        context.register(ZERO_DENSITY_KEY, DensityFunctions.zero());
    }
}
