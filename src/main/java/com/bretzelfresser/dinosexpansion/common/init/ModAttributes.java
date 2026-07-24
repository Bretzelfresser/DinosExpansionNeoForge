package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, DinosExpansion.MODID);

    public static final DeferredHolder<Attribute, Attribute> MAX_TORPOR = ATTRIBUTES.register("max_torpor",
            () -> new RangedAttribute("attribute.name.dinosexpansion.max_torpor", 100.0D, 1.0D, 100000.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> MAX_HUNGER = ATTRIBUTES.register("max_hunger",
            () -> new RangedAttribute("attribute.name.dinosexpansion.max_hunger", 100.0D, 1.0D, 100000.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> HUNGER_DECREASE = ATTRIBUTES.register("hunger_decrease",
            () -> new RangedAttribute("attribute.name.dinosexpansion.hunger_decrease", 0.002, 0, 100000.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> TORPOR_DECREASE = ATTRIBUTES.register("torpor_decrease",
            () -> new RangedAttribute("attribute.name.dinosexpansion.torpor_decrease", 0.001, 0, 100000.0D).setSyncable(true));


    public static final DeferredHolder<Attribute, Attribute> TORPOR_WAKE_UP_THRESHOLD = ATTRIBUTES.register("torpor_wake_up_threshold",
            () -> new RangedAttribute("attribute.name.dinosexpansion.torpor_wake_up_threshold", 0.1, 0, 1).setSyncable(true));
}
