package com.bretzelfresser.dinosexpansion.common.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.schedule.Activity;

public class DinoSwimBehaviour extends Swim {
    public DinoSwimBehaviour(float chance) {
        super(chance);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob owner) {
        return owner.getBrain().getActiveNonCoreActivity().map(a -> a != Activity.REST).orElse(true) && super.checkExtraStartConditions(level, owner);
    }
}
