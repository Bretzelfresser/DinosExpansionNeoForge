package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.GiantJungleTreeConfiguration;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.PineFoliagePlacer;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.PineTrunkPlacer;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.RedwoodTreeConfiguration;
import com.bretzelfresser.dinosexpansion.util.TrunkForm;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import com.bretzelfresser.dinosexpansion.common.worldgen.feature.HotSpringFeature;

import java.util.List;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> GEYSER_HOT_SPRING = create("geyser_hot_spring");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FERN_PATCH = create("fern_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TALL_PREHISTORIC_PINE = create("tall_prehistoric_pine");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_PREHISTORIC_REDWOOD = create("mega_prehistoric_redwood");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GIANT_JUNGLE_TREE = create("giant_jungle_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SMALL_JUNGLE_TREE = create("small_jungle_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TEST_TREE = create("test_tree");

    public static void generate(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        var featureLookup = context.lookup(Registries.FEATURE);
        var placedFeatureLookup = context.lookup(Registries.FEATURE);

        context.register(GEYSER_HOT_SPRING, new ConfiguredFeature<>(ModFeatures.GEYSER_HOT_SPRING_FEATURE.get(), new HotSpringFeature.Configuration(
                BlockStateProvider.simple(Blocks.WATER),
                BlockStateProvider.simple(Blocks.CALCITE),
                BiasedToBottomInt.of(5, 10), // size (radius)
                UniformInt.of(1, 4)  // depth
        )));

        context.register(FERN_PATCH, new ConfiguredFeature<>(Feature.RANDOM_PATCH, new RandomPatchConfiguration(2000, 10, 4,
                PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder().add(Blocks.FERN.defaultBlockState(), 5).add(Blocks.LARGE_FERN.defaultBlockState(), 15)))))
        ));

        context.register(TALL_PREHISTORIC_PINE, new ConfiguredFeature<>(Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.SPRUCE_LOG),
                new PineTrunkPlacer.Builder(12, 5, 0)
                        .constantRadius(0)
                        .build(),
                BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
                new PineFoliagePlacer(UniformFloat.of(0.4f, 0.6f), ConstantFloat.of(0.1f), ConstantInt.of(1), ConstantInt.of(2), ConstantInt.of(3)),

                new TwoLayersFeatureSize(1, 1, 2)
        ).build()));

        context.register(MEGA_PREHISTORIC_REDWOOD, new ConfiguredFeature<>(ModFeatures.REDWOOD_TREE_FEATURE.get(), new RedwoodTreeConfiguration(
                UniformInt.of(4, 6), // radius
                UniformFloat.of(15, 25), // radiusToHeightFactor: 3..4 * 13..17 = 39..68 blocks tall
                BlockStateProvider.simple(Blocks.SPRUCE_LOG),
                BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
                UniformFloat.of(0.35f, 0.45f), // branchStartHeight
                ConstantInt.of(4), // branchInterval
                ConstantInt.of(8), // branchesPerInterval
                UniformInt.of(5, 7), // branchLength
                UniformInt.of(2, 3), // foliageRadius
                UniformInt.of(4, 5), // rootFlareHeight
                UniformInt.of(3, 4) // rootFlareLength
        )));

        context.register(TEST_TREE, new ConfiguredFeature<>(Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.SPRUCE_LOG),
                new PineTrunkPlacer.Builder(30, 10, 10)
                        .topRadius(12)
                        .bottomRadius(6)
                        .thicknessSpline(CubicSpline.builder(ToFloatFunction.IDENTITY)
                                .addPoint(0, 1, -1)
                                .addPoint(0.2f, 0f, 0)
                                .addPoint(0.8f, 0f, 0)
                                .addPoint(1f, 1f, 1)
                                .build())
                        .form(TrunkForm.CIRCLE)
                        .build(),
                BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
                new PineFoliagePlacer(UniformFloat.of(0.4f, 0.6f), UniformFloat.of(0.1f, 0.3f), ConstantInt.of(1), ConstantInt.of(4), ConstantInt.of(3)),
                new TwoLayersFeatureSize(1, 2, 4)
        ).build()));

        context.register(GIANT_JUNGLE_TREE, new ConfiguredFeature<>(ModFeatures.GIANT_JUNGLE_TREE_FEATURE.get(), new GiantJungleTreeConfiguration(
                UniformInt.of(2, 4),
                UniformFloat.of(15, 20),
                BlockStateProvider.simple(Blocks.JUNGLE_LOG),
                BlockStateProvider.simple(Blocks.JUNGLE_LEAVES),
                UniformFloat.of(0.4f, 0.7f),
                UniformInt.of(5, 7),
                UniformInt.of(8, 14),
                UniformInt.of(4, 6),
                UniformFloat.of(0.05f, 0.15f)
        )));
        context.register(SMALL_JUNGLE_TREE, new ConfiguredFeature<>(ModFeatures.GIANT_JUNGLE_TREE_FEATURE.get(), new GiantJungleTreeConfiguration(
                UniformInt.of(1, 2),
                UniformFloat.of(8, 11),
                BlockStateProvider.simple(Blocks.JUNGLE_LOG),
                BlockStateProvider.simple(Blocks.JUNGLE_LEAVES)
        )));
    }


    public static ResourceKey<ConfiguredFeature<?, ?>> create(String id) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, id));
    }
}
