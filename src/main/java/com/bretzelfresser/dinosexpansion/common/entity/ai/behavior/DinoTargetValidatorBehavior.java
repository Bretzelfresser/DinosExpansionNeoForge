package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class DinoTargetValidatorBehavior extends Behavior<BaseDinoEntity<?>> {
    public DinoTargetValidatorBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT
        ));
    }

    @Override
    protected void start(ServerLevel level, BaseDinoEntity<?> owner, long gameTime) {
        LivingEntity target = owner.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null || !target.isAlive() || target.level() != owner.level() || owner.distanceToSqr(target) > 256.0D) {
            owner.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }
}
