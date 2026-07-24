package com.bretzelfresser.dinosexpansion.common.init;


import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;

import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE =
            DeferredRegister.create(Registries.PARTICLE_TYPE, DinosExpansion.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SLEEPING_PARTICLES = PARTICLE_TYPE.register("sleeping", () -> new SimpleParticleType(true));


}
