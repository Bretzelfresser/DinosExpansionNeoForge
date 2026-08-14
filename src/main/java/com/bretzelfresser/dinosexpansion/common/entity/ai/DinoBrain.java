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

    /**
     * this is the default core activity, this core activit wont execute when either sleeping or unconscious doe to having the look wim and walk behaviors
     * this makes the entity actually drown when knocked out or sleeping inside water
     */
    public static <T extends BaseDinoEntity<T>> void initCoreActivity(Brain<T> brain) {
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
    public static <T extends BaseDinoEntity<T>> void initIdleAttackingActivity(Brain<T> brain) {
        initIdleActivity(brain, true);
    }
    /**
     * will initialize the {@link Activity#IDLE} without searching for an attackable target vial {@link BaseDinoEntity#findAttackTarget()}
     */
    public static <T extends BaseDinoEntity<T>> void initIdlePassiveActivity(Brain<T> brain) {
        initIdleActivity(brain, false);
    }

    private static <T extends BaseDinoEntity<T>> void initIdleActivity(Brain<T> brain, boolean attacking) {
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

    /**
     * basic tamed idle, this will all of the normal goals like wandering, following owner etc. behind the orders given to the entity
     */
    public static <T extends BaseDinoEntity<T>> void initTamedIdleActivity(Brain<T> brain) {
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
    public static <T extends BaseDinoEntity<T>> void initUnconsciousActivity(Brain<T> brain) {
        // When unconscious, eat narcotics if low torpor, eat preferred food if hungry, otherwise do nothing
        brain.addActivityWithConditions(ModActivities.UNCONSCIOUS.get(), ImmutableList.of(
                Pair.of(0, NarcoticBehaviour.eatNarcotics(true)),
                Pair.of(2, new DoNothing(100, 200))
        ), Set.of(
                Pair.of(ModMemoryModules.UNCONSCIOUS.get(), MemoryStatus.VALUE_PRESENT)
        ));
    }

    /**
     * basically an activity which will be used for sleeping, u can add custom behaviors while sleeping here
     * but this will just make the entity do nothing
     */
    public static <T extends BaseDinoEntity<T>> void initSleepActivity(Brain<T> brain) {
        // When unconscious, do absolutely nothing but sleep
        brain.addActivityWithConditions(ModActivities.SLEEP.get(), ImmutableList.of(
                Pair.of(0, new DoNothing(100, 200))
        ), Set.of(
                Pair.of(ModMemoryModules.SLEEPING.get(), MemoryStatus.VALUE_PRESENT)
        ));
    }

    /**
     * this will initialize the fight activity, with basic behaviors, like wakling to the target,
     * then choosing one of the registered attacks, and even stopping attacking when the attack target becomes invalid
     */
    public static <T extends BaseDinoEntity<T>> void initFightActivity(Brain<T> brain) {
        brain.addActivityWithConditions(Activity.FIGHT, ImmutableList.of(
                Pair.of(0, DinoSetWalkTargetBehavior.setWalkTarget(1.25F)),
                Pair.of(1, DinoAttackBehavior.attack()),
                Pair.of(2, StopAttackingIfTargetInvalid.create())
        ), Set.of(
                Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
        ));
    }

    /**
     * actually this is just kept there as a blueprint, i dont think this will actually be used ever again
     * @param dino
     */
    public static void updateActivity(BaseDinoEntity<?> dino) {
        Brain<?> brain = dino.getBrain();
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
