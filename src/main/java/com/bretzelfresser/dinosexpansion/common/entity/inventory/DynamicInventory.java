package com.bretzelfresser.dinosexpansion.common.entity.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class DynamicInventory extends ItemStackHandler {

    protected Optional<BiPredicate<Integer, ItemStack>> validItem = Optional.empty();

    public DynamicInventory() {
    }

    public DynamicInventory(int size) {
        super(size);
    }

    public DynamicInventory(NonNullList<ItemStack> stacks) {
        super(stacks);
    }


    public DynamicInventory addFilter(@NotNull BiPredicate<Integer, ItemStack> filter) {
        this.validItem = Optional.of(filter);
        return this;
    }

    public DynamicInventory addFilter(@NotNull Predicate<ItemStack> filter) {
        return addFilter((i, s) -> filter.test(s));
    }

    public DynamicInventory removeFilter() {
        this.validItem = Optional.empty();
        return this;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return validItem.map(c -> c.test(slot, stack)).orElse(super.isItemValid(slot, stack));
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

        for (var stack : oldStacks) {
            var remainingSDtack = ItemHandlerHelper.insertItemStacked(this, stack, false);
            if (!remainingSDtack.isEmpty()) {
                notInsertedIntoNewSize.add(remainingSDtack);
            }
        }
        return notInsertedIntoNewSize;

    }
}
