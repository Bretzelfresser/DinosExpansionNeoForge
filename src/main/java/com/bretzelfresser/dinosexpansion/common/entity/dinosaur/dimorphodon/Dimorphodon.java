package com.bretzelfresser.dinosexpansion.common.entity.dinosaur.dimorphodon;

import com.bretzelfresser.dinosexpansion.common.entity.ai.attack.DinoAttack;
import com.bretzelfresser.dinosexpansion.common.entity.ai.attack.DinoAttackBuilder;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoOrderMode;
import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import com.bretzelfresser.dinosexpansion.common.entity.util.EntityUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public class Dimorphodon extends FlyingDinosaur<Dimorphodon> implements VariantHolder<Dimorphodon.Variant> {
    private static final EntityDataAccessor<Byte> VARIANT = SynchedEntityData.defineId(Dimorphodon.class, EntityDataSerializers.BYTE);

    public static final DinoAttack BITE = new DinoAttackBuilder()
            .animationName("attack")
            .cooldownTicks(15)
            .durationTicks(14)
            .hitFrameTick(10)
            .onHitHurt()
            .selectionWeight(10)
            .range(1.5d)
            .build("Bite");

    public static AttributeSupplier.Builder createDinoDefaultAttributes() {
        return FlyingDinosaur.createDinoDefaultAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.12D)
                .add(Attributes.FLYING_SPEED, 0.6D);
    }

    public enum Variant {
        COMMON((byte) 0, "common"),
        UNCOMMON((byte) 1, "uncommon"),
        RARE((byte) 2, "rare"),
        EPIC((byte) 3, "epic"),
        LEGENDARY((byte) 4, "legendary");

        private final byte id;
        private final String name;

        Variant(byte id, String name) {
            this.id = id;
            this.name = name;
        }

        public byte getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static Variant byId(byte id) {
            for (Variant v : values()) {
                if (v.id == id) {
                    return v;
                }
            }
            return COMMON;
        }
    }

    public Dimorphodon(EntityType<? extends Dimorphodon> entityType, Level level) {
        super(entityType, level);
        this.registerAttack(BITE);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (super.hurt(source, amount)) {
            if (!this.isFlying() && this.flightBehaviour != null) {
                this.flightBehaviour.takeOff();
            }
            this.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            this.getNavigation().stop();
            return true;
        }
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, (byte) 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Variant", this.getVariant().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byId(tag.getByte("Variant")));
    }

    @Override
    public @NotNull Variant getVariant() {
        return Variant.byId(this.entityData.get(VARIANT));
    }

    @Override
    public void setVariant(Variant variant) {
        this.entityData.set(VARIANT, variant.getId());
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        this.setVariant(getRandomVariant(level.getRandom()));
        if (!this.onGround()) {
            this.setFlying(true);
        }
        return spawnGroupData;
    }

    private Variant getRandomVariant(RandomSource random) {
        float r = random.nextFloat();
        if (r < 0.02F) return Variant.LEGENDARY;  // 2%
        if (r < 0.10F) return Variant.EPIC;       // 8%
        if (r < 0.25F) return Variant.RARE;       // 15%
        if (r < 0.50F) return Variant.UNCOMMON;   // 25%
        return Variant.COMMON;                    // 50%
    }

    @Override
    protected @NotNull Brain.Provider<?> brainProvider() {
        return DimorphodonBrain.makeBrainProvider();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return DimorphodonBrain.createBrain((Brain<Dimorphodon>) this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("dimorphodonBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();

        DimorphodonBrain.updateActivity(this);
        super.customServerAiStep();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "dino_controller", 10, event -> {
            if (this.getSleepBehaviour().isSleeping()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("sleep"));
            }
            if (this.isUnconscious()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("knockout"));
            }
            if (this.getOrderMode() == DinoOrderMode.STAY && this.onGround()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("sit"));
            }
            if(!EntityUtils.isMovingVertically(this) && !isFlying())
                return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            return PlayState.STOP;
        }).triggerableAnim("attack", RawAnimation.begin().thenPlay("attack")));

        registrar.add(new AnimationController<>(this, "dino_move_controller", 5, event -> {
            if (!this.getSleepBehaviour().isSleeping() && !this.isUnconscious()) {
                if (this.isFlying()) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("fly"));
                }else if(EntityUtils.isMovingVertically(this))
                    return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
            return PlayState.STOP;
        }));

        registrar.add(new AnimationController<>(this, DINO_ATTACK_CONTROLLER_NAME, 2, event -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("attack")));
    }

    @Override
    protected Vec3 sleepParticlesRelative() {
        return new Vec3(0f, 0.6f, 0.2f);
    }

    @Override
    public void playerTriggerAttack() {
        this.playerTriggerAttack(BITE);
    }
}
