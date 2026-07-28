package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DinosExpansion.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DINOS_EXPANSION_TAB =
            CREATIVE_MODE_TABS.register("dinosexpansion_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.dinosexpansion"))
                    .icon(() -> new ItemStack(ModItems.BASIC_KIBBLE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.TEST_DINO_SADDLE.get());
                        output.accept(ModItems.NARCOTICS.get());
                        output.accept(ModItems.TRANQUILIZER_ARROW.get());
                        output.accept(ModItems.SPYGLASS.get());
                        output.accept(ModItems.ADVANCED_SPYGLASS.get());
                        output.accept(ModItems.BASIC_KIBBLE.get());
                        output.accept(ModItems.SIMPLE_KIBBLE.get());
                        output.accept(ModItems.REGULAR_KIBBLE.get());
                        output.accept(ModItems.SUPERIOR_KIBBLE.get());
                        output.accept(ModItems.EXCEPTIONAL_KIBBLE.get());
                        output.accept(ModItems.EXTRAORDINARY_KIBBLE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
