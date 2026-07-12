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
                frozen(false),
                all(),
                Climate.Parameter.span(-1.0F, -0.6F),
                all(),
                Climate.Parameter.span(0.2F, 1.0F),
                all()
        );

        // Prehistoric Ocean
        addBiome(parameters, biomes, ModBiomes.PREHISTORIC_OCEAN,
                frozen(false),
                all(),
                Climate.Parameter.span(-0.6F, -0.05F),
                all(),
                all(),
                all()
        );

        // Deep Ocean
        addBiome(parameters, biomes, ModBiomes.DEEP_PREHISTORIC_FROZEN_OCEAN,
                frozen(true),
                all(),
                Climate.Parameter.span(-1.0F, -0.6F),
                all(),
                Climate.Parameter.span(0.2F, 1.0F),
                all()
        );

        // Prehistoric Ocean
        addBiome(parameters, biomes, ModBiomes.PREHISTORIC_FROZEN_OCEAN,
                frozen(true),
                all(),
                Climate.Parameter.span(-0.6F, -0.05F),
                all(),
                all(),
                all()
        );

        makeBeach(parameters, biomes, Climate.Parameter.span(-0.05f, .15f));
        makeNormalInland(parameters, biomes, Climate.Parameter.span(.15f, 1f));


        return MultiNoiseBiomeSource.createFromList(new Climate.ParameterList<>(parameters));
    }

    protected static void makeNormalInland(List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters,
                                           HolderGetter<Biome> biomes,
                                           Climate.Parameter continents) {

        {//hot scope -> just for me for orientation
            //hot will be desert in flat terrain
            addBiome(parameters, biomes, ModBiomes.BONE_DESERT,
                    hot(true),
                    dry(true),
                    continents,
                    Climate.Parameter.span(-1.0f, -0.1f),
                    all(),
                    all()
            );
            //got and dry and mountains -> badlands
            addBiome(parameters, biomes, ModBiomes.PETRIFIED_BADLANDS,
                    hot(true),
                    dry(true),
                    continents,
                    Climate.Parameter.span(-0.1f, 1.0f),
                    all(),
                    all()
            );

            // hot and normal humidity and flat -> geyser valley
            addBiome(parameters, biomes, ModBiomes.GEYSER_VALLEY,
                    hot(true),
                    normalHumidity(),
                    continents,
                    Climate.Parameter.span(-1.0f, -0.1f),
                    all(),
                    all()
            );
            // hot and normal humidity and mountains -> badlands
            addBiome(parameters, biomes, ModBiomes.PETRIFIED_BADLANDS,
                    hot(true),
                    normalHumidity(),
                    continents,
                    Climate.Parameter.span(-0.1f, 1.0f),
                    all(),
                    all()
            );
            addBiome(parameters, biomes, ModBiomes.PRIMORDIAL_JUNGLE,
                    hot(true),
                    wet(true),
                    continents,
                    all(),
                    all(),
                    all()
            );
        }
        {//normal termperature (-.5, .5)
            addBiome(parameters, biomes, ModBiomes.FOGGY_SWAMP,
                    normalTemperature(),
                    wet(true),
                    continents,
                    flat(true),
                    all(),
                    all()
            );
            addBiome(parameters, biomes, ModBiomes.DELTA_MANGROVE,
                    normalTemperature(),
                    wet(true),
                    continents,
                    normalErosion(),
                    all(),
                    all()
            );
            addBiome(parameters, biomes, ModBiomes.FERN_PLAINS,
                    normalTemperature(),
                    wet(true),
                    continents,
                    steep(true),
                    all(),
                    all()
            );


            addBiome(parameters, biomes, ModBiomes.REDWOOD_FOREST,
                    normalTemperature(),
                    normalHumidity(),
                    continents,
                    Climate.Parameter.span(-.5f, 0f),
                    all(),
                    all()
            );
            addBiome(parameters, biomes, ModBiomes.FERN_PLAINS,
                    normalTemperature(),
                    normalHumidity(),
                    continents,
                    Climate.Parameter.span(0f, .5f),
                    all(),
                    all()
            );
            addBiome(parameters, biomes, ModBiomes.FERN_PLAINS,
                    normalTemperature(),
                    normalHumidity(),
                    continents,
                    steep(true),
                    all(),
                    all()
            );
            addBiome(parameters, biomes, ModBiomes.FERN_PLAINS,
                    normalTemperature(),
                    normalHumidity(),
                    continents,
                    flat(true),
                    all(),
                    all()
            );
        }
        {//cold (-.5, -1)
            addBiome(parameters, biomes, ModBiomes.ANCIENT_GLACIAL_TUNDRA,
                    frozen(true),
                    all(),
                    continents,
                    all(),
                    all(),
                    all()
            );
        }


    }

    protected static void makeBeach(List<Pair<Climate.ParameterPoint, Holder<Biome>>> parameters,
                                    HolderGetter<Biome> biomes,
                                    Climate.Parameter continents) {
        //HOT beaches
        //when hot always bone desert
        addBiome(parameters, biomes, ModBiomes.PREHISTORIC_COAST,
                hot(true),
                all(),
                continents,
                all(),
                all(),
                all()
        );
        //frozen beaches
        //when frozen always frozen tundra
        addBiome(parameters, biomes, ModBiomes.ANCIENT_GLACIAL_TUNDRA,
                frozen(true),
                all(),
                continents,
                all(),
                all(),
                all()
        );
        //Normal Temperature beaches
        // Prehistoric Coast only at flat beaches, not when hot and not when wet, cause then the biomes reach inside the ocean without a coast biome in between
        addBiome(parameters, biomes, ModBiomes.PREHISTORIC_COAST,// not wet
                normalTemperature(),
                wet(false),
                continents,
                steep(false),
                all(),
                all()
        );
        addBiome(parameters, biomes, ModBiomes.FERN_PLAINS,//not wet
                normalTemperature(),
                wet(false),
                continents,
                steep(true),
                all(),
                all()
        );
        addBiome(parameters, biomes, ModBiomes.FOGGY_SWAMP,//wet
                normalTemperature(),
                wet(true),
                continents,
                flat(true),
                all(),
                all()
        );
        addBiome(parameters, biomes, ModBiomes.DELTA_MANGROVE,//wet
                normalTemperature(),
                wet(true),
                continents,
                normalErosion(),
                all(),
                all()
        );
        addBiome(parameters, biomes, ModBiomes.FERN_PLAINS,//wet
                normalTemperature(),
                wet(true),
                continents,
                steep(true),
                all(),
                all()
        );


    }

    private static Climate.Parameter land() {
        return Climate.Parameter.span(-0.05f, 1f);
    }

    private static Climate.Parameter landWithoutBeach() {
        return Climate.Parameter.span(0.05f, 1f);
    }

    private static Climate.Parameter beach() {
        return Climate.Parameter.span(-0.05f, 0.05f);
    }

    private static Climate.Parameter flat(boolean flat) {
        return flat ? Climate.Parameter.span(-1f, -0.5f) : Climate.Parameter.span(-.5f, 1f);
    }

    private static Climate.Parameter normalErosion() {
        return Climate.Parameter.span(-0.5f, 0.5f);
    }

    private static Climate.Parameter steep(boolean steep) {
        return steep ? Climate.Parameter.span(.5f, 1f) : Climate.Parameter.span(-1f, .5f);
    }

    private static Climate.Parameter frozen(boolean frozen) {
        return frozen ? Climate.Parameter.span(-1f, -0.15f) : Climate.Parameter.span(-0.15f, 1f);
    }

    private static Climate.Parameter normalTemperature() {
        return Climate.Parameter.span(-0.15f, 0.15f);
    }

    private static Climate.Parameter hot(boolean hot) {
        return hot ? Climate.Parameter.span(0.15f, 1f) : Climate.Parameter.span(-1f, 0.15f);
    }


    private static Climate.Parameter wet(boolean wet) {
        return wet ? Climate.Parameter.span(0.15f, 1f) : Climate.Parameter.span(-1f, 0.15f);
    }

    private static Climate.Parameter dry(boolean dry) {
        return dry ? Climate.Parameter.span(-1f, -0.15f) : Climate.Parameter.span(-0.15f, 1f);
    }

    private static Climate.Parameter normalHumidity() {
        return Climate.Parameter.span(-0.15f, 0.15f);
    }


    private static Climate.Parameter all() {
        return Climate.Parameter.span(-1f, 1f);
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
