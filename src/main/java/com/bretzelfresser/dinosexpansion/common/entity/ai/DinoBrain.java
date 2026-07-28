package com.bretzelfresser.dinosexpansion.common.entity.ai;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModActivities;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.bretzelfresser.dinosexpansion.common.init.ModDataComponents;
import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.antlr.v4.runtime.misc.Triple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
                Pair.of(1, new RunOne<>(ImmutableList.of(
                        Pair.of(RandomStroll.stroll(1.0F), 2),
                        Pair.of(SetEntityLookTarget.create(6.0F), 1),
                        Pair.of(new DoNothing(30, 60), 1)
                )))
        ));
    }

    /**
     * adds the unconscious activity, which has the requiroment that the {@link ModMemoryModules#UNCONSCIOUS} module is present and then will just eat narcotics when possible
     *
     * @param brain
     */
    public static void initUnconsciousActivity(Brain<BaseDinoEntity> brain) {
        // When unconscious, eat narcotics if low torpor, eat preferred food if hungry, otherwise do nothing
        brain.addActivityWithConditions(ModActivities.UNCONSCIOUS.get(), ImmutableList.of(
                Pair.of(0, eatNarcotics(true, true)),
                Pair.of(2, new DoNothing(100, 200))
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

    public static OneShot<BaseDinoEntity> eatNarcotics(boolean findBiggestBelowThreshold) {
        return eatNarcotics(findBiggestBelowThreshold, true);
    }

    public static OneShot<BaseDinoEntity> eatNarcotics(boolean findBiggestBelowThreshold, boolean onlyWhenTaming) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(ModMemoryModules.UNCONSCIOUS.get())).apply(instance, (unconsciousMemory) ->
                        (serverLevel, dino, gameTime) -> {
                            IItemHandlerModifiable inventory = dino.getChestInventory();
                            if (onlyWhenTaming && !dino.currentlyTaming())
                                return false;
                            float missingTorpor = dino.getMissingTorpor();
                            if (missingTorpor <= 0) {
                                return false;
                            }

                            List<Triple<Integer, ItemStack, Float>> narcoticStacks = new ArrayList<>();

                            for (int i = 0; i < inventory.getSlots(); i++) {
                                ItemStack stack = inventory.getStackInSlot(i);
                                if (!stack.isEmpty() && stack.has(ModDataComponents.NARCOTIC_VALUE.get())) {
                                    float val = stack.getOrDefault(ModDataComponents.NARCOTIC_VALUE.get(), 0f);
                                    narcoticStacks.add(new Triple<>(i, stack, val));
                                }
                            }
                            if (narcoticStacks.isEmpty())
                                return false;

                            if (findBiggestBelowThreshold) {
                                narcoticStacks.sort(Comparator.<Triple<Integer, ItemStack, Float>>comparingDouble(t -> t.c)
                                        .reversed()
                                        .thenComparingInt(t -> t.a));
                            }
                            int narcoticIndex = 0;

                            while (narcoticIndex < narcoticStacks.size()) {
                                var stack = narcoticStacks.get(narcoticIndex).b;
                                var narcoticValue = narcoticStacks.get(narcoticIndex).c;

                                missingTorpor = dino.getSurvivalBehaviour().getTotalMissingTorpor();


                                if (!dino.getSurvivalBehaviour().shouldWakeUpFromUnconscious(1) && missingTorpor < narcoticValue){
                                    break;
                                }

                                dino.getSurvivalBehaviour().applyBufferedNarcotics(narcoticValue);
                                stack.shrink(1);
                                if (stack.isEmpty()){
                                    narcoticIndex++;
                                }

                            }
                            return true;
                        }
                )

        );
    }
}
