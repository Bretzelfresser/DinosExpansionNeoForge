package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;

public class ModEnchantments {
    public static final ResourceKey<Enchantment> TORPOR_ENCHANTMENT = ResourceKey.create(
            Registries.ENCHANTMENT,
            DinosExpansion.modLoc("torpor_enchantment")
    );

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);

        context.register(
                TORPOR_ENCHANTMENT,
                new Enchantment.Builder(
                        Enchantment.definition(
                                items.getOrThrow(Tags.Items.TORPOR_WEAPONS), // supported items
                                items.getOrThrow(Tags.Items.TORPOR_WEAPONS), // primary items
                                5, // weight (rare)
                                3, // max level
                                Enchantment.constantCost(10), // min cost
                                Enchantment.constantCost(30), // max cost
                                2, // anvil cost
                                EquipmentSlotGroup.MAINHAND // slot group
                        )
                )
                .withEffect(
                        ModEnchantmentEffectComponents.TORPOR_MODIFICATION.get(),
                        new AddValue(new LevelBasedValue.Linear(10.0F, 10.0F)) // +10 per level
                )
                .build(TORPOR_ENCHANTMENT.location())
        );
    }
}
