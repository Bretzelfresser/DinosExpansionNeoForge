package com.bretzelfresser.dinosexpansion.common.entity.ai;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.bretzelfresser.dinosexpansion.common.init.ModDataComponents;
import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;

public class DinoUnconsciousEatNarcoticsBehavior extends Behavior<BaseDinoEntity> {
    public DinoUnconsciousEatNarcoticsBehavior() {
        super(ImmutableMap.of(ModMemoryModules.UNCONSCIOUS.get(), MemoryStatus.VALUE_PRESENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, BaseDinoEntity owner) {
        return owner.getMissingTorpor() > 0;
    }

    @Override
    protected void start(ServerLevel level, BaseDinoEntity owner, long gameTime) {
        Container inventory = owner.getInventory();
        for (int i = 2; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.has(ModDataComponents.NARCOTIC_VALUE.get())) {
                float val = stack.getOrDefault(ModDataComponents.NARCOTIC_VALUE.get(), 0f);
                owner.applyBufferedNarcotics(val);
                stack.shrink(1);
                break;
            }
        }
    }
}
