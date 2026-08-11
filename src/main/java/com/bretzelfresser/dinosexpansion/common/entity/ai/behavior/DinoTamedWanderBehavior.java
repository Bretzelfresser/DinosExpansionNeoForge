package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoOrderMode;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class DinoTamedWanderBehavior {
    public static OneShot<BaseDinoEntity> create(float speedModifier) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.absent(MemoryModuleType.WALK_TARGET)
        ).apply(instance, walkTarget ->
                (level, dino, gameTime) -> {
                    if (!dino.canMove() || !dino.isTamed()) {
                        return false;
                    }
                    if (dino.getOrderMode() != DinoOrderMode.WANDER) {
                        return false;
                    }
                    
                    if (dino.getRandom().nextInt(120) != 0) { // Check occasionally
                        return false;
                    }

                    Vec3 randomPos = DefaultRandomPos.getPos(dino, 10, 7);
                    if (randomPos != null) {
                        walkTarget.set(new WalkTarget(randomPos, speedModifier, 1));
                        return true;
                    }
                    return false;
                }
        ));
    }
}
