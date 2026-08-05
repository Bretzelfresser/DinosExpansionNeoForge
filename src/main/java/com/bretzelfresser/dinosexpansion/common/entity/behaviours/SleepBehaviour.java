package com.bretzelfresser.dinosexpansion.common.entity.behaviours;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.SleepRhythm;
import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

/**
 * this whole class is the sleep Behavior, only use method which change state on the server side, nothing here is synced, it may use values which are synced
 */
public class SleepBehaviour {

    protected final BaseDinoEntity dino;
    protected SleepRhythm rhythm;
    private int sleepCooldown = 0;

    public SleepBehaviour(BaseDinoEntity dino, SleepRhythm rhythm) {
        this.dino = dino;
        this.rhythm = rhythm;
    }

    /**
     *
     * @return whether this entity sleeps, forceAwake just works on the server side cause its linked to the brain
     * simply checking whether the time of day is right so sleep also works on the client side
     */
    public boolean shouldSleep() {
        if (this.dino.isUnconscious())
            return false;
        if (!canSleep())
            return false;
        boolean shouldSleep = false;
        if (this.rhythm == SleepRhythm.DIURNAL) {
            // Diurnal dinos sleep during the night
            shouldSleep = !dino.level().isDay();
        } else if (this.rhythm == SleepRhythm.NOCTURNAL) {
            // Nocturnal dinos sleep during the day
            shouldSleep = dino.level().isDay();
        }

        return shouldSleep;
    }


    /**
     *
     * @return whether this entity is sleeping, fully client synced
     */
    public boolean isSleeping() {
        if (this.rhythm == SleepRhythm.NONE) {
            return false;
        }
        return dino.getDinoFlag(2);
    }

    /**
     * only works on server side
     * @param sleeping whether this entity should slee, no additional checks are done in here, this will literally oput the entity to sleep right now
     */
    public void setSleeping(boolean sleeping) {
        if (this.rhythm == SleepRhythm.NONE) {
            return;
        }
        if (this.dino.level().isClientSide)
            return;
        dino.setDinoFlag(2, sleeping);
        if (sleeping) {
            dino.getBrain().setMemory(ModMemoryModules.SLEEPING.get(), Unit.INSTANCE);
            dino.ejectPassengers();
        } else {
            dino.getBrain().eraseMemory(ModMemoryModules.SLEEPING.get());
        }
    }

    /**
     * only use on server side, otherwise this wont do anything
     *
     * @param ticks
     */
    public void forceAwake(int ticks) {
        if (this.rhythm == SleepRhythm.NONE) {
            return;
        }
        if (this.dino.level().isClientSide)
            return;
        if (this.isSleeping()) {
            this.setSleeping(false);
        }
        this.sleepCooldown += ticks;
    }

    public boolean canSleep() {
        if (dino.getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT))
            return false;
        //ensure when the entity might find another target that it doesnt fall asleep
        if(dino.findAttackTarget().map(dino::canAttack).orElse(false))
            return false;
        return this.sleepCooldown <= 0 && !dino.isVehicle() && dino.getLastHurtByMob() == null && dino.getTarget() == null;
    }

    public SleepRhythm getRhythm() {
        return this.rhythm;
    }

    public void tick() {
        if (this.rhythm == SleepRhythm.NONE) {
            return;
        }
        if (this.dino.level().isClientSide)
            return;

        //only decreasing the cooldown if we can sleep, so then when those conditions are true the entity actually waits
        // for example when attacking for the force awake ticks and then goes to sleep and not instant
        if (this.sleepCooldown > 0 && this.canSleep()) {
            this.sleepCooldown--;
        }

        if (shouldSleep() != isSleeping()) {
            setSleeping(shouldSleep());
        }
    }
}
