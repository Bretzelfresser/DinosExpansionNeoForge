package com.bretzelfresser.dinosexpansion.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DinosaurConfig {

    public final ModConfigSpec.DoubleValue BUFFERED_TORPOR_REDUCTION;
    public final ModConfigSpec.DoubleValue MIN_BUFFERED_TORPOR_REDUCTION;

    public DinosaurConfig(ModConfigSpec.Builder builder) {
        builder.push("Dinosaurs");

        builder.comment("When hitting the entity, the torpor gets buffered and then slowly added to the actual torpor of the entity.");
        builder.comment("This is the percentage of how much of the buffered torpor is added to the entity every tick");
        BUFFERED_TORPOR_REDUCTION = builder.defineInRange("buffered_torpor_reduction", 0.05, 0, 1);

        builder.comment("when we constantly reduce the buffered torpor by a percentage we take very long on the last ones");
        builder.comment("this defines a min value so we wont take forever on small values");
        MIN_BUFFERED_TORPOR_REDUCTION = builder.defineInRange("min_biffered_torpor_reduction", 0.5, 0.001, Double.MAX_VALUE);

        builder.pop();
    }
}
