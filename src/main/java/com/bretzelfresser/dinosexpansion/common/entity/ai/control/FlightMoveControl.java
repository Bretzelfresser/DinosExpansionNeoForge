package com.bretzelfresser.dinosexpansion.common.entity.ai.control;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class FlightMoveControl extends MoveControl {
    private final FlyingDinosaur dinosaur;

    public FlightMoveControl(FlyingDinosaur dinosaur) {
        super(dinosaur);
        this.dinosaur = dinosaur;
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            Vec3 target = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 currentPos = this.dinosaur.position();
            Vec3 toTarget = target.subtract(currentPos);
            double distance = toTarget.length();

            if (distance < 0.5D) {
                this.operation = Operation.WAIT;
                this.dinosaur.setDeltaMovement(this.dinosaur.getDeltaMovement().scale(0.5D));
            } else {
                // Trigger flight state if the target is significantly higher or we are off the ground
                if (toTarget.y > 1.5D || !this.dinosaur.onGround()) {
                    this.dinosaur.setFlying(true);
                }
            }
        }
    }
}
