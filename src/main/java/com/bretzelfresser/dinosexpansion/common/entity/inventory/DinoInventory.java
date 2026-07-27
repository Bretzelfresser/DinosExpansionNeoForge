package com.bretzelfresser.dinosexpansion.common.entity.inventory;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import com.bretzelfresser.dinosexpansion.util.NbtUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import java.util.Collection;

public class DinoInventory extends CombinedInvWrapper implements INBTSerializable<CompoundTag> {


    protected DinoEquipmentInventory equipmentInventory;
    protected DynamicInventory inventory;
    protected final BaseDinoEntity dino;

    public DinoInventory(BaseDinoEntity dino, int size) {
        super();
        this.dino = dino;
        equipmentInventory = new DinoEquipmentInventory(dino.getEquipments());
        equipmentInventory.addListener(this::updateEquipment);

        this.inventory = new DynamicInventory(size);


    }

    protected void updateEquipment(DinoEquipment equipment) {
        if (equipment == DinoEquipment.SADDLE) {
            dino.setSaddled(!equipmentInventory.getEquipment(equipment).isEmpty());
        }
        if (equipment == DinoEquipment.CHEST){
            int newSize = dino.getChestSize(equipmentInventory.getEquipment(equipment));
            if (this.inventory.getSlots() != newSize) {
                var items = this.updateInventorySize(newSize);
                if (!this.dino.level().isClientSide()) {
                    for (var item : items) {
                        this.dino.spawnAtLocation(item, 1.0f);
                    }
                }
            }
        }
    }

    public DinoEquipmentInventory getEquipmentInventory() {
        return equipmentInventory;
    }

    public DynamicInventory getChestInventory() {
        return inventory;
    }

    /**
     *
     * @param newSize
     * @return a collection of stacks which couldnt be added the the new inventory with the size
     */
    public Collection<ItemStack> updateInventorySize(int newSize) {
        return inventory.updateSize(newSize);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("equipmentInventory", this.equipmentInventory.serializeNBT(provider));
        tag.put("chestInventory", this.getChestInventory().serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NbtUtils.setIfExists(nbt, "equipmentInventory", CompoundTag::getCompound, compoundTag -> equipmentInventory.deserializeNBT(provider, compoundTag));
        NbtUtils.setIfExists(nbt, "chestInventory", CompoundTag::getCompound, compoundTag -> inventory.deserializeNBT(provider, compoundTag));
    }
}
