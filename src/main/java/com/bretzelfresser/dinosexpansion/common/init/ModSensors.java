package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.bretzelfresser.dinosexpansion.common.entity.ai.sensor.ShouldSleepSensor;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSensors {

    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(Registries.SENSOR_TYPE, DinosExpansion.MODID);

    public static final DeferredHolder<SensorType<?>, SensorType<ShouldSleepSensor>> SHOULD_SLEEP = SENSOR_TYPES.register("should_sleep", () -> new SensorType<>(ShouldSleepSensor::new));

}
