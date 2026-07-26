package com.bretzelfresser.dinosexpansion.common.menu.slot;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DinoInventorySlot extends SlotItemHandler {
    private final BaseDinoEntity dino;

    public DinoInventorySlot(BaseDinoEntity dino, int index, int x, int y) {
        super(dino.getChestInventory(), index, x, y);
        this.dino = dino;
    }

    @Override
    public ItemStack getItem() {
        if (!isActive())
            return ItemStack.EMPTY;
        return super.getItem();
    }

    @Override
    public void initialize(ItemStack stack) {
        if (!isActive())
            return;
        super.initialize(stack);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        if (!isActive())
            return false;
        return super.mayPickup(playerIn);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!isActive())
            return false;
        return super.mayPlace(stack);
    }

    @Override
    public void set(ItemStack stack) {
        if (isActive()) {
            super.set(stack);
        }
    }

    @Override
    public ItemStack remove(int amount) {
        if (!isActive())
            return ItemStack.EMPTY;
        return super.remove(amount);
    }

    @Override
    public ItemStack safeInsert(ItemStack stack) {
        return super.safeInsert(stack);
    }

    @Override
    public boolean isActive() {
        return this.index < this.dino.getChestInventory().getSlots();
    }
}
