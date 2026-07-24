package com.bretzelfresser.dinosexpansion.common.entity;

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
     * client synced
     */
    public boolean isSleeping(){
        return dino.getDinoFlag(2);
    }

    public void setSleeping(boolean sleeping){
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
        if (this.isSleeping()) {
            this.setSleeping(false);
        }
        this.sleepCooldown = ticks;
    }

    public boolean canSleep() {
        return this.sleepCooldown <= 0;
    }

    public void tick(){
        if (this.sleepCooldown > 0) {
            this.sleepCooldown--;
        }
        // Force the sleep flag to false if we are on a sleep cooldown
        if (this.sleepCooldown > 0 && this.isSleeping()) {
            this.setSleeping(false);
        }
    }
}
