package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Optional;

public class DinoAcquireTargetBehavior extends Behavior<BaseDinoEntity> {
    public DinoAcquireTargetBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT
        ));
    }

    @Override
    protected void start(ServerLevel level, BaseDinoEntity owner, long gameTime) {
        Optional<? extends LivingEntity> targetOpt = owner.findAttackTarget();
        targetOpt.ifPresent(livingEntity -> owner.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity));

    }
}
