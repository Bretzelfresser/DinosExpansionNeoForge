package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoOrderMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.List;

public class DinoTamedLookBehavior {
    public static OneShot<BaseDinoEntity<?>> create(float maxDist) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.absent(MemoryModuleType.LOOK_TARGET)
        ).apply(instance, lookTarget ->
                (level, dino, gameTime) -> {
                    if (dino.getOrderMode() == DinoOrderMode.STAY) {
                        return false;
                    }
                    
                    if (dino.getRandom().nextInt(20) != 0) {
                        return false;
                    }

                    List<LivingEntity> entities = dino.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(List.of());
                    for (LivingEntity target : entities) {
                        if (target.isAlive() && dino.distanceToSqr(target) < maxDist * maxDist) {
                            lookTarget.set(new EntityTracker(target, true));
                            return true;
                        }
                    }
                    return false;
                }
        ));
    }
}
