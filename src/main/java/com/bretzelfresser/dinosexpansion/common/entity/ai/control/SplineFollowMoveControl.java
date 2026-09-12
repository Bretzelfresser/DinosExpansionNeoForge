package com.bretzelfresser.dinosexpansion.common.entity.ai.control;

import com.bretzelfresser.dinosexpansion.common.entity.ai.navigation.ParametricSpline;
import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SplineFollowMoveControl extends MoveControl {
    private final FlyingDinosaur<?> dinosaur;

    // Closed-loop spline following state
    protected ParametricSpline activeSpline = null;
    protected double currentSplineProgress = 0.0D;
    protected boolean splineDone = false;
    protected boolean stuck = false;
    protected int stuckTicks = 0;
    protected Vec3 lastPosition = Vec3.ZERO;

    public SplineFollowMoveControl(FlyingDinosaur<?> dinosaur) {
        super(dinosaur);
        this.dinosaur = dinosaur;
    }

    public void followSpline(ParametricSpline spline, double speedModifier) {
        this.activeSpline = spline;
        this.speedModifier = speedModifier;
        this.currentSplineProgress = 0.0D;
        this.splineDone = false;
        this.stuck = false;
        this.stuckTicks = 0;
        this.lastPosition = this.mob.position();
        this.operation = Operation.MOVE_TO;
    }

    public void clearSpline() {
        this.activeSpline = null;
        this.splineDone = false;
        this.stuck = false;
        this.stuckTicks = 0;
        this.mob.setZza(0.0F);
        this.mob.setXxa(0.0F);
        this.mob.setYya(0.0F);
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
        }
    }

    public boolean isSplineDone() {
        return this.splineDone;
    }

    public boolean isStuck() {
        return this.stuck;
    }

    public boolean hasActiveSpline() {
        return this.activeSpline != null && !this.activeSpline.isEmpty() && !this.splineDone;
    }

    @Override
    public void tick() {
        // If there is no active spline, do nothing and clear inputs
        if (!hasActiveSpline()) {
            this.mob.setZza(0.0F);
            this.mob.setXxa(0.0F);
            this.mob.setYya(0.0F);
            return;
        }
        this.mob.setNoGravity(true);

        double baseSpeed = this.mob.getAttributeValue(Attributes.FLYING_SPEED);
        double flySpeed = baseSpeed * (this.speedModifier <= 0.0D ? 1.0D : this.speedModifier);

        Vec3 currentPos = this.mob.position();

        // Stuck detection: verify physical movement progress over time
        double movedDistSq = currentPos.distanceToSqr(this.lastPosition);
        if (movedDistSq < 0.005D) {
            this.stuckTicks++;
            if (this.stuckTicks > 25) { // Stuck for > 1.25 seconds
                this.stuck = true;
                this.mob.setZza(0.0F);
                this.mob.setXxa(0.0F);
                this.mob.setYya(0.0F);
                return;
            }
        } else {
            this.stuckTicks = 0;
        }
        this.lastPosition = currentPos;

        // Project current physical position onto spline curve (prevents skipping ahead)
        double searchWindow = Math.max(3.0D, flySpeed * 6.0D);
        double closestS = this.activeSpline.findClosestDistance(currentPos, this.currentSplineProgress, searchWindow);
        this.currentSplineProgress = Math.max(this.currentSplineProgress, closestS);

        double totalLen = this.activeSpline.getTotalLength();
        double distToEnd = currentPos.distanceTo(this.activeSpline.getEndPosition());

        // Check arrival at end of spline
        if (this.currentSplineProgress >= totalLen - 0.5D && distToEnd <= 1.5D) {
            this.splineDone = true;
            this.operation = Operation.WAIT;
            this.mob.setZza(0.0F);
            this.mob.setXxa(0.0F);
            this.mob.setYya(0.0F);
            return;
        }

        // Pure pursuit look-ahead target
        double lookAhead = Math.min(totalLen, this.currentSplineProgress + Math.max(1.5D, flySpeed * 4.0D));
        Vec3 targetPos = this.activeSpline.getPositionAtDistance(lookAhead);
        Vec3 toTarget = targetPos.subtract(currentPos);
        Vec3 tangent = this.activeSpline.getTangentAtDistance(lookAhead);

        // Blend direction to target with curve tangent for smooth banking
        Vec3 dirToTarget = toTarget.lengthSqr() > 1.0E-4D ? toTarget.normalize() : tangent;
        Vec3 targetDir = dirToTarget.lerp(tangent, 0.35D);

        // Mild collision repulsion
        Vec3 repulsion = calculateRepulsionVector(this.mob, 1.5D);
        targetDir = targetDir.add(repulsion);

        // 1. Calculate and update entity Yaw and Pitch rotation from target direction
        if (targetDir.horizontalDistanceSqr() > 1.0E-4D) {
            float targetYaw = (float) (Mth.atan2(targetDir.z, targetDir.x) * (180.0D / Math.PI)) - 90.0F;
            float turnSpeed = this.dinosaur.getMaxTurnSpeed();
            float lerpedYaw = rotlerp(this.mob.getYRot(), targetYaw, turnSpeed);

            this.mob.setYRot(lerpedYaw);
            this.mob.yBodyRot = lerpedYaw;
            this.mob.yHeadRot = lerpedYaw;

            double horizontalDistance = Math.sqrt(targetDir.x * targetDir.x + targetDir.z * targetDir.z);
            float targetPitch = (float) (-(Mth.atan2(targetDir.y, horizontalDistance) * (180.0D / Math.PI)));
            this.mob.setXRot(rotlerp(this.mob.getXRot(), targetPitch, turnSpeed));
        }

        // 2. Project targetDir onto entity's local body axes to compute zza (forward) and xxa (strafe)
        float yawRad = this.mob.getYRot() * (float) (Math.PI / 180.0);
        float sin = Mth.sin(yawRad);
        float cos = Mth.cos(yawRad);

        float forward = (float) (-targetDir.x * sin + targetDir.z * cos);
        float strafe  = (float) ( targetDir.x * cos + targetDir.z * sin);
        float vertical = (float) Mth.clamp(targetDir.y, -1.0D, 1.0D);

        // 3. Set speed and inputs for LivingEntity.travel()
        this.mob.setSpeed((float) flySpeed);
        this.mob.setZza(forward);
        this.mob.setXxa(strafe);
        this.mob.setYya(vertical);
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

        if (totalPush.lengthSqr() > 0.04D) {
            totalPush = totalPush.normalize().scale(0.2D);
        }

        return totalPush;
    }
}
