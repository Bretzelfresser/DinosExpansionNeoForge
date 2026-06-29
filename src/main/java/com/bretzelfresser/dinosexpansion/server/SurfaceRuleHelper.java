package com.bretzelfresser.dinosexpansion.server;

import com.bretzelfresser.dinosexpansion.common.init.ModNoiseParameters;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class SurfaceRuleHelper {



    public static SurfaceRules.RuleSource generateDinoSurfaceRules(){

        var waterSandGlassRule = andConditions(
                ifElse(
                        SurfaceRules.noiseCondition(ModNoiseParameters.CONTINENTS, -0.2f),
                        SurfaceRules.state(Blocks.SAND.defaultBlockState()),
                        SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())),
                SurfaceRules.not(SurfaceRules.waterBlockCheck(0,1)),
                SurfaceRules.ON_FLOOR,
                SurfaceRules.abovePreliminarySurface()
        );

        var surfaceRule = SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(4, false, CaveSurface.FLOOR), SurfaceRules.state(Blocks.DIRT.defaultBlockState())));


        var stoneDeepslateRule =  SurfaceRules.sequence(SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.aboveBottom(63), 0),
                        SurfaceRules.sequence(
                                andConditions(SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState()),
                                        SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.aboveBottom(70), 0)),
                                        SurfaceRules.noiseCondition(ModNoiseParameters.DEEPSLATE_NOISE, -1, -0.5)),
                                SurfaceRules.state(Blocks.STONE.defaultBlockState())
                        )),
                SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState()));

        return SurfaceRules.sequence(
                waterSandGlassRule,
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
