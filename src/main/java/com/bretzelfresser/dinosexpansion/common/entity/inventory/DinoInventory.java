package com.bretzelfresser.dinosexpansion.common.entity.inventory;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import java.util.Collection;

public class DinoInventory extends CombinedInvWrapper {


    protected DinoEquipmentInventory equipmentInventory;
    protected DynamicInventory inventory;
    protected final BaseDinoEntity dino;

    public DinoInventory(BaseDinoEntity dino) {
        super();
        this.dino = dino;
        equipmentInventory = new DinoEquipmentInventory(dino.getEquipments());
        equipmentInventory.addListener(this::updateEquipment);


    }

    protected void updateEquipment(DinoEquipment equipment){
        if (equipment == DinoEquipment.SADDLE){
            dino.setSaddled(!equipmentInventory.getEquipment(equipment).isEmpty());
        }
    }

    public ItemStackHandler getEquipmentInventory() {
        return equipmentInventory;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public Collection<ItemStack> updateInventorySize(int newSize){
        return inventory.updateSize(newSize);
    }
}
