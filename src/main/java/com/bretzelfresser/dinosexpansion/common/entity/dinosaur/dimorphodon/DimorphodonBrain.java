package com.bretzelfresser.dinosexpansion.common.entity.dinosaur.dimorphodon;

import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

public class DimorphodonBrain {

    public static Brain.Provider<?> makeBrainProvider(){
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

    public static Brain<Dimorphodon> createBrain(Brain<Dimorphodon> brain){

    }

    public static void updateActivity(Brain<Dimorphodon> brain){
        brain.setActiveActivityToFirstValid(ImmutableList.of(Activity.IDLE));
    }

}
