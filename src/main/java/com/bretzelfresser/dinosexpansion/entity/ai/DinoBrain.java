package com.bretzelfresser.dinosexpansion.entity.ai;

import com.bretzelfresser.dinosexpansion.entity.BaseDinoEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

public class DinoBrain {

    public static Brain<?> makeBrain(Brain<BaseDinoEntity> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initUnconsciousActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<BaseDinoEntity> brain) {
        brain.addActivity(Activity.CORE, ImmutableList.of(
                Pair.of(0, new Swim(0.8F)),
                Pair.of(1, new LookAtTargetSink(45, 90)),
                Pair.of(2, new MoveToTargetSink())
        ));
    }

    private static void initIdleActivity(Brain<BaseDinoEntity> brain) {
        brain.addActivity(Activity.IDLE, ImmutableList.of(
                Pair.of(0, new RunOne<>(ImmutableList.of(
                        Pair.of(RandomStroll.stroll(1.0F), 2),
                        Pair.of(SetEntityLookTarget.create(6.0F), 1),
                        Pair.of(new DoNothing(30, 60), 1)
                )))
        ));
    }

    private static void initUnconsciousActivity(Brain<BaseDinoEntity> brain) {
        // When unconscious, do absolutely nothing but sleep
        brain.addActivity(Activity.REST, ImmutableList.of(
                Pair.of(0, new DoNothing(100, 200))
        ));
    }

    public static void updateActivity(BaseDinoEntity dino) {
        Brain<BaseDinoEntity> brain = dino.getBrain();
        if (dino.isUnconscious()) {
            brain.setActiveActivityIfPossible(Activity.REST);
        } else {
            brain.setActiveActivityIfPossible(Activity.IDLE);
        }
    }
}
