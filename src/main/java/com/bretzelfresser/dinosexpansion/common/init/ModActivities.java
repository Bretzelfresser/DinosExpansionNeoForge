package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModActivities {

    public static final DeferredRegister<Activity> ACTIVITES = DeferredRegister.create(Registries.ACTIVITY, DinosExpansion.MODID);


    public static final DeferredHolder<Activity, Activity> SLEEP = register("sleep");
    public static final DeferredHolder<Activity, Activity> UNCONSCIOUS = register("unconscious");
    public static final DeferredHolder<Activity, Activity> TAMED_IDLE = register("tamed_idle");


    public static DeferredHolder<Activity, Activity> register(String name) {
        return ACTIVITES.register(name, () -> new Activity(DinosExpansion.modLoc(name).toLanguageKey("activity")));
    }

}
