package com.bretzelfresser.dinosexpansion.common.entity.ai.control;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.client.event.ClientRenderingEvents;
import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class SmoothFlyingMoveControl extends MoveControl {
    private final FlyingDinosaur<?> dinosaur;
    protected double flightHeight = 10;
    protected long randomPrevTick = 0;
    protected Vec3 randomMoveVec = new  Vec3(1.0D, 0.0D, 0.0D);

    public SmoothFlyingMoveControl(FlyingDinosaur<?> dinosaur) {
        super(dinosaur);
        this.dinosaur = dinosaur;
    }


    public void setPreferredFlightHeight(double flightHeight){
        this.flightHeight = flightHeight;
    }

    @Override
    public void tick() {

        double flySpeed = this.mob.getAttributeValue(Attributes.FLYING_SPEED);

        this.mob.setSpeed((float) (flySpeed * this.speedModifier));

        var currentMovement = mob.getDeltaMovement();

        var collisionPreventing = calculateRepulsionVector(this.mob, 4);

        Vec3 current = Vec3.ZERO;
        current = current.add(collisionPreventing);
        current = current.add(flightHeightNudging());

        if (this.operation == Operation.MOVE_TO) {
            Vec3 target = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
            Vec3 currentPos = this.dinosaur.position();
            Vec3 toTarget = target.subtract(currentPos);
            double distance = toTarget.length();

            if (distance < 0.5D) {
                this.operation = Operation.WAIT;
                return;

            }
            current = current.add(toTarget);

        }else {

            if (this.mob.level().getGameTime() - randomPrevTick > 4){
                currentMovement = current.multiply(1, 0, 1);
                if (currentMovement.length() <= 0.001){
                    currentMovement = new Vec3(1, 0, 0);
                }
                //randomVector = randomVector.yRot((this.mob.getRandom().nextFloat() * Mth.PI  / 2) - Mth.PI / 4);
                this.randomMoveVec = currentMovement.yRot((this.mob.getRandom().nextFloat() * Mth.PI  / 2) - Mth.PI / 4);
                randomPrevTick = this.mob.level().getGameTime();
            }
            current = current.add(randomMoveVec);
        }

        DinosExpansion.LOGGER.debug("current move vector: {}", current);
        this.mob.setXxa((float) current.x);
        this.mob.setZza((float) current.z);
        this.mob.setYya((float) current.y);


        float yRotFromMovementVector = (float)(Mth.atan2(current.z, current.x) * 180.0F / (float)Math.PI) + 90;

        this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yRotFromMovementVector, 90.0F));
    }




    public Vec3 flightHeightNudging(){
        double floorHeight = this.mob.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.mob.getBlockX(), this.mob.getBlockZ());
        double currentEntityHeightAboveSurface = Math.max(0, this.mob.getY() - floorHeight);

        return new Vec3(0, this.flightHeight - currentEntityHeightAboveSurface, 0);
    }

    public Vec3 calculateTargetVector() {
        Vec3 target = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
        Vec3 currentPos = this.dinosaur.position();
        Vec3 toTarget = target.subtract(currentPos);
        double distance = toTarget.length();

        return toTarget;

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
            // Only push away from solid / collision blocks
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                Vec3 blockCenter = Vec3.atCenterOf(pos);
                Vec3 awayFromBlock = entityPos.subtract(blockCenter);
                double distSq = awayFromBlock.lengthSqr();

                // Ignore blocks outside the desired push radius
                if (distSq > 0.0001 && distSq < radius * radius) {
                    double dist = Math.sqrt(distSq);

                    // Weight: Closer blocks push stronger (linear falloff)
                    double strength = (radius - dist) / radius;
                    Vec3 pushDir = awayFromBlock.normalize().scale(strength * 2);

                    totalPush = totalPush.add(pushDir);
                }
            }
        }

        return totalPush; // Add this vector to the entity's deltaMovement or goal direction
    }
}
