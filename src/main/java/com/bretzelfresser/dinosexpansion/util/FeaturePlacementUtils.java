package com.bretzelfresser.dinosexpansion.util;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class FeaturePlacementUtils {

    public static PlacedFeature wrap(Holder<ConfiguredFeature<?, ?>> feature){
        return new PlacedFeature(feature, List.of());
    }

    public static Holder<PlacedFeature> wrapHolder(Holder<ConfiguredFeature<?, ?>> feature){
        return Holder.direct(new PlacedFeature(feature, List.of()));
    }
}
