package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.client.event.ClientRenderingEvents;
import com.bretzelfresser.dinosexpansion.common.entity.ai.control.ComposedMoveControl;
import com.bretzelfresser.dinosexpansion.common.entity.ai.navigation.SmoothFlyingPathNavigation;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public abstract class FlyingDinosaur<T extends FlyingDinosaur<T>> extends BaseDinoEntity<T> {

    public static AttributeSupplier.Builder createDinoDefaultAttributes() {
        return BaseDinoEntity.createDinoDefaultAttributes()
                .add(Attributes.FLYING_SPEED, 1.0D)
                ;
    }


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
        this.setNoGravity(this.isFlying());
        super.travel(travelVector);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            // Land if we are on the ground and not actively wanting to move upwards
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
        return 0.02D;
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
