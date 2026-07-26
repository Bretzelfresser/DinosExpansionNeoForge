package com.bretzelfresser.dinosexpansion.gametest;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import com.bretzelfresser.dinosexpansion.common.entity.inventory.DinoEquipmentInventory;
import com.bretzelfresser.dinosexpansion.common.entity.inventory.DynamicInventory;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.Collection;
import java.util.EnumMap;
import java.util.function.Predicate;

@GameTestHolder(DinosExpansion.MODID)
public class InventoryGameTests {

    @GameTest(template = "empty", batch = "inventory tests")
    public static void testDynamicInventoryResizeLarger(GameTestHelper helper) {
        DynamicInventory inv = new DynamicInventory(3);
        ItemStack diamond = new ItemStack(Items.DIAMOND, 5);
        inv.setStackInSlot(0, diamond);

        Collection<ItemStack> overflow = inv.updateSize(5);
        helper.assertTrue(overflow.isEmpty(), "Overflow should be empty when resizing larger");
        helper.assertTrue(inv.getSlots() == 5, "New size should be 5");
        helper.assertTrue(ItemStack.matches(inv.getStackInSlot(0), diamond), "Items should persist in original slot");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "inventory tests")
    public static void testDynamicInventoryResizeSmaller(GameTestHelper helper) {
        DynamicInventory inv = new DynamicInventory(3);
        ItemStack diamond = new ItemStack(Items.DIAMOND, 10);
        ItemStack emerald = new ItemStack(Items.EMERALD, 15);
        inv.setStackInSlot(0, diamond);
        inv.setStackInSlot(2, emerald);

        Collection<ItemStack> overflow = inv.updateSize(2);
        helper.assertTrue(inv.getSlots() == 2, "New size should be 2");
        helper.assertTrue(ItemStack.matches(inv.getStackInSlot(0), diamond), "Diamonds should remain in slot 0");
        helper.assertTrue(ItemStack.matches(inv.getStackInSlot(1), emerald), "Emeralds should compress/move to slot 1");
        helper.assertTrue(overflow.isEmpty(), "Overflow should be empty as all items fit after compression");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "inventory tests")
    public static void testDynamicInventoryResizeSmallerWithOverflow(GameTestHelper helper) {
        DynamicInventory inv = new DynamicInventory(3);
        ItemStack diamond = new ItemStack(Items.DIAMOND, 64);
        ItemStack emerald = new ItemStack(Items.EMERALD, 64);
        ItemStack gold = new ItemStack(Items.GOLD_INGOT, 64);
        inv.setStackInSlot(0, diamond);
        inv.setStackInSlot(1, emerald);
        inv.setStackInSlot(2, gold);

        Collection<ItemStack> overflow = inv.updateSize(2);
        helper.assertTrue(inv.getSlots() == 2, "New size should be 2");
        helper.assertTrue(ItemStack.matches(inv.getStackInSlot(0), diamond), "Slot 0 should have diamonds");
        helper.assertTrue(ItemStack.matches(inv.getStackInSlot(1), emerald), "Slot 1 should have emeralds");
        helper.assertTrue(overflow.size() == 1, "There should be 1 overflow item stack");
        ItemStack overflowItem = overflow.iterator().next();
        helper.assertTrue(ItemStack.matches(overflowItem, gold), "Overflow item should be gold");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "inventory tests")
    public static void testDinoEquipmentInventoryValidity(GameTestHelper helper) {
        EnumMap<DinoEquipment, Predicate<ItemStack>> config = new EnumMap<>(DinoEquipment.class);
        config.put(DinoEquipment.SADDLE, stack -> stack.is(Items.SADDLE));
        config.put(DinoEquipment.ARMOR, stack -> stack.is(Items.IRON_CHESTPLATE));

        DinoEquipmentInventory inv = new DinoEquipmentInventory(config);
        ItemStack saddle = new ItemStack(Items.SADDLE);
        ItemStack diamond = new ItemStack(Items.DIAMOND);

        helper.assertTrue(inv.isItemValid(0, saddle), "Saddle should be valid in saddle slot");
        helper.assertTrue(!inv.isItemValid(0, diamond), "Diamond should not be valid in saddle slot");

        ItemStack remaining = inv.insertItem(DinoEquipment.SADDLE, saddle, false);
        helper.assertTrue(remaining.isEmpty(), "Saddle should be fully inserted");
        helper.assertTrue(ItemStack.matches(inv.getEquipment(DinoEquipment.SADDLE), saddle), "Saddle should be in slot");

        ItemStack invalidRemaining = inv.insertItem(DinoEquipment.SADDLE, diamond, false);
        helper.assertTrue(ItemStack.matches(invalidRemaining, diamond), "Invalid item should not be inserted");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "inventory tests")
    public static void testDinoEquipmentInventoryUpdate(GameTestHelper helper) {
        EnumMap<DinoEquipment, Predicate<ItemStack>> initialConfig = new EnumMap<>(DinoEquipment.class);
        initialConfig.put(DinoEquipment.SADDLE, stack -> true);
        initialConfig.put(DinoEquipment.ARMOR, stack -> true);

        DinoEquipmentInventory inv = new DinoEquipmentInventory(initialConfig);
        ItemStack saddle = new ItemStack(Items.SADDLE);
        ItemStack armor = new ItemStack(Items.IRON_CHESTPLATE);

        inv.setEquipment(DinoEquipment.SADDLE, saddle);
        inv.setEquipment(DinoEquipment.ARMOR, armor);

        EnumMap<DinoEquipment, Predicate<ItemStack>> newConfig = new EnumMap<>(DinoEquipment.class);
        newConfig.put(DinoEquipment.SADDLE, stack -> true);
        newConfig.put(DinoEquipment.CHEST, stack -> true);

        Collection<ItemStack> dropped = inv.update(newConfig);

        helper.assertTrue(ItemStack.matches(inv.getEquipment(DinoEquipment.SADDLE), saddle), "Saddle should remain");
        helper.assertTrue(dropped.size() == 1, "Dropped list should contain 1 item stack");
        ItemStack droppedItem = dropped.iterator().next();
        helper.assertTrue(ItemStack.matches(droppedItem, armor), "Dropped item should be armor");
        helper.succeed();
    }
}
