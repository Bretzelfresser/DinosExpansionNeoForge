package com.bretzelfresser.dinosexpansion.common.entity.behaviours;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DinoStaminaBehaviour {


    protected final BaseDinoEntity dino;

    public DinoStaminaBehaviour(BaseDinoEntity dino) {
        this.dino = dino;
    }

    /**
     *
     * @param addition the amount we want to add, can be negative to reduce stamina
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void addStamina(float addition) {
        this.dino.setStamina(this.dino.getStamina() - Mth.clamp(addition, -dino.getStamina(), dino.getMissingStamina()));
    }

    public boolean canAddStamina(float addition) {
        return addition == 0 || (addition > -dino.getStamina() && addition < dino.getMissingStamina());
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    public void tick() {

    }
}
