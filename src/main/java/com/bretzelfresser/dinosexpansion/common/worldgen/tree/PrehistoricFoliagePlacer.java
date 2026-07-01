package com.bretzelfresser.dinosexpansion.common.worldgen.tree;

import com.bretzelfresser.dinosexpansion.common.init.ModTreePlacers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

public class PrehistoricFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<PrehistoricFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> foliagePlacerParts(instance)
                    .and(IntProvider.CODEC.fieldOf("tier_count").forGetter(fp -> fp.tierCount))
                    .and(IntProvider.CODEC.fieldOf("tier_spacing").forGetter(fp -> fp.tierSpacing))
                    .apply(instance, PrehistoricFoliagePlacer::new)
    );

    private final IntProvider tierCount;
    private final IntProvider tierSpacing;

    public PrehistoricFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider tierCount, IntProvider tierSpacing) {
        super(radius, offset);
        this.tierCount = tierCount;
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
        int count = this.tierCount.sample(random);
        int spacing = this.tierSpacing.sample(random);

        // 1. Place a small cap at the very top (Tier 0)
        placeLeavesRow(level, blockSetter, random, config, topPos, 1, 0, attachment.doubleTrunk());

        // 2. Loop down and place each horizontal tier of leaves
        for (int i = 0; i < count; i++) {
            // Y-coordinate of this tier
            int yOffset = -(1 + i * spacing);
            BlockPos tierCenter = topPos.above(yOffset);

            // Radius increases as we go down:
            int radius = 2 + i;
            if (radius > foliageRadius) {
                radius = foliageRadius; // Cap at max foliageRadius configured
            }

            // Place a flat horizontal disc of leaves of thickness 1 at this Y
            placeLeavesRow(level, blockSetter, random, config, tierCenter, radius, 0, attachment.doubleTrunk());

            // Add a cross-shape of leaves just below the disc to make it look organic
            if (radius > 1) {
                placeLeavesRow(level, blockSetter, random, config, tierCenter.below(), radius - 1, 0, attachment.doubleTrunk());
            }
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        return 0; // We handle the height internally based on tiers
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        // For a clean circular disc, we skip corner blocks
        return Math.abs(localX) == range && Math.abs(localZ) == range && range > 0;
    }
}
