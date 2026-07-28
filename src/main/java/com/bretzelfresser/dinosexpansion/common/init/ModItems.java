package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.item.TranquilizerArrowItem;
import com.bretzelfresser.dinosexpansion.common.item.ZoomItem;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DinosExpansion.MODID);

    public static final DeferredItem<Item> TEST_DINO_SADDLE = ITEMS.register("test_dino_saddle",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> NARCOTICS = ITEMS.register("narcotics",
            () -> new Item(new Item.Properties().component(ModDataComponents.NARCOTIC_VALUE.get(), 10.0F)));

    public static final DeferredItem<Item> TRANQUILIZER_ARROW = ITEMS.register("tranquilizer_arrow",
            () -> new TranquilizerArrowItem(new Item.Properties().component(ModDataComponents.NARCOTIC_VALUE.get(), 10.0F)));

    public static final DeferredItem<Item> SPYGLASS = ITEMS.register("spyglass",
            () -> new ZoomItem(new Item.Properties().stacksTo(1), 0.5f, false));

    public static final DeferredItem<Item> ADVANCED_SPYGLASS = ITEMS.register("advanced_spyglass",
            () -> new ZoomItem(new Item.Properties().stacksTo(1), 0.5f, true));

    public static final DeferredItem<Item> BASIC_KIBBLE = ITEMS.register("basic_kibble",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SIMPLE_KIBBLE = ITEMS.register("simple_kibble",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> REGULAR_KIBBLE = ITEMS.register("regular_kibble",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SUPERIOR_KIBBLE = ITEMS.register("superior_kibble",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> EXCEPTIONAL_KIBBLE = ITEMS.register("exceptional_kibble",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> EXTRAORDINARY_KIBBLE = ITEMS.register("extraordinary_kibble",
            () -> new Item(new Item.Properties()));
}
