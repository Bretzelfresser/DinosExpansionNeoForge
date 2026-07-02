package com.bretzelfresser.dinosexpansion.common.worldgen.tree;

import com.bretzelfresser.dinosexpansion.common.init.ModTreePlacers;
import com.ibm.icu.impl.units.MeasureUnitImpl;
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
import org.apache.logging.log4j.core.appender.rolling.action.IfAll;

public class PineFoliagePlacer extends FoliagePlacer {
    public static final MapCodec<PineFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> foliagePlacerParts(instance)
                    .and(FloatProvider.CODEC.fieldOf("percentageFoliage").orElse(ConstantFloat.of(0.6f)).forGetter(t -> t.percentageFoliage))
                    .and(FloatProvider.CODEC.fieldOf("chanceRandomness").orElse(ConstantFloat.of(0.1f)).forGetter(t -> t.chanceRandomness))
                    .and(IntProvider.CODEC.fieldOf("minFoliageRadius").orElse(ConstantInt.of(0)).forGetter(t -> t.minFoliageRadius))
                    .and(IntProvider.CODEC.fieldOf("maxFoliageRadius").orElse(ConstantInt.of(2)).forGetter(t -> t.maxFoliageRadius))
                    .and(IntProvider.CODEC.fieldOf("distanceFoliage").orElse(ConstantInt.of(3)).forGetter(t -> t.foliageDistance))
                    .apply(instance, PineFoliagePlacer::new)
    );

    protected final FloatProvider percentageFoliage, chanceRandomness;
    protected final IntProvider minFoliageRadius, maxFoliageRadius, foliageDistance;

    /**
     * @param percentageFoliage percentage of tree height until foliage will be placed
     * @param maxFoliageRadius  radius at the bottom of the tree, capped with the percentageFoliage
     * @param minFoliageRadius  radius at the top of the tree, between will be linear interpolated
     * @param foliageDistance the distance between those foliage disks
     * @param chanceRandomness  the chance of the outer leaves to not be placed
     */
    public PineFoliagePlacer(FloatProvider percentageFoliage, FloatProvider chanceRandomness, IntProvider minFoliageRadius, IntProvider maxFoliageRadius, IntProvider foliageDistance) {
        this(ConstantInt.of(1), ConstantInt.of(1), percentageFoliage, chanceRandomness, minFoliageRadius, maxFoliageRadius, foliageDistance);
    }

    public PineFoliagePlacer(IntProvider radius, IntProvider offset, FloatProvider percentageFoliage, FloatProvider chanceRandomness, IntProvider minFoliageRadius, IntProvider maxFoliageRadius, IntProvider foliageDistance) {
        super(radius, offset);
        this.percentageFoliage = percentageFoliage;
        this.chanceRandomness = chanceRandomness;
        this.minFoliageRadius = minFoliageRadius;
        this.maxFoliageRadius = maxFoliageRadius;
        this.foliageDistance = foliageDistance;
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
        BlockPos topPosTree = attachment.pos().mutable();
        int foliageDistance = this.foliageDistance.sample(random);
        int foliageHeigh = Math.round((float) maxFreeTreeHeight * (1f - Mth.clamp(percentageFoliage.sample(random), 0f, 1f)));

        int minRadius = this.minFoliageRadius.sample(random);
        int maxRadius = this.maxFoliageRadius.sample(random);

        for (int y = 0; y <= foliageHeigh; y += foliageDistance) {

            int radius = Mth.lerpInt((float) y / (float) foliageHeigh, minRadius, maxRadius);

            //at pos disk
            placeLeavesRow(level, blockSetter, random, config, topPosTree.offset(0, -y, 0), radius, 0, false);
            //pos above
            placeLeavesRow(level, blockSetter, random, config, topPosTree.offset(0, -y, 0), radius, 1, false);
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        return 3;
    }

    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        // For a clean circular disc, we skip corner blocks
        if ((Math.abs(localX) == range || Math.abs(localZ) == range) && range > 0)
            return random.nextFloat() < Mth.clamp(this.chanceRandomness.sample(random), 0f, 1f);
        return false;
    }
}
