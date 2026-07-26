package com.bretzelfresser.dinosexpansion.common.menu;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import com.bretzelfresser.dinosexpansion.common.init.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DinoContainerMenu extends AbstractContainerMenu {
    public final BaseDinoEntity dino;
    public final Container dinoInventory;

    public DinoContainerMenu(int windowId, Inventory playerInv, int entityId) {
        super(ModMenus.DINO_MENU.get(), windowId);
        Player player = playerInv.player;
        this.dino = (BaseDinoEntity) player.level().getEntity(entityId);
        
        if (this.dino != null) {
            this.dinoInventory = this.dino.getInventory();
        } else {
            throw new IllegalArgumentException("Dino Entity with ID " + entityId + " not found!");
        }

        // Slot 0: Saddle Slot
        this.addSlot(new Slot(this.dinoInventory, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.TEST_DINO_SADDLE.get());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Slot 1: Armor Slot
        this.addSlot(new Slot(this.dinoInventory, 1, 8, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Can define specific armor items later
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Slots 2-37: Dino Main Inventory (6 columns x 6 rows)
        int dinoInvStartIndex = 2;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                int slotIdx = dinoInvStartIndex + (row * 6) + col;
                // Position starting at x=80, y=18
                this.addSlot(new DinoInventorySlot(this.dinoInventory, slotIdx, 80 + col * 18, 18 + row * 18, this.dino, slotIdx - dinoInvStartIndex));
            }
        }

        // Player Inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Player Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 198));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.dino != null && this.dino.isAlive() && this.dino.distanceTo(player) < 8.0F && this.dino.canPlayerAccess(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            // If clicking a dino slot (0-37)
            if (index < 38) {
                // Move to player inventory/hotbar
                if (!this.moveItemStackTo(itemstack1, 38, 74, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Clicking player inventory
                // If it is a saddle, try to put in saddle slot (0)
                if (itemstack1.is(ModItems.TEST_DINO_SADDLE.get())) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Else try to put in main dino inventory slots (2 to 2 + active size)
                else {
                    int activeDinoSlots = this.dino.getInventorySize();
                    if (activeDinoSlots > 0) {
                        if (!this.moveItemStackTo(itemstack1, 2, 2 + activeDinoSlots, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}
