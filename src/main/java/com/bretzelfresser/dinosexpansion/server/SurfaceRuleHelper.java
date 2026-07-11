package com.bretzelfresser.dinosexpansion.server;

import com.bretzelfresser.dinosexpansion.common.init.ModBiomes;
import com.bretzelfresser.dinosexpansion.common.init.ModNoiseParameters;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class SurfaceRuleHelper {

    public static SurfaceRules.RuleSource generateDinoSurfaceRules(){

        var waterSandRule = andConditions(
                ifElse(
                        SurfaceRules.noiseCondition(ModNoiseParameters.CONTINENTS, -0.2f),
                        SurfaceRules.state(Blocks.SAND.defaultBlockState()),
                        SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())),
                SurfaceRules.not(SurfaceRules.waterBlockCheck(0,1)),
                SurfaceRules.ON_FLOOR,
                SurfaceRules.abovePreliminarySurface()
        );

        var beachSandRule = SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.PREHISTORIC_COAST), SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(4, false, CaveSurface.FLOOR), SurfaceRules.state(Blocks.SAND.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(6, false, CaveSurface.FLOOR), SurfaceRules.state(Blocks.SANDSTONE.defaultBlockState()))

        ) );

        var surfaceRule = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(4, false, CaveSurface.FLOOR), SurfaceRules.state(Blocks.DIRT.defaultBlockState())));


        var geyserValleySurfaceRule = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(ModBiomes.GEYSER_VALLEY),
                SurfaceRules.sequence(
                        // Underwater floor rule
                        SurfaceRules.ifTrue(
                                SurfaceRules.waterBlockCheck(-1, 0),
                                SurfaceRules.ifTrue(
                                        SurfaceRules.ON_FLOOR,
                                        SurfaceRules.sequence(
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.noiseCondition(ModNoiseParameters.TEMPERATURE, 0.2, 1.0),
                                                        SurfaceRules.state(Blocks.MAGMA_BLOCK.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.noiseCondition(ModNoiseParameters.HUMIDITY, 0.2, 1.0),
                                                        SurfaceRules.state(Blocks.SOUL_SAND.defaultBlockState())
                                                ),
                                                SurfaceRules.ifTrue(
                                                        SurfaceRules.noiseCondition(ModNoiseParameters.EROSION, 0.4, 1.0),
                                                        SurfaceRules.state(Blocks.OBSIDIAN.defaultBlockState())
                                                ),
                                                ifElse(
                                                        SurfaceRules.noiseCondition(ModNoiseParameters.TEMPERATURE, -0.2, 0.2),
                                                        SurfaceRules.state(Blocks.CLAY.defaultBlockState()),
                                                        SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())
                                                )
                                        )
                                )
                        ),
                        // Dry floor rule
                        SurfaceRules.ifTrue(
                                SurfaceRules.ON_FLOOR,
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.TEMPERATURE, 0.35, 1.0),
                                                SurfaceRules.state(Blocks.CALCITE.defaultBlockState())
                                        ),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.HUMIDITY, 0.3, 1.0),
                                                SurfaceRules.state(Blocks.YELLOW_TERRACOTTA.defaultBlockState())
                                        ),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.EROSION, 0.25, 1.0),
                                                SurfaceRules.state(Blocks.ORANGE_TERRACOTTA.defaultBlockState())
                                        ),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.TEMPERATURE, -1.0, -0.4),
                                                SurfaceRules.state(Blocks.CLAY.defaultBlockState())
                                        ),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.HUMIDITY, -1.0, -0.4),
                                                SurfaceRules.state(Blocks.MUD.defaultBlockState())
                                        ),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.EROSION, -1.0, -0.5),
                                                SurfaceRules.state(Blocks.SMOOTH_BASALT.defaultBlockState())
                                        ),
                                        ifElse(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.TEMPERATURE, -0.15, 0.15),
                                                SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState()),
                                                SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())
                                        )
                                )
                        ),
                        // Deep under floor
                        SurfaceRules.ifTrue(
                                SurfaceRules.stoneDepthCheck(4, false, CaveSurface.FLOOR),
                                SurfaceRules.sequence(
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.TEMPERATURE, 0.35, 1.0),
                                                SurfaceRules.state(Blocks.CALCITE.defaultBlockState())
                                        ),
                                        SurfaceRules.ifTrue(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.TEMPERATURE, -1.0, -0.4),
                                                SurfaceRules.state(Blocks.CLAY.defaultBlockState())
                                        ),
                                        ifElse(
                                                SurfaceRules.noiseCondition(ModNoiseParameters.HUMIDITY, -0.3, 0.3),
                                                SurfaceRules.state(Blocks.COARSE_DIRT.defaultBlockState()),
                                                SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())
                                        )
                                )
                        )
                )
        );

        var stoneDeepslateRule =  SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.aboveBottom(63), 0),
                        SurfaceRules.sequence(
                                andConditions(SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState()),
                                        SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.aboveBottom(70), 0)),
                                        SurfaceRules.noiseCondition(ModNoiseParameters.DEEPSLATE_NOISE, -1, -0.5)),
                                SurfaceRules.state(Blocks.STONE.defaultBlockState())
                        )),
                SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState()));

        return SurfaceRules.sequence(
                waterSandRule,
                SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(),  beachSandRule),
                SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(),  geyserValleySurfaceRule),
                SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(),  surfaceRule),
                stoneDeepslateRule
        );
    }


    public static SurfaceRules.RuleSource ifElse(SurfaceRules.ConditionSource condition, SurfaceRules.RuleSource whenTrue, SurfaceRules.RuleSource whenFalse) {
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(condition, whenTrue),
                whenFalse
        );
    }

    private static SurfaceRules.RuleSource andConditions(SurfaceRules.RuleSource ifTrue, SurfaceRules.ConditionSource... conditions){
        if (conditions == null || conditions.length == 0){
            return ifTrue;
        }
        var start = SurfaceRules.ifTrue(conditions[0], ifTrue);

        for (int i = 1; i < conditions.length; i++){
            start = SurfaceRules.ifTrue(conditions[i], start);
        }
        return start;
    }
}
