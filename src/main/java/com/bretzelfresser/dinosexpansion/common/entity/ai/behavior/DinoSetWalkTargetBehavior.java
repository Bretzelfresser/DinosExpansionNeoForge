package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.attack.DinoAttack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class DinoSetWalkTargetBehavior {
    public static OneShot<BaseDinoEntity> setWalkTarget(float speedModifier) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.registered(MemoryModuleType.WALK_TARGET)
        ).apply(instance, (attackTarget, walkTarget) ->
                (level, dino, gameTime) -> {
                    if (!dino.canMove()) {
                        return false;
                    }
                    LivingEntity target = instance.get(attackTarget);
                    if (!target.isAlive()) {
                        return false;
                    }

                    // Check if the target is within reach of any available attack
                    boolean anyAttackInRange = false;
                    for (DinoAttack attack : dino.getAvailableAttacks()) {
                        if (!dino.isAttackOnCooldown(attack.getName()) && attack.canUse(dino, target)) {
                            anyAttackInRange = true;
                            break;
                        }
                    }

                    // If target is out of range of all attacks, set WalkTarget
                    if (!anyAttackInRange) {
                        walkTarget.set(new WalkTarget(new EntityTracker(target, false), speedModifier, 1));
                        return true;
                    }
                    
                    // If target is in range of an attack, clear WalkTarget so we stop to attack
                    walkTarget.erase();
                    return true;
                }
        ));
    }
}
