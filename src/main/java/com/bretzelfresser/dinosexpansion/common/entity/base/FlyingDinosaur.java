package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.common.entity.ai.control.ComposedMoveControl;
import com.bretzelfresser.dinosexpansion.common.entity.ai.navigation.SmoothFlyingPathNavigation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class FlyingDinosaur<T extends FlyingDinosaur<T>> extends BaseDinoEntity<T> implements FlyingAnimal {

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
        boolean wasFlying = this.isFlying();
        this.entityData.set(FLYING, flying);
        if (wasFlying != flying && this.getNavigation() != null) {
            this.getNavigation().stop();
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            // Land if we are on the ground and not actively wanting to move upwards
        }
    }

    public float getMaxTurnSpeed() {
        return 10.0F;
    }

}
