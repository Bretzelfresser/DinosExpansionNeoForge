package com.bretzelfresser.dinosexpansion.common.entity.ai;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModActivities;
import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
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

import java.util.Set;

public class DinoBrain {


    /**
     *
     * @return a builder of an immutable list of all the essential memory modules a dino should have
     */
    public static ImmutableList.Builder<MemoryModuleType<?>> baseDinoMemoryModules() {
        return ImmutableList.<MemoryModuleType<?>>builder()
                .add(MemoryModuleType.WALK_TARGET)
                .add(MemoryModuleType.LOOK_TARGET)
                .add(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                .add(MemoryModuleType.PATH)
                .add(ModMemoryModules.UNCONSCIOUS.get())
                .add(ModMemoryModules.SLEEPING.get());
    }

    public static Brain<?> makeBrain(Brain<BaseDinoEntity> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initUnconsciousActivity(brain);
        initSleepActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<BaseDinoEntity> brain) {
        brain.addActivityAndRemoveMemoriesWhenStopped(Activity.CORE, ImmutableList.of(
                        Pair.of(0, new Swim(0.8F)),
                        Pair.of(1, new LookAtTargetSink(45, 90)),
                        Pair.of(2, new MoveToTargetSink())
                ), ImmutableSet.of(
                        Pair.of(ModMemoryModules.UNCONSCIOUS.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(ModMemoryModules.SLEEPING.get(), MemoryStatus.VALUE_ABSENT)
                ), Set.of(
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.LOOK_TARGET,
                        MemoryModuleType.PATH,
                        MemoryModuleType.ATTACK_TARGET
                )
        );
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

    /**
     * adds the unconscious activity, which has the requiroment that the {@link ModMemoryModules#UNCONSCIOUS} module is present and then does nothing
     *
     * @param brain
     */
    public static void initUnconsciousActivity(Brain<BaseDinoEntity> brain) {
        // When unconscious, do absolutely nothing but sleep
        brain.addActivityWithConditions(ModActivities.UNCONSCIOUS.get(), ImmutableList.of(
                Pair.of(0, new DoNothing(100, 200))
        ), Set.of(
                Pair.of(ModMemoryModules.UNCONSCIOUS.get(), MemoryStatus.VALUE_PRESENT)
        ));
    }

    public static void initSleepActivity(Brain<BaseDinoEntity> brain) {
        // When unconscious, do absolutely nothing but sleep
        brain.addActivityWithConditions(ModActivities.SLEEP.get(), ImmutableList.of(
                Pair.of(0, new DoNothing(100, 200))
        ), Set.of(
                Pair.of(ModMemoryModules.SLEEPING.get(), MemoryStatus.VALUE_PRESENT)
        ));
    }

    public static void updateActivity(BaseDinoEntity dino) {
        Brain<BaseDinoEntity> brain = dino.getBrain();
        brain.setActiveActivityToFirstValid(ImmutableList.of(ModActivities.UNCONSCIOUS.get(), ModActivities.SLEEP.get(), Activity.IDLE));
    }
}
