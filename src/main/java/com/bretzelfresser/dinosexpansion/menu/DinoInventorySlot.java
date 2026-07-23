package com.bretzelfresser.dinosexpansion.menu;

import com.bretzelfresser.dinosexpansion.entity.BaseDinoEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class DinoInventorySlot extends Slot {
    private final BaseDinoEntity dino;
    private final int dinoSlotIndex;

    public DinoInventorySlot(Container container, int index, int x, int y, BaseDinoEntity dino, int dinoSlotIndex) {
        super(container, index, x, y);
        this.dino = dino;
        this.dinoSlotIndex = dinoSlotIndex;
    }

    @Override
    public boolean isActive() {
        return this.dinoSlotIndex < this.dino.getInventorySize();
    }
}
