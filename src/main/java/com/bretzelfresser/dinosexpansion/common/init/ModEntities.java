package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.Certosaurus;
import com.bretzelfresser.dinosexpansion.common.entity.TranquilizerArrow;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, DinosExpansion.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Certosaurus>> TEST_DINO = ENTITIES.register("test_dino",
            () -> EntityType.Builder.of(Certosaurus::new, MobCategory.CREATURE)
                    .sized(1.0F, 1.5F)
                    .build("test_dino"));

    public static final DeferredHolder<EntityType<?>, EntityType<TranquilizerArrow>> TRANQUILIZER_ARROW = ENTITIES.register("tranquilizer_arrow",
            () -> EntityType.Builder.<TranquilizerArrow>of(TranquilizerArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("tranquilizer_arrow"));
}
