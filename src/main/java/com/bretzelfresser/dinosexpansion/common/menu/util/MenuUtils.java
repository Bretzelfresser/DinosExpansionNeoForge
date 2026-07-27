package com.bretzelfresser.dinosexpansion.common.menu.util;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MenuUtils {

    public static final int HOTBAR_SLOT_COUNT = 9;
    public static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    public static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    public static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    public static final int PLAYER_INVENTORY_WITH_HOTBAR_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;


    public static int addHorizontalSlots(Container handler, int Index, int x, int y, int amount,
                                         int distanceBetweenSlots, Consumer<Slot> slotAdder) {
        return addHorizontalSlots(handler, Index, x, y, amount, distanceBetweenSlots, Slot::new, slotAdder);
    }

    public static <T> int addHorizontalSlots(T handler, int Index, int x, int y, int amount,
                                             int distanceBetweenSlots, ISlotProvider<T> provider, Consumer<Slot> slotAdder) {
        for (int i = 0; i < amount; i++) {
            slotAdder.accept(provider.createSlot(handler, Index, x, y));
            Index++;
            x += distanceBetweenSlots;
        }
        return Index;
    }

    public static int addHorizontalSlots(IItemHandler handler, int Index, int x, int y, int amount,
                                         int distanceBetweenSlots, Consumer<Slot> slotAdder) {
        return addHorizontalSlots(handler, Index, x, y, amount, distanceBetweenSlots, SlotItemHandler::new, slotAdder);
    }

    public static int addHorizontalSlots(Container handler, int Index, int x, int y, int amount, Consumer<Slot> slotAdder) {
        return addHorizontalSlots(handler, Index, x, y, amount, 18, slotAdder);
    }

    public static <T> int addHorizontalSlots(T handler, int Index, int x, int y, int amount, ISlotProvider<T> provider, Consumer<Slot> slotAdder) {
        return addHorizontalSlots(handler, Index, x, y, amount, 18, provider, slotAdder);
    }

    public static int addHorizontalSlots(IItemHandler handler, int Index, int x, int y, int amount, Consumer<Slot> slotAdder) {
        return addHorizontalSlots(handler, Index, x, y, amount, 18, SlotItemHandler::new, slotAdder);
    }

    public static void addPlayerInventory(Inventory playerInv, int x, int y, Consumer<Slot> slotAdder) {
        // the player inventory
        addSlotField(playerInv, 9, x, y, 9, 18, 3, 18, slotAdder);
        y += 58;
        // Hotbar
        addHorizontalSlots(playerInv, 0, x, y, 9, 18, slotAdder);
    }


    public static <T> int addSlotField(T handler, int StartIndex, int x, int y, int horizontalAmount,
                                       int horizontalDistance, int verticalAmount, int VerticalDistance, ISlotProvider<T> provider, Consumer<Slot> slotAdder) {
        for (int i = 0; i < verticalAmount; i++) {
            StartIndex = addHorizontalSlots(handler, StartIndex, x, y, horizontalAmount, horizontalDistance, provider, slotAdder);
            y += VerticalDistance;
        }
        return StartIndex;
    }

    public static <T> int addSlotField(T handler, int StartIndex, int x, int y, int horizontalAmount, int verticalAmount, int VerticalDistance, ISlotProvider<T> provider, Consumer<Slot> slotAdder) {
        return addSlotField(handler, StartIndex, x, y, horizontalAmount, 18, verticalAmount, VerticalDistance, provider, slotAdder);
    }
    public static <T> int addSlotField(T handler, int StartIndex, int x, int y, int horizontalAmount, int verticalAmount, ISlotProvider<T> provider, Consumer<Slot> slotAdder) {
        return addSlotField(handler, StartIndex, x, y, horizontalAmount, verticalAmount, 18, provider, slotAdder);
    }

    public static int addSlotField(Container handler, int StartIndex, int x, int y, int horizontalAmount,
                                   int horizontalDistance, int verticalAmount, int VerticalDistance, Consumer<Slot> slotAdder) {
        return addSlotField(handler, StartIndex, x, y, horizontalAmount, horizontalDistance, verticalAmount, VerticalDistance, Slot::new, slotAdder);
    }

    public static int addSlotField(IItemHandler handler, int StartIndex, int x, int y, int horizontalAmount,
                                   int horizontalDistance, int verticalAmount, int VerticalDistance, Consumer<Slot> slotAdder) {
        return addSlotField(handler, StartIndex, x, y, horizontalAmount, horizontalDistance, verticalAmount, VerticalDistance, SlotItemHandler::new, slotAdder);
    }

    public static int addSlotField(Container handler, int StartIndex, int x, int y, int horizontalAmount, int verticalAmount, int VerticalDistance, Consumer<Slot> slotAdder) {
        return addSlotField(handler, StartIndex, x, y, horizontalAmount, 18, verticalAmount, VerticalDistance, Slot::new, slotAdder);
    }

    public static int addSlotField(IItemHandler handler, int StartIndex, int x, int y, int horizontalAmount, int verticalAmount, int VerticalDistance, Consumer<Slot> slotAdder) {
        return addSlotField(handler, StartIndex, x, y, horizontalAmount, verticalAmount, VerticalDistance, SlotItemHandler::new, slotAdder);
    }

    public static int addSlotField(Container handler, int StartIndex, int x, int y, int horizontalAmount, int verticalAmount, Consumer<Slot> slotAdder) {
        return addSlotField(handler, StartIndex, x, y, horizontalAmount, verticalAmount, 18, slotAdder);
    }

    public static int addSlotField(IItemHandler handler, int StartIndex, int x, int y, int horizontalAmount, int verticalAmount, Consumer<Slot> slotAdder) {
        return addSlotField(handler, StartIndex, x, y, horizontalAmount, verticalAmount, 18, SlotItemHandler::new, slotAdder);
    }

    public static ISlotProvider<IItemHandler> createFilteredItemHandlerSLot(Item filter) {
        return createFilteredItemHandlerSLot(stack -> stack.is(filter));
    }

    public static ISlotProvider<IItemHandler> createFilteredItemHandlerSLot(TagKey<Item> filter) {
        return createFilteredItemHandlerSLot(stack -> stack.is(filter));
    }

    public static ISlotProvider<IItemHandler> createFilteredItemHandlerSLot(Predicate<ItemStack> filter) {
        return (inv, index, x, y) -> new FilteredItemHandlerSlot(inv, index, x, y, filter);
    }


    public static class FilteredItemHandlerSlot extends SlotItemHandler {

        protected final Predicate<ItemStack> filter;
        public FilteredItemHandlerSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Predicate<ItemStack> filter) {
            super(itemHandler, index, xPosition, yPosition);
            this.filter = filter;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return filter.test(stack);
        }


    }



    @FunctionalInterface
    public interface ISlotProvider<T> {
        Slot createSlot(T inv, int index, int x, int y);
    }

    public static boolean stillValid(ContainerLevelAccess pAccess, Player pPlayer, Block pTargetBlock) {
        return pAccess.evaluate((p_38916_, p_38917_) -> p_38916_.getBlockState(p_38917_).is(pTargetBlock) && pPlayer.distanceToSqr((double) p_38917_.getX() + 0.5D, (double) p_38917_.getY() + 0.5D, (double) p_38917_.getZ() + 0.5D) <= 64.0D, true);
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static <T extends BlockEntity> T getClientBLockEntity(final Inventory inventory,
                                                                 final FriendlyByteBuf buffer, Class<T> desiredBlockentityClass) {
        BlockEntity e = getClientBLockEntity(inventory, buffer);
        if (desiredBlockentityClass.isInstance(e)) {
            return desiredBlockentityClass.cast(e);
        }
        return null;
    }

    /**
     * @param inventory
     * @param buffer
     * @return the blockentity, when a blockpos is written to the buffer, this isnt checked for null return
     */

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static BlockEntity getClientBLockEntity(final Inventory inventory,
                                                   final FriendlyByteBuf buffer) {
        Objects.requireNonNull(inventory, "the inventory must not be null");
        Objects.requireNonNull(buffer, "the buffer must not be null");
        return inventory.player.level().getBlockEntity(buffer.readBlockPos());
    }

    public static MutableComponent createMenuDescription(ResourceKey<MenuType<?>> menu){
        return createMenuDescription(menu.location());
    }

    public static MutableComponent createMenuDescription(ResourceLocation menu){
        return Component.translatable(Util.makeDescriptionId("menu", menu));
    }
}
