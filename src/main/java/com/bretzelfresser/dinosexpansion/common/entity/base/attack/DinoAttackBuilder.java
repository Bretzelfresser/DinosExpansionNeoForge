package com.bretzelfresser.dinosexpansion.common.entity.base.attack;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

public class DinoAttackBuilder {


    private int durationTicks, hitFrameTick, cooldownTicks;
    private String animationName;
    private boolean cannotMove = false, randomWeight = true;
    protected BiConsumer<BaseDinoEntity, LivingEntity> onHit;
    protected ToDoubleFunction<BaseDinoEntity> range = d -> 3d;
    protected ToDoubleBiFunction<BaseDinoEntity, LivingEntity> selectionWeight = (dino, target) -> 1;
    protected BiPredicate<BaseDinoEntity, LivingEntity> canUse = (dino, target) -> dino.isAlive() && target.isAlive() && dino.distanceTo(target) <= range.applyAsDouble(dino);


}
