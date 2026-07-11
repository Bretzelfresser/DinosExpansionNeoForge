package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.worldgen.feature.HotSpringFeature;
import com.bretzelfresser.dinosexpansion.common.worldgen.feature.PrehistoricFossilFeature;
import com.bretzelfresser.dinosexpansion.common.worldgen.feature.PrehistoricDesertVegetationFeature;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.GiantJungleTreeConfiguration;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.GiantJungleTreeFeature;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.RedwoodTreeConfiguration;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.RedwoodTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {


    public static final DeferredRegister<Feature<?>> FEATURE_CONFIGS = DeferredRegister.create(Registries.FEATURE, DinosExpansion.MODID);


    public static final DeferredHolder<Feature<?>, GiantJungleTreeFeature> GIANT_JUNGLE_TREE_FEATURE = FEATURE_CONFIGS.register("giant_jungle_tree", () -> new GiantJungleTreeFeature(GiantJungleTreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, RedwoodTreeFeature> REDWOOD_TREE_FEATURE = FEATURE_CONFIGS.register("redwood_tree", () -> new RedwoodTreeFeature(RedwoodTreeConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, HotSpringFeature> GEYSER_HOT_SPRING_FEATURE = FEATURE_CONFIGS.register("geyser_hot_spring", () -> new HotSpringFeature(HotSpringFeature.Configuration.CODEC));
    public static final DeferredHolder<Feature<?>, PrehistoricFossilFeature> PREHISTORIC_FOSSIL_FEATURE = FEATURE_CONFIGS.register("prehistoric_fossil", () -> new PrehistoricFossilFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredHolder<Feature<?>, PrehistoricDesertVegetationFeature> PREHISTORIC_DESERT_VEGETATION_FEATURE = FEATURE_CONFIGS.register("prehistoric_desert_vegetation", () -> new PrehistoricDesertVegetationFeature(NoneFeatureConfiguration.CODEC));
}
