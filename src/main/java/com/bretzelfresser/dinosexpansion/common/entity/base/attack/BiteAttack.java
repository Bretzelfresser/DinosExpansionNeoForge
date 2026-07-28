package com.bretzelfresser.dinosexpansion.common.entity.base.attack;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;

public class BiteAttack extends DinoAttack {
    public BiteAttack() {
        // name: "bite", duration: 12 ticks (~0.58s), hitFrameTick: 5 ticks, cooldown: 20 ticks (1s), animationName: "attack"
        super("bite", 12, 5, 20, "attack");
    }

    @Override
    public void executeDamage(BaseDinoEntity attacker) {
        if (attacker.getTarget() != null) {
            attacker.doHurtTarget(attacker.getTarget());
        }
    }
}
