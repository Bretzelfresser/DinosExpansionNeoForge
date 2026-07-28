package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.attack.DinoAttack;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DinoMeleeAttackBehavior extends Behavior<BaseDinoEntity> {
    public DinoMeleeAttackBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, BaseDinoEntity owner) {
        if (owner.isSleeping() || owner.isUnconscious()) {
            return false;
        }
        LivingEntity target = getAttackTarget(owner);
        return target != null && target.isAlive();
    }

    @Override
    protected void start(ServerLevel level, BaseDinoEntity owner, long gameTime) {
        LivingEntity target = getAttackTarget(owner);
        if (target == null) return;

        // Face the target
        owner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));

        // Choose and perform an attack
        DinoAttack chosenAttack = chooseAttack(owner, target);
        if (chosenAttack != null) {
            owner.performAttack(chosenAttack);
        }
    }

    @Nullable
    private LivingEntity getAttackTarget(BaseDinoEntity owner) {
        return owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    @Nullable
    private DinoAttack chooseAttack(BaseDinoEntity owner, LivingEntity target) {
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

    private DinoAttack chooseAttackByWeight(List<DinoAttack> usableAttacks, BaseDinoEntity owner, LivingEntity target) {
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
