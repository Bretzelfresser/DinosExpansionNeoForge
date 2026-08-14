package com.bretzelfresser.dinosexpansion.common.entity.dinosaur.dimorphodon;

import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class DimorphodonBrain {

    public static Brain.Provider<?> makeBrainProvider() {
        return Brain.provider(
                DinoBrain.baseDinoMemoryModules()
                        .add(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                        .add(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                        .add(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
                        .add(MemoryModuleType.NEAREST_PLAYERS)
                        .add(MemoryModuleType.NEAREST_VISIBLE_PLAYER)
                        .add(MemoryModuleType.ATTACK_TARGET)
                        .build(),
                ImmutableList.of(
                        SensorType.NEAREST_PLAYERS,
                        SensorType.NEAREST_LIVING_ENTITIES
                )
        );
    }

    public static Brain<Dimorphodon> createBrain(Brain<Dimorphodon> brain) {
        DinoBrain.initCoreActivity(brain);
        initIdleActivity(brain);
        DinoBrain.initUnconsciousActivity(brain);
        DinoBrain.initSleepActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateActivity(Dimorphodon entity) {
        entity.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

    private static <T extends BaseDinoEntity<T>> void initIdleActivity(Brain<T> brain) {
        brain.addActivity(Activity.IDLE, Util.make(ImmutableList.<Pair<Integer, ? extends BehaviorControl<? super T>>>builder(), builder-> {
            builder.add(Pair.of(1, new RunOne<>(ImmutableList.of(
                    Pair.of(RandomStroll.fly(1.0F), 2),
                    Pair.of(SetEntityLookTarget.create(6.0F), 1),
                    Pair.of(new DoNothing(60, 120), 1)
            ))));
            builder.add(Pair.of(1, StartAttacking.create(BaseDinoEntity::findAttackTarget)));
        }).build());
    }

}
