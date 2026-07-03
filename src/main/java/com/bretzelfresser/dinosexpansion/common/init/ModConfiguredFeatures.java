package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.PineTrunkPlacer;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.PineFoliagePlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> FERN_PATCH = create("fern_patch");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TALL_PREHISTORIC_PINE = create("tall_prehistoric_pine");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_PREHISTORIC_REDWOOD = create("mega_prehistoric_redwood");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TEST_TREE = create("test_tree");

    public static void generate(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        var featureLookup = context.lookup(Registries.FEATURE);
        var placedFeatureLookup = context.lookup(Registries.FEATURE);

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

        context.register(MEGA_PREHISTORIC_REDWOOD, new ConfiguredFeature<>(Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(Blocks.SPRUCE_LOG),
                new PineTrunkPlacer.Builder(20, 8, 4)
                        .bottomRadius(1)
                        .topRadius(0)
                        .thicknessSpline(0.7f)
                        .form(PineTrunkPlacer.TrunkForm.CIRCLE)
                        .build(),
                BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
                new PineFoliagePlacer(UniformFloat.of(0.4f, 0.6f), UniformFloat.of(0.1f, 0.3f), ConstantInt.of(1), ConstantInt.of(4), ConstantInt.of(3)),
                new TwoLayersFeatureSize(1, 2, 4)
        ).build()));

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
                        .form(PineTrunkPlacer.TrunkForm.CIRCLE)
                        .build(),
                BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
                new PineFoliagePlacer(UniformFloat.of(0.4f, 0.6f), UniformFloat.of(0.1f, 0.3f), ConstantInt.of(1), ConstantInt.of(4), ConstantInt.of(3)),
                new TwoLayersFeatureSize(1, 2, 4)
        ).build()));
    }


    public static ResourceKey<ConfiguredFeature<?, ?>> create(String id) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, id));
    }
}
