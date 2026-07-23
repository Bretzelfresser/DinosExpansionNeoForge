package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.PineTrunkPlacer;
import com.bretzelfresser.dinosexpansion.common.worldgen.tree.PineFoliagePlacer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTreePlacers {
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPES =
            DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, DinosExpansion.MODID);

    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES =
            DeferredRegister.create(Registries.FOLIAGE_PLACER_TYPE, DinosExpansion.MODID);

    public static final DeferredHolder<TrunkPlacerType<?>, TrunkPlacerType<PineTrunkPlacer>> PINE_TRUNK_PLACER =
            TRUNK_PLACER_TYPES.register("pine_trunk_placer", () -> new TrunkPlacerType<>(PineTrunkPlacer.CODEC));

    public static final DeferredHolder<FoliagePlacerType<?>, FoliagePlacerType<PineFoliagePlacer>> PREHISTORIC_FOLIAGE_PLACER =
            FOLIAGE_PLACER_TYPES.register("prehistoric_foliage_placer", () -> new FoliagePlacerType<>(PineFoliagePlacer.CODEC));

    public static void register(IEventBus eventBus) {
        TRUNK_PLACER_TYPES.register(eventBus);
        FOLIAGE_PLACER_TYPES.register(eventBus);
    }
}
