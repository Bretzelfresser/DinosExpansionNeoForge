package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class ModMemoryModules {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, DinosExpansion.MODID);


    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SLEEPING = register("sleeping", Unit.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> UNCONSCIOUS = register("unconscious", Unit.CODEC);



    public static <I> DeferredHolder<MemoryModuleType<?>, MemoryModuleType<I>> register(String name, Codec<I> codec){
        return MEMORY_MODULE_TYPES.register(name, () -> new MemoryModuleType<>(Optional.of(codec)));
    }
}
