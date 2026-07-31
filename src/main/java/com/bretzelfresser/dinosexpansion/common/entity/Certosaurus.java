package com.bretzelfresser.dinosexpansion.common.entity;

import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import com.bretzelfresser.dinosexpansion.common.entity.base.attack.DinoAttack;
import com.bretzelfresser.dinosexpansion.common.entity.base.attack.DinoAttackBuilder;
import com.bretzelfresser.dinosexpansion.common.init.ModMobEffects;
import com.google.common.collect.ImmutableList;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Certosaurus extends BaseDinoEntity {

    public static final DinoAttack ROAR = new DinoAttackBuilder()
            .animationName("roar")
            .cooldownTicks(5 * 60 * 20)//5 minutes
            .hitFrameTick(0)
            .cannotMove(true)
            .range(10)
            .durationTicks(Math.round(2.1667f * 20f))//actually stolen from the animation, adjust if animation changes
            .canUse((dino, target) -> dino.isAlive() && target.isAlive() && target instanceof Player && !dino.hasEffect(ModMobEffects.CERATOSAURUS_ROAR))
            .onHit(Certosaurus::roar)
            .selectionWeight(100)
            .build("Roar");

    public static final DinoAttack BITE = new DinoAttackBuilder()
            .animationName("attack")
            .cooldownTicks(15)
            .durationTicks(12)
            .hitFrameTick(8)
            .onHitHurt()
            .selectionWeight(10)
            .range(2d)
            .build("Bite");


    public Certosaurus(EntityType<? extends Certosaurus> entityType, Level level) {
        super(entityType, level);
        this.registerAttack(ROAR);
        this.registerAttack(BITE);
    }


    protected static void roar(BaseDinoEntity dino, LivingEntity target) {
        int duration = 60 * 20;//one minute
        int amplifier = 0;
        dino.addEffect(new MobEffectInstance(ModMobEffects.CERATOSAURUS_ROAR, duration, amplifier));
        var visibleEntities = dino.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        visibleEntities.ifPresent(entities -> {
            //TODO add range check, but for testing its better without
            for (var living : entities.findAll(l -> l != dino && l.getType() == dino.getType())) {
                living.addEffect(new MobEffectInstance(ModMobEffects.CERATOSAURUS_ROAR, duration, amplifier), dino);
                if (living.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)) {
                    living.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, target);
                }
            }
        });
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
                DinoBrain.baseDinoMemoryModules()
                        .add(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                        .add(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                        .add(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER)
                        .add(MemoryModuleType.NEAREST_PLAYERS)
                        .add(MemoryModuleType.NEAREST_VISIBLE_PLAYER)
                        .add(MemoryModuleType.ATTACK_TARGET)
                        .build(),
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
    public Optional<? extends LivingEntity> findAttackTarget() {
        Optional<? extends LivingEntity> baseTarget = super.findAttackTarget();
        List<LivingEntity> potentialPrey = new ArrayList<>(this.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(List.of()));
        baseTarget.ifPresent(potentialPrey::addFirst);
        for (LivingEntity prey : potentialPrey) {
            if (prey.isAlive()) {
                if (prey instanceof Player player) {
                    if (!canPlayerAccess(player, false))
                        return Optional.of(player);
                    return Optional.empty();
                }
                if (!(prey instanceof Animal) || prey.getType() == this.getType())
                    continue;
                return Optional.of(prey);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target);
    }

    @Override
    protected Vec3 sleepParticlesRelative() {
        return new Vec3(-0.1, .8, 1.3f);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "dino_controller", 10, event -> {
            if (this.getSleepBehaviour().isSleeping()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("sleep"));
            }
            if (this.isUnconscious()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("knockedout"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }).triggerableAnim("attack", RawAnimation.begin().thenPlay("attack")));
        registrar.add(new AnimationController<>(this, "dino_move_controller", 5, event -> {
            if (!this.getSleepBehaviour().isSleeping() && !this.isUnconscious() && event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop(isSprinting() ? "run" : "walk"));
            }
            return PlayState.STOP;
        }));
        // 3. Attack controller with 2 ticks transition
        registrar.add(new AnimationController<>(this, "dino_attack_controller", 2, event -> {
            return PlayState.STOP;
        }).triggerableAnim("attack", RawAnimation.begin().thenPlay("attack"))
                .triggerableAnim("roar", RawAnimation.begin().thenPlay("roar")));
    }
}
