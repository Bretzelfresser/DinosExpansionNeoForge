package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
import net.minecraft.util.Unit;

public class SleepBehaviour {

    protected final BaseDinoEntity dino;
    protected SleepRhythm rhythm;
    private int sleepCooldown = 0;

    public SleepBehaviour(BaseDinoEntity dino, SleepRhythm rhythm) {
        this.dino = dino;
        this.rhythm = rhythm;
    }

    /**
     * Returns the sleeping state, or null if the rhythm is NONE.
     */
    public boolean isSleeping() {
        if (this.rhythm == SleepRhythm.NONE) {
            return false;
        }
        return dino.getDinoFlag(2);
    }

    public void setSleeping(boolean sleeping) {
        if (this.rhythm == SleepRhythm.NONE) {
            return;
        }
        dino.setDinoFlag(2, sleeping);
        if (!dino.level().isClientSide()) {
            if (sleeping) {
                dino.getBrain().setMemory(ModMemoryModules.SLEEPING.get(), Unit.INSTANCE);
            } else {
                dino.getBrain().eraseMemory(ModMemoryModules.SLEEPING.get());
            }
        }
    }

    public void forceAwake(int ticks) {
        if (this.rhythm == SleepRhythm.NONE) {
            return;
        }
        if (this.isSleeping()) {
            this.setSleeping(false);
        }
        this.sleepCooldown = ticks;
    }

    public boolean canSleep() {
        return this.sleepCooldown <= 0;
    }

    public SleepRhythm getRhythm() {
        return this.rhythm;
    }

    public void setRhythm(SleepRhythm rhythm) {
        this.rhythm = rhythm;
    }

    public void tick() {
        if (this.rhythm == SleepRhythm.NONE) {
            return;
        }

        if (this.sleepCooldown > 0) {
            this.sleepCooldown--;
        }

        if (!dino.level().isClientSide()) {
            boolean shouldSleep = false;
            if (this.rhythm == SleepRhythm.DIURNAL) {
                // Diurnal dinos sleep during the night
                shouldSleep = !dino.level().isDay();
            } else if (this.rhythm == SleepRhythm.NOCTURNAL) {
                // Nocturnal dinos sleep during the day
                shouldSleep = dino.level().isDay();
            }

            if (shouldSleep) {
                if (this.canSleep() && !this.isSleeping() && !dino.isUnconscious()) {
                    this.setSleeping(true);
                }
            } else {
                if (this.isSleeping()) {
                    this.setSleeping(false);
                }
            }
        }

        // Force the sleep flag to false if we are on a sleep cooldown
        if (this.sleepCooldown > 0 && this.isSleeping()) {
            this.setSleeping(false);
        }
    }
}
