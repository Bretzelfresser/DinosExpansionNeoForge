package com.bretzelfresser.dinosexpansion.common.entity.base.attack;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

/**
 * A builder class for creating modular {@link DinoAttack} instances.
 */
public class DinoAttackBuilder {
    private int durationTicks;
    private int hitFrameTick;
    private int cooldownTicks;
    private String animationName;
    private boolean cannotMove = false;
    private boolean randomWeight = true;
    protected BiConsumer<BaseDinoEntity, LivingEntity> onHit;
    protected ToDoubleFunction<BaseDinoEntity> range = d -> 3.0D;
    protected ToDoubleBiFunction<BaseDinoEntity, LivingEntity> selectionWeight = (dino, target) -> 1.0D;
    protected BiPredicate<BaseDinoEntity, LivingEntity> canUse = (dino, target) -> 
            dino.isAlive() && target.isAlive() && dino.distanceTo(target) <= range.applyAsDouble(dino);

    /**
     * Sets the total duration of the attack execution in server ticks.
     *
     * @param durationTicks the total duration ticks of the attack
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder durationTicks(int durationTicks) {
        this.durationTicks = durationTicks;
        return this;
    }

    /**
     * Sets the hit frame tick where the damage/effect should be applied.
     *
     * @param hitFrameTick the server tick count at which damage occurs during the attack
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder hitFrameTick(int hitFrameTick) {
        this.hitFrameTick = hitFrameTick;
        return this;
    }

    /**
     * Sets the cooldown duration in server ticks before this attack can be used again.
     *
     * @param cooldownTicks the cooldown ticks
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder cooldownTicks(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
        return this;
    }

    /**
     * Sets the GeckoLib animation name to trigger when starting this attack.
     *
     * @param animationName the animation key name
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder animationName(String animationName) {
        this.animationName = animationName;
        return this;
    }

    /**
     * Sets whether the dinosaur is completely locked from moving or turning during the attack.
     *
     * @param cannotMove true to freeze the dinosaur during the attack, false otherwise
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder cannotMove(boolean cannotMove) {
        this.cannotMove = cannotMove;
        return this;
    }

    /**
     * Sets whether this attack uses randomized selection weights.
     *
     * @param randomWeight true to enable weight randomizing, false otherwise
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder randomWeight(boolean randomWeight) {
        this.randomWeight = randomWeight;
        return this;
    }

    /**
     * Sets the callback handler executed on the server when the attack reaches its hit frame.
     *
     * @param onHit a consumer accepting the attacking dinosaur and the target entity
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder onHit(BiConsumer<BaseDinoEntity, LivingEntity> onHit) {
        this.onHit = onHit;
        return this;
    }

    /**
     * Sets the attack range calculation function based on the attacker's state.
     *
     * @param range a function returning the maximum reach distance
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder range(ToDoubleFunction<BaseDinoEntity> range) {
        this.range = range;
        return this;
    }

    /**
     * Sets a constant attack range value.
     *
     * @param range the constant maximum reach distance
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder range(double range) {
        this.range = d -> range;
        return this;
    }

    /**
     * Sets the selection weight calculation function used by the AI brain.
     *
     * @param selectionWeight a function returning the dynamic selection weight
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder selectionWeight(ToDoubleBiFunction<BaseDinoEntity, LivingEntity> selectionWeight) {
        this.selectionWeight = selectionWeight;
        return this;
    }

    /**
     * Sets a constant selection weight value.
     *
     * @param selectionWeight the constant selection weight
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder selectionWeight(double selectionWeight) {
        this.selectionWeight = (dino, target) -> selectionWeight;
        return this;
    }

    /**
     * Sets the pre-requisite conditions predicate for when the attack can be chosen.
     *
     * @param canUse a predicate validating if the attack can be executed
     * @return this builder instance for chaining
     */
    public DinoAttackBuilder canUse(BiPredicate<BaseDinoEntity, LivingEntity> canUse) {
        this.canUse = canUse;
        return this;
    }

    /**
     * Builds and returns a concrete {@link DinoAttack} instance configured by this builder.
     *
     * @param name the unique registration name of the attack
     * @return the constructed DinoAttack instance
     */
    public DinoAttack build(String name) {
        return new DinoAttack(name, this.durationTicks, this.hitFrameTick, this.cooldownTicks, this.animationName, this.cannotMove) {
            @Override
            public void executeDamage(BaseDinoEntity attacker, @Nullable LivingEntity target) {
                if (onHit != null) {
                    onHit.accept(attacker, target);
                }
            }

            @Override
            protected double getAttackRange(BaseDinoEntity attacker) {
                return range.applyAsDouble(attacker);
            }

            @Override
            public double getSelectionWeight(BaseDinoEntity attacker, LivingEntity target) {
                return selectionWeight.applyAsDouble(attacker, target);
            }

            @Override
            public boolean canUse(BaseDinoEntity attacker, LivingEntity target) {
                return canUse.test(attacker, target);
            }
        };
    }
}
