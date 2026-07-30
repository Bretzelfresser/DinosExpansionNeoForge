package com.bretzelfresser.dinosexpansion.common.entity;

import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BrainUtils {


    public static <E extends LivingEntity> void setOrAddExpiry(Brain<E> brain, MemoryModuleType<Unit> memory, long ticks){
        setOrAddExpiry(brain, memory, Unit.INSTANCE, ticks);
    }

    /**
     *
     * @param brain the brain we are working on
     * @param memory the Memory module we want to set
     * @param value the value thats gonna be there, this will overwrite the old value
     * @param ticks the ticks until expirery
     * @param <E> the entity the brain is from
     * @param <V> the type of value the memory module holds
     */
    public static <E extends LivingEntity, V> void setOrAddExpiry(Brain<E> brain, MemoryModuleType<V> memory, V value, long ticks){
        long previousExpiry = brain.getTimeUntilExpiry(memory);
        brain.setMemoryWithExpiry(memory, value, previousExpiry + ticks);
    }
}
