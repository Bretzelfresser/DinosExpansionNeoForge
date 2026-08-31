package com.bretzelfresser.dinosexpansion.common.entity.ai.navigation;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SmoothFlyingPathNavigation extends FlyingPathNavigation {

    protected final FlyingDinosaur<?> dino;

    public SmoothFlyingPathNavigation(FlyingDinosaur<?> dino, Level level) {
        super(dino, level);
        this.dino = dino;
    }

    @Override
    protected void followThePath() {
        if (this.path == null || this.path.isDone()) {
            return;
        }

        Vec3 currNode = this.path.getNextEntityPos(this.dino);
        this.mob.getMoveControl().setWantedPosition(currNode.x, currNode.y, currNode.z, this.speedModifier);

        // Advance to next waypoint when close enough to current node
        double reachDistance = Math.max(1.2D, this.dino.getBbWidth() * 1.2D);
        if (this.dino.distanceToSqr(currNode) <= reachDistance * reachDistance) {
            this.path.advance();
        }
    }
}
