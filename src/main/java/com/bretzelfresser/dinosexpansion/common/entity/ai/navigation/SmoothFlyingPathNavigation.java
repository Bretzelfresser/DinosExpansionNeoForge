package com.bretzelfresser.dinosexpansion.common.entity.ai.navigation;

import com.bretzelfresser.dinosexpansion.common.entity.base.FlyingDinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SmoothFlyingPathNavigation extends FlyingPathNavigation {

    protected final FlyingDinosaur<?> dino;
    protected ParametricSpline activeSpline = null;
    protected double currentSplineDistance = 0.0D;

    public SmoothFlyingPathNavigation(FlyingDinosaur<?> dino, Level level) {
        super(dino, level);
        this.dino = dino;
    }

    @Override
    protected @Nullable Path createPath(Set<BlockPos> targets, int distance, boolean reachTarget, int maxVisitedNodes) {
        Path path = super.createPath(targets, distance, reachTarget, maxVisitedNodes);
        if (path != null && this.dino.isFlying()) {
            buildSplineFromPath(path);
        } else {
            this.activeSpline = null;
        }
        return path;
    }

    @Override
    public boolean moveTo(@Nullable Path path, double speed) {
        boolean success = super.moveTo(path, speed);
        if (success && path != null && this.dino.isFlying()) {
            buildSplineFromPath(path);
        } else if (!this.dino.isFlying()) {
            this.activeSpline = null;
        }
        return success;
    }

    @Override
    public void stop() {
        super.stop();
        this.activeSpline = null;
        this.currentSplineDistance = 0.0D;
    }

    protected void buildSplineFromPath(Path path) {
        if (path == null || path.getNodeCount() == 0) {
            this.activeSpline = null;
            this.currentSplineDistance = 0.0D;
            return;
        }

        List<Vec3> rawPoints = new ArrayList<>();
        rawPoints.add(this.dino.position());

        for (int i = path.getNextNodeIndex(); i < path.getNodeCount(); i++) {
            rawPoints.add(path.getEntityPosAtNode(this.dino, i));
        }

        if (rawPoints.size() <= 1) {
            this.activeSpline = null;
            this.currentSplineDistance = 0.0D;
            return;
        }

        // 1. Line-of-Sight Pruning (String-Pulling)
        List<Vec3> prunedKeypoints = PathStringPuller.stringPull(this.level, this.dino, rawPoints);

        // 2. Build Continuous Parametric Spline P(s)
        this.activeSpline = new ParametricSpline(prunedKeypoints);
        this.currentSplineDistance = 0.0D;
    }

    @Override
    protected void followThePath() {
        if (this.path == null || this.path.isDone()) {
            this.activeSpline = null;
            return;
        }

        // When flying, evaluate continuous parametric spline function P(s) directly
        if (this.dino.isFlying() && this.activeSpline != null && !this.activeSpline.isEmpty()) {
            double flySpeed = this.dino.getAttributeValue(Attributes.FLYING_SPEED) * this.speedModifier;
            double step = Math.max(0.1D, flySpeed);

            // Advance distance parameter s along the curve
            this.currentSplineDistance += step;

            if (this.currentSplineDistance >= this.activeSpline.getTotalLength()) {
                this.stop();
                return;
            }

            // Look-ahead distance to steer smoothly along curve tangent
            double lookAheadDistance = Math.min(this.activeSpline.getTotalLength(), this.currentSplineDistance + 1.5D);
            Vec3 targetPos = this.activeSpline.getPositionAtDistance(lookAheadDistance);

            this.mob.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, this.speedModifier);
        } else {
            // Grounded mode: Fallback to standard ground node pathing (no airborne 3D splines)
            this.activeSpline = null;
            Vec3 currNode = this.path.getNextEntityPos(this.dino);
            this.mob.getMoveControl().setWantedPosition(currNode.x, currNode.y, currNode.z, this.speedModifier);

            double reachDistance = Math.max(1.2D, this.dino.getBbWidth() * 1.2D);
            if (this.dino.distanceToSqr(currNode) <= reachDistance * reachDistance) {
                this.path.advance();
            }
        }
    }
}
