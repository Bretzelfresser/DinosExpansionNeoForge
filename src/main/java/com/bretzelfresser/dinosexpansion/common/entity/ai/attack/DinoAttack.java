package com.bretzelfresser.dinosexpansion.common.entity.ai.attack;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public abstract class DinoAttack {
    private final String name;
    private final int durationTicks;
    private final int hitFrameTick;
    private final int cooldownTicks;
    private final String animationName;
    private final boolean cannotMove;

    public DinoAttack(String name, int durationTicks, int hitFrameTick, int cooldownTicks, String animationName) {
        this(name, durationTicks, hitFrameTick, cooldownTicks, animationName, false);
    }

    public DinoAttack(String name, int durationTicks, int hitFrameTick, int cooldownTicks, String animationName, boolean cannotMove) {
        this.name = name;
        this.durationTicks = durationTicks;
        this.hitFrameTick = hitFrameTick;
        this.cooldownTicks = cooldownTicks;
        this.animationName = animationName;
        this.cannotMove = cannotMove;
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

    public boolean cannotMove() {
        return cannotMove;
    }

    public abstract void executeDamage(BaseDinoEntity attacker, @Nullable LivingEntity target);

    public boolean canUse(BaseDinoEntity attacker, LivingEntity target) {
        return attacker.isAlive() && target.isAlive() && attacker.distanceTo(target) <= getAttackRange(attacker);
    }

    protected double getAttackRange(BaseDinoEntity attacker) {
        return 3.0D;
    }

    public double getSelectionWeight(BaseDinoEntity attacker, LivingEntity target) {
        return 1.0D;
    }
}
