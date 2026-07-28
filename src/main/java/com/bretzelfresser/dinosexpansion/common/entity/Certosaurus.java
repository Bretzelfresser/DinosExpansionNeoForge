package com.bretzelfresser.dinosexpansion.common.entity;

import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.phys.Vec3;
import com.mojang.serialization.Dynamic;
import net.minecraft.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.EnumMap;
import java.util.function.Predicate;

public class Certosaurus extends BaseDinoEntity {
    public Certosaurus(EntityType<? extends Certosaurus> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    public EnumMap<DinoEquipment, Predicate<ItemStack>> getEquipments() {
        return Util.make(super.getEquipments(), map -> {
                    map.put(DinoEquipment.CHEST, this::isValidChest);
                    map.put(DinoEquipment.SADDLE, s -> s.is(Items.SADDLE));
                }
        );
    }

    @Override
    protected @NotNull Brain.Provider<?> brainProvider() {
        return Brain.provider(
                DinoBrain.baseDinoMemoryModules().build(),
                ImmutableList.of(
                        SensorType.NEAREST_PLAYERS,
                        SensorType.NEAREST_LIVING_ENTITIES
                )
        );
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("certosaurusBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();

        DinoBrain.updateActivity(this);
        super.customServerAiStep();
    }

    @Override
    public @NotNull Vec3 getPassengerRidingPosition(Entity passenger) {
        // TODO: Modify this to place the rider correctly relative to Certosaurus
        return super.getPassengerRidingPosition(passenger);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "dino_controller", 10, event -> {
            if (this.getSleepBehaviour().isSleeping()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("sleep"));
            }
            if (this.isUnconscious()){
                return event.setAndContinue(RawAnimation.begin().thenLoop("knockedout"));
            }
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }).triggerableAnim("attack", RawAnimation.begin().thenPlay("attack")));
    }
}
