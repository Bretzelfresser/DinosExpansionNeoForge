package com.bretzelfresser.dinosexpansion.common.entity.ai.navigation;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.world.entity.Mob;
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
        //directly advancing to the last node
        if (path.getNextNodeIndex() < path.getNodeCount() - 1){
            path.setNextNodeIndex(path.getNodeCount() - 1);
        }
        var currNode = this.path.getNextEntityPos(dino);

        this.mob.getMoveControl().setWantedPosition(currNode.x, currNode.y, currNode.z, speedModifier);

        if (this.mob.distanceToSqr(currNode) <= 1){
            //ensure the path is finished
            this.path.setNextNodeIndex(path.getNodeCount());
        }
    }
}
