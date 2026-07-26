package com.bretzelfresser.dinosexpansion.common.entity;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public class Certosaurus extends BaseDinoEntity {
    public Certosaurus(EntityType<? extends Certosaurus> entityType, Level level) {
        super(entityType, level);
    }



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "dino_controller", 10, event -> {
            if (this.isUnconscious() || this.getSleepBehaviour().isSleeping()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("sleep"));
            }
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }
            return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }).triggerableAnim("attack", RawAnimation.begin().thenPlay("attack")));
    }
}
