package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.OptionalLong;

public class ModDimensionTypes {
    public static final ResourceKey<DimensionType> DINO_DIM_TYPE_KEY = create("dino_dimension");

    public static void bootstrap(BootstrapContext<DimensionType> context) {
        context.register(DINO_DIM_TYPE_KEY, new DimensionType(
                OptionalLong.empty(), // fixedTime
                true, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                true, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                -64, // minY
                384, // height
                384, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                ResourceLocation.withDefaultNamespace("overworld"), // effectsLocation
                0.0F, // ambientLight
                new DimensionType.MonsterSettings(false, true, UniformInt.of(0, 7), 0) // monsterSettings
        ));
    }

    public static ResourceKey<DimensionType> create(String name){
        return ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }
}
