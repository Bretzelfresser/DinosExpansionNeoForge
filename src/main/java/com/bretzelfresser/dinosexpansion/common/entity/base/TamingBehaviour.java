package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.common.food.DinoFoodCache;
import com.bretzelfresser.dinosexpansion.common.food.DinoFoodEntry;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import oshi.util.platform.unix.solaris.KstatUtil;

import java.util.*;
import java.util.stream.Collectors;

public class TamingBehaviour {
    private final BaseDinoEntity dino;

    public TamingBehaviour(BaseDinoEntity dino) {
        this.dino = dino;
    }


    public void tick() {
        if (dino.currentlyTaming()) {
            tryToEatFromInventoryWhenTaming();
        } else {
            tryToEatFromInventory();
        }
    }

    /**
     * will try to eat the food with the highest food value, only eats when the food value will not exceed amxHunger, this behavior is overridden when the hunger is lower then 1
     */
    public void tryToEatFromInventory() {
        var foods = getFoodsInInventory();
        if (foods.isEmpty())
            return;

        foods.removeIf(f -> f.getSecond().tamingOnly());

        foods.sort(
                Comparator.<Pair<Integer, DinoFoodEntry.FoodValues>>comparingDouble(p -> (double) p.getSecond().hungerValue())
                        .reversed()
                        .thenComparingInt(Pair::getFirst)
        );


        int currentEatIndex = 0;
        while (currentEatIndex < foods.size()) {
            var hungerValue = foods.get(currentEatIndex).getSecond().hungerValue();

            var stack = dino.getChestInventory().getStackInSlot(foods.get(currentEatIndex).getFirst());

            if (dino.getHunger() > 1 && hungerValue > dino.getMissingHunger())
                break;
            dino.setHunger(dino.getHunger() + hungerValue);
            stack.shrink(1);
            if (stack.isEmpty()) {
                currentEatIndex++;
            }
        }

    }

    /**
     * will go threw every food inside the inventory, the food with the best foodValue/tamingValue will be chosen, eats from front to back regarding index
     */
    public void tryToEatFromInventoryWhenTaming() {
        var foundFoodValues = getFoodsInInventory();

        if (foundFoodValues.isEmpty())
            return;


        //automatically sort by the food which has the lowest hunger to taming progress value
        //then sort so the lowest container index is used so it actually uses stuff from front to back
        foundFoodValues.sort(
                Comparator.<Pair<Integer, DinoFoodEntry.FoodValues>>comparingDouble(
                        //0 -> last element
                        p -> p.getSecond().tamingValue() == 0 ? Double.POSITIVE_INFINITY : (double) p.getSecond().hungerValue() / p.getSecond().tamingValue()
                ).thenComparingInt(Pair::getFirst)
        );

        int eatingIndex = 0;
        while (eatingIndex < foundFoodValues.size() && dino.canEat(foundFoodValues.get(eatingIndex).getSecond())) {
            float hungerVal = foundFoodValues.get(eatingIndex).getSecond().hungerValue();
            float tamingVal = foundFoodValues.get(eatingIndex).getSecond().tamingValue();
            dino.setHunger(dino.getHunger() + hungerVal);

            var stack = dino.getChestInventory().getStackInSlot(foundFoodValues.get(eatingIndex).getFirst());

            stack.shrink(1);
            if (stack.isEmpty() || tamingVal == 0) {
                eatingIndex++;
            }
            if (tamingVal == 0)
                continue;

            //actually ensuring we have an unconscious owner here, cause when i think of the dodo, the dodo will be tamed with hands and when u knockout it with torpor u cant tame it
            if (dino.currentlyTaming()) {
                float progressGain = tamingVal * dino.getTamingEffectiveness();
                dino.setTamingProgress(dino.getTamingProgress() + progressGain);
                if (dino.getTamingProgress() >= 1.0f) {
                    dino.onTameCompleted(dino.getTamingEffectiveness(), dino.getUnconsciousOwnerUUID().orElse(null));
                }
            }
        }
    }

    protected List<Pair<Integer, DinoFoodEntry.FoodValues>> getFoodsInInventory() {
        IItemHandlerModifiable inventory = dino.getChestInventory();
        List<Pair<Integer, DinoFoodEntry.FoodValues>> foundFoodValues = new LinkedList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && this.isPreferredFood(stack)) {
                foundFoodValues.add(Pair.of(i, this.getFoodValues(stack)));
            }
        }
        return foundFoodValues;
    }

    public boolean isPreferredFood(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return DinoFoodCache.getFoodsFor(dino.getType(), dino.level().registryAccess()).containsKey(stack.getItem());
    }

    public DinoFoodEntry.FoodValues getFoodValues(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return DinoFoodCache.getFoodsFor(dino.getType(), dino.level().registryAccess()).get(stack.getItem());
    }
}
