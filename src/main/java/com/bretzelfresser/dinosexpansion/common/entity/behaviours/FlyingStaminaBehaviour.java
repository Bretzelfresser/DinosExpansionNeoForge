package com.bretzelfresser.dinosexpansion.common.entity.behaviours;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;

public class FlyingStaminaBehaviour extends DinoStaminaBehaviour{
    public FlyingStaminaBehaviour(BaseDinoEntity<?> dino) {
        super(dino);
    }

    public FlyingStaminaBehaviour(BaseDinoEntity<?> dino, int staminaRegenerationCooldown) {
        super(dino, staminaRegenerationCooldown);
    }
}
