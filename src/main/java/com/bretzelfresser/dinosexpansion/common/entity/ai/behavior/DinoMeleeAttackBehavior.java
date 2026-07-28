package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.attack.DinoAttack;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DinoMeleeAttackBehavior {

    public static OneShot<BaseDinoEntity> meleeAttack() {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET)).apply(instance, (attacktarget, lookTarget) ->
                (level, dino, gameTime) -> {
                    if (!dino.canMove())
                        return false;
                    LivingEntity target = instance.get(attacktarget);
                    if (!target.isAlive())
                        return false;
                    //actually  trying to look at the target
                    lookTarget.set(new EntityTracker(target, true));
                    // Choose and perform an attack
                    DinoAttack chosenAttack = chooseAttack(dino, target);
                    if (chosenAttack != null) {
                        dino.performAttack(chosenAttack);
                        return true;
                    }

                    return false;
                }
        ));
    }



    @Nullable
    private static LivingEntity getAttackTarget(BaseDinoEntity owner) {
        return owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    @Nullable
    private static DinoAttack chooseAttack(BaseDinoEntity owner, LivingEntity target) {
        List<DinoAttack> usableAttacks = new ArrayList<>();
        for (DinoAttack attack : owner.getAvailableAttacks()) {
            if (!owner.isAttackOnCooldown(attack.getName()) && attack.canUse(owner, target)) {
                usableAttacks.add(attack);
            }
        }
        if (usableAttacks.isEmpty()) {
            return null;
        }
        return chooseAttackByWeight(usableAttacks, owner, target);
    }

    private static DinoAttack chooseAttackByWeight(List<DinoAttack> usableAttacks, BaseDinoEntity owner, LivingEntity target) {
        double totalWeight = 0;
        for (DinoAttack attack : usableAttacks) {
            totalWeight += attack.getSelectionWeight(owner, target);
        }
        if (totalWeight <= 0) {
            return usableAttacks.get(owner.getRandom().nextInt(usableAttacks.size()));
        }
        double randomValue = owner.getRandom().nextDouble() * totalWeight;
        double currentSum = 0;
        for (DinoAttack attack : usableAttacks) {
            currentSum += attack.getSelectionWeight(owner, target);
            if (randomValue <= currentSum) {
                return attack;
            }
        }
        return usableAttacks.get(usableAttacks.size() - 1);
    }
}
