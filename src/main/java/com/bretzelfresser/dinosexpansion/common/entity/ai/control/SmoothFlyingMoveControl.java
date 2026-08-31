package com.bretzelfresser.dinosexpansion.common.entity.ai.control;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class SmoothFlyingMoveControl extends MoveControl {
    private final FlyingDinosaur<?> dinosaur;
    protected double flightHeight = 10.0D;
    protected float wanderHeading;
    protected int wanderTimer = 0;

    public SmoothFlyingMoveControl(FlyingDinosaur<?> dinosaur) {
        super(dinosaur);
        this.dinosaur = dinosaur;
        this.wanderHeading = dinosaur.getYRot();
    }

    public void setPreferredFlightHeight(double flightHeight) {
        this.flightHeight = flightHeight;
    }

    @Override
    public void tick() {
        double baseSpeed = this.mob.getAttributeValue(Attributes.FLYING_SPEED);
        double flySpeed = baseSpeed * this.speedModifier == 0 ? 1 : this.speedModifier;

        Vec3 currentPos = this.mob.position();
        Vec3 currentMovement = this.mob.getDeltaMovement();

        Vec3 targetDir;

        if (this.operation == Operation.MOVE_TO) {
            Vec3 targetPos = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 toTarget = targetPos.subtract(currentPos);
            double distance = toTarget.length();

            if (distance < 0.5D) {
                this.operation = Operation.WAIT;
                return;
            }

            targetDir = toTarget.normalize().scale(flySpeed);
        } else {
            // Idle wandering flight behavior
            if (--this.wanderTimer <= 0) {
                this.wanderTimer = 20 + this.mob.getRandom().nextInt(40); // Change direction every 2-4 seconds
                float angleChange = (this.mob.getRandom().nextFloat() - 0.5F) * 180.0F; // Smooth angle change (-45 to +45 deg)
                this.wanderHeading = Mth.wrapDegrees(this.wanderHeading + angleChange);
            }

            // Compute target horizontal direction from wanderHeading
            float rad = this.wanderHeading * (float) (Math.PI / 180.0);
            double dx = -Mth.sin(rad) * flySpeed;
            double dz = Mth.cos(rad) * flySpeed;

            // Smooth height adjustment during wandering
            double desiredY = getTargetHeightY();
            double heightDiff = desiredY - currentPos.y;
            double targetY = Mth.clamp(heightDiff * 0.05D, -flySpeed * 0.5D, flySpeed * 0.5D);

            targetDir = new Vec3(dx, targetY, dz);
        }

        // Add mild block collision repulsion (smaller radius during pathfinding to avoid fighting path waypoints)
        double repulsionRadius = (this.operation == Operation.MOVE_TO) ? 1.5D : 2.0D;
        Vec3 repulsion = calculateRepulsionVector(this.mob, repulsionRadius);
        targetDir = targetDir.add(repulsion);

        // Interpolate velocity smoothly for realistic flight momentum
        double steering = this.dinosaur.getSteeringForce();
        Vec3 newMovement = currentMovement.lerp(targetDir, Mth.clamp(steering, 0.01D, 0.8D));

        this.mob.setDeltaMovement(newMovement);

        // Calculate and update Yaw and Pitch from actual movement velocity
        if (newMovement.horizontalDistanceSqr() > 1.0E-4D) {
            // Correct Minecraft Yaw: atan2(z, x) * 180 / PI - 90
            float targetYaw = (float) (Mth.atan2(newMovement.z, newMovement.x) * (180.0D / Math.PI)) - 90.0F;
            float lerpedYaw = rotlerp(this.mob.getYRot(), targetYaw, 10.0F);

            this.mob.setYRot(lerpedYaw);
            this.mob.yBodyRot = lerpedYaw;
            this.mob.yHeadRot = lerpedYaw;

            // Pitch from vertical movement component
            double horizontalDistance = Math.sqrt(newMovement.x * newMovement.x + newMovement.z * newMovement.z);
            float targetPitch = (float) (-(Mth.atan2(newMovement.y, horizontalDistance) * (180.0D / Math.PI)));
            this.mob.setXRot(rotlerp(this.mob.getXRot(), targetPitch, 10.0F));
        }
    }

    private double getTargetHeightY() {
        Level level = this.mob.level();
        BlockPos pos = this.mob.blockPosition();
        double floorHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        double desiredY = floorHeight + this.flightHeight;

        // Check for indoor/cave ceiling above entity to prevent getting pushed into roof blocks
        int maxScan = (int) Math.ceil(this.flightHeight);
        for (int i = 1; i <= maxScan; i++) {
            BlockPos checkPos = pos.above(i);
            if (!level.getBlockState(checkPos).getCollisionShape(level, checkPos).isEmpty()) {
                double ceilingY = checkPos.getY() - 0.5D;
                desiredY = Math.min(desiredY, ceilingY - 1.2D);
                break;
            }
        }
        return Math.max(pos.getY(), desiredY);
    }

    public static Vec3 calculateRepulsionVector(Entity entity, double radius) {
        Level level = entity.level();
        Vec3 entityPos = entity.position();
        int blockRadius = (int) Math.ceil(radius);
        BlockPos center = entity.blockPosition();

        Vec3 totalPush = Vec3.ZERO;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-blockRadius, -blockRadius, -blockRadius),
                center.offset(blockRadius, blockRadius, blockRadius)
        )) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                Vec3 blockCenter = Vec3.atCenterOf(pos);
                Vec3 awayFromBlock = entityPos.subtract(blockCenter);
                double distSq = awayFromBlock.lengthSqr();

                if (distSq > 0.0001 && distSq < radius * radius) {
                    double dist = Math.sqrt(distSq);
                    double strength = (radius - dist) / radius;
                    Vec3 pushDir = awayFromBlock.normalize().scale(strength * 0.05D);
                    totalPush = totalPush.add(pushDir);
                }
            }
        }

        // Cap total repulsion force magnitude so it never cancels out target movement
        if (totalPush.lengthSqr() > 0.04D) {
            totalPush = totalPush.normalize().scale(0.2D);
        }

        return totalPush;
    }
}
