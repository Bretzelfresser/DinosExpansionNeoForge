package com.bretzelfresser.dinosexpansion.common.entity.ai;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;

public class DinoEatBehavior extends Behavior<BaseDinoEntity> {
    public DinoEatBehavior() {
        super(ImmutableMap.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, BaseDinoEntity owner) {
        return owner.getHunger() < (float) owner.getAttributeValue(ModAttributes.MAX_HUNGER);
    }

    @Override
    protected void start(ServerLevel level, BaseDinoEntity owner, long gameTime) {
        owner.getTamingBehaviour().tryToEatFromInventory();
    }
}
