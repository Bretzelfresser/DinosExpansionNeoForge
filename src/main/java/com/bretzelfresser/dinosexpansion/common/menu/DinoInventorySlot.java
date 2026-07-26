package com.bretzelfresser.dinosexpansion.common.menu;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DinoInventorySlot extends SlotItemHandler {
    private final BaseDinoEntity dino;

    public DinoInventorySlot(BaseDinoEntity dino, int index, int x, int y) {
        super(dino.getChestInventory(), index, x, y);
        this.dino = dino;
    }



    @Override
    public boolean isActive() {
        return this.index < this.dino.getChestInventory().getSlots();
    }
}
