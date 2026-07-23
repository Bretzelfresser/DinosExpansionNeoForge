package com.bretzelfresser.dinosexpansion.common.menu;

import com.bretzelfresser.dinosexpansion.common.entity.BaseDinoEntity;
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
