package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.common.entity.ai.control.ComposedMoveControl;
import com.bretzelfresser.dinosexpansion.common.entity.ai.control.FlightMoveControl;
import com.bretzelfresser.dinosexpansion.common.entity.ai.navigation.SmoothFlyingPathNavigation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class FlyingDinosaur<T extends FlyingDinosaur<T>> extends BaseDinoEntity<T> {
    private static final EntityDataAccessor<Boolean> FLYING = SynchedEntityData.defineId(FlyingDinosaur.class, EntityDataSerializers.BOOLEAN);

    protected FlyingDinosaur(EntityType<? extends BaseDinoEntity> entityType, Level level) {
        this(entityType, level, 2);
    }

    protected FlyingDinosaur(EntityType<? extends BaseDinoEntity> entityType, Level level, int baseInventorySize) {
        super(entityType, level, baseInventorySize);
        this.moveControl = new ComposedMoveControl<>(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLYING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Flying", this.isFlying());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setFlying(tag.getBoolean("Flying"));
    }

    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.entityData.set(FLYING, flying);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new SmoothFlyingPathNavigation(this, level);
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isFlying()) {
            Vec3 velocity = this.getDeltaMovement();

            // 1. Calculate steering (wanted pull) vector
            Vec3 steering = Vec3.ZERO;
            if (this.moveControl.hasWanted()) {
                Vec3 targetPos = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
                Vec3 toTarget = targetPos.subtract(this.position());
                double distance = toTarget.length();

                if (distance > 0.1D) {
                    double maxSpeed = this.getMaxFlyingSpeed();
                    double slowingRadius = this.getSlowingRadius();
                    
                    // Decelerate as we approach the waypoint (Arrive behavior)
                    Vec3 desiredVelocity;
                    if (distance < slowingRadius) {
                        desiredVelocity = toTarget.normalize().scale(maxSpeed * (distance / slowingRadius));
                    } else {
                        desiredVelocity = toTarget.normalize().scale(maxSpeed);
                    }

                    // Steering force = Desired Velocity - Current Velocity
                    Vec3 rawSteering = desiredVelocity.subtract(velocity);
                    double maxSteerForce = this.getSteeringForce();
                    if (rawSteering.lengthSqr() > maxSteerForce * maxSteerForce) {
                        steering = rawSteering.normalize().scale(maxSteerForce);
                    } else {
                        steering = rawSteering;
                    }
                }
            }

            // 2. Gravity
            Vec3 gravity = new Vec3(0.0D, -this.getGravityStrength(), 0.0D);

            // 3. Lift: counteracts gravity proportional to forward speed
            double horizontalSpeed = velocity.horizontalDistance();
            Vec3 lift = new Vec3(0.0D, Math.min(this.getGravityStrength(), horizontalSpeed * this.getLiftFactor()), 0.0D);

            // 4. Combine acceleration forces
            velocity = velocity.add(steering).add(gravity).add(lift);

            // 5. Apply drag/air resistance
            velocity = velocity.scale(this.getDragCoefficient());

            // Apply calculated movement to entity
            this.setDeltaMovement(velocity);
            this.move(MoverType.SELF, this.getDeltaMovement());

            // 6. Smooth rotation matching actual flight velocity direction
            if (velocity.lengthSqr() > 0.01D) {
                double yaw = Mth.atan2(velocity.z, velocity.x) * (180D / Math.PI) - 90.0D;
                double pitch = -Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180D / Math.PI);
                this.setYRot(this.rotlerp(this.getYRot(), (float) yaw, 8.0F));
                this.setXRot(this.rotlerp(this.getXRot(), (float) pitch, 8.0F));
                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            // Land if we are on the ground and not actively wanting to move upwards
            if (this.isFlying()) {
                if (this.onGround() && (!this.moveControl.hasWanted() || this.moveControl.getWantedY() <= this.getY() + 0.5D)) {
                    this.setFlying(false);
                }
            }
        }
    }

    protected float rotlerp(float current, float target, float maxChange) {
        float f = Mth.wrapDegrees(target - current);
        if (f > maxChange) {
            f = maxChange;
        }
        if (f < -maxChange) {
            f = -maxChange;
        }
        return current + f;
    }

    // Default physical flight configuration parameters (override in concrete dinosaur classes)
    
    public double getMaxFlyingSpeed() {
        return 0.6D;
    }

    public double getSteeringForce() {
        return 0.05D;
    }

    public double getSlowingRadius() {
        return 4.0D;
    }

    public double getDragCoefficient() {
        return 0.98D;
    }

    public double getGravityStrength() {
        return 0.04D;
    }

    public double getLiftFactor() {
        return 0.1D;
    }
}
