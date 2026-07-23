package com.bretzelfresser.dinosexpansion.registry;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.menu.DinoContainerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, DinosExpansion.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<DinoContainerMenu>> DINO_MENU = MENUS.register("dino_menu",
            () -> IMenuTypeExtension.create((windowId, inv, data) -> {
                int entityId = data.readInt();
                return new DinoContainerMenu(windowId, inv, entityId);
            }));
}
