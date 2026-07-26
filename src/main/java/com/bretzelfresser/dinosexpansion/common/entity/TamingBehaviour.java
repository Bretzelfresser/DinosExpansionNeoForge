package com.bretzelfresser.dinosexpansion.common.entity;

import com.bretzelfresser.dinosexpansion.common.food.DinoFoodCache;
import com.bretzelfresser.dinosexpansion.common.food.DinoFoodEntry;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;
import java.util.UUID;

public class TamingBehaviour {
    private final BaseDinoEntity dino;

    public TamingBehaviour(BaseDinoEntity dino) {
        this.dino = dino;
    }

    public void tick() {
        if (dino.getHunger() <= (float) dino.getAttributeValue(ModAttributes.MAX_HUNGER) - 50.0f) {
            this.tryToEatFromInventory();
        }
    }

    public void tryToEatFromInventory() {
        Container inventory = dino.getInventory();
        for (int i = 2; i < 38; i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && this.isPreferredFood(stack)) {
                DinoFoodEntry.FoodValues values = this.getFoodValues(stack);
                Objects.requireNonNull(values);
                float hungerVal = values.hungerValue();
                float tamingVal = values.tamingValue();

                // Restore hunger
                dino.setHunger(dino.getHunger() + hungerVal);

                if (!dino.isTamed() && dino.isUnconscious()) {
                    // Wild & asleep: eating increases taming progress
                    float progressGain = tamingVal * dino.getTamingEffectiveness();
                    dino.setTamingProgress(dino.getTamingProgress() + progressGain);
                    if (dino.getTamingProgress() >= 1.0f) {
                        dino.setTamedBy(dino.getUnconsciousOwnerUUID().orElse(null)); // Tame it!
                        dino.setUnconsciousFrom((UUID) null); // Wake up
                    }
                }

                stack.shrink(1);
                break;
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
