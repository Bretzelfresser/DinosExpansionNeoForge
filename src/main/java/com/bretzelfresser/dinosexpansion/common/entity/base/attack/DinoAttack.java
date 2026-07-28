package com.bretzelfresser.dinosexpansion.common.entity.base.attack;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.world.entity.LivingEntity;

public abstract class DinoAttack {
    private final String name;
    private final int durationTicks;
    private final int hitFrameTick;
    private final int cooldownTicks;
    private final String animationName;

    public DinoAttack(String name, int durationTicks, int hitFrameTick, int cooldownTicks, String animationName) {
        this.name = name;
        this.durationTicks = durationTicks;
        this.hitFrameTick = hitFrameTick;
        this.cooldownTicks = cooldownTicks;
        this.animationName = animationName;
    }

    public String getName() {
        return name;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getHitFrameTick() {
        return hitFrameTick;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public String getAnimationName() {
        return animationName;
    }

    public abstract void executeDamage(BaseDinoEntity attacker);

    public boolean canUse(BaseDinoEntity attacker, LivingEntity target) {
        return attacker.isAlive() && target.isAlive() && attacker.distanceTo(target) <= getAttackRange(attacker);
    }

    protected double getAttackRange(BaseDinoEntity attacker) {
        return 3.0D;
    }
}
