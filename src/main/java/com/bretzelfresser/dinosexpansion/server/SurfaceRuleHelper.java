package com.bretzelfresser.dinosexpansion.server;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class SurfaceRuleHelper {



    public static SurfaceRules.RuleSource generateDinoSurfaceRules(){
        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState())),
                SurfaceRules.state(Blocks.DIRT.defaultBlockState()));
    }
}
