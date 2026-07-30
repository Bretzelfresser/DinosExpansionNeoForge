package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.effect.CeratosaurusRoarEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, DinosExpansion.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> CERATOSAURUS_ROAR = MOB_EFFECTS.register("ceratosaurus_roar", CeratosaurusRoarEffect::new);
}
