package com.bretzelfresser.dinosexpansion.common.entity.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.Collection;

public class DynamicInventory extends ItemStackHandler {

    public DynamicInventory() {
    }

    public DynamicInventory(int size) {
        super(size);
    }

    public DynamicInventory(NonNullList<ItemStack> stacks) {
        super(stacks);
    }


    /**
     *
     * @param newSize the new size of the inventory
     * @return a list of items which cant be inserted into the new inventory size, especially useful when size gets smaller
     */
    public Collection<ItemStack> updateSize(int newSize) {
        Collection<ItemStack> notInsertedIntoNewSize = new ArrayList<>();

        var oldStacks = NonNullList.copyOf(this.stacks);

        this.stacks = NonNullList.withSize(newSize, ItemStack.EMPTY);

        for (var stack : oldStacks){
            var remainingSDtack = ItemHandlerHelper.insertItemStacked(this, stack, false);
            if (!remainingSDtack.isEmpty()){
                notInsertedIntoNewSize.add(remainingSDtack);
            }
        }
        return notInsertedIntoNewSize;

    }
}
