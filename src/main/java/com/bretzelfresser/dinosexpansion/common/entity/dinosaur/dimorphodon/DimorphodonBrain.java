package com.bretzelfresser.dinosexpansion.common.entity.dinosaur.dimorphodon;

import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;

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

    private static void initIdleActivity(Brain<Dimorphodon> brain) {
        brain.addActivity(Activity.IDLE, Util.make(ImmutableList.<Pair<Integer, ? extends BehaviorControl<? super Dimorphodon>>>builder(), builder -> {
            builder.add(Pair.of(1, new RunOne<>(ImmutableList.of(
                    Pair.of(wanderOrFly(), 2),
                    Pair.of(SetEntityLookTarget.create(6.0F), 1),
                    Pair.of(new DoNothing(40, 80), 1)
            ))));
            builder.add(Pair.of(2, StartAttacking.create(BaseDinoEntity::findAttackTarget)));
        }).build());
    }

    public static BehaviorControl<Dimorphodon> wanderOrFly() {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.absent(MemoryModuleType.WALK_TARGET)
        ).apply(instance, walkTarget -> (level, dino, gameTime) -> {
            if (dino.isFlying()) {
                Vec3 target = AirAndWaterRandomPos.getPos(dino, 10, 7, -2, dino.getViewVector(0.0F).x, dino.getViewVector(0.0F).z, (float) (Math.PI / 2));
                if (target == null) {
                    target = AirAndWaterRandomPos.getPos(dino, 8, 4, 0, 0, 0, (float) (Math.PI / 2));
                }
                if (target != null) {
                    walkTarget.set(new WalkTarget(target, 1.0F, 0));
                    return true;
                }
            } else {
                Vec3 target = LandRandomPos.getPos(dino, 6, 3);
                if (target != null) {
                    walkTarget.set(new WalkTarget(target, 1.0F, 0));
                    return true;
                }
            }
            return false;
        }));
    }
}
