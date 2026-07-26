package com.bretzelfresser.dinosexpansion.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TorporConfig {


    public final ModConfigSpec.DoubleValue DAMAGE_REDUCTION, DAMAGE_SCALING;
    public final ModConfigSpec.DoubleValue OVER_MAX_LIMIT_EFFECTIVENESS;

    public TorporConfig(ModConfigSpec.Builder builder){
        builder.push("Torpor");

        builder.comment("The percentage of the original damage which should be inflicted if the weapon had torpor on it");
        DAMAGE_REDUCTION = builder.defineInRange("torpor_damage_reduction", 0.1d, 0, 1);

        builder.comment("The scaling which the torpor should be increased based on the damage that would have been inflicted");
        builder.comment("0 means no damage scale 2 means inflicted torpor gets increase be 2 times of the original damage");
        builder.comment("this damage isnt influenced by the damage reduction");
        DAMAGE_SCALING = builder.defineInRange("torpor_damage_scaling", 2, 0, Float.MAX_VALUE);

        builder.comment("The effectiveness of applied stacked torpor when it exceeds the dinosaur's max torpor");
        OVER_MAX_LIMIT_EFFECTIVENESS = builder.defineInRange("over_max_limit_effectiveness", 0.6, 0.0, 1.0);

        builder.pop();
    }
}
