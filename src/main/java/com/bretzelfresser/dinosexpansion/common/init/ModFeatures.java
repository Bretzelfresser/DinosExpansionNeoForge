package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.GiantJungleTreeConfiguration;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.GiantJungleTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {


    public static final DeferredRegister<Feature<?>> FEATURE_CONFIGS = DeferredRegister.create(Registries.FEATURE, DinosExpansion.MODID);


    public static final DeferredHolder<Feature<?>, GiantJungleTreeFeature> GIANT_JUNGLE_TREE_FEATURE = FEATURE_CONFIGS.register("giant_jungle_tree", () -> new GiantJungleTreeFeature(GiantJungleTreeConfiguration.CODEC));
}
