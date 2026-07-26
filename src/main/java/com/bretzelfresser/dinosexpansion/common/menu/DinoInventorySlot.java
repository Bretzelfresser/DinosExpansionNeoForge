package com.bretzelfresser.dinosexpansion.common.menu;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DinoInventorySlot extends SlotItemHandler {
    private final BaseDinoEntity dino;
    private final int dinoSlotIndex;

    public DinoInventorySlot(IItemHandlerModifiable container, int index, int x, int y, BaseDinoEntity dino, int dinoSlotIndex) {
        super(container, index, x, y);
        this.dino = dino;
        this.dinoSlotIndex = dinoSlotIndex;
    }

    @Override
    public boolean isActive() {
        return this.dinoSlotIndex < this.dino.getInventorySize();
    }
}
