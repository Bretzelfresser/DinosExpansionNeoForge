package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.common.food.DinoFoodCache;
import com.bretzelfresser.dinosexpansion.common.food.DinoFoodEntry;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class TamingBehaviour {
    private final BaseDinoEntity dino;

    public TamingBehaviour(BaseDinoEntity dino) {
        this.dino = dino;
    }

    public void tryToEatFromInventory() {
        Container inventory = dino.getInventory();
        List<Pair<Integer, DinoFoodEntry.FoodValues>> foundFoodValues = new LinkedList<>();
        for (int i = 2; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && this.isPreferredFood(stack)) {
                foundFoodValues.add(Pair.of(i, this.getFoodValues(stack)));
            }
        }


        //automatically sort by the food which has the lowest hunger to taming progress value
        //then sort so the lowest container index is used so it actually uses stuff from front to back
        foundFoodValues.sort(
                Comparator.<Pair<Integer, DinoFoodEntry.FoodValues>>comparingDouble(
                        p -> (double) p.getSecond().hungerValue() / p.getSecond().tamingValue()
                ).thenComparingInt(Pair::getFirst)
        );

        int eatingIndex = 0;
        while (eatingIndex < foundFoodValues.size() && dino.canEat(foundFoodValues.get(eatingIndex).getSecond())) {
            float hungerVal = foundFoodValues.get(eatingIndex).getSecond().hungerValue();
            float tamingVal = foundFoodValues.get(eatingIndex).getSecond().tamingValue();
            dino.setHunger(dino.getHunger() + hungerVal);

            var stack = inventory.getItem(foundFoodValues.get(eatingIndex).getFirst());

            stack.shrink(1);
            if (stack.isEmpty()){
                eatingIndex++;
            }

            //actually ensuring we have an unconscious owner here, cause when i think of the dodo, the dodo will be tamed with hands and when u knockout it with torpor u cant tame it
            if (!dino.isTamed() && dino.isUnconscious() && dino.getUnconsciousOwnerUUID().isPresent()) {
                float progressGain = tamingVal * dino.getTamingEffectiveness();
                dino.setTamingProgress(dino.getTamingProgress() + progressGain);
                if (dino.getTamingProgress() >= 1.0f) {
                    dino.setTamedBy(dino.getUnconsciousOwnerUUID().orElse(null)); // Tame it!
                    dino.setUnconsciousFrom((UUID) null); // Wake up
                }
            }
        }
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
