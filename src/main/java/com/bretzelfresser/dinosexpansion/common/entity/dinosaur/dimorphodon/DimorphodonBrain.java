package com.bretzelfresser.dinosexpansion.common.entity.dinosaur.dimorphodon;

import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.bretzelfresser.dinosexpansion.common.entity.ai.behavior.FindTreeBehavior;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModActivities;
import com.bretzelfresser.dinosexpansion.common.init.ModSensors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.RandomStroll;
import net.minecraft.world.entity.ai.behavior.RunOne;
import net.minecraft.world.entity.ai.behavior.SetEntityLookTarget;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
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
                        SensorType.NEAREST_LIVING_ENTITIES,
                        ModSensors.SHOULD_SLEEP.get()
                )
        );
    }

    public static Brain<Dimorphodon> createBrain(Brain<Dimorphodon> brain) {
        DinoBrain.initCoreActivity(brain);
        initIdleActivity(brain);
        initFlyActivity(brain);
        DinoBrain.initUnconsciousActivity(brain);
        DinoBrain.initSleepActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateActivity(Dimorphodon entity) {
        Brain<?> brain = entity.getBrain();
        Activity defaultActivity = entity.isFlying() ? ModActivities.FLY.get() : Activity.IDLE;
        brain.setActiveActivityToFirstValid(ImmutableList.of(
                ModActivities.UNCONSCIOUS.get(),
                ModActivities.SLEEP.get(),
                defaultActivity
        ));
    }

    private static void initFlyActivity(Brain<Dimorphodon> brain) {
        brain.addActivity(ModActivities.FLY.get(), Util.make(ImmutableList.<Pair<Integer, ? extends BehaviorControl<? super Dimorphodon>>>builder(), builder -> {
            builder.add(Pair.of(0, FindTreeBehavior.create()));
            builder.add(Pair.of(1, continuousFlyWander()));
            builder.add(Pair.of(2, StartAttacking.create(BaseDinoEntity::findAttackTarget)));
        }).build());
    }

    private static void initIdleActivity(Brain<Dimorphodon> brain) {
        brain.addActivity(Activity.IDLE, Util.make(ImmutableList.<Pair<Integer, ? extends BehaviorControl<? super Dimorphodon>>>builder(), builder -> {
            builder.add(Pair.of(1, new RunOne<>(ImmutableList.of(
                    Pair.of(RandomStroll.stroll(1.0F), 1),
                    Pair.of(SetEntityLookTarget.create(6.0F), 3),
                    Pair.of(new DoNothing(60, 140), 4)
            ))));
            builder.add(Pair.of(2, StartAttacking.create(BaseDinoEntity::findAttackTarget)));
        }).build());
    }

    public static BehaviorControl<Dimorphodon> continuousFlyWander() {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.absent(MemoryModuleType.WALK_TARGET)
        ).apply(instance, walkTarget -> (level, dino, gameTime) -> {
            if (!dino.isFlying()) {
                return false;
            }
            Vec3 target = AirAndWaterRandomPos.getPos(dino, 12, 7, -2, dino.getViewVector(0.0F).x, dino.getViewVector(0.0F).z, (float) (Math.PI / 2));
            if (target == null) {
                target = AirAndWaterRandomPos.getPos(dino, 8, 5, 0, 0, 0, (float) (Math.PI / 2));
            }
            if (target != null) {
                walkTarget.set(new WalkTarget(target, 1.0F, 0));
                return true;
            }
            return false;
        }));
    }
}
