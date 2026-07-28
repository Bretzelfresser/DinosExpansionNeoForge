package com.bretzelfresser.dinosexpansion.common.entity;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import net.minecraft.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.Tags;
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
