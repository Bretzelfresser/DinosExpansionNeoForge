package com.bretzelfresser.dinosexpansion.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class FeaturePlacementUtils {

    public static PlacedFeature wrap(Holder<ConfiguredFeature<?, ?>> feature){
        return new PlacedFeature(feature, List.of());
    }

    public static Holder<PlacedFeature> wrapHolder(Holder<ConfiguredFeature<?, ?>> feature){
        return Holder.direct(new PlacedFeature(feature, List.of()));
    }

    public static void placeLiquid(BlockStateProvider fluid, WorldGenLevel level, RandomSource random, BlockPos pos) throws IllegalArgumentException {
        var fluidToPlace = fluid.getState(random, pos);
        var fluidState = fluidToPlace.getFluidState();
        if (fluidState.isEmpty()){
            throw new IllegalArgumentException(String.format("tried to place a liquid but the provided BlockStateProvider: {%s} did produce a state: {%s} which doesnt contain a fluid", fluid.getClass().getTypeName(), fluidToPlace));
        }
        level.setBlock(pos, fluidToPlace, 2);
        level.scheduleTick(pos, fluidState.getType(), 0);
    }
}
