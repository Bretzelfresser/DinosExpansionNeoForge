package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DinosExpansion.MODID);

    public static final DeferredItem<Item> TEST_DINO_SADDLE = ITEMS.register("test_dino_saddle",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> NARCOTICS = ITEMS.register("narcotics",
            () -> new Item(new Item.Properties().component(ModDataComponents.NARCOTIC_VALUE.get(), 40.0F)));
}
