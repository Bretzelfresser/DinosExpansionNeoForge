package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoOrderMode;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class DinoTamedFollowOwnerBehavior {
    public static OneShot<BaseDinoEntity<?>> create(float speedModifier) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.registered(MemoryModuleType.LOOK_TARGET)
        ).apply(instance, (walkTarget, lookTarget) ->
                (level, dino, gameTime) -> {
                    if (!dino.canMove() || !dino.isTamed()) {
                        return false;
                    }
                    if (dino.getOrderMode() != DinoOrderMode.FOLLOW) {
                        return false;
                    }
                    
                    LivingEntity owner = dino.getOwner();
                    if (owner == null || !owner.isAlive() || owner.level() != dino.level()) {
                        return false;
                    }

                    double distanceSq = dino.distanceToSqr(owner);
                    if (distanceSq > 256.0D) { // Teleport if > 16 blocks away
                        double x = owner.getX() + (dino.getRandom().nextDouble() - 0.5D) * 3.0D;
                        double y = owner.getY();
                        double z = owner.getZ() + (dino.getRandom().nextDouble() - 0.5D) * 3.0D;
                        dino.moveTo(x, y, z);
                        dino.getNavigation().stop();
                        walkTarget.erase();
                        return true;
                    } else if (distanceSq > 25.0D) { // Follow if > 5 blocks away
                        walkTarget.set(new WalkTarget(new EntityTracker(owner, false), speedModifier, 3));
                        lookTarget.set(new EntityTracker(owner, true));
                        return true;
                    } else if (distanceSq < 9.0D) { // Stop if within 3 blocks
                        walkTarget.erase();
                        return true;
                    }
                    return false;
                }
        ));
    }
}
