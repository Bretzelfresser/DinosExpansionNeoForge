package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.dinosaur.ceratosaurus.Certosaurus;
import com.bretzelfresser.dinosexpansion.common.entity.dinosaur.dimorphodon.Dimorphodon;
import com.bretzelfresser.dinosexpansion.common.entity.misc.TranquilizerArrow;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, DinosExpansion.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<Certosaurus>> CERATOSAURS = register("ceratosaurs",
            EntityType.Builder.of(Certosaurus::new, MobCategory.CREATURE)
                    .sized(1.0F, 2.0F)
                    .eyeHeight(1.9f)
                    .attach(EntityAttachment.PASSENGER, new Vec3(0, 2.0f, -0.2f)));

    public static final DeferredHolder<EntityType<?>, EntityType<Dimorphodon>> DIMORPHODON = register("dimorphodon",
            EntityType.Builder.of(Dimorphodon::new, MobCategory.CREATURE)
                    .sized(0.8F, 0.8F)
                    .eyeHeight(0.6f));

    public static final DeferredHolder<EntityType<?>, EntityType<TranquilizerArrow>> TRANQUILIZER_ARROW = register("tranquilizer_arrow",
            EntityType.Builder.<TranquilizerArrow>of(TranquilizerArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20));

    public static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        return register(name, () -> builder);
    }

    public static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, Supplier<EntityType.Builder<T>> builder) {
        return ENTITIES.register(name, () -> builder.get().build(name));
    }
}
