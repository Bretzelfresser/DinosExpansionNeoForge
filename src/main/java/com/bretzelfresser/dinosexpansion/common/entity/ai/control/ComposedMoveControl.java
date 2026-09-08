package com.bretzelfresser.dinosexpansion.common.entity.ai.control;

import com.bretzelfresser.dinosexpansion.common.entity.ai.navigation.ParametricSpline;
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

    public MoveControl getFlyingMoveControl() {
        return this.flyingMoveControl;
    }

    public void followSpline(ParametricSpline spline, double speed) {
        if (this.flyingMoveControl instanceof SmoothFlyingMoveControl smooth) {
            smooth.followSpline(spline, speed);
        }
    }

    public void clearSpline() {
        if (this.flyingMoveControl instanceof SmoothFlyingMoveControl smooth) {
            smooth.clearSpline();
        }
    }

    public boolean isSplineDone() {
        if (this.flyingMoveControl instanceof SmoothFlyingMoveControl smooth) {
            return smooth.isSplineDone();
        }
        return true;
    }

    public boolean isStuck() {
        if (this.flyingMoveControl instanceof SmoothFlyingMoveControl smooth) {
            return smooth.isStuck();
        }
        return false;
    }

    public boolean hasActiveSpline() {
        if (this.flyingMoveControl instanceof SmoothFlyingMoveControl smooth) {
            return smooth.hasActiveSpline();
        }
        return false;
    }

    @Override
    public void setWantedPosition(double x, double y, double z, double speed) {
        super.setWantedPosition(x, y, z, speed);
        flyingMoveControl.setWantedPosition(x, y, z, speed);
    }

    @Override
    public boolean hasWanted() {
        return flyingPredicate.test(this.mob) ? flyingMoveControl.hasWanted() : super.hasWanted();
    }

    @Override
    public void tick() {
        if (!this.mob.canMove())
            return;
        if (flyingPredicate.test(this.mob)) {
            flyingMoveControl.tick();
        } else
            super.tick();
    }
}
