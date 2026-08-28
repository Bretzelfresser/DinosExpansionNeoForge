package com.bretzelfresser.dinosexpansion.common.entity.ai.control;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;

import java.util.function.Predicate;

public class ComposedMoveControl<T extends BaseDinoEntity<T>> extends MoveControl {

    protected final BaseDinoEntity<T> mob;
    protected Predicate<BaseDinoEntity<T>> flyingPredicate = Predicate.not(Mob::onGround);
    protected MoveControl flyingMoveControl;


    public ComposedMoveControl(BaseDinoEntity<T> mob) {
        super(mob);
        this.mob = mob;
        flyingMoveControl = new FlyingMoveControl(mob, 10, false);
    }

    public ComposedMoveControl<T> withFlyingPredicate(Predicate<BaseDinoEntity<T>> flyingPredicate) {
        this.flyingPredicate = flyingPredicate;
        return this;
    }

    public ComposedMoveControl<T> withFlyingMoveControl(MoveControl flyingMoveControl) {
        this.flyingMoveControl = flyingMoveControl;
        return this;
    }


    @Override
    public void tick() {
        if (flyingPredicate.test(this.mob)) {
            flyingMoveControl.tick();
        }else
            super.tick();
    }
}
