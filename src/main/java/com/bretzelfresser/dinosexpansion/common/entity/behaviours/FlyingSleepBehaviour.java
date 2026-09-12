package com.bretzelfresser.dinosexpansion.common.entity.behaviours;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import com.bretzelfresser.dinosexpansion.common.entity.base.SleepRhythm;

public class FlyingSleepBehaviour extends SleepBehaviour {

    protected final FlyingDinosaur<?> flyingDino;

    public FlyingSleepBehaviour(FlyingDinosaur<?> dino, SleepRhythm rhythm) {
        super(dino, rhythm);
        this.flyingDino = dino;
    }

    @Override
    public boolean canSleep() {
        if (this.flyingDino.isFlying()) {
            return false;
        }
        return super.canSleep();
    }

    @Override
    public void tick() {
        if (this.rhythm == SleepRhythm.NONE) {
            return;
        }
        if (this.dino.level().isClientSide()) {
            return;
        }

        // If it is time to sleep and we are flying, request landing (e.g. in trees)
        if (isSleepTime() && this.flyingDino.isFlying() && this.flyingDino.getFlightBehaviour() != null) {
            this.flyingDino.getFlightBehaviour().startLandingForSleep();
        }

        super.tick();
    }
}
