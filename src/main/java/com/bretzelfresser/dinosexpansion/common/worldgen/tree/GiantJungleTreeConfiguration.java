package com.bretzelfresser.dinosexpansion.common.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record GiantJungleTreeConfiguration(IntProvider radius, FloatProvider radiusToHeightFactor, BlockStateProvider woodProvider,
                                           BlockStateProvider foliageProvider, FloatProvider branchStartHeight,
                                           IntProvider branchCount, IntProvider branchLength,
                                           IntProvider foliageRadius, FloatProvider vinesChance) implements FeatureConfiguration {

    public GiantJungleTreeConfiguration(IntProvider radius, FloatProvider radiusToHeightFactor, BlockStateProvider woodProvider, BlockStateProvider foliageProvider) {
        this(radius, radiusToHeightFactor, woodProvider, foliageProvider,
             UniformFloat.of(0.45f, 0.65f), UniformInt.of(3, 5), UniformInt.of(6, 10),
             UniformInt.of(3, 4), UniformFloat.of(0.05f, 0.15f));
    }

    public static final Codec<GiantJungleTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IntProvider.CODEC.fieldOf("radius").orElse(UniformInt.of(4, 6)).forGetter(GiantJungleTreeConfiguration::radius),
            FloatProvider.CODEC.fieldOf("radiusToHeightFactor").orElse(UniformFloat.of(8, 15)).forGetter(GiantJungleTreeConfiguration::radiusToHeightFactor),
            BlockStateProvider.CODEC.fieldOf("woodProvider").forGetter(GiantJungleTreeConfiguration::woodProvider),
            BlockStateProvider.CODEC.fieldOf("foliageProvider").forGetter(GiantJungleTreeConfiguration::foliageProvider),
            FloatProvider.CODEC.fieldOf("branchStartHeight").orElse(UniformFloat.of(0.45f, 0.65f)).forGetter(GiantJungleTreeConfiguration::branchStartHeight),
            IntProvider.CODEC.fieldOf("branchCount").orElse(UniformInt.of(3, 5)).forGetter(GiantJungleTreeConfiguration::branchCount),
            IntProvider.CODEC.fieldOf("branchLength").orElse(UniformInt.of(6, 10)).forGetter(GiantJungleTreeConfiguration::branchLength),
            IntProvider.CODEC.fieldOf("foliageRadius").orElse(UniformInt.of(3, 4)).forGetter(GiantJungleTreeConfiguration::foliageRadius),
            FloatProvider.CODEC.fieldOf("vinesChance").orElse(UniformFloat.of(0.05f, 0.15f)).forGetter(GiantJungleTreeConfiguration::vinesChance)
    ).apply(instance, GiantJungleTreeConfiguration::new));

}
