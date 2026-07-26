package com.bretzelfresser.dinosexpansion.common.menu.slot;

import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import com.bretzelfresser.dinosexpansion.common.entity.inventory.DinoEquipmentInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class DinoEquipmentSlot extends SlotItemHandler {

    DinoEquipment slot;
    DinoEquipmentInventory inventory;

    public DinoEquipmentSlot(DinoEquipmentInventory dinoEquipmentInventory, DinoEquipment equipment, int xPosition, int yPosition) {
        super(dinoEquipmentInventory, 0, xPosition, yPosition);
        this.slot = equipment;
        this.inventory = dinoEquipmentInventory;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return inventory.isItemValid(getSlotIndex(), stack);
    }

    @Override
    public int getSlotIndex() {
        return this.inventory.getSlot(this.slot);
    }

    @Override
    public int getContainerSlot() {
        return getSlotIndex();
    }

    @Override
    public ItemStack getItem() {
        return inventory.getEquipment(slot);
    }

    // Override if your IItemHandler does not implement IItemHandlerModifiable
    @Override
    public void set(ItemStack stack) {
        inventory.setEquipment(slot, stack);
        this.setChanged();
    }

    // Override if your IItemHandler does not implement IItemHandlerModifiable
    // @Override
    public void initialize(ItemStack stack) {
        inventory.setEquipment(slot, stack);
        this.setChanged();
    }

    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {
    }

    @Override
    public int getMaxStackSize() {
        return this.inventory.getSlotLimit(this.getSlotIndex());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), this.inventory.getSlotLimit(this.getSlotIndex()));
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !inventory.extractItem(slot, 1, true).isEmpty();
    }

    @Override
    public ItemStack remove(int amount) {
        return inventory.extractItem(slot, amount, false);
    }

    @Override
    public boolean isActive() {
        return inventory.hasEquipment(slot);
    }

    @Override
    public IItemHandler getItemHandler() {
        return this.inventory;
    }
}
