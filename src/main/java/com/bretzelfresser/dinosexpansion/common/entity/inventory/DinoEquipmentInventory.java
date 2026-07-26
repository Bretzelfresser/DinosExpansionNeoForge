package com.bretzelfresser.dinosexpansion.common.entity.inventory;

import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DinoEquipmentInventory extends ItemStackHandler {

    EnumMap<DinoEquipment, Predicate<ItemStack>> equipments;
    final EnumMap<DinoEquipment, Integer> equipmentMapper = new EnumMap<>(DinoEquipment.class);
    final Map<Integer, DinoEquipment> slotMapper = new TreeMap<>();
    List<Consumer<DinoEquipment>> listener = new ArrayList<>();

    public DinoEquipmentInventory(EnumMap<DinoEquipment, Predicate<ItemStack>> equipments) {
        super(equipments.size());
        this.equipments = equipments;
        setupMappings();
    }

    public void addListener(Consumer<DinoEquipment> onContentsChange){
        this.listener.add(onContentsChange);
    }

    protected void setupMappings() {
        int index = 0;
        for (var equipmentSlot : equipments.keySet()) {
            equipmentMapper.put(equipmentSlot, index);
            slotMapper.put(index, equipmentSlot);
            index++;
        }
    }


    /**
     *
     * @param equipments the new set of equipments
     * @return a list of items that couldnt be inserted into the new inventory
     */
    public Collection<ItemStack> update(EnumMap<DinoEquipment, Predicate<ItemStack>> equipments) {

        EnumMap<DinoEquipment, ItemStack> oldStacks = new EnumMap<>(DinoEquipment.class);
        for (var eq : this.equipments.keySet()) {
            var stack = getEquipment(eq);
            oldStacks.put(eq, stack);
        }
        //resetting our stacks
        this.stacks = NonNullList.withSize(equipments.size(), ItemStack.EMPTY);
        this.equipments = equipments;
        setupMappings();
        var itemList = new ArrayList<ItemStack>();
        for (var pair : oldStacks.entrySet()){
            if (hasEquipment(pair.getKey()))
                setEquipment(pair.getKey(), pair.getValue());
            else
                itemList.add(pair.getValue());
        }

        return itemList;
    }

    public boolean hasEquipment(DinoEquipment equipment){
        return this.equipments.containsKey(equipment);
    }

    public ItemStack getEquipment(DinoEquipment equipment) {
        if (!this.equipmentMapper.containsKey(equipment))
            return ItemStack.EMPTY;
        return getStackInSlot(equipmentMapper.get(equipment));
    }

    public void setEquipment(DinoEquipment equipment, ItemStack stack) {
        if (!this.equipmentMapper.containsKey(equipment))
            return;
        setStackInSlot(equipmentMapper.get(equipment), stack);
    }


    public ItemStack insertItem(DinoEquipment eq, ItemStack stack, boolean simulate) {
        if (!equipmentMapper.containsKey(eq)){
            return stack;
        }
       var slot = equipmentMapper.get(eq);
        return insertItem(slot, stack, simulate);
    }

    public ItemStack extractItem(DinoEquipment eq, int amount, boolean simulate) {
        if (!equipmentMapper.containsKey(eq)){
            return ItemStack.EMPTY;
        }
        var slot = equipmentMapper.get(eq);
        return extractItem(slot, amount, simulate);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        var optional = slotMapper.getOrDefault(slot, null);
        if (optional == null)
            return false;
        return equipments.getOrDefault(optional, s -> false).test(stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        //this musn`t ever return null
        var eq = this.slotMapper.getOrDefault(slot, null);
        super.onContentsChanged(slot);
        onContentsChange(eq);
    }

    public void onContentsChange(DinoEquipment equipmentSLot) {
        this.listener.forEach(l -> l.accept(equipmentSLot));
    }
}
