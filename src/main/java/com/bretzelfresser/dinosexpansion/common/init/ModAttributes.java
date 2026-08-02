package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.mojang.datafixers.types.Func;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;
import java.util.function.Function;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, DinosExpansion.MODID);

    public static final DeferredHolder<Attribute, Attribute> MAX_TORPOR = register("max_torpor",
            key -> new RangedAttribute(key, 100.0D, 1.0D, 100000.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> MAX_HUNGER = register("max_hunger",
            key -> new RangedAttribute(key, 100.0D, 1.0D, 100000.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> CARRYING_CAPACITY = register("carrying_capacity",
            key -> new RangedAttribute(key, 4.0D, 1.0D, 36.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> HUNGER_DECREASE = register("hunger_decrease",
            key -> new RangedAttribute(key, 0.005, 0, 100000.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> TORPOR_DECREASE = register("torpor_decrease",
            key -> new RangedAttribute(key, 0.01, 0, 100000.0D).setSyncable(true));


    public static final DeferredHolder<Attribute, Attribute> TORPOR_WAKE_UP_THRESHOLD = register("torpor_wake_up_threshold",
            key -> new RangedAttribute(key, 0.1, 0, 1).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> NATURAL_REGENERATION = register("natural_regeneration",
            key -> new RangedAttribute(key, 0.01D, 0.0D, 100000.0D).setSyncable(true));



    public static <T extends Attribute> DeferredHolder<Attribute, T> register(String name, Function<String, T> generator){
        return ATTRIBUTES.register(name, () -> generator.apply(makeAttributeDescription(name)));
    }

    public static String makeAttributeDescription(String name){
        return "attribute.name." + DinosExpansion.MODID + "." + name;
    }
}
