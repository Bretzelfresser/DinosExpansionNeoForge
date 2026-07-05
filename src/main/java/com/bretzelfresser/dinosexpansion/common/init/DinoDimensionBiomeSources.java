package com.bretzelfresser.dinosexpansion.common.init;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.util.ArrayList;
import java.util.List;

public class DinoDimensionBiomeSources {

    public static BiomeSource createDinoDimensionBiomeSource(HolderGetter<Biome> biomes) {
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters = new ArrayList<>();

        // Deep Ocean
        addBiome(parameters, biomes, ModBiomes.DEEP_PREHISTORIC_OCEAN,
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, -0.6F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(0.2F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        // Prehistoric Ocean
        addBiome(parameters, biomes, ModBiomes.PREHISTORIC_OCEAN,
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-0.6F, -0.2F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        // Prehistoric Coast
        addBiome(parameters, biomes, ModBiomes.PREHISTORIC_COAST,
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-0.2F, -0.15F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        // Cold Biome
        addBiome(parameters, biomes, ModBiomes.ANCIENT_GLACIAL_TUNDRA,
                Climate.Parameter.span(-1.0F, -0.4F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-0.15F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        // Fern Plains (Default land biome)
        addBiome(parameters, biomes, ModBiomes.FERN_PLAINS,
                Climate.Parameter.span(-0.4F, 0.4F),
                Climate.Parameter.span(-0.4F, 0.4F),
                Climate.Parameter.span(-0.15F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        // Dry/Hot Biomes
        addBiome(parameters, biomes, ModBiomes.BONE_DESERT,
                Climate.Parameter.span(0.4F, 1.0F),
                Climate.Parameter.span(-1.0F, -0.3F),
                Climate.Parameter.span(-0.15F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        addBiome(parameters, biomes, ModBiomes.PETRIFIED_BADLANDS,
                Climate.Parameter.span(0.4F, 1.0F),
                Climate.Parameter.span(-0.3F, 0.1F),
                Climate.Parameter.span(-0.15F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        // Swamp/Wet/Forest Biomes
        addBiome(parameters, biomes, ModBiomes.FOGGY_SWAMP,
                Climate.Parameter.span(-0.2F, 0.3F),
                Climate.Parameter.span(0.4F, 1.0F),
                Climate.Parameter.span(-0.15F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        addBiome(parameters, biomes, ModBiomes.REDWOOD_FOREST,
                Climate.Parameter.span(0.1F, 0.5F),
                Climate.Parameter.span(0.1F, 0.6F),
                Climate.Parameter.span(-0.15F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        addBiome(parameters, biomes, ModBiomes.PRIMORDIAL_JUNGLE,
                Climate.Parameter.span(0.5F, 1.0F),
                Climate.Parameter.span(0.4F, 1.0F),
                Climate.Parameter.span(-0.15F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        addBiome(parameters, biomes, ModBiomes.DELTA_MANGROVE,
                Climate.Parameter.span(0.3F, 0.8F),
                Climate.Parameter.span(0.3F, 0.8F),
                Climate.Parameter.span(-0.15F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        addBiome(parameters, biomes, ModBiomes.GEYSER_VALLEY,
                Climate.Parameter.span(0.6F, 1.0F),
                Climate.Parameter.span(-0.1F, 0.4F),
                Climate.Parameter.span(-0.15F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F),
                Climate.Parameter.span(-1.0F, 1.0F)
        );

        return MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(parameters));
    }

    private static void addBiome(List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters,
                                 HolderGetter<Biome> biomes,
                                 net.minecraft.resources.ResourceKey<Biome> key,
                                 Climate.Parameter temperature,
                                 Climate.Parameter humidity,
                                 Climate.Parameter continentalness,
                                 Climate.Parameter erosion,
                                 Climate.Parameter depth,
                                 Climate.Parameter weirdness) {
        parameters.add(Pair.of(
                Climate.parameters(temperature, humidity, continentalness, erosion, depth, weirdness, 0.0F),
                biomes.getOrThrow(key)
        ));
    }
}
