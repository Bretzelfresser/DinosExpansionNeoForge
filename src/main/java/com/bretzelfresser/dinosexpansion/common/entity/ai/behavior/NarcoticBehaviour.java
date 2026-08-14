package com.bretzelfresser.dinosexpansion.common.entity.ai.behavior;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModDataComponents;
import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.antlr.v4.runtime.misc.Triple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NarcoticBehaviour {

    public static OneShot<BaseDinoEntity<?>> eatNarcotics(boolean findBiggestBelowThreshold) {
        return eatNarcotics(findBiggestBelowThreshold, true);
    }

    public static OneShot<BaseDinoEntity<?>> eatNarcotics(boolean findBiggestBelowThreshold, boolean onlyWhenTaming) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(ModMemoryModules.UNCONSCIOUS.get())).apply(instance, (unconsciousMemory) ->
                        (serverLevel, dino, gameTime) -> {
                            IItemHandlerModifiable inventory = dino.getChestInventory();
                            if (onlyWhenTaming && !dino.currentlyTaming())
                                return false;
                            float missingTorpor = dino.getMissingTorpor();
                            if (missingTorpor <= 0) {
                                return false;
                            }

                            List<Triple<Integer, ItemStack, Float>> narcoticStacks = new ArrayList<>();

                            for (int i = 0; i < inventory.getSlots(); i++) {
                                ItemStack stack = inventory.getStackInSlot(i);
                                if (!stack.isEmpty() && stack.has(ModDataComponents.NARCOTIC_VALUE.get())) {
                                    float val = stack.getOrDefault(ModDataComponents.NARCOTIC_VALUE.get(), 0f);
                                    narcoticStacks.add(new Triple<>(i, stack, val));
                                }
                            }
                            if (narcoticStacks.isEmpty())
                                return false;

                            if (findBiggestBelowThreshold) {
                                narcoticStacks.sort(Comparator.<Triple<Integer, ItemStack, Float>>
                                                comparingDouble(t -> t.c)
                                        .reversed()
                                        .thenComparingInt(t -> t.a));
                            }
                            int narcoticIndex = 0;

                            while (narcoticIndex < narcoticStacks.size()) {
                                var stack = narcoticStacks.get(narcoticIndex).b;
                                var narcoticValue = narcoticStacks.get(narcoticIndex).c;

                                missingTorpor = dino.getSurvivalBehaviour().getTotalMissingTorpor();


                                if (!dino.getSurvivalBehaviour().shouldWakeUpFromUnconscious(1) && missingTorpor < narcoticValue){
                                    break;
                                }

                                dino.getSurvivalBehaviour().applyBufferedNarcotics(narcoticValue);
                                stack.shrink(1);
                                if (stack.isEmpty()){
                                    narcoticIndex++;
                                }

                            }
                            return true;
                        }
                )

        );
    }
}
