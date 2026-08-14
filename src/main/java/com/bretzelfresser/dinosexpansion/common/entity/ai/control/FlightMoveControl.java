package com.bretzelfresser.dinosexpansion.common.entity.ai.control;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public class FlightMoveControl extends MoveControl {
    private final FlyingDinosaur<?> dinosaur;

    public FlightMoveControl(FlyingDinosaur<?> dinosaur) {
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
                this.dinosaur.setSpeed(0.0F);
                this.dinosaur.setDeltaMovement(this.dinosaur.getDeltaMovement().scale(0.5D));
            } else {
                // Take off if target is high, target is far, or we are off the ground
                if (toTarget.y > 1.5D || distance > 4.0D || !this.dinosaur.onGround()) {
                    if (!this.dinosaur.isFlying()) {
                        this.dinosaur.setFlying(true);
                        // Apply a small upward takeoff boost to clear the ground smoothly
                        this.dinosaur.setDeltaMovement(this.dinosaur.getDeltaMovement().add(0.0D, 0.25D, 0.0D));
                    }
                }

                if (!this.dinosaur.isFlying()) {
                    // Ground walking movement & steering (similar to vanilla MoveControl)
                    float f = (float)(Mth.atan2(toTarget.z, toTarget.x) * (180F / (float)Math.PI)) - 90.0F;
                    this.dinosaur.setYRot(this.rotlerp(this.dinosaur.getYRot(), f, 90.0F));
                    this.dinosaur.setYBodyRot(this.dinosaur.getYRot());
                    this.dinosaur.setSpeed((float)(this.speedModifier * this.dinosaur.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                }
            }
        } else {
            this.dinosaur.setSpeed(0.0F);
        }
    }
}
