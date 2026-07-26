package com.bretzelfresser.dinosexpansion.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DinosaurConfig {

    public final ModConfigSpec.DoubleValue BUFFERED_TORPOR_REDUCTION;
    public final ModConfigSpec.DoubleValue MIN_BUFFERED_TORPOR_REDUCTION;

    public DinosaurConfig(ModConfigSpec.Builder builder) {
        builder.push("Dinosaurs");

        //those values directly resmeble that the buffered torpor reaches 10% of its original value in 5 seconds regardless of how much torpor is stored

        builder.comment("When hitting the entity, the torpor gets buffered and then slowly added to the actual torpor of the entity.");
        builder.comment("This is the percentage of how much of the buffered torpor is added to the entity every tick");
        BUFFERED_TORPOR_REDUCTION = builder.defineInRange("buffered_torpor_reduction", 0.13, 0, 1);


        //as this is dependant on the actual buffered torpor, this can be reached faster or slower, basically this checks for the derivative between steps, if its this value or lower and then proceed using this value
        //when the buffered value is 1, then this will tart at 0.38 seconds -> not linear -> exponential
        builder.comment("when we constantly reduce the buffered torpor by a percentage we take very long on the last ones");
        builder.comment("this defines a min value so we wont take forever on small values");
        MIN_BUFFERED_TORPOR_REDUCTION = builder.defineInRange("min_buffered_torpor_reduction", 0.1, 0.001, Double.MAX_VALUE);

        builder.pop();
    }
}
