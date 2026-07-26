package com.bretzelfresser.dinosexpansion.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DinosaurConfig {

    public final ModConfigSpec.DoubleValue PERCENTAGE_BUFFERED_TORPOR_REDUCTION;
    public final ModConfigSpec.DoubleValue FLAT_BUFFERED_TORPOR_REDUCTION;

    public DinosaurConfig(ModConfigSpec.Builder builder) {
        builder.push("Dinosaurs");

        builder.comment("When hitting the entity, the torpor gets buffered and then slowly added to the actual torpor of the entity.");
        builder.comment("This is the percentage coefficient of the stacked torpor added to the entity every tick");
        PERCENTAGE_BUFFERED_TORPOR_REDUCTION = builder.defineInRange("percentage_buffered_torpor_reduction", 0.001, 0.0, 1.0);

        builder.comment("This is the flat rate of stacked torpor added to the entity every tick");
        FLAT_BUFFERED_TORPOR_REDUCTION = builder.defineInRange("flat_buffered_torpor_reduction", 0.01, 0.0, Double.MAX_VALUE);

        builder.pop();
    }
}
