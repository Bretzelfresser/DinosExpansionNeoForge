package com.bretzelfresser.dinosexpansion.common.entity.ai;

import com.bretzelfresser.dinosexpansion.common.entity.ai.behavior.NarcoticBehaviour;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.ai.behavior.DinoAcquireTargetBehavior;
import com.bretzelfresser.dinosexpansion.common.entity.ai.behavior.DinoAttackBehavior;
import com.bretzelfresser.dinosexpansion.common.entity.ai.behavior.DinoSetWalkTargetBehavior;
import com.bretzelfresser.dinosexpansion.common.entity.ai.behavior.DinoTargetValidatorBehavior;
import com.bretzelfresser.dinosexpansion.common.entity.ai.behavior.DinoTamedFollowOwnerBehavior;
import com.bretzelfresser.dinosexpansion.common.entity.ai.behavior.DinoTamedWanderBehavior;
import com.bretzelfresser.dinosexpansion.common.entity.ai.behavior.DinoTamedLookBehavior;
import com.bretzelfresser.dinosexpansion.common.init.ModActivities;
import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;

public class DinoBrain {

    public static ImmutableList.Builder<MemoryModuleType<?>> baseDinoMemoryModules() {
        return baseDinoMemoryModules(ImmutableList.builder());
    }

    /**
     *
     * @return a builder of an immutable list of all the essential memory modules a dino should have
     */
    public static ImmutableList.Builder<MemoryModuleType<?>> baseDinoMemoryModules(ImmutableList.Builder<MemoryModuleType<?>> list) {
        return list.add(MemoryModuleType.WALK_TARGET)
                .add(MemoryModuleType.LOOK_TARGET)
                .add(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
                .add(MemoryModuleType.PATH)
                .add(ModMemoryModules.UNCONSCIOUS.get())
                .add(ModMemoryModules.SLEEPING.get());
    }

    public static void initCoreActivity(Brain<? extends BaseDinoEntity> brain) {
        brain.addActivityWithConditions(Activity.CORE, ImmutableList.of(
                        Pair.of(0, new Swim(0.8F)),
                        Pair.of(1, new LookAtTargetSink(45, 90)),
                        Pair.of(2, new MoveToTargetSink())
                ), ImmutableSet.of(
                        Pair.of(ModMemoryModules.UNCONSCIOUS.get(), MemoryStatus.VALUE_ABSENT),
                        Pair.of(ModMemoryModules.SLEEPING.get(), MemoryStatus.VALUE_ABSENT)
                )
        );
    }
    /**
     * will initialize the {@link Activity#IDLE} with searching for an attackable target vial {@link BaseDinoEntity#findAttackTarget()}
     */
    public static <T extends BaseDinoEntity> void initIdleAttackingActivity(Brain<T> brain) {
        initIdleActivity(brain, true);
    }

    /**
     * will initialize the {@link Activity#IDLE} without searching for an attackable target vial {@link BaseDinoEntity#findAttackTarget()}
     */
    public static <T extends BaseDinoEntity> void initIdlePassiveActivity(Brain<T> brain) {
        initIdleActivity(brain, false);
    }

    private static <T extends BaseDinoEntity> void initIdleActivity(Brain<T> brain, boolean attacking) {
        brain.addActivity(Activity.IDLE, Util.make(ImmutableList.<Pair<Integer, ? extends BehaviorControl<? super T>>>builder(), builder-> {
            builder.add(Pair.of(1, new RunOne<>(ImmutableList.of(
                    Pair.of(RandomStroll.stroll(1.0F), 2),
                    Pair.of(SetEntityLookTarget.create(6.0F), 1),
                    Pair.of(new DoNothing(60, 120), 1)
            ))));
            if (attacking) {
                builder.add(Pair.of(1, StartAttacking.create(BaseDinoEntity::findAttackTarget)));
            }
        }).build());
    }

    public static void initTamedIdleActivity(Brain<? extends BaseDinoEntity> brain) {
        brain.addActivity(ModActivities.TAMED_IDLE.get(), ImmutableList.of(
                Pair.of(1, DinoTamedFollowOwnerBehavior.create(1.0F)),
                Pair.of(2, DinoTamedWanderBehavior.create(1.0F)),
                Pair.of(3, DinoTamedLookBehavior.create(6.0F)),
                Pair.of(4, new DoNothing(60, 120))
        ));
    }

    /**
     * adds the unconscious activity, which has the requiroment that the {@link ModMemoryModules#UNCONSCIOUS} module is present and then will just eat narcotics when possible
     *
     * @param brain
     */
    public static void initUnconsciousActivity(Brain<? extends BaseDinoEntity> brain) {
        // When unconscious, eat narcotics if low torpor, eat preferred food if hungry, otherwise do nothing
        brain.addActivityWithConditions(ModActivities.UNCONSCIOUS.get(), ImmutableList.of(
                Pair.of(0, NarcoticBehaviour.eatNarcotics(true)),
                Pair.of(2, new DoNothing(100, 200))
        ), Set.of(
                Pair.of(ModMemoryModules.UNCONSCIOUS.get(), MemoryStatus.VALUE_PRESENT)
        ));
    }

    public static void initSleepActivity(Brain<? extends BaseDinoEntity> brain) {
        // When unconscious, do absolutely nothing but sleep
        brain.addActivityWithConditions(ModActivities.SLEEP.get(), ImmutableList.of(
                Pair.of(0, new DoNothing(100, 200))
        ), Set.of(
                Pair.of(ModMemoryModules.SLEEPING.get(), MemoryStatus.VALUE_PRESENT)
        ));
    }

    public static void initFightActivity(Brain<? extends BaseDinoEntity> brain) {
        brain.addActivityWithConditions(Activity.FIGHT, ImmutableList.of(
                Pair.of(0, DinoSetWalkTargetBehavior.setWalkTarget(1.25F)),
                Pair.of(1, DinoAttackBehavior.attack()),
                Pair.of(2, StopAttackingIfTargetInvalid.create())
        ), Set.of(
                Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
        ));
    }

    public static void updateActivity(BaseDinoEntity dino) {
        Brain<BaseDinoEntity> brain = dino.getBrain();
        if (dino.isTamed()) {
            brain.setActiveActivityToFirstValid(ImmutableList.of(
                    ModActivities.UNCONSCIOUS.get(),
                    ModActivities.SLEEP.get(),
                    Activity.FIGHT,
                    ModActivities.TAMED_IDLE.get()
            ));
        } else {
            brain.setActiveActivityToFirstValid(ImmutableList.of(
                    ModActivities.UNCONSCIOUS.get(),
                    ModActivities.SLEEP.get(),
                    Activity.FIGHT,
                    Activity.IDLE
            ));
        }
    }
}
