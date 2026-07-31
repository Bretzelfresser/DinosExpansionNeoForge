package com.bretzelfresser.dinosexpansion.common.entity.base;

import net.minecraft.world.entity.ai.control.LookControl;

public class DinoLookControl extends LookControl {

    protected final BaseDinoEntity dino;

    public DinoLookControl(BaseDinoEntity mob) {
        super(mob);
        this.dino = mob;
    }


    @Override
    public void tick() {
        if (!dino.hasControllingPassenger() && dino.canLook())
            super.tick();
    }
}
