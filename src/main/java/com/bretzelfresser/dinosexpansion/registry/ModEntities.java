package com.bretzelfresser.dinosexpansion.registry;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.entity.TestDinoEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, DinosExpansion.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TestDinoEntity>> TEST_DINO = ENTITIES.register("test_dino",
            () -> EntityType.Builder.of(TestDinoEntity::new, MobCategory.CREATURE)
                    .sized(1.0F, 1.5F)
                    .build("test_dino"));
}
