package com.bretzelfresser.dinosexpansion.common.entity.dinosaur.ceratosaurus;

import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModActivities;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.schedule.Activity;

public class CeratosaurusBrain {


    public static Brain<Certosaurus> makeBrain(Brain<Certosaurus> brain){
        DinoBrain.initCoreActivity(brain);
        DinoBrain.initIdleAttackingActivity(brain);
        DinoBrain.initTamedIdleActivity(brain);
        DinoBrain.initFightActivity(brain);
        DinoBrain.initUnconsciousActivity(brain);
        DinoBrain.initSleepActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateActivity(BaseDinoEntity dino) {
        Brain<BaseDinoEntity> brain = dino.getBrain();
        if (dino.isTamed()) {
            brain.setActiveActivityToFirstValid(ImmutableList.of(
                    ModActivities.UNCONSCIOUS.get(),
                    ModActivities.SLEEP.get(),
                    Activity.FIGHT,
                    ModActivities.TAMED_IDLE.get()
            ));
        } else {
            brain.setActiveActivityToFirstValid(ImmutableList.of(
                    ModActivities.UNCONSCIOUS.get(),
                    ModActivities.SLEEP.get(),
                    Activity.FIGHT,
                    Activity.IDLE
            ));
        }
    }
}
