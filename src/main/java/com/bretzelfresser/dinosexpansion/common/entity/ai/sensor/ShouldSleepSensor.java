package com.bretzelfresser.dinosexpansion.common.entity.ai.sensor;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.Set;

public class ShouldSleepSensor extends Sensor<BaseDinoEntity<?>> {

    public ShouldSleepSensor() {
        super(20);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(ModMemoryModules.SHOULD_SLEEP.get());
    }

    @Override
    protected void doTick(ServerLevel level, BaseDinoEntity<?> entity) {
        if (entity.getSleepBehaviour().isSleepTime() && entity.getSleepBehaviour().canSleepConditionsMet()) {
            entity.getBrain().setMemory(ModMemoryModules.SHOULD_SLEEP.get(), Unit.INSTANCE);
        } else {
            entity.getBrain().eraseMemory(ModMemoryModules.SHOULD_SLEEP.get());
        }
    }
}
