package com.bretzelfresser.dinosexpansion.common.worldgen.tree;

import com.bretzelfresser.dinosexpansion.common.init.ModTreePlacers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class PrehistoricFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<PrehistoricFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> foliagePlacerParts(instance)
                    .and(FloatProvider.CODEC.fieldOf("start_percentage").orElse(ConstantFloat.of(0.4f)).forGetter(fp -> fp.startPercentage))
                    .and(IntProvider.CODEC.fieldOf("tier_spacing").orElse(ConstantInt.of(2)).forGetter(fp -> fp.tierSpacing))
                    .apply(instance, PrehistoricFoliagePlacer::new)
    );

    private final FloatProvider startPercentage;
    private final IntProvider tierSpacing;

    public PrehistoricFoliagePlacer(IntProvider radius, IntProvider offset, FloatProvider startPercentage, IntProvider tierSpacing) {
        super(radius, offset);
        this.startPercentage = startPercentage;
        this.tierSpacing = tierSpacing;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return ModTreePlacers.PREHISTORIC_FOLIAGE_PLACER.get();
    }

    @Override
    protected void createFoliage(
            LevelSimulatedReader level,
            FoliageSetter blockSetter,
            RandomSource random,
            TreeConfiguration config,
            int maxFreeTreeHeight,
            FoliageAttachment attachment,
            int foliageHeight,
            int foliageRadius,
            int offset
    ) {
        BlockPos topPos = attachment.pos();
        int spacing = this.tierSpacing.sample(random);

        // 1. Generate the pointed pyramid top (upper 3 blocks of the canopy)
        // At the very top (Y = topPos): radius 0 (1 leaf block)
        placeLeavesRow(level, blockSetter, random, config, topPos, 0, 0, attachment.doubleTrunk());
        // At Y = topPos - 1: radius 1
        placeLeavesRow(level, blockSetter, random, config, topPos.below(1), 1, 0, attachment.doubleTrunk());
        // At Y = topPos - 2: radius 1
        placeLeavesRow(level, blockSetter, random, config, topPos.below(2), 1, 0, attachment.doubleTrunk());

        // 2. Generate the horizontal tiers starting from 3 blocks below the topPos down to the canopy bottom
        int yStart = 3;
        for (int yOffset = yStart; yOffset < foliageHeight; yOffset += spacing) {
            BlockPos tierCenter = topPos.below(yOffset);

            // Relative progress from yStart to foliageHeight
            float progress = (float) (yOffset - yStart) / (float) (foliageHeight - yStart);
            progress = Mth.clamp(progress, 0.0f, 1.0f);

            // Interpolate radius from 2 (near top) to foliageRadius (at bottom)
            int radius = Math.round(Mth.lerp(progress, 2.0f, (float) foliageRadius));

            // Place main disc of leaves at tierCenter
            placeLeavesRow(level, blockSetter, random, config, tierCenter, radius, 0, attachment.doubleTrunk());

            // Place a slightly smaller disc directly above it (tapering upwards)
            if (radius > 1) {
                placeLeavesRow(level, blockSetter, random, config, tierCenter.above(1), radius - 1, 0, attachment.doubleTrunk());
            }
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        float startPercent = this.startPercentage.sample(random);
        return Math.max(4, Math.round(height * (1.0f - startPercent)));
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        // For a clean circular disc, we skip corner blocks
        return Math.abs(localX) == range && Math.abs(localZ) == range && range > 0;
    }
}
