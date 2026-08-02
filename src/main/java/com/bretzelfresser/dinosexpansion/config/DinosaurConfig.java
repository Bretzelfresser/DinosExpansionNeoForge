package com.bretzelfresser.dinosexpansion.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DinosaurConfig {

    public final ModConfigSpec.DoubleValue PERCENTAGE_BUFFERED_TORPOR_REDUCTION;
    public final ModConfigSpec.DoubleValue FLAT_BUFFERED_TORPOR_REDUCTION;
    public final ModConfigSpec.IntValue MIN_LEVEL;
    public final ModConfigSpec.IntValue MAX_LEVEL;
    public final ModConfigSpec.IntValue AVERAGE_LEVEL;
    public final ModConfigSpec.DoubleValue NATURAL_REGENERATION_HUNGER_THRESHOLD;
    public final ModConfigSpec.DoubleValue SPRINT_STAMINA_COST;
    public final ModConfigSpec.DoubleValue SPRINT_HUNGER_COST;
    public final ModConfigSpec.DoubleValue STAMINA_REGEN_HUNGER_COST;
    public final ModConfigSpec.DoubleValue JUMP_STAMINA_COST;

    public DinosaurConfig(ModConfigSpec.Builder builder) {
        builder.push("Dinosaurs");

        builder.comment("When hitting the entity, the torpor gets buffered and then slowly added to the actual torpor of the entity.");
        builder.comment("This is the percentage coefficient of the stacked torpor added to the entity every tick");
        PERCENTAGE_BUFFERED_TORPOR_REDUCTION = builder.defineInRange("percentage_buffered_torpor_reduction", 0.01, 0.0, 1.0);

        builder.comment("This is the flat rate of stacked torpor added to the entity every tick");
        FLAT_BUFFERED_TORPOR_REDUCTION = builder.defineInRange("flat_buffered_torpor_reduction", 0.01, 0.0, Double.MAX_VALUE);

        builder.comment("The minimum level with which a wild dinosaur can spawn.");
        MIN_LEVEL = builder.defineInRange("min_level", 1, 1, Integer.MAX_VALUE);

        builder.comment("The maximum level with which a wild dinosaur can spawn.");
        MAX_LEVEL = builder.defineInRange("max_level", 150, 1, Integer.MAX_VALUE);

        builder.comment("The average level (peak of Gaussian distribution) with which a wild dinosaur can spawn. Set to sopmething smaller then 0 to use the midpoint between min and max level.");
        AVERAGE_LEVEL = builder.defineInRange("average_level", -1, -1, Integer.MAX_VALUE);

        builder.comment("The percentage of max hunger required for natural regeneration to occur (e.g. 0.9 for 90%).");
        NATURAL_REGENERATION_HUNGER_THRESHOLD = builder.defineInRange("natural_regeneration_hunger_threshold", 0.9, 0.0, 1.0);

        builder.comment("The amount of stamina drained per tick while sprinting.");
        SPRINT_STAMINA_COST = builder.defineInRange("sprint_stamina_cost", 0.15, 0.0, Double.MAX_VALUE);

        builder.comment("The extra amount of hunger drained per tick while sprinting.");
        SPRINT_HUNGER_COST = builder.defineInRange("sprint_hunger_cost", 0.01, 0.0, Double.MAX_VALUE);

        builder.comment("The extra amount of hunger drained per tick while actively regenerating stamina.");
        STAMINA_REGEN_HUNGER_COST = builder.defineInRange("stamina_regen_hunger_cost", 0.02, 0.0, Double.MAX_VALUE);

        builder.comment("The amount of stamina consumed when performing a jump.");
        JUMP_STAMINA_COST = builder.defineInRange("jump_stamina_cost", 5.0, 0.0, Double.MAX_VALUE);

        builder.pop();
    }
}
