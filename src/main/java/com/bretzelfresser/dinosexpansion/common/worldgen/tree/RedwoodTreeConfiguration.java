package com.bretzelfresser.dinosexpansion.common.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record RedwoodTreeConfiguration(
        IntProvider radius,
        FloatProvider radiusToHeightFactor,
        BlockStateProvider woodProvider,
        BlockStateProvider foliageProvider,
        FloatProvider branchStartHeight,
        IntProvider branchInterval,
        IntProvider branchesPerInterval,
        IntProvider branchLength,
        IntProvider foliageRadius,
        IntProvider rootFlareHeight,
        IntProvider rootFlareLength
) implements FeatureConfiguration {

    public RedwoodTreeConfiguration(IntProvider radius, FloatProvider radiusToHeightFactor, BlockStateProvider woodProvider, BlockStateProvider foliageProvider) {
        this(radius, radiusToHeightFactor, woodProvider, foliageProvider,
             UniformFloat.of(0.25f, 0.35f), ConstantInt.of(4), ConstantInt.of(8),
             UniformInt.of(5, 7), UniformInt.of(2, 3), UniformInt.of(4, 5), UniformInt.of(3, 4));
    }

    public static final Codec<RedwoodTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProvider.CODEC.fieldOf("radius").orElse(UniformInt.of(3, 4)).forGetter(RedwoodTreeConfiguration::radius),
            FloatProvider.CODEC.fieldOf("radiusToHeightFactor").orElse(UniformFloat.of(12, 16)).forGetter(RedwoodTreeConfiguration::radiusToHeightFactor),
            BlockStateProvider.CODEC.fieldOf("woodProvider").forGetter(RedwoodTreeConfiguration::woodProvider),
            BlockStateProvider.CODEC.fieldOf("foliageProvider").forGetter(RedwoodTreeConfiguration::foliageProvider),
            FloatProvider.CODEC.fieldOf("branchStartHeight").orElse(UniformFloat.of(0.25f, 0.35f)).forGetter(RedwoodTreeConfiguration::branchStartHeight),
            IntProvider.CODEC.fieldOf("branchInterval").orElse(ConstantInt.of(4)).forGetter(RedwoodTreeConfiguration::branchInterval),
            IntProvider.CODEC.fieldOf("branchesPerInterval").orElse(ConstantInt.of(8)).forGetter(RedwoodTreeConfiguration::branchesPerInterval),
            IntProvider.CODEC.fieldOf("branchLength").orElse(UniformInt.of(5, 7)).forGetter(RedwoodTreeConfiguration::branchLength),
            IntProvider.CODEC.fieldOf("foliageRadius").orElse(UniformInt.of(2, 3)).forGetter(RedwoodTreeConfiguration::foliageRadius),
            IntProvider.CODEC.fieldOf("rootFlareHeight").orElse(UniformInt.of(4, 5)).forGetter(RedwoodTreeConfiguration::rootFlareHeight),
            IntProvider.CODEC.fieldOf("rootFlareLength").orElse(UniformInt.of(3, 4)).forGetter(RedwoodTreeConfiguration::rootFlareLength)
    ).apply(instance, RedwoodTreeConfiguration::new));

}
