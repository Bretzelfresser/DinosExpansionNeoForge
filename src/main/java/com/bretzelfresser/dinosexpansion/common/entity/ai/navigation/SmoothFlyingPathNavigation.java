package com.bretzelfresser.dinosexpansion.common.entity.ai.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SmoothFlyingPathNavigation extends FlyingPathNavigation {
    
    public SmoothFlyingPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected void followThePath() {
        if (this.path == null || this.path.isDone()) {
            return;
        }

        Vec3 pos = this.mob.position();
        int currentIndex = this.path.getNextNodeIndex();
        int pathLength = this.path.getNodeCount();

        // Calculate a dynamic acceptance threshold based on entity size
        double threshold = Math.max(3.0D, this.mob.getBbWidth() * 2.0D);
        double thresholdSqr = threshold * threshold;

        // Check if we are close enough to any of the upcoming nodes to skip them
        int lookAheadIndex = currentIndex;
        for (int i = currentIndex; i < Math.min(currentIndex + 3, pathLength); i++) {
            Vec3 nodePos = this.path.getEntityPosAtNode(this.mob, i);
            if (pos.distanceToSqr(nodePos) < thresholdSqr) {
                lookAheadIndex = i + 1;
            }
        }

        // Advance the path index
        if (lookAheadIndex > currentIndex) {
            if (lookAheadIndex >= pathLength) {
                lookAheadIndex = pathLength - 1;
            }
            while (this.path.getNextNodeIndex() < lookAheadIndex) {
                this.path.advance();
            }
        }

        // Feed the target waypoint to the move control
        if (!this.path.isDone()) {
            Vec3 targetNodePos = this.path.getEntityPosAtNode(this.mob, this.path.getNextNodeIndex());
            this.mob.getMoveControl().setWantedPosition(targetNodePos.x, targetNodePos.y, targetNodePos.z, this.speedModifier);
        }
    }
}
